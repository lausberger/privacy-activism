package burp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BurpExtender implements IBurpExtender, IHttpListener {

    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private PrintWriter debug;
    private int cookieCounter;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) { // This is what allows the code to interface with BurpSuite
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.callbacks.setExtensionName("Lucas's Burp Extension");
        this.callbacks.registerHttpListener(this); // This waits for a packet to be received and forwards it to the code
        this.cookieCounter = 0;

        this.debug = new PrintWriter(callbacks.getStdout(), true);
    }

    /*
    Goals for the future and suggestions for what to work on:
    1. Successfully transplant a reassembled cookie onto a header and send it out
    2. Rather than searching for cookies, simply apply logic to any key=value detected in the packet
    3. Modify logic to be more rigorous, ex. for GPS=1, scrambing '1' would do nothing
    4. See if it's possible to keep cookie fields 'usable' so that the server doesn't just chuck them
    5. Figure out what else we can do when it comes to stateful tracking
    */

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        if (messageIsRequest) { // Is the packet a HTTP request coming from the client?
            IRequestInfo request = this.helpers.analyzeRequest(messageInfo.getHttpService(), messageInfo.getRequest());
            for (String header : request.getHeaders()) { // Here, each header is defined as a field in the packet, ex. 'User-Agent: ...'
                String headerField = header.split(":")[0]; // Separate what's before and after the semicolon to get a [Header, Data] pair
                if (headerField.equals("Cookie")) { // For now, we only look at ones specifically referred to as a cookie
                    
                    this.cookieCounter++;
                    //debug.println(header); // uncomment to view only the cookies

                    String[] cookieFields = header.split(" "); // get a list of each separate word in the header
                    
                    for (int i=1; i<cookieFields.length; i++) { // iterate through these words, skipping the first entry, "Cookie: "
                        /*
                        Would be nice to have a Regex that can tolerate fields such as:
                            "Trk0=Value=1483300&Creation=13%2f02%2f2022+02%3a55%3a06"
                        Ideally, only the value after the last "=" would be grabbed.
                        Then, we can skip checking for the word "Cookie" and instead focus on wherever a key=value pair is assigned
                        */
                        String cookieField = cookieFields[i];
                        String[] keyValueSets = cookieField.split("="); // Get a list of each key or value within the cookie field
                        String reassembledCookieField = "";

                        for (int j=0; j<keyValueSets.length-1; j++) { // re-add the key(s), but not the value, to our replacement cookie
                            reassembledCookieField += keyValueSets[j] + "=";
                        }
                        
                        String valueToScramble = keyValueSets[keyValueSets.length-1].split(";")[0]; // cookie fields are separated by semicolons
                        
                        // Here's a bunch of Java bullshit. All it does is rearrange the set of characters in the value section
                        ArrayList<Character> scrambleChars = new ArrayList<Character>(valueToScramble.length());
                        for (char c : valueToScramble.toCharArray()) {
                            scrambleChars.add(c);
                        }

                        Collections.shuffle(scrambleChars);
                        char[] shuffled = new char[scrambleChars.size()];

                        for (int k=0; k<shuffled.length; k++) {
                            shuffled[k] = scrambleChars.get(k);
                        }

                        String scrambledValue = new String(shuffled);
                        if (i < cookieFields.length-1) { // the last key/value doesn't have a semicolon
                            scrambledValue += ";";
                        }
                        reassembledCookieField += scrambledValue; // add value back onto its key(s)
                        ///*
                        debug.println("Original cookie field: " + cookieField);
                        debug.println("Scrambled cookie field: " + reassembledCookieField);
                        debug.println();
                        //*/
                    }
                }
                //debug.println(header); // uncomment to see all of the HTTP response packet
            }

        } else { // to work with HTTP responses sent by the website/server and received by our computer
            IResponseInfo response = this.helpers.analyzeResponse(messageInfo.getResponse());
            for (String sr : response.getHeaders()) {
                String field = sr.split(":")[0];
                if (field.equals("Set-Cookie")) { // HTTP responses are what create cookies in the first place. 
                    this.cookieCounter++;
                    debug.println(sr);
                }
            }
            // Get all cookies as an object if we want to
            /*
            for (ICookie c : response.getCookies()) {
                debug.println(c.getValue());
            }
            */
        }
    }
}