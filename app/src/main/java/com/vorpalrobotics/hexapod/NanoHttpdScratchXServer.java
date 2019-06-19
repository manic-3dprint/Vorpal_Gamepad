package com.vorpalrobotics.hexapod;

//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;

import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * implementation of the ScratchXServer interface using nanohttpd
 * see: http://nanohttpd.org
 */
public class NanoHttpdScratchXServer extends NanoHTTPD implements ScratchXServer {
    private static final String LOG_TAG = "DEBUG_SERVER"; // key for debug messages in Logcat
    private static final String SCRATCHX_RECEIVE_TAG = "SCRATCHX_RECEIVE"; // scratchX page received serial data from Gamepad
    private static final int HTTP_SERVER_PORT = 8080; // port for communication with ScratchX
    private byte[] serialInput = ScratchXServer.EMPTY_SERIAL_INPUT; // serial input buffer
//  private byte[] serialOutput; // serial output buffer

    /**
     * Constructor
     */
    public NanoHttpdScratchXServer() {
        super(HTTP_SERVER_PORT);
    }

    /**
     * start the Server
     * @return true if the Server started successfully, false otherwise
     */
    public boolean startMe()
    {
        boolean isSuccess = false;
        try
        {
            super.start();
            isSuccess = true;
        } catch (IOException ex)
        {
            Log.wtf(LOG_TAG, "serve exception " + ex.getMessage());
        }
        return isSuccess;
    }

    // http://www.java2s.com/Code/Java/Data-Type/hexStringToByteArray.htm
    private byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    /**
     * process an http call from the caller (ScratchX page)
     * @param session IHTTPSession
     * @return the response to send to the caller (ScratchX page)
     */
    @Override
    public Response serve(IHTTPSession session)
    {
        Method method = session.getMethod();
        String uri = session.getUri();
        Log.wtf(LOG_TAG, "serve " + method + " '" + uri + "' ");
        if (method == Method.GET)
        {
            Map<String, List<String>> parms = session.getParameters();
            String hexData = null;
            hexData = parms.get("hexData").get(0);
            serialInput = hexStringToByteArray(hexData);
            Log.wtf(LOG_TAG, "serve result " + new String(serialInput) + ", " + serialInput.length + " ");
        }
        Response response = newFixedLengthResponse("{}");
        addCORSHeaders(null, response, "*");
        return response;
    }

    /**
     * get the Serial input from the caller (ScratchX page)
     * @return the Serial input
     */
    @Override
    public byte[] getSerialInput()
    {
        byte[] saveSerialInput = serialInput;
        serialInput = EMPTY_SERIAL_INPUT;
        return saveSerialInput;
    }

    /**
     * send the Serial output to the caller (ScratchX page)
     * (not implemented)
     * @param serialOutput_ the Serial output
     */
    @Override
    public void setSerialOutput(byte[] serialOutput_)
    {
        // not implemented
        // serialOutput = serialOutput_;
        String serialOutputString = new String(serialOutput_);
        Log.wtf(SCRATCHX_RECEIVE_TAG, serialOutputString);
    }

/*
    private boolean checkNetworkStatus() {
        boolean networkStatus = false;
        ConnectivityManager check = callingActivity.getConnectivityManager();
        NetworkInfo[] info = check.getAllNetworkInfo();
        for (int i = 0; i < info.length; i++) {
            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                networkStatus = true;
            }
        }
        return networkStatus;
    }
*/

///////////////////////////// NanoHTTPD SimpleWebServer /////////////////////////////
    protected Response addCORSHeaders(Map<String, String> queryHeaders, Response resp, String cors) {
        resp.addHeader("Access-Control-Allow-Origin", cors);
        resp.addHeader("Access-Control-Allow-Headers", calculateAllowHeaders(queryHeaders));
        resp.addHeader("Access-Control-Allow-Credentials", "true");
        resp.addHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
        resp.addHeader("Access-Control-Max-Age", "" + MAX_AGE);

        return resp;
    }

    private String calculateAllowHeaders(Map<String, String> queryHeaders) {
        // here we should use the given asked headers
        // but NanoHttpd uses a Map whereas it is possible for requester to send
        // several time the same header
        // let's just use default values for this version
        return System.getProperty(ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME, DEFAULT_ALLOWED_HEADERS);
    }

    private final static String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";

    private final static int MAX_AGE = 42 * 60 * 60;

    // explicitly relax visibility to package for tests purposes
    public final static String DEFAULT_ALLOWED_HEADERS = "origin,accept,content-type";

    public final static String ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME = "AccessControlAllowHeader";
///////////////////////////// end NanoHTTPD SimpleWebServer /////////////////////////////
}
