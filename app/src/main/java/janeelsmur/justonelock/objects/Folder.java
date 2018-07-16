package janeelsmur.justonelock.objects;


public class Folder {

    public String fullFilePath, folderName;
    public int i, folderId;

    public Folder(String fullFilePath, int i, String folderName, int folderId) {
        this.fullFilePath = fullFilePath;
        this.folderId = folderId;
        this.i = i;
        this.folderName = folderName;
    }
}
