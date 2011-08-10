(defproject katello "1.0.0-SNAPSHOT"
  :description "Katello automation"  
  :main katello.tests.suite
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [webui-framework "1.0.2-SNAPSHOT"]
                 [test-clj "0.2.0-SNAPSHOT"]
                 [error.handler "1.1.0-SNAPSHOT"]
                 [inflections "0.4"]
                 [clj-http "0.1.3"]
                 [robert/hooke "1.1.1"]]
  :repositories {"clojars" {:url "http://clojars.org/repo"
                            :snapshots {:update :always}}})

(comment "Execute this in the repl to load everything and start selenium"
         (do (require 'katello.tasks :reload-all)
             (require 'katello.conf :reload)
             (require 'katello.tests.setup)
             (katello.conf/init)
             (katello.tests.setup/start-sel)
             (katello.tasks/login "admin" "admin"))
         )

