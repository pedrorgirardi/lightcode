(ns lightcode.server.core
  (:require
   [yada.yada :as yada]
   [integrant.core :as ig]
   [clojure.tools.nrepl :as nrepl]))


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
                                            (let [port    (Integer. (:port body))
                                                  message (dissoc body :port)]

                                              (println "[MESSAGE]" message)

                                              (with-open [conn (nrepl/connect :port port)]
                                                (-> (nrepl/client conn 1000)
                                                    (nrepl/message message)
                                                    (nrepl/combine-responses)
                                                    doall))))}}})]]]
                 {:port 8383}))


(defmethod ig/halt-key! ::listener [_ server]
  ((server :close)))

