package com.sb.downloadprogress;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * Helper Methods
 */
public class HelperService {
    // global variables
    private final static String GENERIC_ERROR_MESSAGE = "Sorry, Slow Internet Connection on your device!!";

    // returns a default instance of HelperService to implement singleton
    // in other words only one instance of HelperService object exists in the application
    private static HelperService helperService;

    public static HelperService getDefaultInstance() {
        if (helperService == null) {
            helperService = new HelperService();
        }
        return helperService;
    }

    // returns GENERIC_ERROR_MESSAGE
    public String returnGenericErrorMessage() {
        return GENERIC_ERROR_MESSAGE;
    }

    /**
     * appends generic errorMessage
     *
     * @param appendGenericErrorMessageToIt hashMap to be which generic errorMessage will be appended
     */
    public void appendGenericErrorMessage(LinkedHashMap<String, String> appendGenericErrorMessageToIt) {
        appendGenericErrorMessageToIt.put("errorMessage", GENERIC_ERROR_MESSAGE);
    }

    /**
     * show an alertDialog with a title, a message and a cancel button with buttonTitle
     *
     * @param title of alertDialog
     * @param message of alertDialog
     * @param cancelButtonTitle of alertDialog
     * @param showInActivity Activity in which alertDialog will be displayed
     */
    public void showAlertDialog(String title, String message, String cancelButtonTitle, Activity showInActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(showInActivity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(cancelButtonTitle,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * @param string string in which HTML entities have to be replaced
     * @return string with all HTML entities replaced
     */
    public String replaceHTMLEntities(String string) {
        if (string.length() > 0) {
            // replace &amp;
            string = string.replaceAll("&amp;", "&");
            // replace &quot;
            string = string.replaceAll("&quot;", "\"");
            // replace &#x27;
            string = string.replaceAll("&#x27;", "'");
            // replace &#x39;
            string = string.replaceAll("&#x39;", "'");
            // replace &#x92;
            string = string.replaceAll("&#x92;", "'");
            // replace &#x96;
            string = string.replaceAll("&#x96;", "'");
            // replace &gt;
            string = string.replaceAll("&gt;", ">");
            // replace &lt;
            string = string.replaceAll("&lt;", "<");
            // replace &#8211;
            string = string.replaceAll("&#8211;", "-");
            // replace &#8271;
            string = string.replaceAll("&#8271;", ";");
            // replace &#8217;
            string = string.replaceAll("&#8217;", "'");
        }

        return string;
    }

    /**
     * sorts attachments using _sortBy
     * @param attachments attachments to be sorted
     * @param _sortBy to be sorted by
     * @return sorted attachments
     */
    public ArrayList<Attachment> sortAttachments(ArrayList<Attachment> attachments, String _sortBy) {
        final String sortBy = _sortBy;
        Collections.sort(attachments, new Comparator<Attachment>() {
            @Override
            public int compare(Attachment lhs, Attachment rhs) {
                if (sortBy.equals("name")) {// sort in ascending order
                    return lhs.getName().compareTo(rhs.getName());
                }
                else if (sortBy.equals("amountDownloaded")) {// sort in descending order
                    return rhs.getAmountDownloaded() - lhs.getAmountDownloaded();
                }
                else if (sortBy.equals("totalLength")) {// sort in descending order
                    return (int)(rhs.getTotalLength() - lhs.getTotalLength());
                }
                return 0;
            }
        });

        return attachments;
    }

    /**
     *
     * @param url an absolute url given to this method
     * @return suggested fileName
     */
    public String returnFileNameFromUrl(URL url) {
        // get the file of this url
        String urlString = url.getFile();
        return urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
    }
}