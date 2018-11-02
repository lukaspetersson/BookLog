package com.lukas.android.booklog.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.lukas.android.booklog.data.BookContract.BookEntry;


public class BookProvider extends ContentProvider {

    // URI matcher number for the content URI for the books table
    private static final int BOOKS = 100;
    // URI matcher number for the content URI for a single book in the books table
    private static final int BOOK_ID = 101;

    //create UriMatcher to match with the correct number
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // static initializer, runs first time class is called
    static {
        //the calls to addURI() go here, for all of the content URI patterns that the provides should recognize
        //paths added to UriMatcher so that the correct number can be returned when match is found
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    //database helper object
    private BookDbHelper mDbHelper;

    //tag for log messages
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    //create the database helper
    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper((getContext()));
        return true;
    }

    //query the info provided by the parameters from a given URI
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // cursor holds result of query
        Cursor cursor;

        //find match for URI number
        int match = sUriMatcher.match(uri);
        switch (match) {
            //entire table queryed
            case BOOKS:
                // For the BOOKS code, query the books table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the books table.

                if (sortOrder.equals("rating") || sortOrder.equals("_id")) {
                    //if we are sorting based on integers, reverce order så highers rating or latest book is displayed first
                    //load the cursor with the queryed info
                    cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                            null, null, sortOrder + " DESC");
                } else {
                    //if we sort based on numbers we make it case insensative
                    //load the cursor with the queryed info
                    cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                            null, null, "UPPER(" + sortOrder + ")");
                }

                break;
            //single book queryed
            case BOOK_ID:
                //selection provides the table we want the book from and "=?"
                // selectionArgs fill the space of the ? to get info from specific row
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                //load cursor with the queryd info of the single book
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                //throw illigal argument and displays the uri if there is an error
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //set notification on the cursor so it can be updated only if we need to change it
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // return the cursor
        return cursor;
    }

    //insert new data into the provider with the given ContentValues
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            //we only need this one case because you can only insert 1 book at the time
            case BOOKS:
                return insertPet(uri, contentValues);
            default:
                //throw illigal argument and displays the uri if there is an error
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    //contentvalues are inserted in database
    private Uri insertPet(Uri uri, ContentValues values) {
        //sanity check!

        //check that the title is not null
        String title = values.getAsString(BookEntry.COLUMN_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Book requires a title");
        }

        //check that the status is valid
        Integer status = values.getAsInteger(BookEntry.COLUMN_STATUS);
        if (status == null || !BookEntry.isValidStatus(status)) {
            throw new IllegalArgumentException("Book requires valid status");
        }

        //if the rating is provided, check that it is a valid number
        Integer rating = values.getAsInteger(BookEntry.COLUMN_RATING);
        if (rating != null && rating < 0 && rating > 5) {
            throw new IllegalArgumentException("Book requires valid rating");
        }

        //no need to check the author and date, any value is valid

        //get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //insert the new book with the given values
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        //if id is -1 there is an error
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //notify all listeners that the data has changed
        getContext().getContentResolver().notifyChange(uri, null);

        //return the new URI with the id
        return ContentUris.withAppendedId(uri, id);
    }

    //updates data in the database with new ContentValues
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            //entire table
            case BOOKS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            //single book
            case BOOK_ID:
                //selection provides the table we want the book from and "=?"
                // selectionArgs fill the space of the ? to get info from specific row
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                //throw illegal argument and displays the uri if there is an error
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    //update database and return number of rows updated
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //sanity check!
        //check that the title is not null
        String title = values.getAsString(BookEntry.COLUMN_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Book requires a title");
        }
        //check that the status is valid
        Integer status = values.getAsInteger(BookEntry.COLUMN_STATUS);
        if (status == null || !BookEntry.isValidStatus(status)) {
            throw new IllegalArgumentException("Book requires valid status");
        }

        //if the rating is provided, check that it is a valid number
        Integer rating = values.getAsInteger(BookEntry.COLUMN_RATING);
        if (rating != null && rating < 0 && rating > 5) {
            throw new IllegalArgumentException("Book requires valid rating");
        }

        //no need to check the author and date, any value is valid

        //if there are no values to update, then dont try to update the database
        if (values.size() == 0) {
            return 0;
        }

        //otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    //delete data from table
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            // entire table (maybe used in future)
            case BOOKS:
                //delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            //delete a single row given by the id in the URI
            case BOOK_ID:
                //selection provides the table we want the book from and "=?"
                // selectionArgs fill the space of the ? to get info from specific row
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        //if 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //return the number of rows deleted
        return rowsDeleted;
    }

    //returns the MIME type of data for the content URI
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
