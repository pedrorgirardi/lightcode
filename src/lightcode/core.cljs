(ns lightcode.core
  (:require
   ["vscode" :as vscode]
   ["net" :as net]
   ["axios" :as axios]

   [lightcode.lib :as lib]
   [lightcode.op :as op]
   [kitchen-async.promise :as p]
   [clojure.string :as str]))

;; ------------------------------------------------
;; HELPERS
;; ------------------------------------------------

(defn- register-command [cmd]
  (vscode/commands.registerCommand (-> cmd meta :cmd) cmd))


(defn- register-disposable [^js context ^js disposable]
  (-> (.-subscriptions context)
      (.push disposable)))


(defn- reg-cmd [^js context cmd]
  (->> (register-command cmd)
       (register-disposable context)))


;; ------------------------------------------------
;; COMMANDS
;; ------------------------------------------------

(defn ^{:cmd "lightcode.loadFile"} cmd-load-file []
  (when-let [document (lib/active-clojure-document)]
    (op/load-file! (:lc.document/content document)
                   (:lc.document/src-path-relative-path document)
                   (:lc.document/file-name document)))

  nil)


(defn ^{:cmd "lightcode.loadNamespaces"} cmd-load-namespaces []
  (op/ns-load-all!)

  nil)


;; ------------------------------------------------
;; PROVIDERS
;; ------------------------------------------------

(deftype ClojureDefinitionProvider []
  Object
  (provideDefinition [_ document position _]
    (let [word-at-position (lib/word-at-position document position)
          document-ns      (lib/read-document-ns-name document)]
      (-> (op/info! document-ns word-at-position)
          (p/then
           (fn [response]
             (js/console.log "[PROVIDE-DEFINITION]" response)

             (let [response (js->clj response :keywordize-keys true)
                   file     (get-in response [:data :file])
                   line     (get-in response [:data :line])
                   column   (get-in response [:data :column])
                   uri      (when file
                              (vscode/Uri.parse file))
                   position (when (and line column)
                              (vscode/Position. (dec line) column))]
               (when (and uri position)
                 (vscode/Location. uri position)))))
          (p/catch*
           (fn [error]
             (js/console.error "[PROVIDE-DEFINITION]" error)))))))


(deftype ClojureHoverProvider []
  Object
  (provideHover [_ document position _]
    (let [word-at-position (lib/word-at-position document position)
          document-ns      (lib/read-document-ns-name document)]
      (-> (op/info! document-ns word-at-position)
          (p/then
           (fn [response]
             (js/console.log "[PROVIDE-HOVER]" response)

             (let [response   (js->clj response :keywordize-keys true)
                   namespace  (get-in response [:data :ns] "")
                   name       (get-in response [:data :name] "")
                   name       (str namespace (when-not (str/blank? namespace) "/") "**" name "**")
                   doc        (get-in response [:data :doc] "")
                   args       (get-in response [:data :arglists-str] "")
                   markdown   (doto (vscode/MarkdownString. name)
                                (.appendText "\n\n")
                                (.appendText doc)
                                (.appendText "\n\n")
                                (.appendCodeblock args "clojure"))]

               (when-not (str/blank? name)
                 (vscode/Hover. markdown)))))

          (p/catch*
           (fn [error]
             (js/console.error "[PROVIDE-HOVER]" error)))))))


(deftype ClojureDocumentSymbolProvider []
  Object
  (provideDocumentSymbols [_ document _]
    (let [ns (lib/read-document-ns-name document)]

      (let [message (clj->js {:remote {:port (lib/nrepl-port!)}
                              :provide {:provider "DocumentSymbolProvider"
                                        :ns ns}})]
        (-> (.post axios "http://localhost:8383/tooling" message)
            (p/then
             (fn [response]

               (js/console.log "[PROVIDE-DOCUMENT-SYMBOLS]" response)

               (clj->js
                (map
                 (fn [{:keys [name column line file]}]
                   (let [uri      (vscode/Uri.parse file)
                         position (vscode/Position. (dec line) column)
                         location (vscode/Location. uri position)]
                     (vscode/SymbolInformation. name vscode/SymbolKind.Variable "" location)))
                 (:data (js->clj response :keywordize-keys true))))))
            (p/catch*
             (fn [error]
               (js/console.error "[PROVIDE-DOCUMENT-SYMBOLS]" error))))))))


;; ------------------------------------------------


(def clojure-document-selector
  #js {:scheme   "file"
       :language "clojure"})


(def clojure-language-configuration
  #js {:wordPattern #"[^\[\]\(\)\{\};\s\"\\]+"})


(defn activate [^js context]
  (vscode/languages.setLanguageConfiguration "clojure" clojure-language-configuration)

  (register-disposable context (vscode/languages.registerDefinitionProvider clojure-document-selector (ClojureDefinitionProvider.)))
  (register-disposable context (vscode/languages.registerHoverProvider clojure-document-selector (ClojureHoverProvider.)))
  (register-disposable context (vscode/languages.registerDocumentSymbolProvider clojure-document-selector (ClojureDocumentSymbolProvider.)))

  (reg-cmd context #'cmd-load-file)
  (reg-cmd context #'cmd-load-namespaces)

  (js/console.log "Light Code is active."))


(defn deactivate []
  nil)