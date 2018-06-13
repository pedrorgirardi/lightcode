(ns lightcode.core
  (:require
   ["vscode" :as vscode]
   ["vscode-languageclient" :as vscode-languageclient]

   [lightcode.editor :as editor]
   [lightcode.op :as op]))

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
  (when-let [document (editor/active-clojure-document)]
    (op/load-file! (:lc.document/env document)
                   (:lc.document/content document)
                   (:lc.document/src-path-relative-path document)
                   (:lc.document/file-name document)))

  nil)


(defn ^{:cmd "lightcode.loadNamespaces"} cmd-load-namespaces []
  (when-let [document (editor/active-clojure-document)]
    (op/ns-load-all! (editor/env document)))

  nil)


(defn activate [^js context]
  (let [server-options {:run   {:command "lightcode"}
                        :debug {:command "bash"
                                :args    ["-c" "cd /Users/pedro/Developer/lightcode.server && lein run"]}}

        client-options {:documentSelector [{:scheme "file" :language "clojure"}]
                        :synchronize      {:configurationSection "lightcode"
                                           :fileEvents           (vscode/workspace.createFileSystemWatcher "**/.clientrc")}}

        language-client (vscode-languageclient/LanguageClient. "lightcode" "Light Code" (clj->js server-options) (clj->js client-options))]

    (.start language-client)

    (register-disposable context language-client))

  (js/console.log "Light Code is active."))


(defn deactivate []
  nil)