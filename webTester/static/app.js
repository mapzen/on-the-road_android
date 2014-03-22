var dropDot = function(theMap, lat, lng, color) {
	L.marker([lat, lng], {icon: L.divIcon({className: 'count-icon-' + color, html: "", iconSize: [10, 10]})}).addTo(theMap);
}

var index = 0;
var addDot = function(theMap) {
	if(index < window.locs.length) {
		var value = window.locs[index];
		dropDot(theMap, value[2], value[3], 'red');
		dropDot(theMap, value[0], value[1], 'blue');
		console.log("Testing");
		console.log(index);
		++index;
		window.setTimeout(function() { addDot(theMap) }, 200);
		theMap.setView([value[2], value[3]], 18);
	}
}

$(function() {
	var initialLocation = new L.LatLng(window.locs[0][0], window.locs[0][1]);

	var map = L.mapbox.map('map', 'randyme.gajlngfe')
		.setView([window.locs[0][0], window.locs[0][1]], 18);
	var polyline = L.polyline([], {color: '#000'}).addTo(map);
	$.each(linePoints, function(index,value) {
		polyline.addLatLng(new L.LatLng(value[0], value[1]));
	});

	if(/slowmo/.test(location.search)) {
		addDot(map);
	} else {
		$.each(window.locs, function( index, value ) {
			dropDot(map, value[2], value[3], 'red');
			dropDot(map, value[0], value[1], 'blue');
		})
	}


	var dropper = L.marker(initialLocation, {
	               icon: L.mapbox.marker.icon({'marker-color': 'CC0033'}),
	               draggable: true
	           }); 
	var result = L.marker(initialLocation, { icon: L.divIcon({className: 'count-icon-corrector', html: "", iconSize: [10,10]})}).addTo(map); 

	var currentLeg = 0;
	dropper.on('dragend', function(x) {
	    var latLng = x.target.getLatLng();
	    var foo = new LatLon(latLng.lat, latLng.lng);
	    $.get('snap?current_leg='+currentLeg+'&route_id=' + $('#map').data().currentRoute + '&lat=' + latLng.lat + '&amp;lng=' + latLng.lng, function(resp) {
	      currentLeg = resp.split(",")[2];
              console.log(resp);
	      result.setLatLng(new L.LatLng(resp.split(",")[0], resp.split(",")[1]));
  	      console.log(foo.distanceTo(new LatLon(resp.split(",")[0], resp.split(",")[1])));
	    });
	});
	dropper.addTo(map);
});
