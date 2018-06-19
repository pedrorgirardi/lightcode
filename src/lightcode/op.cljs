(ns lightcode.op)


(defn ->info
  "`ns` Namespace name, e.g. lightcode.op
   `symbol` Symbol, e.g. send!"
  [ns symbol]
  {:op     "info"
   :ns     ns
   :symbol symbol})


(defn ->eldoc
  "`ns` Namespace name, e.g. lightcode.op
   `symbol` Symbol, e.g. send!"
  [ns symbol]
  {:op     "eldoc"
   :ns     ns
   :symbol symbol})


(defn ->load-file
  "`content` Full contents of a file of code
   `path` Source-path-relative path of the source file, e.g. clojure/java/io.clj
   `name` Name of source file, e.g. io.clj"
  [content path name]
  {:op        "load-file"
   :file      content
   :file-path path
   :file-name name})


(defn ->ns-vars
  "`ns` Namespace name, e.g. lightcode.op"
  [ns]
  {:op "ns-vars"
   :ns ns})


(defn ->ns-vars-with-meta
  "`ns` Namespace name, e.g. lightcode.op"
  [ns]
  {:op "ns-vars-with-meta"
   :ns ns})


(defn ->ns-load-all []
  {:op "ns-load-all"})
