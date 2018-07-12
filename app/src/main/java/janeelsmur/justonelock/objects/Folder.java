package janeelsmur.justonelock.objects;


public class Folder {
    public String fullFilePath, foldername;
    public int i, folderId;
    public Folder(String fullFilePath, int i, String foldername, int folderId) {
        this.fullFilePath=fullFilePath;
        this.folderId=folderId;
        this.i=i;
        this.foldername=foldername;
    }
}
