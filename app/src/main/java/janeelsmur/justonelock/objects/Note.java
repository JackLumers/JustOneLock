package janeelsmur.justonelock.objects;

public class Note {

    public String header, text;
    public int noteId;

    public Note(String header, String text, int noteId){
        this.header = header;
        this.text = text;
        this.noteId = noteId;
    }
}
