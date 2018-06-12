(ns dev
  (:require
   [integrant.core :as ig]
   [integrant.repl :refer [go reset reset-all]]
   [integrant.repl.state :refer [system]]
   [clojure.tools.nrepl :as nrepl]

   [lightcode.server.repl :as repl]
   [lightcode.server.language :as language]))


(integrant.repl/set-prep! (fn []
                            (ig/load-namespaces {:lightcode.server.core/listener {}})

                            {:lightcode.server.core/listener {}}))



(comment


 (def port
   (Integer. (slurp ".nrepl-port")))


 @repl/*sessions


 ;; CLOJURE TESTS

 ;; New Clojure session - if there isn't one already
 (with-open [conn (nrepl/connect :port port)]
   (let [client (nrepl/client conn 1000)]
     (repl/session! {} client ["clj" port])))


 ;; Load 'lightcode.server.core'
 (repl/repl {:op      "load-file"
             :file    (slurp "src/main/lightcode/server/core.clj")
             :context {:env   "clj"
                       :nrepl {:port port}}})


 ;; Ask for info for the symbol `op!`
 (repl/repl {:op      "info"
             :ns      "lightcode.server.core"
             :symbol  "session!"
             :context {:env   "clj"
                       :nrepl {:port port}}})


 (language/provider {:provider "DocumentSymbolProvider"
                     :text     (slurp "src/main/lightcode/server/core.clj")
                     :context  {:env   "clj"
                                :nrepl {:port port}}})



 ;; CLOJURE SCRIPT TESTS

 (with-open [conn (nrepl/connect :port 63998)]
   (let [client  (nrepl/client conn 5000)
         context {:cljs-repl            "shadow-cljs"
                  :start-cljs-repl-code "(shadow.cljs.devtools.api/nrepl-select :extension)"}]
     (repl/session! context client ["cljs" 63998])))


 (repl/repl {:op      "info"
             :ns      "lightcode.core"
             :symbol  "register-disposable"
             :context {:env                  "cljs"
                       :nrepl                {:port 63998}
                       :cljs-repl            "shadow-cljs"
                       :start-cljs-repl-code "(shadow.cljs.devtools.api/nrepl-select :extension)"}})

 )