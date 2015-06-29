package com.sb.downloadprogress;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;

/**
 * Handles all interaction with the servers
 */
public class WebService {
    // returns a default instance of WebService to implement singleton
    // in other words only one instance of WebService object exists in the application
    private static WebService webService;

    public static WebService getDefaultInstance() {
        if (webService == null) {
            webService = new WebService();
        }
        return webService;
    }

    /**
     *
     * @return server's urlString
     */
    private static String returnServerURLString() {
        return "http://sidechats.appspot.com/codingtest/files/";
    }

    /**
     * make a web service call to get attachments' data
     * if data is received from server then return that data
     * else errorMessage
     * @return JSONArray if attachmentsData is returned from the server
     */
    public static Object returnAttachmentsData() {
        Object dataToBeReturned = null;
        HttpURLConnection httpURLConnection = null;
        try {
            // create URL
            URL url = new URL(returnServerURLString());
            // open connection
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            // 15 seconds
            httpURLConnection.setConnectTimeout(15000);

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                BufferedReader input = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()), 8192);
                String strLine = null;
                while ((strLine = input.readLine()) != null) {
                    response.append(strLine);
                }
                input.close();
                Object dataReturnedFromServer = new JSONTokener(response.toString()).nextValue();

                if (dataReturnedFromServer instanceof JSONArray) {
                    dataToBeReturned = dataReturnedFromServer;
                } else {// attachment's data couldn't be downloaded // append generic errorMessage
                    dataToBeReturned = new LinkedHashMap<String, String>();
                    HelperService.getDefaultInstance().appendGenericErrorMessage((LinkedHashMap<String, String>) dataToBeReturned);
                }
            }
        } catch (Exception e) {
            // append generic errorMessage
            dataToBeReturned = new LinkedHashMap<String, String>();
            HelperService.getDefaultInstance().appendGenericErrorMessage((LinkedHashMap<String, String>) dataToBeReturned);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return dataToBeReturned;
    }
}