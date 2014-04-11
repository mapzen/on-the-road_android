root = File.dirname(__FILE__)
$:.unshift File.expand_path('../lib', root) 
$:.unshift File.expand_path('../build/libs/', root) 

require "rubygems"
require "bundler"
require 'active_record'
require 'digest/md5'
require 'celluloid'
require 'celluloid/autostart'
require File.join( File.dirname(__FILE__), 'import_data_worker' )
require File.join( File.dirname(__FILE__), 'models' )
