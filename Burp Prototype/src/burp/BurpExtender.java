package burp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class BurpExtender implements IBurpExtender, IHttpListener {

    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private PrintWriter debug;
    private int cookieCounter;
//Test comment
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) { // This is what allows the code to interface with BurpSuite
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.callbacks.setExtensionName("Lucas's Burp Extension");
        this.callbacks.registerHttpListener(this); // This waits for a packet to be received and forwards it to the code
        this.cookieCounter = 0;

        this.debug = new PrintWriter(callbacks.getStdout(), true); // lets us print either to the Burp app, a file, or the terminal
    }

    /*
    Goals for the future and suggestions for what to work on:
    1. [DONE] Successfully transplant a reassembled cookie onto a header and send it out
    2. Rather than searching for cookies, simply apply logic to any key=value detected in the packet
        * This would require some serious checking to avoid breaking websites! Ex. in-packet HTML code or lang=en_us
    3. Modify logic to be more rigorous, ex. for GPS=1, scrambing '1' would do nothing
    4. See if it's possible to keep cookie values 'usable' so that the server doesn't just chuck them
    5. Figure out what else we can do when it comes to stateful tracking
    */

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse message) {
        if (messageIsRequest) { // Is the packet a HTTP request coming from the client?
            IRequestInfo request = this.helpers.analyzeRequest(message);
            List<String> request_headers = request.getHeaders();
            Boolean packet_was_modified = false;


            for (int h=0; h<request_headers.size(); h++) { // Each "header" is a section of the packet, ex. 'User-Agent: ...'
                String header = request_headers.get(h); 
                String header_name = header.split(":")[0]; // The first word before the semicolon will be the header's name

                if (header_name.equals("Cookie")) { // For now, we only look at ones specifically referred to as a cookie
                    this.cookieCounter++;
                    String[] cookie_contents = header.split(" "); // get a list of each space-separated word in the cookie header

                    for (int i=1; i<cookie_contents.length; i++) { // iterate through these words, skipping the first entry, "Cookie:"
                        
                        /*
                        Would be nice to have a Regex that can tolerate fields such as:
                            "Trk0=Value=1483300&Creation=13%2f02%2f2022+02%3a55%3a06"
                        Ideally, only the value after the last "=" would be grabbed.
                        Then, we can skip checking for the word "Cookie" and instead focus on wherever a key=value pair is assigned
                        */

                        String cookie_entry = cookie_contents[i];
                        String[] keyValueSet = cookie_entry.split("="); // Get a list of the key(s) and value within the cookie entry
                        String scrambledCookieEntry = "";
                        
                        for (int j=0; j<keyValueSet.length-1; j++) { // re-add everything leading up to the value to our replacement cookie
                            scrambledCookieEntry += keyValueSet[j] + "=";
                        }
                        
                        String cookie_entry_value = keyValueSet[keyValueSet.length-1].split(";")[0]; // cookie entries are separated by semicolons, this removes it
                        
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
                        cookie_contents[i] = scrambledCookieEntry; // swap the original entry with the one we've manipulated
                    }

                    String reassembled_cookie = "";
                    for (String entry : cookie_contents) { // turn our list of cookie components back into a single string
                        reassembled_cookie += entry + " ";
                    }

                    reassembled_cookie = reassembled_cookie.substring(0, reassembled_cookie.length()-1); // remove extra space at end

                    if (!packet_was_modified) {
                        packet_was_modified = true;
                    }

                    request_headers.set(h, reassembled_cookie); // replace the current header with the one we've edited
                }
            }

            if (packet_was_modified) { // Once we've changed a request packet, we can create a new one in its place                
                int bodyOffset = request.getBodyOffset();
                String body = new String(message.getRequest()).substring(bodyOffset);

                byte[] modified_request_bytes = this.helpers.buildHttpMessage(request_headers, body.getBytes());
                message.setRequest(modified_request_bytes); // required if using http listener
                IRequestInfo modified_request = this.helpers.analyzeRequest(message);
                
                // debug printout to make sure things are working
                debug.println("STARTING PACKET ANALYSIS");
                debug.println("\tORIGINAL PACKET:");
                for (String s : request.getHeaders()) {
                    debug.println(s);
                }
                debug.println("\n");
                debug.println("\tMODIFIED PACKET:");
                for (String s : modified_request.getHeaders()) {
                    debug.println(s);
                }
                debug.println("END OF PACKET ANALYSIS\n");
            }
        } 
        /*
        else { // allows us to access HTTP responses sent by the website/server and received by our computer
            IResponseInfo response = this.helpers.analyzeResponse(message.getResponse());
            for (String sr : response.getHeaders()) {
                String field = sr.split(":")[0];
                if (field.equals("Set-Cookie")) { // HTTP responses are what create cookies in the first place. 
                    this.cookieCounter++;
                    //debug.println(sr);
                }
            }
            // Get all cookies as an object if we want to
            for (ICookie c : response.getCookies()) {
                debug.println(c.getValue());
            }
        }
        */
    }
}