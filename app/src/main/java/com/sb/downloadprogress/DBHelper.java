package com.sb.downloadprogress;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.net.URL;
import java.util.ArrayList;

/**
 * DBHelper class
 * does all the interaction with SQLite Database
 */
public class DBHelper extends SQLiteOpenHelper {

    // globals
    public static final String DATABASE_NAME = "DownloadProgress.db";
    // not persisting URL
    public static final String ATTACHMENT_TABLE_NAME = "attachment";
    public static final String AID_COLUMN_NAME = "aid";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String WEBURL_COLUMN_NAME = "webURL";
    public static final String AMOUNT_DOWNLOADED_COLUMN_NAME = "amountDownloaded";
    public static final String LOCAL_NAME_COLUMN_NAME = "localName";
    public static final String LOCAL_PATH_COLUMN_NAME = "localPath";
    public static final String LOCAL_FILE_SIZE_COLUMN_NAME = "localFileSize";
    public static final String FILE_SIZE_TO_BE_IGNORED_COLUMN_NAME = "fileSizeToBeIgnored";
    public static final String MIME_TYPE_COLUMN_NAME = "mimeType";
    public static final String TOTAL_LENGTH_COLUMN_NAME = "totalLength";
    public static final String DOWNLOAD_IN_PROGRESS_COLUMN_NAME = "downloadInProgress";
    public static final String DOWNLOAD_PAUSED_COLUMN_NAME = "downloadPaused";
    public static final String DOWNLOAD_COMPLETED_COLUMN_NAME = "downloadCompleted";
    public static final String DOWNLOAD_QUEUED_COLUMN_NAME = "downloadQueued";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table attachment " +
                        "(aid text primary key, name text, webURL text, amountDownloaded integer, " +
                        "localName text, localPath text, localFileSize integer, " +
                        "fileSizeToBeIgnored integer, mimeType text, totalLength integer, " +
                        "downloadInProgress integer, downloadPaused integer, downloadCompleted integer, " +
                        "downloadQueued integer);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS attachment");
        onCreate(db);
    }

    /**
     *
     * drops table attachment and then creates it again
     * faster way to drop all attachment records...
     */
    public void dropAndCreateAttachmentTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS attachment");
        onCreate(db);
    }

    /**
     * inserts attachment
     *
     * @param aid attachment's id
     * @param name attachment's name
     * @param webURL attachment's webURL
     * @param amountDownloaded attachment's amountDownloaded
     * @param localName attachment's localName
     * @param localPath attachment's localPath
     * @param localFileSize attachment's localFileSize
     * @param fileSizeToBeIgnored attachment's fileSizeToBeIgnored
     * @param mimeType attachment's mimeType
     * @param totalLength attachment's totalLength
     * @param downloadInProgress attachment's downloadInProgress
     * @param downloadPaused attachment's downloadPaused
     * @param downloadCompleted attachment's downloadComplete
     * @param downloadQueued attachment's downloadQueued
     * @return true is attachment is inserted
     */
    public boolean insertAttachmentRecord(String aid, String name, String webURL, int amountDownloaded, String localName, String localPath, long localFileSize, long fileSizeToBeIgnored, String mimeType, long totalLength, Boolean downloadInProgress, Boolean downloadPaused, Boolean downloadCompleted, Boolean downloadQueued) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues attachmentValues = new ContentValues();
        attachmentValues.put(AID_COLUMN_NAME, aid);
        attachmentValues.put(NAME_COLUMN_NAME, name);
        attachmentValues.put(WEBURL_COLUMN_NAME, webURL);
        attachmentValues.put(AMOUNT_DOWNLOADED_COLUMN_NAME, amountDownloaded);
        attachmentValues.put(LOCAL_NAME_COLUMN_NAME, localName);
        attachmentValues.put(LOCAL_PATH_COLUMN_NAME, localPath);
        attachmentValues.put(LOCAL_FILE_SIZE_COLUMN_NAME, localFileSize);
        attachmentValues.put(FILE_SIZE_TO_BE_IGNORED_COLUMN_NAME, fileSizeToBeIgnored);
        attachmentValues.put(MIME_TYPE_COLUMN_NAME, mimeType);
        attachmentValues.put(TOTAL_LENGTH_COLUMN_NAME, totalLength);
        attachmentValues.put(DOWNLOAD_IN_PROGRESS_COLUMN_NAME, downloadInProgress ? 1 : 0);
        attachmentValues.put(DOWNLOAD_PAUSED_COLUMN_NAME, downloadPaused ? 1 : 0);
        attachmentValues.put(DOWNLOAD_COMPLETED_COLUMN_NAME, downloadCompleted ? 1 : 0);
        attachmentValues.put(DOWNLOAD_QUEUED_COLUMN_NAME, downloadQueued ? 1 : 0);
        // insert in to db
        if (db.insert(ATTACHMENT_TABLE_NAME, null, attachmentValues) == -1) {
            return false;
        }
        return true;
    }

    /**
     * inserts attachment record
     * @param attachment
     * @return true if attachment record is inserted
     */
    public boolean insertAttachmentRecord(Attachment attachment) {
        try {
            // get db
            SQLiteDatabase db = this.getWritableDatabase();
            // get contentValues
            ContentValues attachmentValues = new ContentValues();
            // put in to contentValues
            attachmentValues.put(AID_COLUMN_NAME, attachment.getAid());
            attachmentValues.put(NAME_COLUMN_NAME, attachment.getName());
            attachmentValues.put(WEBURL_COLUMN_NAME, attachment.getWebURL());
            attachmentValues.put(AMOUNT_DOWNLOADED_COLUMN_NAME, attachment.getAmountDownloaded());
            attachmentValues.put(LOCAL_NAME_COLUMN_NAME, attachment.getLocalName());
            attachmentValues.put(LOCAL_PATH_COLUMN_NAME, attachment.getLocalPath());
            attachmentValues.put(LOCAL_FILE_SIZE_COLUMN_NAME, attachment.getLocalFileSize());
            attachmentValues.put(FILE_SIZE_TO_BE_IGNORED_COLUMN_NAME, attachment.getFileSizeToBeIgnored());
            attachmentValues.put(MIME_TYPE_COLUMN_NAME, attachment.getMimeType());
            attachmentValues.put(TOTAL_LENGTH_COLUMN_NAME, attachment.getTotalLength());
            attachmentValues.put(DOWNLOAD_IN_PROGRESS_COLUMN_NAME, attachment.getDownloadInProgress() ? 1 : 0);
            attachmentValues.put(DOWNLOAD_PAUSED_COLUMN_NAME, attachment.getDownloadPaused() ? 1 : 0);
            attachmentValues.put(DOWNLOAD_COMPLETED_COLUMN_NAME, attachment.getDownloadCompleted() ? 1 : 0);
            attachmentValues.put(DOWNLOAD_QUEUED_COLUMN_NAME, attachment.getDownloadQueued() ? 1 : 0);

            // insert in to db
            if (db.insert(ATTACHMENT_TABLE_NAME, null, attachmentValues) == -1) {
                return false;
            }
            return true;
        }
        catch (Exception e) {
            // no need to do anything right now
        }
        return false;
    }

    /**
     * @param aid attachment's id
     * @return attachement record with aid
     */
    public Cursor returnAttachmentRecord(int aid) {
        // get db
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from attachment where aid=" + aid + "", null);
        return res;
    }

    /**
     * deletes all attachment records
     */
    public void deleteAllAttachmentRecords() {
        // get db
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + ATTACHMENT_TABLE_NAME);
        db.close();
    }

    /**
     * finds all attachment records, creates Attachment and appends it to attachments
     *
     * @return all attachments
     */
    public ArrayList<Attachment> returnAllAttachments() {
        ArrayList<Attachment> attachments = new ArrayList<Attachment>();

        // get db
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from attachment", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {// loop and append to attachments
            // create new attachment
            Attachment attachment = new Attachment();
            attachment.setAid(res.getString(res.getColumnIndex(AID_COLUMN_NAME)));
            attachment.setName(res.getString(res.getColumnIndex(NAME_COLUMN_NAME)));
            String webURL = res.getString(res.getColumnIndex(WEBURL_COLUMN_NAME));
            attachment.setWebURL(webURL);
            try {
                attachment.setUrl(new URL(webURL));
            } catch (Exception e) {
                // no need to do anything right now
            }
            attachment.setAmountDownloaded(res.getInt(res.getColumnIndex(AMOUNT_DOWNLOADED_COLUMN_NAME)));
            attachment.setLocalName(res.getString(res.getColumnIndex(LOCAL_NAME_COLUMN_NAME)));
            attachment.setLocalPath(res.getString(res.getColumnIndex(LOCAL_PATH_COLUMN_NAME)));
            attachment.setLocalFileSize(res.getLong(res.getColumnIndex(LOCAL_FILE_SIZE_COLUMN_NAME)));
            attachment.setFileSizeToBeIgnored(res.getLong(res.getColumnIndex(FILE_SIZE_TO_BE_IGNORED_COLUMN_NAME)));
            attachment.setMimeType(res.getString(res.getColumnIndex(MIME_TYPE_COLUMN_NAME)));
            attachment.setTotalLength(res.getLong(res.getColumnIndex(TOTAL_LENGTH_COLUMN_NAME)));
            attachment.setDownloadInProgress(res.getInt(res.getColumnIndex(DOWNLOAD_IN_PROGRESS_COLUMN_NAME)) == 1);
            attachment.setDownloadPaused(res.getInt(res.getColumnIndex(DOWNLOAD_PAUSED_COLUMN_NAME)) == 1);
            attachment.setDownloadCompleted(res.getInt(res.getColumnIndex(DOWNLOAD_COMPLETED_COLUMN_NAME)) == 1);
            attachment.setDownloadQueued(res.getInt(res.getColumnIndex(DOWNLOAD_QUEUED_COLUMN_NAME)) == 1);
            // add attachment to attachments
            attachments.add(attachment);
            res.moveToNext();
        }
        return attachments;
    }
}
