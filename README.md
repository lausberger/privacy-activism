# Privacy Activism!

#### Extension file location: /Burp Prototype/src/burp/BurpExtender.java

## Useful Resources:
* Burp Suite Community Edition: https://portswigger.net/burp/releases/professional-community-2022-1-1?requestededition=community
* Writing your first Burp Suite extension: https://portswigger.net/burp/extender/writing-your-first-burp-suite-extension
* Burp extension coding tutorial: https://www.youtube.com/watch?v=IdM4Sc7WVGU
* burp package documentation: https://portswigger.net/burp/extender/api/index.html
* Basically an abridged version of what we're doing: https://stackoverflow.com/questions/48523890/burp-extension-how-to-intercept-all-traffic
* Another way of intercepting a packet (check bottom of page): https://github.com/bit4woo/burp-api-drops/blob/master/src/burp/Lession6.java

## TO DO:
* Figure out how to prevent a packet from being sent while code logic executes
* Successfully transplant manipulated cookie values onto packet, then release
* Run experiments to gauge impact of different ways of manipulating cookie values on user experience
* Take on stateless tracking! Chromium source code can be forked, allowing access to browser location/canvas/battery/etc APIs. This could be used to directly alter the data given to tracking scripts!
* Run experiments using a custom Chromium-based browser

## Methodology:

#### Effectiveness of Implementation
* Two components: Stateful (cookie/packet header based) and Stateless (Hook into Browser APIs)
* Allows testing with one (10), the other (01), both (11), or neither (00)
* Compare the deltas (differences) between different combinations and the "00" control

#### Building Profiles
* Chromium-based browsers can create "profiles": Essentially, a clean slate user that we can take in whatever direction we want
* No history, cookies, or old data associated with them
* Each profile can do the same activity (e.g., searching for and browsing sneakers on various shopping websites) but with different components enabled
* Assumes cross-contamination between profiles is not a concern--this must be verified by us
* We can measure the proportion of activity-related ads received by each profile

#### Amassing Data For Stateless Tracking
* We would be altering the return values of various API functions that are used for fingerprinting
* An easy way to do this would be to randomly pick from a set of possible values for each fingerprint (location, hardware, battery status, etc)
* Build a bank of possible values using each of our fingerprint data, plus whatever we can add to it
* Theoretically would allow user to seem like a different person every time, preventing cross-site tracking and consumer profile building
