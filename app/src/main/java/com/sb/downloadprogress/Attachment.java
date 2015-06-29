package com.sb.downloadprogress;

import java.net.URL;

/**
 * Attachment object to store all file's or attachment's properties
 */
public class Attachment {

    // to store aid
    private String aid;
    // to store name
    private String name;
    // to store webURL
    private String webURL;
    // to store url
    private URL url;
    // amountDownloaded
    private int amountDownloaded;
    // localName of the file/ attachment
    private String localName;
    // path where the file is created
    private String localPath;
    // fileSize on the device
    private long localFileSize;
    // set fileSizeToBeIgnored when app is crashed or killed when a download was active
    // or when download was resumed back, to ignore chunk of data that has already been downloaded
    private long fileSizeToBeIgnored;
    // mimeType
    private String mimeType;
    // totalLength coming from the server
    private long totalLength;
    // YES if download is in progress
    private Boolean downloadInProgress;
    // YES if download is paused
    private Boolean downloadPaused;
    // YES if download is completed
    private Boolean downloadCompleted;
    // YES if download is queued
    private Boolean downloadQueued;

    /**
     * getters
     */
    public String getAid() {
        return aid;
    }

    public String getName() {
        return name;
    }

    public String getWebURL() {
        return webURL;
    }

    public URL getUrl() {
        return url;
    }

    public int getAmountDownloaded() {
        return amountDownloaded;
    }

    public String getLocalName() {
        return localName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public long getLocalFileSize() {
        return localFileSize;
    }

    public long getFileSizeToBeIgnored() {
        return fileSizeToBeIgnored;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public Boolean getDownloadInProgress() {
        return downloadInProgress;
    }

    public Boolean getDownloadPaused() {
        return downloadPaused;
    }

    public Boolean getDownloadCompleted() {
        return downloadCompleted;
    }

    public Boolean getDownloadQueued() {
        return downloadQueued;
    }

    /**
     * setters
     */
    public void setAid(String aid) {
        this.aid = aid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWebURL(String webURL) {
        this.webURL = webURL;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setAmountDownloaded(int amountDownloaded) {
        this.amountDownloaded = amountDownloaded;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setLocalFileSize(long localFileSize) {
        this.localFileSize = localFileSize;
    }

    public void setFileSizeToBeIgnored(long fileSizeToBeIgnored) {
        this.fileSizeToBeIgnored = fileSizeToBeIgnored;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public void setDownloadInProgress(Boolean downloadInProgress) {
        this.downloadInProgress = downloadInProgress;
    }

    public void setDownloadPaused(Boolean downloadPaused) {
        this.downloadPaused = downloadPaused;
    }

    public void setDownloadCompleted(Boolean downloadCompleted) {
        this.downloadCompleted = downloadCompleted;
    }

    public void setDownloadQueued(Boolean downloadQueued) {
        this.downloadQueued = downloadQueued;
    }

    /**
     * constructors
     */
    public Attachment() {

    }

    public Attachment(String aid, String name, String webURL, int amountDownloaded, String localName, String localPath, long localFileSize, long fileSizeToBeIgnored, String mimeType, long totalLength, Boolean downloadInProgress, Boolean downloadPaused, Boolean downloadCompleted, Boolean downloadQueued) {
        aid = aid;
        name = name;
        webURL = webURL;
        amountDownloaded = amountDownloaded;
        localName = localName;
        localPath = localPath;
        localFileSize = localFileSize;
        fileSizeToBeIgnored = fileSizeToBeIgnored;
        mimeType = mimeType;
        totalLength = totalLength;
        downloadInProgress = downloadInProgress;
        downloadPaused = downloadPaused;
        downloadCompleted = downloadCompleted;
        downloadQueued = downloadQueued;
    }
}
