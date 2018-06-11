(ns lightcode.config)

(def server-url
  "http://localhost:8383")


(def repl-api-url
  (str server-url "/repl"))


(def language-api-url
  (str server-url "/language"))