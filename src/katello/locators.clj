(ns katello.locators
  (:use [com.redhat.qe.auto.selenium.selenium :only [SeleniumLocatable browser ->browser sel-locator]]
        [com.redhat.qe.auto.navigate :only [nav-tree]]
        [com.redhat.qe.config :only [same-name]]
        [clojure.contrib.string :only [capitalize]])
  (:import [com.redhat.qe.auto.selenium Element LocatorTemplate]))

;;ui layer

(defmacro define-strategies
  "Create a function for each locator strategy in map m (which maps
  symbol to LocatorStrategy). Each function will be named the same as
  the symbol, take arguments and return a new element constructed
  with the locator strategy and args."
  [m]
  `(do ~@(for [loc-strat (keys m)]
           `(defn ~loc-strat [& args#]
              (Element. ~(m loc-strat) (into-array args#))))))

(define-strategies
  {link (LocatorTemplate. "" "link=$1")
   tab (LocatorTemplate. "Tab" "link=$1") 
   environment-link (LocatorTemplate. "Environment" "//ul[@class='breadcrumb']//a[normalize-space(.)='$1']")
   org-link (LocatorTemplate. "Organization" "//div[@id='list']//div[@id='$1']")
   cp-link (LocatorTemplate. "Provider" "//div[@id='list']//div[normalize-space(.)='$1']")
   textbox (LocatorTemplate. "" "xpath=//*[self::input[(@type='text' or @type='password' or @type='file') and @name='$1'] or self::textarea[@name='$1']]")
   env-breadcrumb-link (LocatorTemplate. "Environment Breadcrumb" "//a[@class='path_link' and normalize-space(.)='$1']")
   promotion-content-category (LocatorTemplate. "Content Category" "//div[@id='$1']")
   promotion-add-content-item (LocatorTemplate. "Add Content Item" "//a[@data-display_name='$1' and contains(.,'Add')]")
   promotion-remove-content-item (LocatorTemplate. "Remove Content Item" "//a[@data-display_name='$1' and contains(.,'Remove')]")
   promotion-content-item-n (LocatorTemplate. "Content item by index" "//div[@id='list']//li[$1]//span[@class='product-icon']")
   
   provider-sync-checkbox (LocatorTemplate. "Provider sync checkbox" "//td[div[@class='clickable' and contains(.,'$1')]]/input[@type='checkbox']")
   provider-sync-progress (LocatorTemplate.  "Provider progress" "//tr[td/div[@class='clickable' and contains(.,'$1')]]/td[5]")
   product-edit (LocatorTemplate. "Product edit" "//div[@id='products']//div[starts-with(@id, 'edit_product') and normalize-space(.)='$1']")
   notification-close-index (LocatorTemplate. "Notification close button" "xpath=(//a[@class='jnotify-close'])[$1]")
   user (LocatorTemplate. "User" "//div[@id='list']//div[@class='column_1' and normalize-space(.)='$1']")
   username-field (LocatorTemplate. "Username field" "//div[@id='users']//div[normalize-space(.)='$1']") 
   product-expand (LocatorTemplate. "Expand product" "//div[@id='products']//div[contains(@data-url,'products') and normalize-space(.)='$1']/..//img[@alt='Expand']")
   add-repository (LocatorTemplate. "Add Repository" "//div[@id='products']//div[normalize-space(.)='sync-test1-1308340698567']/..//div[normalize-space(.)='Add Repository' and contains(@class, 'button')]")
   system (LocatorTemplate. "System" "//div[@id='list']//div[normalize-space(.)='$1']")
   button-div (LocatorTemplate. "Button" "//div[contains(@class,'button') and normalize-space(.)='$1']")})

(defn promotion-env-breadcrumb [name & [next]]
  (Element. (format (if next "//a[.='%2$s' and contains(@class, 'path_link')]/../../..//a[.='%1$s']"
                        "//a[.='%1$s' and contains(@class, 'path_link')]")
                    name next)))
(defn- tabs "creates mapping eg: {:my-tab 'link=My Tab'}"
  [keys]
  (same-name capitalize tab keys))

(def uimap (merge
            {:notification "//div[contains(@class,'jnotify-notification')]"
             :error-message "//div[contains(@class,'jnotify-notification-error')]"
             :success-message "//div[contains(@class,'jnotify-notification-message')]"
             :spinner "//img[contains(@src,'spinner.gif')]"
             :save-inplace-edit "//button[.='Save']"
             ;; login page
             :username-text (textbox "username")
             :password-text (textbox "password")
             :log-in "commit"

             ;;main banner
             :account "//li[@class='hello']/a"
             :log-out "//a[normalize-space(.)='Logout']"
             
             ;;tabs with special chars in name
             :sub-organizations (tab "Sub-Organizations")

             ;;Organizations tab
             :new-organization "//a[@id='new']"
             :create-organization "organization_save"
             :org-name-text (textbox "name")
             :org-description-text (textbox "description")
             :org-environments (link "Environments")
             :edit-organization (link "Edit")
             :remove-organization (link "Remove Organization")
             :org-description-text-edit "organization[description]"
             
             ;;Environments tab
             :env-name-text (textbox "name")
             :env-description-text (textbox "description")
             :prior-environment "//select[@id='prior']"
             :create-environment "//div[contains(@class, 'environment_create')]"
             :new-environment "//div[normalize-space(.)='Add New Environment']"
             :remove-environment (link "Remove Environment")
             :env-name-text-edit "kp_environment[name]"
             :env-description-text-edit "kp_environment[description]"
             :env-prior-select-edit "kp_environment[prior]" 

             ;;Content Management tab
             ;;Providers
             :new-provider "new"
             :cp-name-text  "provider[name]"
             :cp-description-text "provider[description]"
             :cp-repository-url-text "provider[repository_url]"
             :cp-type-list  "name=provider[provider_type]"
             :cp-username-text (textbox "provider[login_credential_attributes][username]")
             :cp-password-text (textbox "provider[login_credential_attributes][password]")
             :cp-cert-text (textbox "provider[certificate_attributes][contents]")
             :cp-create-save "provider_save"
             :remove-provider (link "Remove Provider")
             :subscriptions (link "Subscriptions")
             :choose-file "//input[@type='file' and @id='kalpana_model_provider_contents']"
             :upload "//input[@value='Upload']"
             :products-and-repositories "//nav[contains(@class,'subnav')]//a[contains(.,'Products')]"
             
             ;;add product
             :add-product (button-div "Add Product")
             :create-product (button-div "Create")
             :product-name-text "product_name_field"
             :product-description-text "product_description_field"
             :product-url-text "product_url_field"
             :product-yum-checkbox "yum_type"
             :product-file-checkbox "file_type"
             ;;add repo
             :add-repository "//ul[//div[starts-with(@id,'edit_product') and normalize-space(.)='$1']]//div[starts-with(@id,'add_repository')]"
             :repo-name-text "//input[@name='repo[name]' and not(ancestor::div[contains(@style,'display: none')])]"
             :repo-url-text "//input[@name='repo[feed]' and not(ancestor::div[contains(@style,'display: none')])]" 
             :save-repository "//div[normalize-space(.)='Save Repository' and not(ancestor::div[contains(@style,'display: none')])]"             
             
             ;;Promotions subtab
             
             :products-category (promotion-content-category "products")
             :expand-path "path-collapsed"
             :errata-category (promotion-content-category "errata")
             :packages-category (promotion-content-category "packages")
             :kickstart-trees-category (promotion-content-category "kickstart trees")
             :review-for-promotion "//div[@id='changeset_action' and contains(.,'Review')]"
             :promote-to-next-environment "//div[@id='changeset_action' and contains(.,'Promote')]"
             :promotion-empty-list "//div[@id='left_accordion']//ul[contains(.,'available for promotion')]"
             :new-changeset "//a[contains(.,'New Changeset')]"
             :changeset-name-text (textbox "name")
             :save-changeset "save_changeset_button"


             
             ;;Sync Management subtab
             :synchronize-now "sync_button"

             ;;System Tab
             ;;Registered subtab
             ;;subtabs
             ;;General
             :system-name-text-edit "system[name]"
             :system-description-text-edit "system[description]"
             :system-location-text-edit "system[location]"
             
             ;;Administration tab
             ;;Users subtab
             :new-user "//a[@id='new']"
             :new-user-username-text "username_field"
             :new-user-password-text "password_field"
             :new-user-confirm-text "confirm_field"
             :save-user "save_user"
             :remove-user (link "Remove User")
             :enable-inline-help-checkbox "user[helptips_enabled]"
             :clear-disabled-helptips "clear_helptips"
             :change-password-text "password_field"
             :confirm-password-text "confirm_field"
             ;;Roles subtab
             :new-role "//a[@id='new']"
             :new-role-name-text "role_name_field"
             :save-role "save_role_button"
             :save-user-edit "save_password"
             }
             
            ;;regularly named tabs
            (tabs [:organizations
                   :administration
                   :systems
                   :content-management
                   :dashboard
                   :environments
                   :subscriptions
                   :create

                   ;;subtabs
                   :providers
                   :sync-management
                   :promotions
                   :users
                   :roles

                   :registered
                   :groups
                   :general
                   :subscriptions
                   :facts
                   :packages
                   ])))

(extend-protocol SeleniumLocatable
  clojure.lang.Keyword
  (sel-locator [k] (uimap k)))

(defn inactive-edit-field "Takes a locator for an active in-place edit field, returns the inactive version" [loc]
  (format "//div[@name='%1s']" (sel-locator loc)))

;;page layout
(defmacro via [link & [ajax-wait-for]]
  (if ajax-wait-for
    `(->browser (~'click ~link)
                (~'waitForVisible ~ajax-wait-for "15000"))
    `(browser ~'clickAndWait ~link)))

(def page-tree
  (nav-tree [:top-level [] (if-not (browser isElementPresent :log-out) (browser open "/"))
             [:content-management-tab [] (via :content-management)
              [:providers-tab [] (via :providers)
               [:new-provider-page [] (via :new-provider :cp-name-text)]
               [:named-provider-page [cp-name] (via (cp-link cp-name) :remove-provider)
                [:provider-products-repos-page [] (do (via :products-and-repositories
                                                           :add-product)
                                                      (browser sleep 2000))]]]
              [:sync-management-page [] (via :sync-management)]
              [:promotions-page [] (via :promotions)
               [:named-environment-promotions-page [env-name next-env-name]
                (do (browser click :expand-path)
                  (via (promotion-env-breadcrumb env-name next-env-name)))]]]
             [:systems-tab [] (via :systems)
              [:named-systems-page [system-name] (via (system system-name)
                                                      (inactive-edit-field :system-name-text-edit))]]
             
             [:organizations-tab [] (via :organizations)
              [:new-organization-page [] (via :new-organization :org-name-text)]
              [:named-organization-page [org-name] (via (org-link org-name) :remove-organization) 
               [:new-environment-page [] (via :new-environment :create-environment)]
               [:named-environment-page [env-name] (via (environment-link env-name) :remove-environment)]]]
             [:administration-tab [] (via :administration)
              [:users-tab [] (via :users)
               [:named-user-page [username] (via (user username) (username-field username) )]]
              [:roles-tab [] (via :roles)]]]))