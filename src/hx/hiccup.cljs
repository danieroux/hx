(ns hx.hiccup
  (:require [clojure.walk :as walk]
            [hx.utils :as util]
            ["react" :as react]))

(defprotocol IElement
  (-parse-element [el args] "Parses an element"))

;; we use a multimethod to dispatch on identity so that consumers
;; can override this for custom values e.g. :<> for React fragments
(defmulti parse-element
  (fn [el & more]
    (identity el))
  :default ::default)

;; if no multimethod for specific el, then apply general parsing rules
(defmethod parse-element
  ::default
  ([el & args]
   (-parse-element el args)))

(defn make-node [el props & children]
  (apply react/createElement el props children))

(defn parse [hiccup]
  (apply parse-element hiccup))

(extend-protocol IElement
  nil
  (-parse-element [_ _]
    nil)
  number
  (-parse-element [n _]
    n)
  string
  (-parse-element [s _]
    s)
  PersistentVector
  (-parse-element [form _]
    (apply parse-element form))

  LazySeq
  (-parse-element [a b]
    (apply make-node
           react/Fragment
           nil
           (into [] (map parse-element a))))

  default
  (-parse-element [el args]
    (let [props (first args)
          children (rest args)
          props? (map? props)]
      (if (react/isValidElement el)
        el
        (apply make-node
               (if (keyword? el) (name el) el)
               (if props?
                 (-> props
                     (util/clj->props))
                 nil)
               (into (if props? [] [(parse-element props)]) (map parse-element children)))))))