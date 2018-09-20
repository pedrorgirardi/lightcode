(ns lightcode.core
  (:require
   [vscode :as vscode]
   [vscode-languageclient :as vscode-languageclient]
   [cljs.reader :as edn]

   [lightcode.workspace :as workspace]
   [lightcode.document :as document]
   [lightcode.out :as out]
   [lightcode.op :as op]))

(defn- register-disposable [^js context ^js disposable]
  (-> (.-subscriptions context)
      (.push disposable)))

(defn activate [^js context]
  (let [output-channel (vscode/window.createOutputChannel "Light Code")

        server-options {:run   {:command "clojure-lsp"}
                        :debug {:command "bash"
                                :args    ["-c" "cd /Users/pedro/Developer/clojure-lsp && lein run"]}}

        client-options {:documentSelector [{:scheme "file"
                                            :language "clojure"}]

                        :synchronize      {:configurationSection "lightcode"
                                           :fileEvents           (vscode/workspace.createFileSystemWatcher "**/.clientrc")}}

        language-client (vscode-languageclient/LanguageClient. "lightcode" "Light Code" (clj->js server-options) (clj->js client-options))]

    (.start language-client)

    (register-disposable context language-client)

    (out/append-line output-channel "Light Code is active. Happy coding!")))

(defn deactivate []
  nil)