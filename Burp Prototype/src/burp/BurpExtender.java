package burp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.Random;



public class BurpExtender implements IBurpExtender, IHttpListener {

    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private PrintWriter debug;
    private List<String> commentsList;
    private List<String> productsList;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) { // This is what allows the code to interface with BurpSuite
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.callbacks.setExtensionName("Lucas's Burp Extension");
        this.callbacks.registerHttpListener(this); // This waits for a packet to be received and forwards it to the code

        this.debug = new PrintWriter(callbacks.getStdout(), true); // lets us print either to the Burp app, a file, or the terminal
        this.commentsList = new ArrayList<String>();
        this.productsList = new ArrayList<String>();

        try {
            File commentsFile = new File("resources/commentSamples.txt");
            File productsFile = new File("resources/productSamples.txt");
            Scanner commentScanner = new Scanner(commentsFile);
            Scanner productScanner = new Scanner(productsFile);
            while (commentScanner.hasNextLine()) {
                String s = commentScanner.nextLine();
                this.commentsList.add(s);
            }
            while (productScanner.hasNextLine()) {
                String s = productScanner.nextLine();
                this.productsList.add(s);
            }
            commentScanner.close();
            productScanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("ERROR READING FILE");
            System.err.println(e);
            //System.exit(1);
        }
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse message) {
        if (messageIsRequest) { // Is the packet a HTTP request coming from the client?
            IRequestInfo request = this.helpers.analyzeRequest(message);
            List<String> request_headers = request.getHeaders();
            Boolean packet_was_modified = false;
            
            for (int h=1; h<request_headers.size(); h++) { // Each "header" is a section of the packet, ex. 'User-Agent: ...'
                String header = request_headers.get(h); 
                String[] header_components = header.split(":");
                String header_name = header_components[0]; // The first word before the semicolon will be the header's name
                String header_body = header_components[1]; // The body follows the colon


                if (header_name.equals("User-Agent")) {
                    //System.out.println(header_body);
                    //packet_was_modified = true;
                    // <product> / <product-version>: [A-Za-z]*\/[\d+\.\d+]*
                    // <comment>: \(([^(]*)\) OR \(([A-Za-z ,\/\d.]*(; )?)*\)

                    List<String> product_matches = new ArrayList<String>();
                    List<String> comment_matches = new ArrayList<String>();

                    Matcher product_matcher = Pattern.compile("[A-Za-z]*\\/[\\d+.\\d+]*").matcher(header_body);
                    Matcher comment_matcher = Pattern.compile("\\(([^(]*)\\)").matcher(header_body);

                    while (product_matcher.find()) {
                        product_matches.add(product_matcher.group());
                    }

                    while (comment_matcher.find()) {
                        comment_matches.add(comment_matcher.group());
                    }

                    Random r = new Random();
                    for (int i=0; i<comment_matches.size(); i++) {
                        String no_parenthesis = comment_matches.get(i).substring(1, comment_matches.get(i).length()-1);
                        String[] comment = no_parenthesis.split("; ");
                        String new_comment = "";

                        for (int j=0; j<comment.length; j++) {
                            String nextStr = this.commentsList.get(r.nextInt(this.commentsList.size()));
                            if (j == comment.length-1) {
                                new_comment = new_comment + nextStr;
                            } else {
                                new_comment = new_comment + nextStr + "; ";
                            }
                        }
                        
                        new_comment = "(" + new_comment + ")";
                        comment_matches.set(i, new_comment);
                    }

                    for (int i=1; i<product_matches.size(); i++) {
                        String new_product = this.productsList.get(r.nextInt(this.productsList.size()));
                        product_matches.set(i, new_product);
                    }

                    List<String> body_list = new ArrayList<String>();

                    int k = 0;
                    String reconstructed_body = "";
                    while (k < comment_matches.size() && k < product_matches.size()) {

                        body_list.add(product_matches.get(k));
                        body_list.add(comment_matches.get(k));
                        k++;
                    }

                    if (k != comment_matches.size()) {
                        System.err.println("ERROR: MORE COMMENTS THAN PRODUCTS");
                        System.exit(1);
                    }

                    while (k < product_matches.size()) {
                        body_list.add(product_matches.get(k));
                        k++;
                    }

                    for (int l=0; l<body_list.size(); l++) {
                        String s = body_list.get(l);
                        if (l == body_list.size()-1) {
                            reconstructed_body = reconstructed_body + s;
                        } else {
                            reconstructed_body = reconstructed_body + s + " ";
                        }
                    }

                    System.out.println(header_body);
                    System.out.println(reconstructed_body);

                    String user_agent = "User-Agent: " + reconstructed_body;
                    request_headers.set(h, user_agent);

                    if (!packet_was_modified) {
                        packet_was_modified = true;
                    }
                }

            /*
                if (header_name.equals("Cookie")) { // For now, we only look at ones specifically referred to as a cookie
                    this.cookieCounter++;
                    String[] cookie_contents = header.split(" "); // get a list of each space-separated word in the cookie header

                    for (int i=1; i<cookie_contents.length; i++) { // iterate through these words, skipping the first entry, "Cookie:"
                        
                        
                        //Would be nice to have a Regex that can tolerate fields such as:
                        //    "Trk0=Value=1483300&Creation=13%2f02%2f2022+02%3a55%3a06"
                        //Ideally, only the value after the last "=" would be grabbed.
                        //Then, we can skip checking for the word "Cookie" and instead focus on wherever a key=value pair is assigned

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
                */
            }
            

            if (packet_was_modified) { // Once we've changed a request packet, we can create a new one in its place                
                int bodyOffset = request.getBodyOffset();
                String body = new String(message.getRequest()).substring(bodyOffset);

                byte[] modified_request_bytes = this.helpers.buildHttpMessage(request_headers, body.getBytes());
                message.setRequest(modified_request_bytes); // required if using http listener
                IRequestInfo modified_request = this.helpers.analyzeRequest(message);

                // debug printout to make sure things are working
                /*
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
                */
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
