require 'sinatra/base'

class Route < ActiveRecord::Base
  self.primary_key = '_id'
  has_many :route_geometries, order: 'position'
  has_many :locations
end

class Location < ActiveRecord::Base
  self.primary_key = '_id'
  belongs_to :route
end

class RouteGeometry < ActiveRecord::Base
  self.primary_key = '_id'
  self.table_name = 'route_geometry'
  belongs_to :route
end

module WebApp
  class App < Sinatra::Base
    post "/upload" do
      raw = request.body.read
      filename = Digest::MD5.hexdigest(raw)
      file = File.new("#{CONF[:upload_path]}/database-#{filename}.db", "w") 
      file.puts raw
      file.close
      "success"
    end

    get "/" do
      if route_id = params[:route_id]
        @route = Route.where(:_id => route_id).first
      else
        @route = Route.first
      end
      @locations = @route.locations
      erb :index
    end

    get "/snap" do
      route = Route.where(:_id => params[:route_id].try(:to_i)).first
      osrm_route = com.mapzen.osrm.Route.new(route.raw)  
      if osrm_route.nil?
        puts "boom"
      end
      osrm_route.set_current_leg(params[:current_leg].to_i)
      snapped = osrm_route.snap_to_route([params[:lat].to_f, params[:lng].to_f])
      currentLeg = osrm_route.get_current_leg
      if snapped
        "#{snapped[0]}, #{snapped[1]}, #{currentLeg}"     
      else
        "lost?"
      end
    end
  end
end
