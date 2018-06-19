(ns lightcode.core
  (:require
   [vscode :as vscode]
   [vscode-languageclient :as vscode-languageclient]
   [cljs.reader :as edn]

   [lightcode.workspace :as workspace]
   [lightcode.editor :as editor]
   [lightcode.document :as document]
   [lightcode.out :as out]
   [lightcode.op :as op]))


(def *sys
  (atom {}))


;; ------------------------------------------------
;; HELPERS
;; ------------------------------------------------

(defn- register-command [cmd]
  (let [cmd-name (-> cmd meta :cmd)
        callback (fn []
                   (js/console.log (str "Run command '" cmd-name "'"))

                   (try
                     (cmd)
                     (catch js/Error e
                       (js/console.error (str "Failed to run command '" cmd-name "'") e))))]

    (vscode/commands.registerCommand cmd-name callback)))


(defn- register-text-editor-command [cmd]
  (let [cmd-name (-> cmd meta :cmd)
        callback (fn [editor edit args]
                   (js/console.log (str "Run editor command '" cmd-name "'"))

                   (try
                     (cmd editor edit args)
                     (catch js/Error e
                       (js/console.error (str "Failed to run editor command '" cmd-name "'") e))))]

    (vscode/commands.registerTextEditorCommand cmd-name callback)))


(defn- register-disposable [^js context ^js disposable]
  (-> (.-subscriptions context)
      (.push disposable)))



;; ------------------------------------------------
;; COMMANDS
;; ------------------------------------------------

(defn ^{:cmd "lightcode.sendSelectionToREPL"} send-selection-to-repl [editor edit args]
  (let [language-client (get @*sys :language-client)
        document        (.-document editor)
        selection       (.-selection editor)
        range           (vscode/Range. (.-start selection) (.-end selection))
        selected-text   (.getText document range)
        message         {:op      "eval"
                         :code    selected-text
                         :context (merge
                                   {:env   (document/clojure-dialect document)
                                    :nrepl {:port (workspace/nrepl-port!)}}
                                   (workspace/cljs-repl-context!))}]

    (.then (.sendRequest language-client "eval" (pr-str message))
           (fn [result]
             (let [output-channel (get @*sys :output-channel)
                   out-str (str "\n" selected-text "\n=> " result "\n=> " (-> (edn/read-string result) :value first))]
               (out/append-line-and-show output-channel out-str)))))

  nil)


(defn ^{:cmd "lightcode.newDocument"} new-document []
  (vscode/workspace.openTextDocument (vscode/Uri.parse (str "untitled:" (.-rootPath vscode/workspace) "/.lightcode/doc.clj"))))


;; ------------------------------------------------


(defn activate [^js context]
  (let [output-channel (vscode/window.createOutputChannel "Light Code")

        server-options {:run   {:command "lightcode"}
                        :debug {:command "bash"
                                :args    ["-c" "cd /Users/pedro/Developer/lightcode.server && lein run"]}}

        client-options {:documentSelector [{:scheme "file" :language "clojure"}]
                        :synchronize      {:configurationSection "lightcode"
                                           :fileEvents           (vscode/workspace.createFileSystemWatcher "**/.clientrc")}}

        language-client (vscode-languageclient/LanguageClient. "lightcode" "Light Code" (clj->js server-options) (clj->js client-options))]

    (.start language-client)

    (register-disposable context language-client)

    (->> (register-command  #'new-document)
         (register-disposable context))

    (->> (register-text-editor-command  #'send-selection-to-repl)
         (register-disposable context))

    (reset! *sys  {:output-channel  output-channel
                   :language-client language-client})

    (out/append-line output-channel "Light Code is active. Happy coding!")))


(defn deactivate []
  nil)