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
