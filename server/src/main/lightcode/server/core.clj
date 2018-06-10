(ns lightcode.server.core
  (:require
   [yada.yada :as yada]
   [integrant.core :as ig]
   [clojure.string :as str]
   [clojure.tools.nrepl :as nrepl]))

(defmulti editor-provider
  (fn [m]
    (get-in m [:provide :provider])))


(defmethod editor-provider "DocumentSymbolProvider" [{:keys [provide] :as m}]
  (let [{:keys [ns-vars]} (with-open [conn (nrepl/connect :port (Integer. (get-in m [:remote :port])))]
                            (-> (nrepl/client conn  1000)
                                (nrepl/message {:op "ns-vars"
                                                :ns (:ns provide)})
                                (nrepl/combine-responses)))]
    (map
     (fn [var-name]
       (let  [{:keys [column line file]} (with-open [conn (nrepl/connect :port (Integer. (get-in m [:remote :port])))]
                                           (-> (nrepl/client conn 1000)
                                               (nrepl/message {:op "info"
                                                               :ns (:ns provide)
                                                               :symbol var-name})
                                               (nrepl/combine-responses)))]
         {:name   var-name
          :column column
          :line   (dec line)
          :file   file}))
     ns-vars)))


(defmethod ig/init-key ::listener [_ _]
  (yada/listener ["/"
                  [["" (yada/resource
                        {:methods
                         {:get
                          {:produces #{"text/html"}
                           :response "<span style='font-family:menlo'>Ligh Code server.</span>"}}})]

                   ["nrepl" (yada/resource
                             {:methods
                              {:post
                               {:consumes #{"application/json"}
                                :produces #{"application/json"}
                                :response (fn [{:keys [body]}]
                                            (let [port    (:port body)
                                                  message (dissoc body :port)]

                                              (println "[MESSAGE]" message)

                                              (when-not (str/blank? port)
                                                (with-open [conn (nrepl/connect :port (Integer. port))]
                                                  (-> (nrepl/client conn 1000)
                                                      (nrepl/message message)
                                                      (nrepl/combine-responses)
                                                      doall)))))}}})]

                   ["tooling" (yada/resource
                               {:methods
                                {:post
                                 {:consumes #{"application/json"}
                                  :produces #{"application/json"}
                                  :response (fn [{:keys [body]}]
                                              (println "[BODY]" body)

                                              (when-not (str/blank? (get-in body [:remote :port]))
                                                (editor-provider body)))}}})]]]
                 {:port 8383}))


(defmethod ig/halt-key! ::listener [_ server]
  ((server :close)))

