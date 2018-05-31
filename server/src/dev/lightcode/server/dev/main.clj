(ns lightcode.server.dev.main
  (:require
   [integrant.repl :refer [go reset reset-all]]
   [integrant.repl.state :refer [system]]
   [rebel-readline.clojure.main]
   [rebel-readline.core]
   [io.aviso.ansi]))


(defn -main
  [& args]
  (rebel-readline.core/ensure-terminal
   (rebel-readline.clojure.main/repl
    :init (fn []
            (try
              (println "[Light Code] Loading Clojure code, please wait...")
              (require 'dev)
              (in-ns 'dev)
              (println (io.aviso.ansi/bold-yellow "[Light Code] Now enter (go) to start the dev system"))
              (catch Exception e
                (.printStackTrace e)))))))