(ns lightcode.server.language
  (:require
   [clojure.string :as str]
   [clojure.edn :as edn]
   [clojure.tools.nrepl :as nrepl]

   [lightcode.server.repl :as repl]))


(defn op! [client message]
  (-> client
      (nrepl/message message)
      (nrepl/combine-responses)))


(defmulti provider :provider)


;(defmethod provider "DocumentSymbolProvider" [{:keys [nrepl text] :as m}]
;  (let [{:keys [client session]} nrepl
;        [_ ns-name] (edn/read-string text)
;        ns-name (name ns-name)]
;    (let [{:keys [ns-vars]} (op! client {:session session
;                                         :op      "ns-vars"
;                                         :ns      ns-name})]
;      (->> ns-vars
;           (map
;            (fn [var-name]
;              (let [{:keys [column line file]} (op! client {:session session
;                                                            :op      "info"
;                                                            :ns      ns-name
;                                                            :symbol  var-name})]
;                {:name   var-name
;                 :column column
;                 :line   (dec line)
;                 :file   file})))
;           (doall)))))


(defmethod provider "DocumentSymbolProvider" [{:keys [context text]}]
  (repl/with-session! context
    (fn [client session]
      (let [[_ ns-name] (edn/read-string text)
            ns-name (name ns-name)
            {:keys [ns-vars]} (-> client
                                  (nrepl/message {:op      "ns-vars"
                                                  :session session
                                                  :ns      ns-name})
                                  (nrepl/combine-responses))]
        (->> ns-vars
             (map
              (fn [var-name]
                (let [{:keys [column line file]} (-> client
                                                     (nrepl/message {:session session
                                                                     :op      "info"
                                                                     :ns      ns-name
                                                                     :symbol  var-name})
                                                     (nrepl/combine-responses))]
                  {:name   var-name
                   :column column
                   :line   (dec line)
                   :file   file})))
             (doall))))))