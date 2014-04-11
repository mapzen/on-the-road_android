require File.join( File.dirname(__FILE__), 'deps')

CONF = YAML.load_file('settings/settings.yml')

Dir["#{CONF[:upload_path]}/*.db"].each do |database|
  puts database
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
    value.each do |stuff|
      record = key.constantize.new(stuff) 
      if record.valid?
        begin
          record.save!
        rescue ActiveRecord::RecordNotUnique => e
          puts "duplicate record skipping"
        rescue StandardError => bang
          puts "something terrible happened"
          puts bang.to_s
        end
      else
        puts "this record is not valid:"
        puts record.errors.to_s
      end 
    end
  end
end



