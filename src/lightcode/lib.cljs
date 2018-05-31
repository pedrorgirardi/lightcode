(ns lightcode.lib
  (:require
   ["vscode" :as vscode]

   [cljs-node-io.core :as io]
   [cljs-node-io.fs :as fs]))


(defn nrepl-port! []
  (let [path (str vscode/workspace.rootPath "/.nrepl-port")]
    (when (fs/file? path)
      (io/slurp path))))