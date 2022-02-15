# Privacy Activism!

#### Extension file location: /Burp Prototype/src/burp/BurpExtender.java

## Useful Resources:

Burp Suite Community Edition: https://portswigger.net/burp/releases/professional-community-2022-1-1?requestededition=community

Writing your first Burp Suite extension: https://portswigger.net/burp/extender/writing-your-first-burp-suite-extension

Burp extension coding tutorial: https://www.youtube.com/watch?v=IdM4Sc7WVGU

burp package documentation: https://portswigger.net/burp/extender/api/index.html

## TO DO:
* Figure out how to prevent a packet from being sent while code logic executes
* Successfully transplant manipulated cookie values onto packet, then release
* Run experiments to gauge impact that different ways of manipulating cookie values have on user experience
* Take on stateless tracking! Chromium source code can be forked, allowing access to browser location/canvas/battery/etc APIs. This could be used to directly alter the data given to tracking scripts!
* Run experiments using a custom Chromium-based browser
