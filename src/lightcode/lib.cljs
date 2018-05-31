(ns lightcode.lib
  (:require
   ["vscode" :as vscode]

   [cljs.reader :as r]
   [cljs-node-io.core :as io]
   [cljs-node-io.fs :as fs]))


(defn read-document [document]
  (r/read-string (str "[" document "]")))


(defn read-document-ns [document]
  (let [[form] (read-document document)]
    form))


(defn nrepl-port! []
  (let [path (str vscode/workspace.rootPath "/.nrepl-port")]
    (when (fs/file? path)
      (io/slurp path))))