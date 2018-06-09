(ns lightcode.nrepl
  (:require
   ["net" :as net]
   ["bencode" :as bencoder]))


(defn client [{:keys [host port] :or {host "localhost"}}]
  (let [*messages (atom {})

        socket    (doto (net/connect #js {:host host :port port})
                    (.on "data" (fn [message]
                                  (let [decoded (bencoder/decode message "utf-8")

                                        _       (js/console.log "[ON-DATA]" decoded)

                                        {:keys [id] :as decoded} (js->clj decoded :keywordize-keys true)]

                                    (if id
                                      (swap! *messages update id (fnil conj []) decoded)
                                      (js/console.log "Can't decode" (str message)))))))]
    (fn []
      {:*messages *messages
       :socket    socket})))


(defn- done? [{:keys [status]}]
  (some #(= % "done") status))


(defn send [client message callback]
  (let [id       (str (random-uuid))
        message  (assoc message :id id)
        bencoded (bencoder/encode (clj->js message))
        {:keys [socket *messages]} (client)]

    (add-watch *messages id (fn [_ _ _ messages-by-id]
                              ; (js/console.log "[WATCH]" (clj->js messages-by-id))

                              (let [messages (get messages-by-id id)]
                                (when (some done? messages)
                                  (remove-watch *messages id)
                                  ;; Reduce messages into a single map
                                  ;; Easier to work with, right? :)
                                  (callback (reduce merge {} messages))))))

    (.write socket bencoded "binary")

    id))


(comment
 (def c (client {:port 51632}))

 (defn callback [x]
   (js/console.log "[CALLBACK]" (clj->js x)))

 (send c {:op     "info"
          :ns     'lightcode.nrepl
          :symbol 'send}
       callback)

 (send c {:op   "eval"
          :code "(print \"out\")"}
       callback)

 (send c {:op   "eval"
          :code "(+ 1 1)"}
       callback))
