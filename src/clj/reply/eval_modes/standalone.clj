(ns reply.eval-modes.standalone
  (:require [reply.eval-modes.standalone.concurrency :as concurrency]
            [reply.eval-state :as eval-state]
            [reply.initialization :as initialization]
            [reply.reader.jline :as reader.jline]
            [reply.signals :as signals]))

(def reply-read
  (fn [prompt exit]
    (concurrency/starting-read!)
    (reader.jline/read prompt exit)))

(def reply-eval
  (concurrency/act-in-future
    (fn [form]
      (eval-state/with-bindings
        (partial eval form)))))

(def reply-print
  (concurrency/act-in-future prn))

(defn handle-ctrl-c [signal]
  (print "^C")
  (flush)
  (concurrency/stop-running-actions)
  (reader.jline/reset-reader))

(defn main [options]
  (signals/set-signal-handler! "INT" handle-ctrl-c)
  (clojure.main/repl :read reply-read
                     :eval reply-eval
                     :print reply-print
                     :init #(initialization/eval-in-user-ns
                             (initialization/construct-init-code options))
                     :prompt (constantly false)
                     :need-prompt (constantly false)))

