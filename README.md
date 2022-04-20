# Privacy Activism!

#### Extension file location: /Burp Prototype/src/burp/BurpExtender.java

## Useful Resources:
* Burp Suite Community Edition: https://portswigger.net/burp/releases/professional-community-2022-1-1?requestededition=community
* Writing your first Burp Suite extension: https://portswigger.net/burp/extender/writing-your-first-burp-suite-extension
* Burp extension coding tutorial: https://www.youtube.com/watch?v=IdM4Sc7WVGU
* burp package documentation: https://portswigger.net/burp/extender/api/index.html
* Someone made an extension that spoofs geolocation with webpage code-injection: https://github.com/chatziko/location-guard
* Chromium development tips and tricks: https://www.chromium.org/chromium-os/tips-and-tricks-for-chromium-os-developers/
* Article from trackers that show their fingerprinting tactics: https://dev.to/savannahjs/how-the-web-audio-api-is-used-for-browser-fingerprinting-4oim
* A paper on literally the same thing we're doing: https://dl.acm.org/doi/abs/10.1145/2736277.2741090
* Another relevant paper: https://dl.acm.org/doi/pdf/10.1145/3386040
* Another one: https://hal.inria.fr/hal-01527580/document
* How Brave browser "farbles" fingerprint data: https://github.com/brave/brave-core/blob/680b0d872e0a295ef94602fb5dc1907358d6a3ba/chromium_src/third_party/blink/renderer/core/execution_context/execution_context.cc#L133
* Alexa Popular Sites by Topic: https://www.alexa.com/popular-articles

## TO DO:

#### Stateful
* ~~Figure out how to prevent a packet from being sent while code logic executes~~
* ~~Successfully transplant manipulated cookie values onto packet, then release~~
* ~~Find a way to manipulate cookies while breaking sites as little as possible~~
* ~~Successfully spoof User-Agent header~~
* ~~Design a principled approach to User-Agent spoofing that avoids breaking websites~~
* ~~Look into other ways of changing stateful information~~
* Short test to see if cookie manipulation itself breaks sites or if some break themselves if they notice fraudulent cookies

#### Stateless
* ~~Make a fork of Chromium or Brave browser~~
* ~~Do a stack trace w/ a custom HTML page that calls location function to find where Location API values are being returned from~~
* ~~Successfully spoof location API return value~~
* ~~Fine-tune location API implementation~~
* ~~Look into other APIs that could be manipulated~~
* Make browser compatible with measurement tool

#### Experiments/Measurement
* Create a tool, proxy extension, or browser extension that saves all ads on a page into a folder
* Come up with categories for the types of ads encountered by this tool
* Design another tool which can automate browsing activity

## Methodology:

#### Effectiveness of Implementation
* Two components: Stateful (cookie/packet header based) and Stateless (Hook into Browser APIs)
* Allows testing with one (10), the other (01), or neither (00)
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


## Credit Roll:

#### Lucas
* Conducted preliminary research on Burp Suite as a Man-in-the-Middle proxy application
* Conducted preliminary research on using Burp extensions to automate proxy functionality
* Created repository
* Compiled resoures for coding a Burp extension, added to README
* Added To-Do list section to README
* Added methodology section to README
* Wrote initial implementation of BurpExtender, which intercepts request cookie field=value pairs and scrambles the values before sending
* Wrote research questions and hypotheses
* Wrote experimental design
* Compiled useful resources for experimentation
* Researched browser API hooking and added pertinent resources to README
* Created fork of Chromium and obtained all relevant IDs and API keys
* Wrote preliminary revised BurpExender implementation, which individually randomizes each individual component of User-Agent header
* Spoofed Geolocation API return values in Chromium
* Created slide presentation for final implementation meeting

#### Elias
* Compiled preliminary list of websites for experiment
* Researched Regex pattern matching for User-Agent headers
* Obtained large database of User-Agents for use in proxy extension
* Implemented final revised BurpExtender: randomize entire User-Agent from large database of valid strings 
* Researched adblockparser Python library
* Created Python script to download advertisement images from URL and place images in their own folder 
* Assisted with experimental design
* Tested Ad tool to check for bugs with retrieving Ad URL's
* Wrote the initial draft of the CS half of the research paper
* Organized list of travel website url's to be used for experimentation 
