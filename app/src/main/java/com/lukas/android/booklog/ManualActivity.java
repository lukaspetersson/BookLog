package com.lukas.android.booklog;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.lukas.android.booklog.data.BookContract.BookEntry;

public class ManualActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //declare views as global variables
    private Spinner mStatusSpinner;
    private RatingBar mRatingBar;
    private EditText mDateView;
    private EditText mTitleView;
    private EditText mAuthorView;
    private EditText mNotesView;

    //declare values from user input
    private int mStatus = BookEntry.STATUS_FINISHED;
    private int mRating;
    private String mDate;
    private String mTitle;
    private String mAuthor;
    private String mNotes;
    private String mThumbnail;

    //uri for the book that is edited, or null if it is a new book
    private Uri mCurrentBookUri;
    //unique identifier for the book data loader
    private static final int EXISTING_BOOK_LOADER = 0;

    //prevent saving when exiting activity normaly
    private boolean normalExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        //find all relevant views to read input from
        mStatusSpinner = (Spinner) findViewById(R.id.status_edit);
        mDateView = (EditText) findViewById(R.id.date_edit);
        mRatingBar = (RatingBar) findViewById(R.id.rating_bar);
        mTitleView = (EditText) findViewById(R.id.title_edit);
        mAuthorView = (EditText) findViewById(R.id.author_edit);
        mNotesView = (EditText) findViewById(R.id.notes_edit);

        //get info from the intent that opend manual activity
        //so that we know if it is a new book or if we are editing a existing
        //we also get info from search activity (if provided)
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        //check if it is new book
        if (mCurrentBookUri == null) {
            setTitle(getString(R.string.add_book));

            //set view with info from the search activity and import the thumbnail
            mTitleView.setText(intent.getStringExtra("title"));
            mAuthorView.setText(intent.getStringExtra("author"));
            mThumbnail = intent.getStringExtra("thumbnail");

            normalExit = false;

            //hide delete option
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_book));
            //display current values
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }
        //set up spinner and ratingbar to get the rating and status
        setupRating();
        setupSpinner();
    }

    private void saveBook() {
        //read from input fields, trim to take away blank spaces
        mDate = mDateView.getText().toString().trim();
        mTitle = mTitleView.getText().toString().trim();
        mAuthor = mAuthorView.getText().toString().trim();
        mNotes = mNotesView.getText().toString().trim();

        //returns early and does not save book if there is not title
        if (mTitle.isEmpty()) {
            //only show toast if it is an normal exit
            if(normalExit){
                Toast.makeText(this, getString(R.string.no_title_error),
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }

        //create contentvalues to put values in correct column in database
        ContentValues insertValues = new ContentValues();
        insertValues.put(BookEntry.COLUMN_TITLE, mTitle);
        insertValues.put(BookEntry.COLUMN_AUTHOR, mAuthor);
        insertValues.put(BookEntry.COLUMN_DATE, mDate);
        insertValues.put(BookEntry.COLUMN_STATUS, mStatus);
        insertValues.put(BookEntry.COLUMN_RATING, mRating);
        insertValues.put(BookEntry.COLUMN_THUMBNAIL, mThumbnail);
        insertValues.put(BookEntry.COLUMN_NOTES, mNotes);


        //check if new book
        if (mCurrentBookUri == null) {
            //insert pet into database
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, insertValues);

            //show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                //if the new content URI is null, then there was an error with insertion
                Toast.makeText(this, getString(R.string.insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //otherwise, the insertion was successful
                Toast.makeText(this, getString(R.string.insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            //existing book, update with new contentvalues
            int rowsAffected = getContentResolver().update(mCurrentBookUri, insertValues, null, null);

            //show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                //if no rows were affected, then there was an error with the update
                Toast.makeText(this, getString(R.string.update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //otherwise, the update was successful
                Toast.makeText(this, getString(R.string.update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        //exit activity when update/insertion is compleat
        finish();
    }

    //read the rating input
    private void setupRating() {
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean b) {
                mRating = (int) rating;
            }
        });
    }

    //read the status input
    private void setupSpinner() {
        //create spinner with string array as options
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_status_options, android.R.layout.simple_spinner_item);

        //set dropdown style
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //apply the adapter to the spinner
        mStatusSpinner.setAdapter(genderSpinnerAdapter);

        //set the integer mStatus to the constant values
        mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //get the option that the user picked
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.to_read_status))) {
                        mStatus = BookEntry.STATUS_TO_READ;
                    } else if (selection.equals(getString(R.string.reading_status))) {
                        mStatus = BookEntry.STATUS_READING;
                    } else {
                        mStatus = BookEntry.STATUS_FINISHED;
                    }
                }
            }

            //defualt status is set
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mStatus = BookEntry.STATUS_TO_READ;
            }
        });
    }

    //create option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //adds menu layout to appbar
        getMenuInflater().inflate(R.menu.menu_option, menu);
        return true;
    }

    //called if it is a new book
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //hides delete option
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        normalExit=true;
        //option in appbar is clicked
        switch (item.getItemId()) {
            //save is clicked
            case R.id.action_save:
                // Save book to database
                saveBook();
                return true;
            //delete is clicked
            case R.id.action_delete:
                //pop up dialog so that user have to confirm deletion
                showDeleteConfirmationDialog();
                return true;
            //up button is clicked
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(ManualActivity.this);
        }
        return super.onOptionsItemSelected(item);
    }

    //create delete dialog
    private void showDeleteConfirmationDialog() {
        //create alertdialog with message, positive and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //user clicked the "Delete" button
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //user clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //create and show the alertdialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //preform deletion of book
    private void deleteBook() {
        //only preform deletion if it is an exisiting book
        if (mCurrentBookUri != null) {

            //deletes the book with given URI
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            //show a toast message depending on whether or not the delete was successful
            if (rowsDeleted == 0) {
                //if no rows were deleted, then there was an error with the delete
                Toast.makeText(this, getString(R.string.delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //otherwise, the delete was successful and we can display a toast
                Toast.makeText(this, getString(R.string.delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        //close the activity when deletion is done
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //if it is not an normal exit, save book
        if(!normalExit){
            saveBook();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //projection with included columns
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_THUMBNAIL,
                BookEntry.COLUMN_TITLE,
                BookEntry.COLUMN_AUTHOR,
                BookEntry.COLUMN_DATE,
                BookEntry.COLUMN_STATUS,
                BookEntry.COLUMN_RATING,
                BookEntry.COLUMN_NOTES};

        //loader execute contentproviders query on background thread
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
            //find the columns of book attributes that we are interested in
            int titleColumnIndex = data.getColumnIndex(BookEntry.COLUMN_TITLE);
            int authorColumnIndex = data.getColumnIndex(BookEntry.COLUMN_AUTHOR);
            int dateColumnIndex = data.getColumnIndex(BookEntry.COLUMN_DATE);
            int statusColumnIndex = data.getColumnIndex(BookEntry.COLUMN_STATUS);
            int ratingColumnIndex = data.getColumnIndex(BookEntry.COLUMN_RATING);
            int thumbnailColumnIndex = data.getColumnIndex(BookEntry.COLUMN_THUMBNAIL);
            int notesColumnIndex = data.getColumnIndex(BookEntry.COLUMN_NOTES);

            //extract out the value from the Cursor for the given column index
            String title = data.getString(titleColumnIndex);
            String author = data.getString(authorColumnIndex);
            String date = data.getString(dateColumnIndex);
            int status = data.getInt(statusColumnIndex);
            int rating = data.getInt(ratingColumnIndex);
            String thumbnail = data.getString(thumbnailColumnIndex);
            String notes = data.getString(notesColumnIndex);

            //update the views on the screen with the values from the database
            mTitleView.setText(title);
            mAuthorView.setText(author);
            mDateView.setText(date);
            mRatingBar.setRating(rating);
            mNotesView.setText(notes);
            mThumbnail = thumbnail;

            //set status of the book to current selection in spinner
            switch (status) {
                case BookEntry.STATUS_TO_READ:
                    mStatusSpinner.setSelection(BookEntry.STATUS_TO_READ);
                    break;
                case BookEntry.STATUS_READING:
                    mStatusSpinner.setSelection(BookEntry.STATUS_READING);
                    break;
                default:
                    mStatusSpinner.setSelection(BookEntry.STATUS_FINISHED);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //if the loader is invalidated, clear out all the data from the input fields
        mTitleView.setText("");
        mAuthorView.setText("");
        mDateView.setText("");
        mRatingBar.setRating(0);
        mNotesView.setText("");
        mStatusSpinner.setSelection(BookEntry.STATUS_TO_READ);
    }
}
