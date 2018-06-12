(ns lightcode.workspace
  (:require
   ["vscode" :as vscode]

   [cljs.reader :as edn]
   [cljs-node-io.fs :as fs]
   [cljs-node-io.core :as io]))


(defn nrepl-port! []
  (let [path (str vscode/workspace.rootPath "/.nrepl-port")]
    (when (fs/file? path)
      (js/parseInt (io/slurp path)))))


(defn cljs-repl-context! []
  (let [shadow-cljs-config-path (str vscode/workspace.rootPath "/shadow-cljs.edn")
        shadow-cljs-build?      (fs/file? shadow-cljs-config-path)
        shadow-cljs-build-id    (when shadow-cljs-build?
                                  (let [config (edn/read-string (io/slurp shadow-cljs-config-path))]
                                    (-> config :build keys first)))]
    (cond
      shadow-cljs-build?
      {:cljs-repl            "shadow-cljs"
       :start-cljs-repl-code (str "(shadow.cljs.devtools.api/nrepl-select " shadow-cljs-build-id ")")}

      :else
      {:cljs-repl            "fighweel"
       :start-cljs-repl-code "(require '[figwheel-sidecar.repl-api :as figwheel]) (figwheel/cljs-repl)"})))