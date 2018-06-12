(ns lightcode.editor
  (:require
   ["vscode" :as vscode]

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


(defn doc-file-extension [doc]
  (fs/ext (.-fileName doc)))


(defn doc-file-name [doc]
  (fs/basename (.-fileName doc)))


(defn doc-src-path-relative-path
  "Source-path-relative path of the source file, e.g. clojure/java/io.clj"
  [doc]
  (let [[_ namespace-name] (doc-ns doc)
        rpath (str/replace (name namespace-name) #"\." "/")
        ext (doc-file-extension doc)]
    (str rpath ext)))


(defn active-editor-clojure? []
  (and vscode/window.activeTextEditor
       (= vscode/window.activeTextEditor.document.languageId "clojure")))


(defn active-clojure-document []
  (when (active-editor-clojure?)
    (let [document vscode/window.activeTextEditor.document]
      {:lc.document/object                    document
       :lc.document/content                   (.getText document)
       :lc.document/src-path-relative-path    (doc-src-path-relative-path document)
       :lc.document/file-name                 (doc-file-name document)
       :lc.document/env                       (str/replace (doc-file-extension document) #"\." "")})))



(defn word-at-position [document position]
  (when-let [range (.getWordRangeAtPosition document position)]
    (.getText document range)))


(defn nrepl-port! []
  (let [path (str vscode/workspace.rootPath "/.nrepl-port")]
    (when (fs/file? path)
      (js/parseInt (io/slurp path)))))


(defn document-language [document]
  (str/replace (doc-file-extension document) #"\." ""))


(defn env [document]
  (str/replace (doc-file-extension document) #"\." ""))

