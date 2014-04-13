require File.join( File.dirname(__FILE__), 'deps' )
Bundler.require :default, (ENV["RACK_ENV"] || "development").to_sym

CONF = YAML.load_file('settings/settings.yml')

ActiveRecord::Base.establish_connection(
  :adapter => CONF[:adapter],   # 'mysql2',
  :database => CONF[:database], # "on_the_road",
  :username => CONF[:username], # "test",
  :password => CONF[:password],  # "test"
  :host => CONF[:host]  # "localhost"
)

require File.join( File.dirname(__FILE__), 'app' )

set :public_folder, Proc.new { File.join(root, "static") }

run WebApp::App
