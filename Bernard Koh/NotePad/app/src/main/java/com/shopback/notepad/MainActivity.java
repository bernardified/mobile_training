package com.shopback.notepad;

import android.graphics.Canvas;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;

    private NotesDbAdapter mDbHelper;
    private NotesRecyclerAdapter notesRecyclerAdapter;
    private RecyclerView notesRecyclerView;

    private ImageButton addButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNote();
            }
        });

        loadNotesRecyclerView();
    }

    @Override
    public void onStart() {
        mDbHelper.open();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID,0, "Add 1000 columns");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                new Thread() {
                    @Override
                    public void run() {
                        mDbHelper.addManyColumns();
                    }
                }.start();
                return true;
        }
        return false;
    }

    private void createNote() {
        Intent createNoteIntent = new Intent(this, NoteEdit.class);
        startActivityForResult(createNoteIntent, ACTIVITY_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTIVITY_CREATE) {
                notesRecyclerAdapter.addNewNote(mDbHelper.fetchListOfNotes());
            } else if (requestCode == ACTIVITY_EDIT){
                notesRecyclerAdapter.updateNote(mDbHelper.fetchListOfNotes());
            }
        }
    }

    private void loadNotesRecyclerView() {
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        notesRecyclerView.setHasFixedSize(true);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        notesRecyclerAdapter = new NotesRecyclerAdapter(mDbHelper.fetchListOfNotes());
        notesRecyclerView.setAdapter(notesRecyclerAdapter);

        //swipe to delete or edit
        final SwipeController swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onLeftButtonClicked(long rowId, int position) {
                Intent editNoteIntent = new Intent(MainActivity.this, NoteEdit.class);
                editNoteIntent.putExtra(NotesDbAdapter.KEY_ROWID, rowId);
                startActivityForResult(editNoteIntent, ACTIVITY_EDIT);
            }

            @Override
            public void onRightButtonClicked(long rowId, int position) {
                mDbHelper.deleteNote(rowId);
                notesRecyclerAdapter.deleteNote(position);
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(notesRecyclerView);

        notesRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }
}