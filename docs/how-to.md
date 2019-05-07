# How to

## Use higher-order components

Generally, we prefer to use Hooks for reusing logic or resources across our app.
However, many React libraries still use HoC to provide things like context
values and sharing logic.

Using HoC is easy in hx. Below is an example:

```clojure
(ns my-app.feature
  (:require [hx.react :as hx :refer [defnc]]
            ["some-npm-lib" :as some-lib]
            ["react" :as react]))

;;
;; First: the simple way
;; We create our component, and then def another var that wraps our component
;; with the HoC, `withSomeValue`.
;;

(defnc MyComponent [{:keys [someValue]}]
  [:div "I was renderd with " someValue])

(def WrappedComponent (some-lib/withSomeValue MyComponent))


;;
;; If we want to wrap the component in multiple HoC, we can use the thread-
;; first operator to make it easier to read.
;;

(def WrappedComponent (-> MyComponent
                          (some-lib/withSomeValue)
                          (react/memo =)))

;;
;; Second: the easy way
;; We use `defnc`'s `:wrap` option to give it a vector of higher-order components
;; that will be applied like `->`.
;;

(defnc WrappedComponent [{:keys [someValue]}]
  ;; This options map is parsed at compile time to expand to the above
  ;; It's the same way you can pass in `:pre` and `:post` checks for
  ;; `defn` (and likewise, `defnc`).
  {:wrap [(some-lib/withSomeValue)
          (react/memo =)]}
  ;; Return hiccup
  [:div "I was rendered with " someValue])
```

## Use children-as-function / render props

Like above, we'd prefer to use hooks, but many 3rd party libraries expose
a child-as-function API like so:

```javascript
function MyComponent () {
   return <SomeLibComponent>
    {(someValue) => <div>I was rendered with {someValue}</div>}
   </SomeLibComponent>
}
```

In hx, it would look like so:

```clojure
(defnc MyComponent [_]
  [SomeLibComponent
   (fn [someValue]
    [:div "I was rendered with " someValue])])
```

hx will detect that you passed a function in as a child, and wrap it
so that if it returns hiccup, it will be parsed into ReactElements for use
with components that expect a render function.

If you have a non-child prop that is expected to be given a render function,
then you'll need to wrap your hiccup in `hx.react/f` to parse the hiccup
into ReactElements.

Assuming this JS API:

```javascript
function MyComponent () {
  let renderFn = (someValue) =>
                    <div> I was rendered with {someValue}</div>;
  return <SomeLibComponent render={renderFn} />;
}
```

```clojure
(defnc MyComponent [_]
  (let [render-fn (fn [someValue] 
                    (hx/f [:div "I was rendered with " someValue]))]
    [SomeLibComponent {:render render-fn}]))
```