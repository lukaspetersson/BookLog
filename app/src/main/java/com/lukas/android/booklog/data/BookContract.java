package com.lukas.android.booklog.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class BookContract {


    //Content authority is a name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.books";

    //use Content authority to create the base of all URI's which apps will use to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //URI´s path
    public static final String PATH_BOOKS = "books";

    // empty constructor to prevent someone from accidentally instantiating the contract class
    private BookContract() {
    }

    public static abstract class BookEntry implements BaseColumns {

        //The MIME type of the for a list of books
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        //The MIME type of the for a single book
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // access book data in provider with the content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        //column names for the book table
        public final static String TABLE_NAME = "books";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_TITLE = "title";
        public final static String COLUMN_AUTHOR = "author";
        public final static String COLUMN_DATE = "date";
        public final static String COLUMN_STATUS = "status";
        public final static String COLUMN_RATING = "rating";
        public final static String COLUMN_THUMBNAIL = "thumbnail";
        public final static String COLUMN_NOTES = "notes";


        //Possible values for the status of book
        public static final int STATUS_TO_READ = 0;
        public static final int STATUS_READING = 1;
        public static final int STATUS_FINISHED = 2;

        //returns whether the status is either of the options
        public static boolean isValidStatus(int status) {
            if (status == STATUS_TO_READ || status == STATUS_READING || status == STATUS_FINISHED) {
                return true;
            }
            return false;
        }

    }
}
