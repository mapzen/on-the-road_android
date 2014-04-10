require File.join( File.dirname(__FILE__), 'deps')

CONF = YAML.load_file('settings/settings.yml')

Dir["#{CONF[:upload_path]}/*.db"].each do |database|
  all_data = {}
  ActiveRecord::Base.establish_connection(
    adapter: 'sqlite3',
    database: database
  )
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
    begin
      key.constantize.create!(value)
    rescue ActiveRecord::RecordNotUnique => e
      puts "duplicated #{key}: value #{value}"
    end
  end
end



