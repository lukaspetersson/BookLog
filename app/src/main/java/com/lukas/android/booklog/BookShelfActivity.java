package com.lukas.android.booklog;

import android.animation.ObjectAnimator;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lukas.android.booklog.data.BookContract.BookEntry;


public class BookShelfActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //identifier for the book data loader
    private static final int BOOK_LOADER = 0;

    //adapter for the ListView
    BookCursorAdapter mCursorAdapter;

    //set up the FABs as global variables
    private FloatingActionButton searchFab;
    private FloatingActionButton manualFab;
    private FloatingActionButton scanFab;
    private FloatingActionButton mainFab;

    //set animation object as global variable
    private ObjectAnimator animationSearch;
    private ObjectAnimator animationManual;
    private ObjectAnimator animationScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);

        //TODO: add books to cloud when user is logged in and when new book is added.
        //TODO: read from cloud if user is authenticated else from sql
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        Log.v("BookShelfActivity", "HEHHEHEHEHEHHEHEHE"+user);

        //find the ListView which will be populated with the bookshelf
        ListView shelfListView = (ListView) findViewById(R.id.shelf_list);

        //find and set empty view on the ListView, so that it only shows when the list is empty
        View emptyView = findViewById(R.id.empty_view);
        shelfListView.setEmptyView(emptyView);

        //gives FABs and layouts context
        mainFab = findViewById(R.id.main_fab);
        //bring mainFab to front so that other FABs comes from underneath
        mainFab.bringToFront();
        manualFab = findViewById(R.id.manual_fab);
        searchFab = findViewById(R.id.search_fab);
        scanFab = findViewById(R.id.scan_fab);

        //makes distances of animation in density independant pixels to fit all screensizes
        float distance1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -62, getResources().getDisplayMetrics());
        float distance2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -118, getResources().getDisplayMetrics());
        float distance3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -174, getResources().getDisplayMetrics());
        //gives animationobjects objective
        animationSearch = ObjectAnimator.ofFloat(searchFab, "translationY", distance1);
        animationSearch.setDuration(300);
        animationManual = ObjectAnimator.ofFloat(manualFab, "translationY", distance2);
        animationManual.setDuration(300);
        animationScan = ObjectAnimator.ofFloat(scanFab, "translationY", distance3);
        animationScan.setDuration(300);

        //makes mainFab clickable
        mainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //test if FABs are already visable, only one is needed
                if (searchFab.getVisibility() == View.GONE) {
                    //makes other FABs apear when mainFab is clicked with a sliding animation upwards
                    animationSearch.start();
                    searchFab.show();
                    animationManual.start();
                    manualFab.show();
                    animationScan.start();
                    scanFab.show();
                } else {
                    //reverse animation if option FABs are visable
                    animationSearch.reverse();
                    searchFab.hide();
                    animationManual.reverse();
                    manualFab.hide();
                    animationScan.reverse();
                    scanFab.hide();
                }
            }
        });
        //makes searchFab clickable
        searchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //intent to open search activity
                Intent openSearch = new Intent(BookShelfActivity.this, SearchActivity.class);
                startActivity(openSearch);
                //hide option FABs
                manualFab.hide();
                searchFab.hide();
                scanFab.hide();
            }
        });
        //makes manualFab clickable
        manualFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //intent to open manual activity
                Intent openManual = new Intent(BookShelfActivity.this, ManualActivity.class);
                startActivity(openManual);
                //hide option FABs
                manualFab.hide();
                searchFab.hide();
                scanFab.hide();
            }
        });
        scanFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //intent to open scan activity
                Intent openScan = new Intent(BookShelfActivity.this, BarcodeScanningActivity.class);
                startActivity(openScan);
                //hide option FABs
                manualFab.hide();
                searchFab.hide();
                scanFab.hide();
            }
        });
        //setup an adapter to create a list item for each row of book data in the Cursor, if empty pass in null
        mCursorAdapter = new BookCursorAdapter(this, null);
        shelfListView.setAdapter(mCursorAdapter);


        //makes added books clickable
        shelfListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //hides option FABS
                searchFab.hide();
                manualFab.hide();
                scanFab.hide();

                //intent to open view activity
                Intent openView = new Intent(BookShelfActivity.this, ViewActivity.class);

                //provides the intent so that the uri follows with it so that activity know what book to show
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                //set the uri on the data field of the intent
                openView.setData(currentBookUri);
                startActivity(openView);
            }
        });
        //start loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);

        //TODO: load from firebase if authenticated
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //make sharedprefrences object to get info from settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        //selection and selection args determines which books apear
        String selection;
        String[] selectionArgs;
        String showStatus = sharedPrefs.getString(
                getString(R.string.settings_show_status_key),
                getString(R.string.settings_show_status_default));
        if (showStatus.equals("show all")) {
            //if show all is chosen set null so no books are neglected
            selection = null;
            selectionArgs = null;
        } else {
            selection = BookEntry.COLUMN_STATUS + "=?";
            selectionArgs = new String[]{showStatus};
        }

        //orderby determines the order the books apear
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        //projection determines which columns to include
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_TITLE,
                BookEntry.COLUMN_AUTHOR,
                BookEntry.COLUMN_RATING,
                BookEntry.COLUMN_STATUS,
                BookEntry.COLUMN_DATE,
                BookEntry.COLUMN_THUMBNAIL};

        //loader execute contentproviders query on background thread
        return new CursorLoader(this,
                BookEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //update BookCursorAdapter with this new cursor containing updated book data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //if menu option is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //intent to open setting activity
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
