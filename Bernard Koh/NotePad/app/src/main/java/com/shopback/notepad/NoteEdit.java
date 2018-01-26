package com.shopback.notepad;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteEdit extends AppCompatActivity {

    EditText titleText, bodyText;
    Button confirmButton;

    Long mRowId;

    private NotesDbAdapter mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);

        titleText = findViewById(R.id.title);
        bodyText = findViewById(R.id.body);
        confirmButton = findViewById(R.id.confirm);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        //check for saved state
        if (savedInstanceState != null) {
            mRowId = savedInstanceState.getLong(NotesDbAdapter.KEY_ROWID);
            Log.d("savedInstance" , "saved state present for "+mRowId.toString());
        } else {
            //if no saved state. try to retrieve bundle from intent
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
                Log.d("No savedInstance" , "edit intent for "+mRowId.toString());
            } else {
                mRowId = null;
                Log.d("No savedInstance" , "create new note intent");
            }
        }

        displayNote();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle extra = new Bundle();
                if(mRowId != null) {
                    extra.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
                }
                Intent sendBackIntent = new Intent();
                sendBackIntent.putExtras(extra);
                setResult(RESULT_OK, sendBackIntent);
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        Log.d("on pause", "saving note");
        super.onPause();
        saveCurrentNote();
    }

    @Override
    protected void onResume() {
        Log.d("on resume", "displaying note");
        super.onResume();
        displayNote();
    }

    private void saveCurrentNote() {
        String title = titleText.getText().toString();
        String body = bodyText.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        long currTime = new Date().getTime();
        String date = sdf.format(currTime);

        if (title.equals("")) {
            if (body.equals("")) {
                return;
            } else {
                title = "Untitled";
            }
        }

        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body, date);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title, body, date);
        }
    }

    private void displayNote() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            titleText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            bodyText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));

        }
    }
}
