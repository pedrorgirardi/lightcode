(ns lightcode.server.main
  (:require
   [lightcode.server.core :as core]
   [integrant.core :as ig])
  (:gen-class))


(defn -main
  "Light Code main."
  [& args]
  (ig/init {:lightcode.server.core/listener {}}))
