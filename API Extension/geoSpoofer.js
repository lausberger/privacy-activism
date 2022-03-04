var s = document.createElement('script');
s.src = chrome.runtime.getURL('geoSpoofer.js');
/*
s.onload = function() {
    this.remove();
};
*/
(document.head || document.documentElement).appendChild(s);

console.log("Extension script executed successfully.");

var x = 100;

var testGeoOverride = navigator.geolocation.getCurrentPosition;

navigator.geolocation.getCurrentPosition = function(success, error, options) {
    console.log("CALL TO HOOKED GEOLOCATION");

    success({ 
        coords: { // Brazil
            latitude: 14.2350,
            longitude: 51.9253
        }
    });

    testGeoOverride(success);
}

/* Leave this alone for now, until getCurrentPosition works
navigator.geolocation.watchPosition = function(watchSuccess, watchError) {
    console.log("WATCH LOCATION BEING CALLED");
}
*/

// For debugging getCurrentPosition
function testThing() {
    navigator.geolocation.getCurrentPosition( function(position) {
        console.log("Latitude: ", position.coords.latitude);
        console.log("Longitude: ", position.coords.longitude);
        console.log(position);
    });
}

//testThing();

// Code I stole from stack
/*
// Success callback
function success(position) {
    console.log('Success');
    console.log(position.coords.lat);
    console.log(position.coords.lng);
}
// Error callback
function error(position) {
    console.log('Could not find you!');
}

navigator.geolocation.getCurrentPosition = function(success, error){
    console.log("GEOLOCATION GETCURRENTPOSITION CALLED");
    var customPosition = {};
    customPosition.coords = {};
    customPosition.coords.lat = 14.2350;
    customPosition.coords.lng = 51.9253;
    success(customPosition); // Brazil
}
*/

//var _geolocation = navigator.geolocation.getCurrentPosition;

navigator.geolocation.getCurrentPosition = function(success) {
    console.log("API HOOKED WORKED");
    console.trace();
}

