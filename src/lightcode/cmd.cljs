(ns lightcode.cmd
  (:require
   ["vscode" :as vscode]
   ["nrepl-client" :as nrepl-client]

   [cljs-node-io.core :as io]
   [cljs-node-io.fs :as fs]))


(defn nrepl-port []
  (let [path (str vscode/workspace.rootPath "/.nrepl-port")]
    (when (fs/file? path)
      (io/slurp path))))


(defn ^{:cmd "lightcode.switchOn"} switch-on [*sys]
  (let [socket (.connect nrepl-client #js {:host "localhost" :port (nrepl-port)})]
    (doto socket
      (.once "connect" (fn []
                         (js/console.log "Light Code is on")

                         (.clone (get @*sys :lc/socket) (fn [err messages]
                                                          (when-not err
                                                            (let [[{:keys [new-session]}] (js->clj messages :keywordize-keys true)]
                                                              (swap! *sys assoc :lc/clj-session new-session)))))))

      (.once "end" (fn []
                     (js/console.log "Light Code is off")))

      (.on "error" (fn [error]
                     (js/console.log "Light Code socket error" error))))

    (swap! *sys assoc :lc/socket socket)))


(defn ^{:cmd "lightcode.switchOff"} switch-off [*sys]
  (when-let [socket (get @*sys :lc/socket)]
    (if-let [session (get @*sys :lc/clj-session)]
      (.close socket session (fn [_ _]
                               (swap! *sys dissoc :lc/clj-session)
                               (.end socket)))
      (.end socket))
    (swap! *sys dissoc :lc/socket)))