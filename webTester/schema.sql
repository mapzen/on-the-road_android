DROP TABLE IF EXISTS `locations`;
CREATE TABLE locations (
  _id integer primary key AUTO_INCREMENT,
  provider varchar(100) not null,
  lat varchar(20) not null,
  corrected_lat varchar(20),
  instruction_lat varchar(20),
  lng varchar(20) not null,
  corrected_lng varchar(20),
  instruction_lng varchar(20),
  instruction_bearing integer,
  alt varchar(20) not null,
  acc integer not null,
  time integer not null,
  route_id integer not null,
  dump varchar(100) not null
);

DROP TABLE IF EXISTS `routes`;
CREATE TABLE routes (
  _id integer primary key AUTO_INCREMENT,
  raw varchar(500) not null
);

DROP TABLE IF EXISTS `log_entries`;
CREATE TABLE log_entries (
  _id integer primary key AUTO_INCREMENT,
  tag varchar(50) not null,
  msg varchar(500) not null
);

DROP TABLE IF EXISTS `route_geometry`;
CREATE TABLE route_geometry (
  _id integer primary key AUTO_INCREMENT,
  route_id integer not null,
  position integer not null,
  lat text not null,
  lng text not null
);

CREATE UNIQUE INDEX route_lat_lng on route_geometry (route_id, lat(20), lng(20));
