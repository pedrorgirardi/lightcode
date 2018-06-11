(ns lightcode.server.core
  (:require
   [yada.yada :as yada]
   [integrant.core :as ig]
   [clojure.string :as str]
   [clojure.tools.logging :as log]

   [lightcode.server.language :as language]
   [lightcode.server.repl :as repl]))


(defmethod ig/init-key ::listener [_ _]
  (yada/listener ["/"
                  [["" (yada/resource
                        {:methods
                         {:get
                          {:produces #{"text/html"}
                           :response "<span style='font-family:menlo'>Ligh Code server.</span>"}}})]

                   ["repl" (yada/resource
                            {:methods
                             {:post
                              {:consumes #{"application/json"}
                               :produces #{"application/json"}
                               :response (fn [{:keys [body]}]
                                           (repl/repl body))}}})]

                   ["language" (yada/resource
                                {:methods
                                 {:post
                                  {:consumes #{"application/json"}
                                   :produces #{"application/json"}
                                   :response (fn [{:keys [body]}]
                                               (or (language/provider body) {}))}}})]]]
                 {:port 8383}))


(defmethod ig/halt-key! ::listener [_ server]
  ((server :close)))

