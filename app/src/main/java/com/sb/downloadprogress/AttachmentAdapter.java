package com.sb.downloadprogress;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Adapter for HomeActivity's attachmentsListView
 */
public class AttachmentAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflator;
    private HomeActivity homeActivity;
    private DBHelper dbHelper;
    // store attachments
    public ArrayList<Attachment> attachments = new ArrayList<Attachment>();
    // get HelperService
    private HelperService helperService = HelperService.getDefaultInstance();
    // STATUS
    private final static String STATUS_BLACK = "Black";
    private final static String STATUS_ACTIVE = "Active";
    private final static String STATUS_PAUSED = "Paused";
    private final static String STATUS_QUEUED = "Queued";

    private final static String SORT_BY_NAME = "name";
    private final static String SORT_BY_AMOUNT_DOWNLOADED = "amountDownloaded";
    private final static String SORT_BY_TOTAL_LENGTH = "totalLength";

    public final String HOME_ACTIVITY_TITLE = "Files - Sorted By";

    // sortBy - to sort attachments
    public ArrayList<String> sortBy;
    // sortByIdentifier
    private int sortByIdentifier;

    public AttachmentAdapter(Context c, HomeActivity homeActivity1, int sortByIdentifier) {
        mContext = c;
        homeActivity = homeActivity1;
        mInflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dbHelper = new DBHelper(mContext);
        sortByIdentifier = sortByIdentifier;
        // initialize sortBy
        initializeSortBy();
        // initialize attachments
        initializeAttachmentsUsingAttachmentTable(homeActivity);
        //checkIfAttachmentsHaveToBeDownloadedFromWebServer
        checkIfAttachmentsHaveToBeDownloadedFromWebServer(mContext, homeActivity);
    }

    // checkIfAttachmentsHaveToBeDownloadedFromWebServer (if they don't exist in attachment table)
    public void checkIfAttachmentsHaveToBeDownloadedFromWebServer(Context mContext, HomeActivity homeActivity) {
        if (attachments.size() == 0) {// make a web service call as there are no records in Attachment table
            new InitAttachmentsTask(mContext, homeActivity).execute();
        }
    }

    // use AttachmentAdapter's attachments to populate attachment table
    public void populateAttachmentTableUsingAttachments(DBHelper dbHelper) {
        for (Attachment attachment : attachments) {// loop through attachments
            // insert attachment record
            dbHelper.insertAttachmentRecord(attachment);
        }
    }

    /**
     * initialize sortBy arrayList
     */
    private void initializeSortBy() {
        sortBy = new ArrayList<String>();
        sortBy.add(SORT_BY_NAME);
        sortBy.add(SORT_BY_AMOUNT_DOWNLOADED);
        sortBy.add(SORT_BY_TOTAL_LENGTH);
    }

    /**
     * initializes attachments using Attachment Table
     */
    private void initializeAttachmentsUsingAttachmentTable(HomeActivity homeActivity) {
        // get all attachments using attachment table
        attachments = dbHelper.returnAllAttachments();
        // sort attachments using sortByIdentifier
        String sortByIdentifierValue = sortBy.get(sortByIdentifier);
        attachments = helperService.sortAttachments(attachments, sortByIdentifierValue);
        // change title
        homeActivity.setTitle(HOME_ACTIVITY_TITLE + " " + sortByIdentifierValue);
    }


    @Override
    public int getCount() {
        return attachments.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflator.inflate(R.layout.attachment_item_layout, parent, false);
        }

        // get attachment
        Attachment attachment = attachments.get(position);
        // update fileNameTextView
        TextView fileNameTextView = (TextView) convertView.findViewById(R.id.fileNameTextView);
        fileNameTextView.setText(attachment.getName());
        // update fileSizeOrStatusTextView
        updateFileSizeOrStatusTextView(convertView, attachment);
        // update downloadProgressBar
        updateProgressForDownloadProgressBar(convertView, attachment);
        return convertView;
    }

    /**
     * update progress for attachment's attachmentCell's progressView
     *
     * @param attachmentItemView attachmentItemView
     * @param attachment attachment object
     */
    public void updateProgressForDownloadProgressBar(View attachmentItemView, Attachment attachment) {
        // find downloadProgressBar
        ProgressBar downloadProgressBar = (ProgressBar) attachmentItemView.findViewById(R.id.downloadProgressBar);
        // calculate progress
        int progress = 0;
        if (attachment.getLocalFileSize() > 0 && attachment.getTotalLength() > 0) {
            // convert it to percentage as max value for downloadProgressBar is 100
            progress = (int) ((attachment.getLocalFileSize() * 100) / attachment.getTotalLength());
        }
        // update amountDownloaded
        attachment.setAmountDownloaded(progress);

        // update downloadProgressBar
        downloadProgressBar.setProgress(attachment.getAmountDownloaded());
        // update fileSizeOrStatusTextView
        if (attachment.getAmountDownloaded() >= 100) {// download complete
            updateFileSizeOrStatusTextView(attachmentItemView, attachment);
        }
    }

    /**
     * updates fileSizeOrStatusTextView
     *
     * @param attachmentItemView attachmentItemView
     * @param attachment object
     */
    public void updateFileSizeOrStatusTextView(View attachmentItemView, Attachment attachment) {
        // find fileSizeOrStatusTextView
        TextView fileSizeOrStatusTextView = (TextView) attachmentItemView.findViewById(R.id.fileSizeOrStatusTextView);
        String fileSizeOrStatus = STATUS_BLACK;
        if (attachment.getDownloadCompleted()) {// downloadCompleted -> show fileSize
            long attachmentLocalFileSize = attachment.getLocalFileSize() / 1024; // KB
            String measure = "KB";
            if (attachmentLocalFileSize > 1024) {
                attachmentLocalFileSize = attachmentLocalFileSize / 1024; // MB
                measure = "MB";
            }
            fileSizeOrStatus = attachmentLocalFileSize + measure;
        } else if (attachment.getDownloadInProgress()) {// downloadInProgress
            fileSizeOrStatus = STATUS_ACTIVE;
        } else if (attachment.getDownloadPaused()) {// downloadPaused
            fileSizeOrStatus = STATUS_PAUSED;
        } else if (attachment.getDownloadQueued()) {// downloadQueued
            fileSizeOrStatus = STATUS_QUEUED;
        }
        // update fileSizeOrStatusTextView
        fileSizeOrStatusTextView.setText(fileSizeOrStatus);
    }

    /**
     * initializes attachments by making a web service call and parsing JSON
     */
    public class InitAttachmentsTask extends AsyncTask<Void, Void, Object> {
        private Context context;
        private HomeActivity homeActivity;

        public InitAttachmentsTask(Context context, HomeActivity homeActivity) {
            this.context = context;
            this.homeActivity = homeActivity;
        }

        @Override
        protected Object doInBackground(Void... params) {
            Object dataReturnedFromWebServiceCall = null;
            // download attachments from the server
            try {
                dataReturnedFromWebServiceCall = WebService.getDefaultInstance().returnAttachmentsData();
            } catch (Exception e) {
                // no need to do anything right now
            }

            return dataReturnedFromWebServiceCall;
        }

        @Override
        protected void onPostExecute(Object dataReturnedFromWebServiceCall) {

            super.onPostExecute(dataReturnedFromWebServiceCall);
            if (dataReturnedFromWebServiceCall instanceof JSONArray) {
                attachments = new ArrayList<Attachment>();
                try {
                    // loop through attachmentData and create attachment
                    for (int i = 0; i < ((JSONArray) dataReturnedFromWebServiceCall).length(); i++) {// attachments data is received
                        Attachment attachment = new Attachment();
                        JSONObject attachmentData = ((JSONArray) dataReturnedFromWebServiceCall).getJSONObject(i);
                        // set properties
                        attachment.setAid(attachmentData.getString("id"));
                        attachment.setName(helperService.replaceHTMLEntities(attachmentData.getString("name")));
                        String webURL = attachmentData.getString("url");
                        // replace all spaces - to avoid issues with some url
                        webURL = webURL.replaceAll(" ", "%20");
                        attachment.setWebURL(webURL);
                        attachment.setUrl(new URL(webURL));
                        // set amountDownloaded and fileSize to be 0.0 to avoid any errors if the user clicks on Sort
                        attachment.setAmountDownloaded(0);
                        attachment.setLocalFileSize(0);
                        // set all download properties to false
                        attachment.setDownloadInProgress(false);
                        attachment.setDownloadPaused(false);
                        attachment.setDownloadCompleted(false);
                        attachment.setDownloadQueued(false);
                        // insert attachment in to Attachment table
                        Boolean inserted = dbHelper.insertAttachmentRecord(attachment);
                        // add attachment to attachments
                        attachments.add(attachment);
                    }
                } catch (Exception e) {// show generic errorMessage
                    HelperService.getDefaultInstance().showAlertDialog("Error", HelperService.getDefaultInstance().returnGenericErrorMessage(), "OK", homeActivity);
                }
            } else {// some errorMessage show "UIAlertView"
                attachments = new ArrayList<Attachment>();
                if (dataReturnedFromWebServiceCall instanceof LinkedHashMap) {
                    String errorMessage = (String) ((LinkedHashMap) dataReturnedFromWebServiceCall).get("errorMessage");
                    HelperService.getDefaultInstance().showAlertDialog("Error", errorMessage, "OK", homeActivity);
                }
            }
            // sort attachments using sortByIdentifier
            String sortByIdentifierValue = sortBy.get(sortByIdentifier);
            attachments = helperService.sortAttachments(attachments, sortByIdentifierValue);
            // notify adapter of dataset change
            AttachmentAdapter attachmentAdapter = (AttachmentAdapter) homeActivity.attachmentsListView.getAdapter();
            attachmentAdapter.notifyDataSetChanged();
        }
    }
}