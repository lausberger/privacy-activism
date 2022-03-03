console.log("Extension script executed successfully.");

function componentDidMount() {
    if (!"geolocation" in navigator) {
        console.error("Geolocation access is unavailable");
    }
}

componentDidMount();

// Ignore me
function testThing() {
    navigator.geolocation.getCurrentPosition( function(position) {
        console.log("Latitude: ", position.coords.latitude);
        console.log("Longitude: ", position.coords.longitude);
        console.log(position);
    });
}

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

// I wrote my own 
/*
navigator.geolocation.getCurrentPosition = function(success, error, options) {
    console.log("CALL TO HOOKED GEOLOCATION");

    success({ 
        coords: { // Brazil
            latitude: 14.2350,
            longitude: 51.9253
        }
    });
}
*/

testThing();

/* Leave this alone for now, until getCurrentPosition works
navigator.geolocation.watchPosition = function(watchSuccess, watchError) {
    console.log("WATCH LOCATION BEING CALLED");
}
*/
