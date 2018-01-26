package com.shopback.notepad;

/**
 * Created by bernardkoh on 22/1/18.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesDbAdapter {

    static final String KEY_TITLE = "title";
    static final String KEY_BODY = "body";
    static final String KEY_ROWID = "_id";
    static final String KEY_DATE = "b_day";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table notes (_id integer primary key autoincrement, "
                    + "title text not null, body text not null, b_day text not null);";
    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 9;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion);
            if (oldVersion < 9) {
                upgradeVersion7(db);
            }
        }

        private void upgradeVersion7(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN " + KEY_DATE + " text");
            Cursor cursor = db.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_DATE},
                    null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                ContentValues dates = new ContentValues();
                String date = new SimpleDateFormat("dd-MM-YYYY HH:MM",
                        Locale.getDefault()).format(Calendar.getInstance().getTime());
                dates.put(KEY_DATE, date);
                db.update(DATABASE_TABLE, dates, null, null);

                cursor.close();
            }
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     *
     * @param title the title of the note
     * @param body  the body of the note
     * @return rowId or -1 if failed
     */
    long createNote(String title, String body, String date) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_DATE, date);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     *
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     *
     * @return Cursor over all notes
     */
    private Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_DATE}, null, null, null, null, null);
    }

    List<Note> fetchListOfNotes() {
        List<Note> list = new ArrayList<>();
        Cursor cursor = fetchAllNotes();
        while (cursor.moveToNext()) {
            Long rowId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ROWID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
            String body = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BODY));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE));
            list.add(new Note(rowId, title, body, Note.NoteType.NOTES, date));
        }
        return list;
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[]{KEY_ROWID,
                        KEY_TITLE, KEY_BODY}, KEY_ROWID + "=" + rowId, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body  value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    boolean updateNote(long rowId, String title, String body, String date) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        args.put(KEY_DATE, date);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }


    void addManyColumns() {
        long startTime = System.currentTimeMillis();
        mDb.beginTransaction();
        try {
            for(int i = 0; i<500; i++) {
                String colName = "col" + i;
                mDb.execSQL("ALTER TABLE "+ DATABASE_TABLE+" ADD COLUMN " + colName + " text");
            }
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
        long endTime = System.currentTimeMillis();
        Log.e("add Columns", "time taken with batch ops = " + (endTime - startTime));

        startTime = System.currentTimeMillis();
        for(int i = 500; i<1000; i++) {
            String colName = "col" + i;
            mDb.execSQL("ALTER TABLE "+ DATABASE_TABLE+" ADD COLUMN " + colName + " text");
        }
        endTime = System.currentTimeMillis();
        Log.e("add Columns", "time taken without batch ops= " + (endTime - startTime));

        mDb.execSQL("DROP TABLE " + DATABASE_TABLE);
        mDb.execSQL(DATABASE_CREATE);
    }
}
