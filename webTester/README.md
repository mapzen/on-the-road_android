router_snapper_tester
=====================

pull locations.db from the application

  $ adb pull /sdcard/Android/data/com.mapzen/files/locations.db db/

get fatjar from geojson

  cd <path to geojson>

  $ gradle clean fatjar
  $ cp build/libs/geojson-0.1-SNAPSHOT.jar <path to router_snapper_tester>/lib/
  

Run with jruby

  $ jruby -S bundle exec jruby app.rb
