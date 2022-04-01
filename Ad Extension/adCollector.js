// Given a URL, append a child containing said URL to the given parent object
function saveURL(url, par) {
  const dummy = document.createElement('a');
  dummy.href = url;
  par.appendChild(dummy);
}

// Add the given text to an object, then download its contents
function download(text, filename) {
    const link = document.createElement("a");
    link.href = text;
    link.download = filename;
    link.click();
}

// Take all image elements on a page, save all of their URLs, then download them in one file
function collectImages() {
    var parentnode = document.createElement("a");
    var images = document.getElementsByTagName('img');
    var urlstring = "";

    for(let i=0; i<images.length; i++) {
        let img = images[i];
        if (img.src != "") {
            saveURL(img.src, parentnode);
        }
    }

    let urls = parentnode.children;

    for (let i=0; i<urls.length; i++) {
        urlstring += urls[i].href + "\n";
    }

    download("data:text/html," + urlstring, "urls.txt");
}

collectImages();