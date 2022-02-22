package burp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
            List<String> request_headers = request.getHeaders();

            for (String header : request_headers) { // Here, each header is defined as a field in the packet, ex. 'User-Agent: ...'
                String header_name = header.split(":")[0]; // The first word before the semicolon will be the header's name

                if (header_name.equals("Cookie")) { // For now, we only look at ones specifically referred to as a cookie
                    this.cookieCounter++;
                    //debug.println(header); // uncomment to view only the cookies

                    String[] cookie_contents = header.split(" "); // get a list of each space-separated word in the cookie header
                    debug.println("Original cookie: " + Arrays.toString(cookie_contents)); 

                    for (int i=1; i<cookie_contents.length; i++) { // iterate through these words, skipping the first entry, "Cookie:"
                        
                        // Would be nice to have a Regex that can tolerate fields such as:
                        //     "Trk0=Value=1483300&Creation=13%2f02%2f2022+02%3a55%3a06"
                        // Ideally, only the value after the last "=" would be grabbed.
                        // Then, we can skip checking for the word "Cookie" and instead focus on wherever a key=value pair is assigned

                        String cookie_entry = cookie_contents[i];
                        String[] keyValueSet = cookie_entry.split("="); // Get a list of the key(s) and value within the cookie entry
                        String scrambledCookieEntry = "";
                        
                        for (int j=0; j<keyValueSet.length-1; j++) { // re-add everything leading up to the value to our replacement cookie
                            scrambledCookieEntry += keyValueSet[j] + "=";
                        }
                        
                        String cookie_entry_value = keyValueSet[keyValueSet.length-1].split(";")[0]; // cookie entries are separated by semicolons
                        
                        // Here's a bunch of Java bullshit. All it does is rearrange the set of characters in the value section
                        ArrayList<Character> scrambledChars = new ArrayList<Character>(cookie_entry_value.length());
                        for (char c : cookie_entry_value.toCharArray()) {
                            scrambledChars.add(c);
                        }

                        Collections.shuffle(scrambledChars);
                        char[] shuffled = new char[scrambledChars.size()];

                        for (int k=0; k<shuffled.length; k++) {
                            shuffled[k] = scrambledChars.get(k);
                        }

                        String scrambledValue = new String(shuffled);
                        if (i < cookie_contents.length-1) { // the last entry doesn't end with a semicolon
                            scrambledValue += ";";
                        }

                        scrambledCookieEntry += scrambledValue; // add value back onto its key(s)

                        //debug.println("Original cookie field: " + cookie_entry);
                        //debug.println("Scrambled cookie field: " + scrambledCookieEntry);
                        //debug.println();

                        String newHeader = header.concat("\n" + scrambledCookieEntry);
                        //System.out.println(scrambledCookieEntry);
                        //debug.println(newHeader);
                        cookie_contents[i] = scrambledCookieEntry; // swap the original entry with the one we've manipulated
                    }
                    //debug.println("Modified cookie: " + Arrays.toString(cookie_contents));
                    String reassembled_cookie = "";
                    
                    for (String entry : cookie_contents) { // turn our list of cookie components back into a single string
                        reassembled_cookie += entry + " ";
                    }

                    reassembled_cookie = reassembled_cookie.substring(0, reassembled_cookie.length()-1); // remove extra space at end
                    debug.println("Modified cookie: " + reassembled_cookie);

                }
                //debug.println(header); // uncomment to see all of the HTTP response packet     
            }

        } else { // to work with HTTP responses sent by the website/server and received by our computer
            IResponseInfo response = this.helpers.analyzeResponse(messageInfo.getResponse());
            for (String sr : response.getHeaders()) {
                String field = sr.split(":")[0];
                if (field.equals("Set-Cookie")) { // HTTP responses are what create cookies in the first place. 
                    this.cookieCounter++;
                    //debug.println(sr);
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