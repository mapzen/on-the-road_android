require 'sucker_punch/job'
require 'logger'

require File.join( File.dirname(__FILE__), 'deps')
CONF = YAML.load_file('settings/settings.yml')

class ImportDataWorker
  include SuckerPunch::Job

  def perform(name)
    logger = ::Logger.new('/var/log/jetty/import_data_worker.log')
    ActiveRecord::Base.establish_connection(
      adapter: 'sqlite3',
      database: "#{CONF[:upload_path]}/#{name}"
    )
    all_data = {}
    ActiveRecord::Base.connection.tables.each do |table|
      unless table =~ /android_metadata|log_entries/
        class_name = table.singularize.camelize
        collection = []
        class_name.constantize.all.each do |record|
          collection << record.attributes
        end
        all_data[class_name] = collection
      end
    end
    ActiveRecord::Base.establish_connection(
      :adapter => CONF[:adapter],   # 'mysql2',
      :database => CONF[:database], # "on_the_road",
      :username => CONF[:username], # "test",
      :password => CONF[:password]  # "test"
    )
    all_data.each do |key, value|
      value.each do |stuff|
        record = key.constantize.new(stuff) 
        if record.valid?
          begin
            record.save!
          rescue ActiveRecord::RecordNotUnique => e
            logger.warn("duplicate record skipping")
          rescue StandardError => bang
            logger.error("something terrible happened")
            logger.error(bang.to_s)
          end
        else
          logger.warn("this record is not valid:")
          logger.warn(record.errors.to_s)
        end 
      end
    end
  end
end

