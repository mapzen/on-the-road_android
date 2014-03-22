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
    get "/" do
      @route = Route.where(:_id => params[:route_id]).first
      @locations = @route.locations
      erb :index
    end

    get "/snap" do
      route = Route.where(:_id => params[:route_id]).first
      osrm_route = com.mapzen.osrm.Route.new(route.raw)  
      osrm_route.set_current_leg(params[:current_leg].to_i)
      osrm_route.get_geometry
      snapped = osrm_route.snap_to_route([params[:lat].to_f, params[:lng].to_f])
      currentLeg = osrm_route.get_current_leg
      "#{snapped[0]}, #{snapped[1]}, #{currentLeg}"     
    end
  end
end
