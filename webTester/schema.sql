DROP TABLE IF EXISTS `locations`;
CREATE TABLE locations (
  _id varchar(100) primary key,
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
  time double not null,
  route_id varchar(100) not null,
  dump varchar(500) not null
) DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `routes`;
CREATE TABLE routes (
  _id varchar(100) primary key,
  raw varchar(5000) not null
) DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `log_entries`;
CREATE TABLE log_entries (
  _id varchar(100) primary key,
  tag varchar(50) not null,
  msg varchar(500) not null
) DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `route_geometry`;
CREATE TABLE route_geometry (
  _id varchar(100) primary key,
  route_id varchar(100) not null,
  position integer not null,
  lat text not null,
  lng text not null
) DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

CREATE UNIQUE INDEX route_lat_lng on route_geometry (route_id, lat(20), lng(20));
