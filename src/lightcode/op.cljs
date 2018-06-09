(ns lightcode.op
  (:require
   ["axios" :as axios]

   [kitchen-async.promise :as p]
   [lightcode.lib :as lib]))

(def server-url
  "http://localhost:8383/nrepl")


(defn send! [message]
  (let [message (clj->js (assoc message :port (lib/nrepl-port!)))]
    (.post axios server-url message)))


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
  [ns symbol]
  (send! (->info ns symbol)))


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
  [content path name]
  (send! (->load-file content path name)))


(defn ->ns-vars
  "`ns` Namespace name, e.g. lightcode.op"
  [ns]
  {:op "ns-vars"
   :ns ns})


(defn ns-vars!
  "Sends a 'ns-vars' message.
  
  `ns` Namespace name, e.g. lightcode.op
  `symbol` Symbol, e.g. send!"
  [ns]
  (send! (->ns-vars ns)))


(defn ->ns-vars-with-meta
  "`ns` Namespace name, e.g. lightcode.op"
  [ns]
  {:op "ns-vars-with-meta"
   :ns ns})


(defn ns-vars-with-meta!
  "Sends a 'ns-vars-with-meta' message.
  
  `ns` Namespace name, e.g. lightcode.op
  `symbol` Symbol, e.g. send!"
  [ns]
  (send! (->ns-vars-with-meta ns)))