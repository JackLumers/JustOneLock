package janeelsmur.justonelock.objects;

public class Password {

    public String description;
    public String title;
    public String fullFilePath;
    //Системное название папки. Может быть null, если пароль был прогружен из таблицы несгруппированных паролей.
    public String systemFolderName;
    public int passwordId;
    public int i;


    public Password(String title, String description, int i, int passwordId, String fullFilePath, String systemFolderName) {
        this.title = title;
        this.description = description;
        this.i=i;
        this.passwordId = passwordId;
        this.fullFilePath =fullFilePath;
        this.systemFolderName=systemFolderName;
    }
}
