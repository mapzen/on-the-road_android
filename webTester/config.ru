root = File.dirname(__FILE__)
$:.unshift File.expand_path('../lib', root) 
$:.unshift File.expand_path('../build/libs/', root) 

require "rubygems"
require "bundler"
require 'active_record'

Bundler.require :default, (ENV["RACK_ENV"] || "development").to_sym

ActiveRecord::Base.establish_connection(
  :adapter => 'sqlite3',
  :database => "#{root}/db/locations.db"
)

set :public_folder, Proc.new { File.join(root, "static") }

require File.join( File.dirname(__FILE__), 'app' )

run WebApp::App
