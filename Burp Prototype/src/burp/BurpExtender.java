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
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.callbacks.setExtensionName("Lucas's Burp Extension");
        this.callbacks.registerHttpListener(this);
        this.cookieCounter = 0;

        this.debug = new PrintWriter(callbacks.getStdout(), true);
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        if (messageIsRequest) {
            IRequestInfo request = this.helpers.analyzeRequest(messageInfo.getHttpService(), messageInfo.getRequest());
            for (String header : request.getHeaders()) {
                String headerField = header.split(":")[0];
                if (headerField.equals("Cookie")) {
                     /*
                    Options:
                    * Scramble every value in a field=value pair within the cookie to render it meaningless
                    * Pass an absurd amount of data through the Cookie header to waste resources
                        - This one is probably not feasible because it means we have to waste bandwidth and memory too
                    * Delete the entire Cookie header 
                    */
                    this.cookieCounter++;
                    debug.println(header);
                    String[] cookieFields = header.split(" ");
                    
                    for (int i=1; i<cookieFields.length; i++) { // skip the first entry, "Cookie: "
                        /*
                        Would be nice to have a Regex that can tolerate fields such as:
                            "Trk0=Value=1483300&Creation=13%2f02%2f2022+02%3a55%3a06"
                        Ideally, only the value after the last "=" would be grabbed.
                        */
                        String cookieField = cookieFields[i];
                        String[] fieldValueSets = cookieField.split("=");
                        String reassembledCookieField = "";

                        for (int j=0; j<fieldValueSets.length-1; j++) { // all the fields but not including the value
                            reassembledCookieField += fieldValueSets[j] + "=";
                        }
                        
                        String valueToScramble = fieldValueSets[fieldValueSets.length-1].split(";")[0];
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
                        if (i < cookieFields.length-1) { // the last field/value doesn't have a semicolon
                            scrambledValue += ";";
                        }
                        reassembledCookieField += scrambledValue;
                        /*
                        debug.println("Original cookie field: " + cookieField);
                        debug.println("Scrambled cookie field: " + reassembledCookieField);
                        debug.println();
                        */
                    }
                }
                //debug.println(header);
            }

        } else {
            IResponseInfo response = this.helpers.analyzeResponse(messageInfo.getResponse());
            for (String sr : response.getHeaders()) {
                String field = sr.split(":")[0];
                if (field.equals("Set-Cookie")) {
                    this.cookieCounter++;
                    debug.println(sr);
                }
            }
            // Get response cookies as an object
            /*
            for (ICookie c : response.getCookies()) {
                debug.println(c.getValue());
            }
            */
        }
    }
}