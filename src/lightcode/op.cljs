(ns lightcode.op
  (:require
   [axios :as axios]

   [kitchen-async.promise :as p]
   [lightcode.config :as config]
   [lightcode.editor :as editor]
   [lightcode.workspace :as workspace]))


(defn send! [env message]
  (let [context {:context (merge {:nrepl {:port (editor/nrepl-port!)}
                                  :env   env}

                                 (workspace/cljs-repl-context!))}
        message (merge message context)]
    (.post axios config/repl-api-url (clj->js message))))


(defn ->info
  "`ns` Namespace name, e.g. lightcode.op
   `symbol` Symbol, e.g. send!"
  [ns symbol]
  {:op     "info"
   :ns     ns
   :symbol symbol})


(defn info!
  "Sends an 'info' message.
  
  `ns` Namespace name, e.g. lightcode.op
  `symbol` Symbol, e.g. send!"
  [env ns symbol]
  (send! env (->info ns symbol)))

(defn ->eldoc
  "`ns` Namespace name, e.g. lightcode.op
   `symbol` Symbol, e.g. send!"
  [ns symbol]
  {:op     "eldoc"
   :ns     ns
   :symbol symbol})


(defn eldoc!
  "Sends an 'eldoc' message.
  
  `ns` Namespace name, e.g. lightcode.op
  `symbol` Symbol, e.g. send!"
  [env ns symbol]
  (send! env (->eldoc ns symbol)))


(defn ->load-file
  "`content` Full contents of a file of code
   `path` Source-path-relative path of the source file, e.g. clojure/java/io.clj
   `name` Name of source file, e.g. io.clj"
  [content path name]
  {:op        "load-file"
   :file      content
   :file-path path
   :file-name name})

(defn load-file!
  "`content` Full contents of a file of code
   `path` Source-path-relative path of the source file, e.g. clojure/java/io.clj
   `name` Name of source file, e.g. io.clj"
  [env content path name]
  (send! env (->load-file content path name)))


(defn ->ns-vars
  "`ns` Namespace name, e.g. lightcode.op"
  [ns]
  {:op "ns-vars"
   :ns ns})


(defn ns-vars!
  "Sends a 'ns-vars' message.
  
  `ns` Namespace name, e.g. lightcode.op
  `symbol` Symbol, e.g. send!"
  [env ns]
  (send! env (->ns-vars ns)))


(defn ->ns-vars-with-meta
  "`ns` Namespace name, e.g. lightcode.op"
  [ns]
  {:op "ns-vars-with-meta"
   :ns ns})


(defn ns-vars-with-meta!
  "Sends a 'ns-vars-with-meta' message.
  
  `ns` Namespace name, e.g. lightcode.op
  `symbol` Symbol, e.g. send!"
  [env ns]
  (send! env (->ns-vars-with-meta ns)))


(defn ->ns-load-all []
  {:op "ns-load-all"})


(defn ns-load-all!
  "Sends a 'ns-load-all' message."
  [env]
  (send! env (->ns-load-all)))