(ns lightcode.config)

(def server-url
  "http://localhost:8383")


(def server-nrepl-url
  (str server-url "/nrepl"))


(def server-tooling-url
  (str server-url "/tooling"))