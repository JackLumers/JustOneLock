package janeelsmur.justonelock.vaultScreens;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import janeelsmur.justonelock.objects.Note;
import janeelsmur.justonelock.utilites.DBTableHelper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.utilites.FileAlgorithms;
import janeelsmur.justonelock.adapters.NoteAdapter;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import java.util.ArrayList;

public class NotesPageFragment extends Fragment {

    private byte[] key = new byte[64];
    private String fullFilePath;
    private RelativeLayout tempLayout;
    private ArrayList<Note> notes = new ArrayList<>();
    private RecyclerView recyclerView;
    private NoteAdapter mAdapter;
    private StaggeredGridLayoutManager gaggeredGridLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_notes, null);

        fullFilePath = getActivity().getIntent().getExtras().getString("fullFilePath");
        key = getActivity().getIntent().getExtras().getByteArray("KEY");

        recyclerView = view.findViewById(R.id.recycler_view);

        tempLayout = view.findViewById(R.id.notesTempLayout);

        mAdapter = new NoteAdapter(notes, fullFilePath, key);
        gaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(gaggeredGridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            notes.clear();
            loadNotesFromDatabase();
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.w("NotesPage", "onStart: " + e.getLocalizedMessage());
        }

        //Ставим или убираем заглушку
        if (notes.size() != 0) tempLayout.setVisibility(View.GONE);
        else tempLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Уведомляет фрагмент о том, что данные в нём изменились
     */
    public void notifyDataHasChanged() {
        try {
            notes.clear();
            loadNotesFromDatabase();
            mAdapter.notifyDataSetChanged();

            //Ставим или убираем заглушку
            if (notes.size() != 0) tempLayout.setVisibility(View.GONE);
            else tempLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.w("NotesPage", "notifyDataHasChanged: " + e.getLocalizedMessage());
        }
    }

    private void loadNotesFromDatabase() {
        String whereClause = DBTableHelper.NOTE_IS_REMOVED + " = ?";
        String[] whereArgs = new String[]{"0"};

        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        Cursor cursor = database.query(DBTableHelper.NOTES_TABLE, null, whereClause, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {

            //Полный текст, включая заголовок
            String fullText = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.NOTE_TEXT)), key);
            //Разлинованный текст
            String[] textLines;
            StringBuilder headerBuilder = new StringBuilder();
            StringBuilder textBuilder = new StringBuilder();
            //Id заметки по базе данных. Используется для удаления и изменения заметки
            int noteId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

            if (fullText != null) {
                textLines = fullText.split(System.getProperty("line.separator"));
                if (textLines[0].length() > 10) {
                    char[] firstLineInChars = textLines[0].toCharArray();
                    for (int i = 0; i < 10; i++) {
                        headerBuilder.append(firstLineInChars[i]);
                    }
                    headerBuilder.append("...");
                } else {
                    headerBuilder.append(textLines[0]);
                }
                for (int i = 1; i < textLines.length; i++) {
                    textBuilder.append(textLines[i]).append('\n');
                }
                notes.add(new Note(headerBuilder.toString(), textBuilder.toString(), noteId));
            } else {
                notes.add(new Note(null, null, noteId));
            }


            while (cursor.moveToNext()) {
                fullText = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.NOTE_TEXT)), key);
                textBuilder = new StringBuilder();
                headerBuilder = new StringBuilder();
                noteId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

                if (fullText != null) {
                    textLines = fullText.split(System.getProperty("line.separator"));
                    if (textLines[0].length() > 10) {
                        char[] firstLineInChars = textLines[0].toCharArray();
                        for (int i = 0; i < 10; i++) {
                            headerBuilder.append(firstLineInChars[i]);
                        }
                        headerBuilder.append("...");
                    } else {
                        headerBuilder.append(textLines[0]);
                    }
                    for (int i = 1; i < textLines.length; i++) {
                        textBuilder.append(textLines[i]).append('\n');
                    }
                    notes.add(new Note(headerBuilder.toString(), textBuilder.toString(), noteId));
                } else {
                    notes.add(new Note(null, null, noteId));
                }
            }

            cursor.close();
            database.close();
        }
    }
}

