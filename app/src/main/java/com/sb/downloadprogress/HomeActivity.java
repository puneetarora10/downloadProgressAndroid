package com.sb.downloadprogress;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

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
    //private HashMap<Attachment, DownloadAttachmentTask> downloadAttachmentTaskForAttachmentIndex = new HashMap<Attachment, DownloadAttachmentTask>();

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
}
