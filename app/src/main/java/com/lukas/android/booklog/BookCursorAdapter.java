package com.lukas.android.booklog;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.lukas.android.booklog.data.BookContract.BookEntry;
import com.squareup.picasso.Picasso;

//makes BookCursorAdapter, an ArrayAdapter provided with the Book object created
//displayed in the book shelf activity
public class BookCursorAdapter extends CursorAdapter {

    //create BookCursorAdapter object
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    //create blank list item to fill info with
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //inflate a list item view using the layout specified in added_book.xml
        return LayoutInflater.from(context).inflate(R.layout.added_book, parent, false);
    }

    //takes the bookdata in the row that the cursor is pointing at and puts it in the list item
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //find individual views that we want to modify in the list item layout
        TextView titleTextView = (TextView) view.findViewById(R.id.added_title);
        TextView authorTextView = (TextView) view.findViewById(R.id.added_author);
        TextView statusTextView = (TextView) view.findViewById(R.id.added_status);
        RatingBar ratingBarView = (RatingBar) view.findViewById(R.id.rating_display);
        ImageView thumbnailView = (ImageView) view.findViewById(R.id.added_thumbnail);

        //find the columns of book attributes that we are interested in
        int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_TITLE);
        int authorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_AUTHOR);
        int statusColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_STATUS);
        int ratingColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_RATING);
        int thumbnailColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_THUMBNAIL);
        int dateColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_DATE);

        //read the book attributes from the Cursor for the current book
        String bookTitle = cursor.getString(titleColumnIndex);
        String bookAuthor = cursor.getString(authorColumnIndex);
        int bookStatus = cursor.getInt(statusColumnIndex);
        int bookRating = cursor.getInt(ratingColumnIndex);
        String bookThumbnail = cursor.getString(thumbnailColumnIndex);
        String bookDate = cursor.getString(dateColumnIndex);

        //if the author is empty string
        if (TextUtils.isEmpty(bookAuthor)) {
            bookAuthor = context.getString(R.string.unknown_author);
        }
        // If the date is empty string
        if (TextUtils.isEmpty(bookDate)) {
            bookDate = context.getString(R.string.unknown_date);
        }

        //if book is finished, hide status and add date instead
        if (bookStatus == BookEntry.STATUS_FINISHED) {
            statusTextView.setText(bookDate);
            ratingBarView.setVisibility(View.VISIBLE);
        } else if (bookStatus == BookEntry.STATUS_READING) {
            statusTextView.setText(R.string.reading_status);
            ratingBarView.setVisibility(View.VISIBLE);
        } else if (bookStatus == BookEntry.STATUS_TO_READ) {
            statusTextView.setText(R.string.to_read_status);
            ratingBarView.setVisibility(View.GONE);
        }

        //update rest of the Views with the attributes for the current book
        titleTextView.setText(bookTitle);
        authorTextView.setText(bookAuthor);
        ratingBarView.setRating(bookRating);

        if (bookThumbnail == null) {
            //put defualt image if URL is not provided
            thumbnailView.setImageResource(R.drawable.defualt_book_big);
        } else {
            //dowload image and display it
            //modify URL so that bigger image is displayed and the curl is taken away
            Picasso.get()
                    .load(bookThumbnail.replace("zoom=5", "zoom=2").replace("edge=curl&", ""))
                    .placeholder(R.drawable.defualt_book_small)
                    .into(thumbnailView);
        }
    }

}