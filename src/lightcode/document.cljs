(ns lightcode.document
  (:require
   [vscode :as vscode]

   [clojure.string :as str]
   [cljs.reader :as r]
   [cljs-node-io.core :as io]
   [cljs-node-io.fs :as fs]))


(defn read-document [document]
  (r/read-string (str "[" document "]")))


(defn read-document-ns-name [document]
  (let [[_ n] (r/read-string (.getText document))]
    (name n)))


(defn doc-ns [doc]
  (r/read-string (.getText doc)))


(defn file-extension [^js document]
  (fs/ext (.-fileName document)))


(defn file-basename [doc]
  (fs/basename (.-fileName doc)))


(defn src-path-relative-path
  "Source-path-relative path of the source file, e.g. clojure/java/io.clj"
  [doc]
  (let [[_ namespace-name] (doc-ns doc)
        rpath (str/replace (name namespace-name) #"\." "/")
        ext (file-extension doc)]
    (str rpath ext)))


(defn word-at-position [document position]
  (when-let [range (.getWordRangeAtPosition document position)]
    (.getText document range)))


(defn clojure? [^js document]
  (=  (.-languageId document) "clojure"))


(defn file-language-extension [document]
  (str/replace (file-extension document) #"\." ""))


