package com.lukas.android.booklog;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.lukas.android.booklog.data.BookContract.BookEntry;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropTransformation;

public class ViewActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    //declare views as global variables
    TextView authorDisplayView;
    TextView statusDateDisplayView;
    RatingBar ratingDisplayView;
    ImageView thumbnailDisplayView;
    TextView notesDisplayView;
    FloatingActionButton shareFab;
    Toolbar toolbar;

    //declare values as global variables
    private String title;
    private int status;
    private int rating;
    private String author;
    private String notes;
    private String thumbnail;
    private String date;


    //content URI for chosen book
    private Uri mCurrentBookUri;

    //uique identifyer for book data loader
    private static final int EXISTING_BOOK_LOADER = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        //set title of appbar
        setTitle(title);

        //get info from previous activity
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        //find referance for the views
        authorDisplayView = findViewById(R.id.author_display);
        statusDateDisplayView = findViewById(R.id.status_date_display);
        ratingDisplayView = findViewById(R.id.rating_display);
        thumbnailDisplayView = findViewById(R.id.thumbnail_display);
        notesDisplayView = findViewById(R.id.notes_display);
        shareFab = findViewById(R.id.view_share_fab);

        //set custom toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            //enable up button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        //makes shareFAB clickable
        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message;
                //send different message depending on what status the book is in
                if (status == BookEntry.STATUS_TO_READ) {
                    message = createMessageToRead(title, author);
                } else if (status == BookEntry.STATUS_READING) {
                    message = createMessageReading(title, author, rating, notes);
                } else {
                    message = createMessageFinished(title, author, rating, date, notes);
                }
                //send intent to app that can handle text messages and populate it with the message
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(intent, "Share"));

            }
        });

        //start loader
        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
    }

    //create message String for books that are to read
    private String createMessageToRead(String title, String author) {
        String priceMessage = title + getString(R.string.by) + author + getString(R.string.next_book);
        return priceMessage;
    }

    //create message String for books that are reading
    private String createMessageReading(String title, String author, int rating, String notes) {
        String priceMessage = getString(R.string.currently_reading) + title + getString(R.string.by) + author + ".";
        priceMessage += "\n" + getString(R.string.would_give_rating) + rating + getString(R.string.rating_so_far);
        if (!notes.trim().isEmpty()) {
            priceMessage += "\n" + getString(R.string.my_notes);
            priceMessage += "\n" + notes;
        }
        return priceMessage;
    }

    //create message String for books that are finished
    private String createMessageFinished(String title, String author, int rating, String date, String notes) {
        String priceMessage;
        if (!date.trim().isEmpty()) {
            priceMessage = getString(R.string.i_read) + title + getString(R.string.by) + author + getString(R.string.during) + date + ".";
        } else {
            priceMessage = getString(R.string.have_read) + title + getString(R.string.by) + author + ".";
        }
        priceMessage += "\n" + getString(R.string.giving_rating) + rating + getString(R.string.final_rating);
        if (!notes.trim().isEmpty()) {
            priceMessage += "\n" + getString(R.string.my_notes);
            priceMessage += "\n" + notes;
        }
        return priceMessage;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //projection whith the columns that we want to read from
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_THUMBNAIL,
                BookEntry.COLUMN_TITLE,
                BookEntry.COLUMN_AUTHOR,
                BookEntry.COLUMN_DATE,
                BookEntry.COLUMN_STATUS,
                BookEntry.COLUMN_RATING,
                BookEntry.COLUMN_NOTES};

        //loader executes contentproviders query
        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //return early if the data cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        //proceed with moving to the first row of the cursor and reading data from it
        if (data.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int titleColumnIndex = data.getColumnIndex(BookEntry.COLUMN_TITLE);
            int authorColumnIndex = data.getColumnIndex(BookEntry.COLUMN_AUTHOR);
            int dateColumnIndex = data.getColumnIndex(BookEntry.COLUMN_DATE);
            int statusColumnIndex = data.getColumnIndex(BookEntry.COLUMN_STATUS);
            int ratingColumnIndex = data.getColumnIndex(BookEntry.COLUMN_RATING);
            int thumbnailColumnIndex = data.getColumnIndex(BookEntry.COLUMN_THUMBNAIL);
            int notesColumnIndex = data.getColumnIndex(BookEntry.COLUMN_NOTES);

            //extract out the value from the Cursor for the given column index
            title = data.getString(titleColumnIndex);
            author = data.getString(authorColumnIndex);
            date = data.getString(dateColumnIndex);
            status = data.getInt(statusColumnIndex);
            rating = data.getInt(ratingColumnIndex);
            thumbnail = data.getString(thumbnailColumnIndex);
            notes = data.getString(notesColumnIndex);

            //set unknown author if it is null
            if (TextUtils.isEmpty(author)) {
                author = getString(R.string.unknown_author);
            }

            //update the views on the screen with the values from the database
            authorDisplayView.setText(author);
            ratingDisplayView.setRating(rating);
            notesDisplayView.setText(notes);

            //only show date if book is finished, dont show rating if book is not started
            if (status == BookEntry.STATUS_FINISHED) {
                if (date.isEmpty()) {
                    statusDateDisplayView.setText(R.string.unknown_date);
                } else {
                    statusDateDisplayView.setText("Read during: " + date);
                }
            } else if (status == BookEntry.STATUS_TO_READ) {
                ratingDisplayView.setVisibility(View.INVISIBLE);
                statusDateDisplayView.setText(R.string.to_read_status);
            } else {
                statusDateDisplayView.setText(R.string.reading_status);
            }

            if (thumbnail == null) {
                //set defualt image if there is no url provided
                thumbnailDisplayView.setImageResource(R.drawable.defualt_book_big);
            } else {
                //dowload image and display it
                //modify URL so that biger image is displayed
                //crop so that you cet top of image
                Picasso.get()
                        .load(thumbnail.replace("zoom=5", "zoom=3").replace("edge=curl", ""))
                        .placeholder(R.drawable.defualt_book_big)
                        .transform(new CropTransformation(575, 575, CropTransformation.GravityHorizontal.CENTER,
                                CropTransformation.GravityVertical.TOP))
                        .into(thumbnailDisplayView);
            }
            //set title od toolbar
            setTitle(title);
            toolbar.setTitle(title);
            thumbnailDisplayView.setContentDescription(title);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //if the loader is invalidated, clear out all the data from the input fields
        authorDisplayView.setText("");
        statusDateDisplayView.setText("");
        ratingDisplayView.setRating(0);
        notesDisplayView.setText("");
    }

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    //if a menu option is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            //closes view activity so you start at book shelf
            finish();

            //creates intent to open maualactivity
            Intent openEdit = new Intent(ViewActivity.this, ManualActivity.class);
            //provides the intent so that the uri follows with it
            // Set the URI on the data field of the intent
            openEdit.setData(mCurrentBookUri);
            startActivity(openEdit);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}