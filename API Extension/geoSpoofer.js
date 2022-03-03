// some code I stoll from Stack
console.log("Extension script has been executed.");

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

/*
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
navigator.geolocation.getCurrentPosition = function(success, error, options) {
    console.log("GETCURRENTPOSITION HOOK SUCCESSFUL");

    success({ 
        coords: { // Brazil
            latitude: 14.2350,
            longitude: 51.9253
        }
    });
}

navigator.geolocation.watchPosition = function(watchSuccess, watchError) {
    console.log("WATCH LOCATION BEING CALLED");
}
