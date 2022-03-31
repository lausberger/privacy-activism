function download(dataurl, filename) {
    const link = document.createElement("a");
    link.href = dataurl;
    link.download = filename;
    link.click();
}

function saveURL(url) {
    const dummy = document.createElement('a');
    dummy.href = url;
    parentnode.appendChild(dummy);
}

function collectImages() {
    var images = document.getElementsByTagName('img');
    var parentnode = document.createElement("a");
    var urlstring = "";

    for(let i=0; i<images.length; i++) {
        let img = images[i];
        if (img.src != "") {
            saveURL(img.src);
        }
    }

    let urls = parentnode.children;

    for (let i=0; i<urls.length; i++) {
        urlstring += urls[i].href + "\n";
    }

    download("data:text/html," + urlstring, "urls.txt");
}

chrome.action.onClicked.addListener((tab) => {
    collectImages();
});

/*
  "content_scripts": [
    {
      "matches": ["<all_urls>"],
      "js": ["adCollector.js"]
    }
  ],
   "web_accessible_resources": [{
    "resources": ["adCollector.js"],
    "matches": ["<all_urls>"]
  }],
*/