package com.sb.downloadprogress;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // attachmentsListView
    public ListView attachmentsListView;

    // context
    private Context context;

    // sortByIdentifier - to be used to for sorting
    private int sortByIdentifier = 0;

    // number of downloads in progress
    private int numberOfDownloadsInProgress = 0;

    // maximum number of downloads allowed
    private int maxNoOfDownloadsAllowed = 2;

    // for faster access of attachments which are queued
    private ArrayList<Attachment> downloadQueueArray = new ArrayList<Attachment>();

    // dbHelper instance
    private DBHelper dbHelper;

    // to store DownloadAttachmentTask associated with an attachment
    private HashMap<Attachment, DownloadAttachmentTask> downloadAttachmentTaskForAttachmentIndex = new HashMap<Attachment, DownloadAttachmentTask>();

    // helperService
    private HelperService helperService;

    private static final String DOWNLOAD_BUTTON_TITLE = "Download";
    private static final String PAUSE_BUTTON_TITLE = "Pause";
    private static final String DELETE_BUTTON_TITLE = "Delete";
    private static final String RESUME_BUTTON_TITLE = "Resume";
    private static final String CANCEL_BUTTON_TITLE = "Cancel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // get context
        context = getApplicationContext();
        // create helperService
        helperService = HelperService.getDefaultInstance();
        // create dbHelper
        dbHelper = new DBHelper(context);
        // get mListView
        attachmentsListView = (ListView) findViewById(R.id.attachments_list);

        attachmentsListView.setAdapter(new AttachmentAdapter(context, HomeActivity.this, sortByIdentifier));
        // Set OnItemClickListener so we can be notified on item clicks
        attachmentsListView.setOnItemClickListener(this);
        // check for attachments with downloadInProgress = YES and downloadQueued = YES
        // to take care of the cases when app is killed/ crashed
        checkIfAnyDownloadsNeedToBeStartedOrQueued();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // find attachment for row selected
        final Attachment attachment = returnAttachmentsListUsingAttachmentAdapter().get(position);
        // show alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(R.string.choose_an_action);
        builder.setMessage(R.string.choose_an_action_Message);
        builder.setCancelable(true);
        // add Cancel button
        builder.setNeutralButton(CANCEL_BUTTON_TITLE,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // cancel the dialog. no need to do anything else
                    }
                });
        // add Download/ Pause/ Resume button
        if (!attachment.getDownloadCompleted()) {// only if downloadCompleted = NO else just show Delete and Cancel buttons
            // to check if pause button is played
            Boolean _pauseButtonDisplayed = false;
            String downloadButtonTitle = DOWNLOAD_BUTTON_TITLE;
            if (attachment.getDownloadInProgress()) {
                downloadButtonTitle = PAUSE_BUTTON_TITLE;
                _pauseButtonDisplayed = true;
            } else if (attachment.getDownloadPaused()) {
                downloadButtonTitle = RESUME_BUTTON_TITLE;
            }
            // to check if pause button is played
            final Boolean pauseButtonDisplayed = _pauseButtonDisplayed;
            // add this button as neutral button
            builder.setNegativeButton(downloadButtonTitle, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (pauseButtonDisplayed) {
                        pauseAttachment(attachment);
                    } else {
                        downloadOrResumeAttachment(attachment);
                    }
                }
            });
        }
        // add Delete Button
        builder.setPositiveButton(DELETE_BUTTON_TITLE, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAttachment(attachment);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * if numberOfDownloadsInProgress < maxNoOfDownloadsAllowed then download/ resume attachment
     * else add it to downloadQueueArray
     *
     * @param attachment object
     */
    private void downloadOrResumeAttachment(Attachment attachment) {
        if (numberOfDownloadsInProgress < maxNoOfDownloadsAllowed) {// start download
            // increment numberOfDownloadsInProgress and update downloadInProgress
            numberOfDownloadsInProgress++;
            attachment.setDownloadInProgress(true);
            // start DownloadAttachmentTask and add that task to attachmentDownloadAttachmentTask HashMap
            DownloadAttachmentTask downloadAttachmentTask = new DownloadAttachmentTask(context);
            downloadAttachmentTaskForAttachmentIndex.put(attachment, downloadAttachmentTask);
            downloadAttachmentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, attachment);
        } else {// add it to queue and update attachment's downloadQueued
            attachment.setDownloadQueued(true);
            attachment.setDownloadPaused(false);
            downloadQueueArray.add(attachment);
            // show alertDialog indicating the file has been Queued..
            helperService.showAlertDialog("Queued", "Exceeded maximum no of downloads. So this download is queued!", "Ok", HomeActivity.this);
        }
        int attachmentIndex = returnAttachmentsListUsingAttachmentAdapter().indexOf(attachment);
        // check if view at attachmentIndex is visible
        if (checkIfViewIsVisibleInAttachmentsListView(attachmentIndex)) {
            // get entire view
            View attachmentItemView = attachmentsListView.getChildAt(attachmentIndex - attachmentsListView.getFirstVisiblePosition());
            AttachmentAdapter attachmentAdapter = (AttachmentAdapter) attachmentsListView.getAdapter();
            // updateFileSizeOrStatusTextView
            attachmentAdapter.updateFileSizeOrStatusTextView(attachmentItemView, attachment);
        }

        // persist data
        persistData();
    }

    /**
     * Pause attachment's download
     * @param attachment object
     */
    private void pauseAttachment(Attachment attachment) {
        // cancel DownloadAttachmentTask if it exists for this attachment
        DownloadAttachmentTask downloadAttachmentTask = downloadAttachmentTaskForAttachmentIndex.get(attachment);
        if (downloadAttachmentTask != null) {
            if (downloadAttachmentTask.getStatus() == AsyncTask.Status.RUNNING) {
                // cancel downloadAttachmentTask
                downloadAttachmentTask.cancel(true);
            }
        }
        // update attachment's properties
        attachment.setDownloadInProgress(false);
        attachment.setDownloadPaused(true);
        // set fileSizeToBeIgnored to ignore bytes which have already been written
        attachment.setFileSizeToBeIgnored(attachment.getLocalFileSize());
        // decrement numberOfDownloadsInProgress
        numberOfDownloadsInProgress--;
        // check if view at attachmentIndex is visible
        int attachmentIndex = returnAttachmentsListUsingAttachmentAdapter().indexOf(attachment);
        if (checkIfViewIsVisibleInAttachmentsListView(attachmentIndex)) {
            // get entire view
            View attachmentItemView = attachmentsListView.getChildAt(attachmentIndex - attachmentsListView.getFirstVisiblePosition());
            AttachmentAdapter attachmentAdapter = (AttachmentAdapter) attachmentsListView.getAdapter();
            // update fileSizeOrStatusTextView
            attachmentAdapter.updateFileSizeOrStatusTextView(attachmentItemView, attachment);
        }

        // persist data
        persistData();
        // startDownloadingQueuedAttachments
        startDownloadingQueuedAttachments();
    }

    /**
     * deletes all attachment from device
     * for now just being called when refresh is clicked
     */
    private void deleteAllAttachmentsFromDevice() {
        // loop through attachments
        for (Attachment attachment : returnAttachmentsListUsingAttachmentAdapter()) {
            // delete the file
            File file = new File(getFilesDir() + "/" + attachment.getLocalName());
            Boolean success = file.delete();
        }
    }

    /**
     * Delete attachment
     * @param attachment object
     */
    private void deleteAttachment(Attachment attachment) {
        // cancel DownloadAttachmentTask if it exists for this attachment
        int attachmentIndex = returnAttachmentsListUsingAttachmentAdapter().indexOf(attachment);
        DownloadAttachmentTask downloadAttachmentTask = downloadAttachmentTaskForAttachmentIndex.get(attachment);
        if (downloadAttachmentTask != null) {
            if (downloadAttachmentTask.getStatus() == AsyncTask.Status.RUNNING) {// user deleted the attachment while it was getting downloaded
                // cancel downloadAttachmentTask
                downloadAttachmentTask.cancel(true);
                // decrement numberOfDownloadsInProgress
                if (numberOfDownloadsInProgress > 0) {
                    numberOfDownloadsInProgress = numberOfDownloadsInProgress - 1;
                }
            }
        }

        // delete the file
        File file = new File(getFilesDir() + "/" + attachment.getLocalName());
        Boolean success = file.delete();

        if (success) {// file has been deleted
            // update attachment
            attachment.setDownloadCompleted(false);
            attachment.setDownloadInProgress(false);
            attachment.setDownloadPaused(false);
            attachment.setLocalFileSize(0);
            attachment.setTotalLength(0);

            // show alertDialog indicating the file has been deleted..
            helperService.showAlertDialog("File Deleted!", "", "Ok", HomeActivity.this);
            // check if view at attachmentIndex is visible
            if (checkIfViewIsVisibleInAttachmentsListView(attachmentIndex)) {
                // get entire view
                View attachmentItemView = attachmentsListView.getChildAt(attachmentIndex - attachmentsListView.getFirstVisiblePosition());
                AttachmentAdapter attachmentAdapter = (AttachmentAdapter) attachmentsListView.getAdapter();
                // update downloadProgressBar
                attachmentAdapter.updateProgressForDownloadProgressBar(attachmentItemView, attachment);
                // update fileSizeOrStatusTextView
                attachmentAdapter.updateFileSizeOrStatusTextView(attachmentItemView, attachment);
            }

            // persist data
            persistData();
            // startDownloadingQueuedAttachments if possible
            startDownloadingQueuedAttachments();
        } else {
            // no need to do anything right now...
        }
    }

    /**
     * sort button is clicked
     * @param view view
     */
    public void sortButtonClicked(View view) {
        // create and show toast
        Toast toast = Toast.makeText(getApplicationContext(), R.string.sorted, Toast.LENGTH_SHORT);
        toast.show();

        if (sortByIdentifier == 2) {// set to 0
            sortByIdentifier = 0;
        } else { // increment identifier
            sortByIdentifier = sortByIdentifier + 1;
        }
        // get attachmentAdapter
        AttachmentAdapter attachmentAdapter = (AttachmentAdapter) attachmentsListView.getAdapter();
        // sortByIdentifierValue
        String sortByIdentifierValue = attachmentAdapter.sortBy.get(sortByIdentifier);
        // sort
        attachmentAdapter.attachments = helperService.sortAttachments(attachmentAdapter.attachments, sortByIdentifierValue);
        // notify data set changed
        attachmentAdapter.notifyDataSetChanged();
        // update title
        setTitle(attachmentAdapter.HOME_ACTIVITY_TITLE + " " + sortByIdentifierValue);
    }

    /**
     * refresh button is clicked
     */
    public void refreshButtonClicked(View view) {
        // create and show toast
        Toast toast = Toast.makeText(getApplicationContext(), R.string.refreshing, Toast.LENGTH_SHORT);
        toast.show();
        // cancel all DownloadAttachmentTasks
        for (DownloadAttachmentTask downloadAttachmentTask : downloadAttachmentTaskForAttachmentIndex.values()) {
            if (downloadAttachmentTask != null) {
                if (downloadAttachmentTask.getStatus() == AsyncTask.Status.RUNNING) {
                    // cancel downloadAttachmentTask
                    downloadAttachmentTask.cancel(true);
                }
            }
        }
        // empty previous DownloadAttachmentTask related data
        downloadAttachmentTaskForAttachmentIndex = new HashMap<Attachment, DownloadAttachmentTask>();
        downloadQueueArray = new ArrayList<Attachment>();
        numberOfDownloadsInProgress = 0;
        // deleteAllAttachmentsFromDevice
        deleteAllAttachmentsFromDevice();
        // empty current attachments
        AttachmentAdapter attachmentAdapter = (AttachmentAdapter) attachmentsListView.getAdapter();
        attachmentAdapter.attachments = new ArrayList<Attachment>();
        // drop Attachment table
        dbHelper.dropAndCreateAttachmentTable();
        // download JSON again from the server
        attachmentAdapter.checkIfAttachmentsHaveToBeDownloadedFromWebServer(context, HomeActivity.this);
    }

    /**
     * @return attachmentsListView's adapter's attachments
     */
    private ArrayList<Attachment> returnAttachmentsListUsingAttachmentAdapter() {
        AttachmentAdapter attachmentAdapter = (AttachmentAdapter) attachmentsListView.getAdapter();
        return attachmentAdapter.attachments;
    }

    /**
     * Downloads attachment using attachment's webURL
     * updates downloadProgressBar
     */
    public class DownloadAttachmentTask extends AsyncTask<Attachment, Integer, Void> {
        private Context context;
        private HomeActivity homeActivity;

        public DownloadAttachmentTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Attachment... params) {
            // get attachment
            Attachment attachment = params[0];
            HttpURLConnection httpURLConnection = null;
            if (!isCancelled()) {
                try {
                    // open connection
                    httpURLConnection = (HttpURLConnection) attachment.getUrl().openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setInstanceFollowRedirects(true);
                    // handle redirects
                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP || httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_SEE_OTHER) {// redirect from http -> https or https -> http
                        // open connection
                        httpURLConnection = (HttpURLConnection) httpURLConnection.getURL().openConnection();
                        httpURLConnection.setRequestMethod("GET");
                        httpURLConnection.setInstanceFollowRedirects(true);
                    }

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // set properties of attachment using response
                        attachment.setTotalLength(httpURLConnection.getContentLength());
                        attachment.setMimeType(httpURLConnection.getContentType());
                        String suggestedFileName = helperService.returnFileNameFromUrl(httpURLConnection.getURL());
                        attachment.setLocalName(suggestedFileName);

                        // create file
                        BufferedInputStream reader = new BufferedInputStream(httpURLConnection.getInputStream());

                        // bytes to read
                        byte[] data = new byte[4096];
                        int len;
                        // to track how many times data is written to file
                        // so that data is persisted only after every 10 writes
                        int writeCounter = 0;
                        // openFileOutput using MODE_APPEND
                        FileOutputStream fOut = openFileOutput(suggestedFileName, MODE_APPEND);
                        while ((len = reader.read(data)) > 0) {
                            if (!isCancelled()) {
                                long attachmentFileSizeToBeIgnored = attachment.getFileSizeToBeIgnored();
                                if (len <= attachmentFileSizeToBeIgnored) {// ignore these bytes
                                    attachmentFileSizeToBeIgnored = attachmentFileSizeToBeIgnored - len;
                                    attachment.setFileSizeToBeIgnored(attachmentFileSizeToBeIgnored);
                                } else {
                                    int bytesToWrite = (int) (len - attachmentFileSizeToBeIgnored);
                                    // write to file
                                    fOut.write(data, (int) attachmentFileSizeToBeIgnored, bytesToWrite);
                                    // update fileSizeToBeIgnored
                                    attachment.setFileSizeToBeIgnored(0);
                                    // update localFileSize
                                    attachment.setLocalFileSize((attachment.getLocalFileSize() + bytesToWrite));

                                    // persist data
                                    if (writeCounter > 0 && (writeCounter % 500 == 0)) {
                                        persistData();
                                    }
                                    // increment writeCounter
                                    writeCounter++;
                                    // update downloadProgressBar
                                    int attachmentIndex = returnAttachmentsListUsingAttachmentAdapter().indexOf(attachment);
                                    publishProgress(attachmentIndex);
                                }
                            }
                        }
                        if (!isCancelled()) {
                            // update downloadInProgress and downloadCompleted
                            attachment.setDownloadInProgress(false);
                            attachment.setDownloadCompleted(true);

                            // decrement numberOfDownloadsInProgress
                            numberOfDownloadsInProgress--;
                            // update downloadProgressBar
                            int attachmentIndex = returnAttachmentsListUsingAttachmentAdapter().indexOf(attachment);
                            publishProgress(attachmentIndex);

                            // persist data
                            persistData();
                        }

                        // startDownloadingQueuedAttachments
                        startDownloadingQueuedAttachments();

                        // close streams
                        fOut.close();
                        reader.close();
                    }
                } catch (Exception e) {
                    // no need to do anything right now
                    // just decrement numberOfDownloadsInProgress for now
                    numberOfDownloadsInProgress--;
                    // startDownloadingQueuedAttachments
                    startDownloadingQueuedAttachments();
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... params) {
            int attachmentIndex = params[0];
            // check if view at attachmentIndex is visible
            if (checkIfViewIsVisibleInAttachmentsListView(attachmentIndex)) {// view is visible
                // get attachment
                Attachment attachment = returnAttachmentsListUsingAttachmentAdapter().get(attachmentIndex);
                // get entire view
                View attachmentItemView = attachmentsListView.getChildAt(attachmentIndex - attachmentsListView.getFirstVisiblePosition());
                AttachmentAdapter attachmentAdapter = (AttachmentAdapter) attachmentsListView.getAdapter();
                attachmentAdapter.updateProgressForDownloadProgressBar(attachmentItemView, attachment);
            }
        }

        @Override
        protected void onPostExecute(Void something) {
            // no need to do anything for now
        }
    }

    /**
     * determine if view at attachmentIndex is visible
     *
     * @param attachmentIndex attachment's index in attachments
     * @return true if attachmentIndex is between firstVisibleRow and lastVisibleRow
     */
    private Boolean checkIfViewIsVisibleInAttachmentsListView(int attachmentIndex) {
        Boolean viewVisible = false;
        // get firstVisibleRow
        int firstVisibleRow = attachmentsListView.getFirstVisiblePosition();
        // get lastVisibleRow
        int lastVisibleRow = attachmentsListView.getLastVisiblePosition();

        // if attachmentIndex is between firstVisibleRow and lastVisibleRow
        // in other words attachment is still visible in attachmentsListView
        if (attachmentIndex >= firstVisibleRow && attachmentIndex <= lastVisibleRow) {// view is visible
            viewVisible = true;
        }
        return viewVisible;
    }

    /**
     * starts downloading an attachment from downloadQueueArray
     */
    private void startDownloadingQueuedAttachments() {
        // numberOfDownloads that can be started
        int numberOfDownloadsToStart = maxNoOfDownloadsAllowed - numberOfDownloadsInProgress;
        for (int i = 0; i < numberOfDownloadsToStart; i++) {// loop through and start downloading
            if (i < downloadQueueArray.size()) {// attachment exists
                Attachment attachment = downloadQueueArray.get(i);
                // increment numberOfDownloadsInProgress
                numberOfDownloadsInProgress++;
                // start DownloadAttachmentTask and add that task to attachmentDownloadAttachmentTask hashmap
                DownloadAttachmentTask downloadAttachmentTask = new DownloadAttachmentTask(context);
                downloadAttachmentTaskForAttachmentIndex.put(attachment, downloadAttachmentTask);
                downloadAttachmentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, attachment);
                // update downloadQueued for attachment
                attachment.setDownloadQueued(false);
                // update downloadInProgress
                attachment.setDownloadInProgress(true);
                // find attachmentIndex
                int attachmentIndex = returnAttachmentsListUsingAttachmentAdapter().indexOf(attachment);
                // check if view at attachmentIndex is visible if yes then update fileSizeOrStatusTextView
                if (checkIfViewIsVisibleInAttachmentsListView(attachmentIndex)) {
                    // get entire view
                    View attachmentItemView = attachmentsListView.getChildAt(attachmentIndex - attachmentsListView.getFirstVisiblePosition());
                    //View attachmentItemView = attachmentsListView.getAdapter().getView(attachmentIndex, null, attachmentsListView);
                    AttachmentAdapter attachmentAdapter = (AttachmentAdapter) attachmentsListView.getAdapter();
                    attachmentAdapter.updateFileSizeOrStatusTextView(attachmentItemView, attachment);
                }
                // remove attachment from downloadQueueArray
                downloadQueueArray.remove(i);
            }
        }
    }

    /**
     * persists data
     * drop and create attachment table
     * use AttachmentAdapter's attachments to populate attachment table
     */
    private void persistData() {
        // drop Attachment table
        dbHelper.dropAndCreateAttachmentTable();
        // use AttachmentAdapter's attachments to populate attachment table
        AttachmentAdapter attachmentAdapter = (AttachmentAdapter) attachmentsListView.getAdapter();
        attachmentAdapter.populateAttachmentTableUsingAttachments(dbHelper);
    }

    /**
     * check for attachments with downloadInProgress = YES and downloadQueued = YES
     * to take care of the cases when app is killed/ crashed
     */
    private void checkIfAnyDownloadsNeedToBeStartedOrQueued() {
        // loop through attachments
        Boolean startDownloadingQueuedAttachments = true;
        for (Attachment attachment : returnAttachmentsListUsingAttachmentAdapter()) {
            if (attachment.getDownloadInProgress()) {// resume this attachment
                startDownloadingQueuedAttachments = false; // because it will automatically get called as soon as this download ends
                // it was active so some data has been written to the file so to ignore those packets!
                attachment.setFileSizeToBeIgnored(attachment.getLocalFileSize());
                // start download
                // increment numberOfDownloadsInProgress
                numberOfDownloadsInProgress++;
                attachment.setDownloadInProgress(true);
                // start DownloadAttachmentTask and add that task to attachmentDownloadAttachmentTask hashmap
                DownloadAttachmentTask downloadAttachmentTask = new DownloadAttachmentTask(context);
                downloadAttachmentTaskForAttachmentIndex.put(attachment, downloadAttachmentTask);
                downloadAttachmentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, attachment);
            } else if (attachment.getDownloadQueued()) {// add it to the downloadQueueArray
                downloadQueueArray.add(attachment);
            }
        }
        // see if any queued attachments can be started
        if (startDownloadingQueuedAttachments) {
            startDownloadingQueuedAttachments();
        }
    }
}
