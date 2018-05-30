(ns lightcode.cmd
  (:require
   ["vscode" :as vscode]
   ["nrepl-client" :as nrepl-client]

   [cljs-node-io.core :as io]
   [cljs-node-io.fs :as fs]

   [lightcode.nrepl :as nrepl]
   [lightcode.out :as out]
   [lightcode.gui :as gui]))


(defn nrepl-port []
  (let [path (str vscode/workspace.rootPath "/.nrepl-port")]
    (when (fs/file? path)
      (io/slurp path))))


(defn ^{:cmd "lightcode.switchOn"} switch-on [*sys]
  (let [socket (.connect nrepl-client #js {:host "localhost" :port (nrepl-port)})]
    (doto socket
      (.once "connect" (fn []
                         (js/console.log "Switch on")))

      (.once "end" (fn []
                     (js/console.log "Switch off")))

      (.on "error" (fn [error]
                     (js/console.log "Error" error))))

    (swap! *sys assoc :socket socket)))


(defn ^{:cmd "lightcode.switchOff"} switch-off [*sys]
  (when-let [socket (get @*sys :socket)]
    (.end socket)))