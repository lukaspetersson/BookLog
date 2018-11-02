package com.lukas.android.booklog.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lukas.android.booklog.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    //name database file
    private static final String DATABASE_NAME = "bookshelf.db";

    //give unique version
    private static final int DATABASE_VERSION = 1;

    //create new instance of the database helper
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //creates the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create a String that contains the SQL statement to create the Books table
        //columns are made different types of values and extra specifications
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + BookEntry.COLUMN_AUTHOR + " TEXT, "
                + BookEntry.COLUMN_STATUS + " INTEGER NOT NULL, "
                + BookEntry.COLUMN_DATE + " TEXT, "
                + BookEntry.COLUMN_RATING + " INTEGER DEFAULT 0, "
                + BookEntry.COLUMN_NOTES + " TEXT, "
                + BookEntry.COLUMN_THUMBNAIL + " TEXT);";
        // execute the SQL statement
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    //this is called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //nothing, database still version 1
    }
}

