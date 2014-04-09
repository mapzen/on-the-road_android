root = File.dirname(__FILE__)
$:.unshift File.expand_path('../lib', root) 
$:.unshift File.expand_path('../build/libs/', root) 

require "rubygems"
require "bundler"
require 'active_record'
require 'digest/md5'

Bundler.require :default, (ENV["RACK_ENV"] || "development").to_sym

CONF = YAML.load_file('conf/settings.yml')

ActiveRecord::Base.establish_connection(
  :adapter => CONF[:adapter],   # 'mysql2',
  :database => CONF[:database], # "on_the_road",
  :username => CONF[:username], # "test",
  :password => CONF[:password]  # "test"
)

set :public_folder, Proc.new { File.join(root, "static") }

require File.join( File.dirname(__FILE__), 'app' )

run WebApp::App
