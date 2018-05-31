(ns lightcode.core
  (:require
   ["vscode" :as vscode]
   ["nrepl-client" :as nrepl-client]

   [lightcode.lib :as lib]))


;; ------------------------------------------------
;; HELPERS
;; ------------------------------------------------

(defn- register-command [*sys cmd]
  (vscode/commands.registerCommand (-> cmd meta :cmd) #(cmd *sys)))


(defn- register-disposable [^js context ^js disposable]
  (-> (.-subscriptions context)
      (.push disposable)))


(defn- reg-cmd [^js context *sys cmd]
  (->> (register-command *sys cmd)
       (register-disposable context)))



;; ------------------------------------------------
;; COMMANDS
;; ------------------------------------------------

(defn ^{:cmd "lightcode.switchOn"} cmd-switch-on [*sys]
  (let [socket (.connect nrepl-client #js {:host "localhost" :port (lib/nrepl-port!)})]
    (doto socket
      (.once "connect" (fn []
                         (js/console.log "Light Code is on")

                         (.clone (get @*sys :socket) (fn [err messages]
                                                       (when-not err
                                                         (let [[{:keys [new-session]}] (js->clj messages :keywordize-keys true)]
                                                           (swap! *sys assoc :lc.session/clj new-session)))))))

      (.once "end" (fn []
                     (js/console.log "Light Code is off")))

      (.on "error" (fn [error]
                     (js/console.log "Light Code socket error" error))))

    (swap! *sys assoc :socket socket)))


(defn ^{:cmd "lightcode.switchOff"} cmd-switch-off [*sys]
  (when-let [socket (get @*sys :socket)]
    (if-let [session (get @*sys :lc.session/clj)]
      (.close socket session (fn [_ _]
                               (swap! *sys dissoc :lc.session/clj)
                               (.end socket)))
      (.end socket))
    (swap! *sys dissoc :socket)))



;; ------------------------------------------------
;; CONFIGURATION
;; ------------------------------------------------

(def ClojureLanguageConfiguration
  (clj->js {:wordPattern #"[^\[\]\(\)\{\};\s\"\\]+"
            :indentationRules {:increaseIndentPattern #"[\[\(\{]"
                               :decreaseIndentPattern nil}}))



;; ------------------------------------------------
;; PROVIDERS
;; ------------------------------------------------




;; ------------------------------------------------
;; EXTENSION STATE
;; ------------------------------------------------

(def *sys
  (atom {}))



(defn activate [^js context]
  (vscode/languages.setLanguageConfiguration "clojure"  ClojureLanguageConfiguration)

  (reg-cmd context *sys #'cmd-switch-on)
  (reg-cmd context *sys #'cmd-switch-off)

  (js/console.log "Light Code is active."))


(defn deactivate []
  nil)