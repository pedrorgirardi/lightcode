(ns lightcode.nrepl
  (:require
   ["net" :as net]
   ["bencoder" :as bencoder]
   ["buffer" :refer [Buffer]]
   [clojure.string :as str]))


(def CONTINUATION_ERROR_MESSAGE
  "Unexpected continuation: \"")

(defn connect
  "Connects to a socket-based REPL at the given host (defaults to localhost) and port."
  [options]
  (let [{:keys [host port on-connect on-error on-end] :or {host "localhost"}} options]
    (doto (net/createConnection #js {:host host :port port})
      (.once "connect" (fn []
                         (when on-connect
                           (on-connect))))

      (.once "end" (fn []
                     (when on-end
                       (on-end))))

      (.once "error" (fn [error]
                       (when on-error
                         (on-error error)))))))

(defn- decode [buffers]
  (mapcat
   (fn [^js buffer]
     (try
       (-> (bencoder/decode buffer)
           (js->clj :keywordize-keys true)
           (vector))
       (catch js/Error e
         (let [exception-message (.-message e)]
           (if (str/includes? exception-message CONTINUATION_ERROR_MESSAGE)
             ;; we might be able to handle this specific error
             ;; of unexpected continuation,
             ;; so we substring the error message to remove
             ;; the continuation error and then
             ;; we are ready to try to decode again
             ;; but this time passing two buffers
             (let [recoverable-content (subs exception-message (count CONTINUATION_ERROR_MESSAGE))
                   recoverable-buffer  (.slice buffer 0 (- (.-length buffer) (count recoverable-content)))]
               (decode [recoverable-buffer (Buffer.from recoverable-content)]))

             ;; can't (don't know) handle other errors
             (js/console.error "FAILED TO DECODE" (.-code exception-message)))))))
   buffers))

(defn message [conn msg callback]
  (let [*state (atom [])]
    (.on conn "data" (fn [chunk]
                       (when-let [decoded-messages (let [empty-buffer (Buffer.from "")

                                                         ;; I don't understand this trick
                                                         ;; but I'm doing just like the JavaScript version
                                                         ;;
                                                         ;; ** do you understand this, Peter? **
                                                         buffer       (Buffer.concat (clj->js [empty-buffer chunk]))]

                                                     (when (= 0 (.-length buffer))
                                                       (js/console.warn "EMPTY BUFFER"))

                                                     (not-empty (decode [buffer])))]

                         (swap! *state into decoded-messages)

                         ;; we've got a 'done' status,
                         ;; so we can call the callback
                         ;; and pass all the previous decoded messages
                         ;;
                         ;; ** not sure about this... we really need to figure the session stuff out **
                         (when (some #(= "done" %) (mapcat :status decoded-messages))
                           (callback (clj->js @*state))))))

    (.write conn (bencoder/encode (clj->js msg)) "binary")))
