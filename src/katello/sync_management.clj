(ns katello.sync-management
  (:require [com.redhat.qe.auto.selenium.selenium :as sel]
            [com.redhat.qe.auto.selenium.selenium :refer [browser]]
            (katello [locators :as locators] 
                     [notifications :as notification] 
                     [ui-tasks :refer [navigate fill-ajax-form in-place-edit]]))
  (:import [com.thoughtworks.selenium SeleniumException]
           [java.text SimpleDateFormat]))

;; Locators

(swap! locators/uimap merge
  {:apply-sync-schedule        "apply_button"
   :new-sync-plan              "new"
   :sync-plan-name-text        "sync_plan[name]"
   :sync-plan-description-text "sync_plan[description]"
   :sync-plan-interval-select  "sync_plan[interval]"
   :sync-plan-date-text        "sync_plan[plan_date]"
   :sync-plan-time-text        "sync_plan[plan_time]"
   :save-sync-plan             "plan_save"})

(sel/template-fns
 {product-schedule       "//div[normalize-space(.)='%s']/following-sibling::div[1]"
  provider-sync-checkbox "//table[@id='products_table']//label[normalize-space(.)='%s']/..//input"
  provider-sync-progress "//tr[td/label[normalize-space(.)='%s']]/td[5]"
  repo-enable-checkbox   "//table[@id='products_table']//label[normalize-space(.)='%s']/..//input" 
  sync-plan              "//div[@id='plans']//div[normalize-space(.)='%s'"
  schedule               "//div[normalize-space(.)='%s']"
  })

;; Tasks

(def plan-dateformat (SimpleDateFormat. "MM/dd/yyyy"))
(def plan-timeformat (SimpleDateFormat. "hh:mm aa"))
(defn- date-str [d] (.format plan-dateformat d))
(defn- time-str [d] (.format plan-timeformat d))

(defn- split-date [{:keys [start-date start-date-literal start-time-literal]}]
  (list (if start-date (date-str start-date) start-date-literal)
        (if start-date (time-str start-date) start-time-literal)))

(defn create-plan
  "Creates a sync plan with the given properties. Either specify a
  start-date (as a java.util.Date object) or a separate string for
  start-date-literal 'MM/dd/yyyy', and start-time-literal 'hh:mm aa'
  The latter can also be used to specify invalid dates for validation
  tests."
  [{:keys [name description interval start-date
           start-date-literal start-time-literal] :as m}]
  (navigate :new-sync-plan-page)
  (let [[date time] (split-date m)]
    (fill-ajax-form {:sync-plan-name-text name
                     :sync-plan-description-text description
                     :sync-plan-interval-select interval
                     :sync-plan-time-text time
                     :sync-plan-date-text date}
                    :save-sync-plan)
    (notification/check-for-success {:match-pred (notification/request-type? :sync-create)})))


(defn edit-plan
  "Edits the given sync plan with optional new properties. See also
  create-sync-plan for more details."
  [name {:keys [new-name
                description interval start-date start-date-literal
                start-time-literal] :as m}]
  (navigate :named-sync-plan-page {:sync-plan-name name})
  (let [[date time] (split-date m)]
    (in-place-edit {:sync-plan-name-text new-name
                    :sync-plan-description-text description
                    :sync-plan-interval-select interval
                    :sync-plan-time-text time
                    :sync-plan-date-text date}))
  (notification/check-for-success {:match-pred (notification/request-type? :sync-update)}))

(defn schedule
  "Schedules the given list of products to be synced using the given
  sync plan name."
  [{:keys [products plan-name]}]
  (navigate :sync-schedule-page)
  (doseq [product products]
    (browser click (schedule product)))
  (browser click (sync-plan plan-name))
  (browser clickAndWait :apply-sync-schedule )
  (notification/check-for-success))  ;notif class is 'undefined' so
                                     ;don't match 

(defn current-plan
  "Returns a map of what sync plan a product is currently scheduled
  for. nil if UI says 'None'"
  [product-names]
  (navigate :sync-schedule-page)
  (zipmap product-names
          (replace {"None" nil}
                   (doall (for [product-name product-names]
                            (browser getText (product-schedule product-name)))))))

(def messages {:ok "Sync complete."
               :fail "Error syncing!"})

(defn complete-status
  "Returns final status if complete. If sync is still in progress, not
  synced, or queued, returns nil."
  [product]
  (some #{(browser getText (provider-sync-progress product))}
        (vals messages)))

(defn success? "Returns true if given sync result is a success."
  [res]
  (= res (:ok messages)))


(defn perform-sync
  "Syncs the given list of repositories. Also takes an optional
  timeout (in ms) of how long to wait for the sync to complete before
  throwing an error.  Default timeout is 2 minutes."
  [repos & [{:keys [timeout]}]]
  (navigate :sync-status-page)
  (doseq [repo repos]
    (browser check (provider-sync-checkbox repo)))
  (browser click :synchronize-now)
  (browser sleep 10000)
  (zipmap repos (for [repo repos]
                     (sel/loop-with-timeout (or timeout 120000) []
                       (or (complete-status repo)
                           (do (Thread/sleep 10000)
                               (recur)))))))
