(ns lightcode.cmd
  (:require
   ["vscode" :as vscode]

   [kitchen-async.promise :as p]
   [datascript.core :as d]

   [lightcode.nrepl :as nrepl]
   [lightcode.out :as out]
   [lightcode.gui :as gui]))


(defn ^{:cmd "lightcode.welcome"} welcome [{:keys [conn]}]
  nil)


(defn ^{:cmd "lightcode.connect"} connect [{:keys [conn]}]
  (p/let [host (gui/show-input-box {:placeHolder "nREPL Server Address"
                                    :ignoreFocusOut true
                                    :value "localhost"})

          port (gui/show-input-box {:placeHolder "nREPL Server Port"
                                    :ignoreFocusOut true})]

    (p/then (p/all [host port])
            (fn [[host port]]
              ;; TODO
              (when (and host port)
                (let [^js socket (nrepl/connect {:host host
                                                 :port port
                                                 :on-connect (fn []
                                                               (d/transact! conn [{:db/id 0
                                                                                   :conn/connected? true
                                                                                   :conn/connecting? false}])
                                                               
                                                               (gui/show-information-message (str "Connected - nrepl://" host ":" port)))
                                                 :on-end (fn []
                                                           (d/transact! conn [{:db/id 0
                                                                               :conn/connected? false
                                                                               :conn/connecting? false}])
                                                           
                                                           (gui/show-information-message  (str "Disconnected - nrepl://" host ":" port)))})]

                  (d/transact! conn [{:db/id 0
                                      :conn/host host
                                      :conn/port port
                                      :conn/connected? false
                                      :conn/connecting? true
                                      :conn/socket socket}])))))))


(defn ^{:cmd "lightcode.disconnect"} disconnect [{:keys [conn]}]
  (let [^js socket (d/q '[:find ?s . :where [0 :conn/socket ?s]] @conn)]
    (when socket
      (.end socket))))