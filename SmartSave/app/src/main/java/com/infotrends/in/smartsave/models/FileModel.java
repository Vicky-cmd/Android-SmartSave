package com.infotrends.in.smartsave.models;

import android.net.Uri;

import java.util.Date;
import java.net.URI;

public class FileModel {

    private String fileName;
    private String fileType;
    private Date fileTimestamp;
    private String fileContent;
    private Uri fileURI;
    private String isImage;
    private String isPublic;
    private String sharedCont;
    private String sharedStatus;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Date getFileTimestamp() {
        return fileTimestamp;
    }

    public void setFileTimestamp(Date fileTimestamp) {
        this.fileTimestamp = fileTimestamp;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public Uri getFileURI() {
        return fileURI;
    }

    public void setFileURI(Uri fileURI) {
        this.fileURI = fileURI;
    }

    public String getIsImage() {
        return isImage;
    }

    public void setIsImage(String isImage) {
        this.isImage = isImage;
    }

    public String getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(String isPublic) {
        this.isPublic = isPublic;
    }

    public String getSharedCont() {
        return sharedCont;
    }

    public void setSharedCont(String sharedCont) {
        this.sharedCont = sharedCont;
    }

    public String getIsSharedStatus() {
        return sharedStatus;
    }

    public void setIsSharedStatus(String sharedStatus) {
        this.sharedStatus = sharedStatus;
    }
}


