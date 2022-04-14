package burp;

import java.io.PrintWriter;
import java.net.URL;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class BurpExtender implements IBurpExtender, IHttpListener {

    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private PrintWriter debug;
    private int IMG_SIZE;
    private String FILENAME;
    private File file;
    private int FNUM;
    private String FILETYPE;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) { // This is what allows the code to interface with BurpSuite
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.callbacks.setExtensionName("Ad Measurement Tool");
        this.callbacks.registerHttpListener(this); // This waits for a packet to be received and forwards it to the code

        this.debug = new PrintWriter(callbacks.getStdout(), true); // lets us print either to the Burp app, a file, or the terminal
        this.IMG_SIZE = 15;
        this.FILENAME = "./measurements/urls";
        this.FNUM = 0;
        this.FILETYPE = ".txt";
        String[] tmp = new File("./measurements/").list();
        for (String s : tmp) {
            if (s.contains(".txt")) {
                this.FNUM++;
            }
        }
        this.file = new File(FILENAME + FNUM + FILETYPE);
        debug.println(FNUM);

        try {
            this.file.createNewFile();
        } catch (IOException e) {
            debug.println(e);
        }
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse message) {
        if (messageIsRequest) {
            //IResponseInfo resp = this.helpers.analyzeResponse(message.getResponse());
            IRequestInfo req = this.helpers.analyzeRequest(message.getHttpService(), message.getRequest());
            URL url = req.getUrl();
            
            try {
                BufferedImage img = ImageIO.read(url);
                if (img == null) {
                    throw new IOException();
                }
                if (img.getWidth() > this.IMG_SIZE) {
                    debug.println(url + "\n");

                    if (this.file.exists()) {
                        BufferedWriter fileAppend = new BufferedWriter(new FileWriter(this.file, true));
                        fileAppend.append(url.toString());
                        fileAppend.newLine();
                        fileAppend.close();
                    } else {
                        throw new IOException("Unable to append to file: " + this.FILENAME + this.FILETYPE);
                    }
                }
            } catch (IOException e) {
                // debug.println("ERROR: Failed to read image url");
            } catch (IllegalArgumentException i) {
                debug.println(i);
            }
        }
    }
}