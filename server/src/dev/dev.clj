(ns dev
  (:require
   [integrant.core :as ig]
   [integrant.repl :refer [go reset reset-all]]
   [integrant.repl.state :refer [system]]))


(integrant.repl/set-prep! (fn []
                            (ig/load-namespaces {:lightcode.server.core/listener {}})

                            {:lightcode.server.core/listener {}}))
