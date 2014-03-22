To test with the web interface

gradle clean fatjar

cd webTester

jruby -S bundle exec warble

java -jar webTester.war


