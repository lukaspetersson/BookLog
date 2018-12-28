package com.lukas.android.booklog;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    //declare views as global variables
    private SearchView searchField;
    private ListView bookListView;
    private View emptyView;
    private ImageView emptyImage;
    private TextView emptyHeading;
    private TextView emptySubHeading;
    private ProgressBar progress;

    //create string with URL to books
    private static String BOOK_SEARCH_URL;

    //create adapter for the list of books
    private BookAdapter mAdapter;

    //create AsyncTask
    private BookAsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //take away elevation of appbar
        getSupportActionBar().setElevation(0);

        //set title of activity
        setTitle(R.string.search_for_book);

        //find reference in layout to the views
        searchField = findViewById(R.id.search_view);
        bookListView = findViewById(R.id.search_list);
        emptyView = findViewById(R.id.empty_view);
        emptyImage = findViewById(R.id.empty_image);
        emptyHeading = findViewById(R.id.empty_title_text);
        emptySubHeading = findViewById(R.id.empty_subtitle_text);
        progress = findViewById(R.id.loading_spinner);

        //make empty view act as a empty view
        bookListView.setEmptyView(emptyView);

        //create a new adapter that takes an empty list of books as input
        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        //set adapter to listview so that it can be provided with books
        bookListView.setAdapter(mAdapter);

        //hide empty view until action is performed
        emptyView.setVisibility(View.GONE);

        searchField.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //take away empty view during search
                emptyView.setVisibility(View.GONE);
                //do things when search is pressed
                //get a reference to the ConnectivityManager to check if there is internet
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                //get details on the currently active default data network
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                //if there is a network connection, get data
                if (networkInfo != null && networkInfo.isConnected()) {
                    //update url with user input string
                    BOOK_SEARCH_URL = "https://www.googleapis.com/books/v1/volumes?q=" + query;

                    //start async task to get data from server
                    task = new BookAsyncTask();
                    task.execute(BOOK_SEARCH_URL);

                    //enable empty view to appear if no search results were found
                    //find and set empty view on the ListView, so that it only shows when the list has 0 items.
                    progress.setVisibility(View.VISIBLE);

                    //update empty state so that it says that there is no books (instead of no internet)
                    NoBooks();
                } else {
                    //otherwise, display error
                    //first, hide loading indicator so error message will be visible
                    progress.setVisibility(View.GONE);
                    //update empty state with no connection error message (instead of no books)
                    NoInternet();
                    emptyView.setVisibility(View.VISIBLE);
                }

                return false;
            }

            //do things when text is changed
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // set an item click listener on the ListView, which sends an intent save data from selected book
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current book that was clicked on
                Book currentBook = mAdapter.getItem(position);

                String title = currentBook.getTitle();
                String author = currentBook.getAuthor();
                String thumbnail = currentBook.getThumbnail();

                //start intent to open maual activity
                //provide manual activity with info from search
                Intent openManual = new Intent(SearchActivity.this, ManualActivity.class);
                openManual.putExtra("title", title);
                openManual.putExtra("author", author);
                openManual.putExtra("thumbnail", thumbnail);
                startActivity(openManual);

                //close search
                finish();
            }
        });
    }

    //AsyncTask is chosen instead of loader so that book list is updated when new search is made
    private class BookAsyncTask extends AsyncTask<String, Void, List<Book>> {
        //runs on background thread so that app is not laggy while book is searched from
        @Override
        protected List<Book> doInBackground(String... urls) {
            //dont perform the request if there are no URLs, or the first URL is null
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }
            List<Book> result = QueryUtilsBook.fetchBookData(urls[0]);
            return result;
        }

        //runs on main thread
        @Override
        protected void onPostExecute(List<Book> data) {
            //take away loading bar when search is done
            progress.setVisibility(View.GONE);
            //make empty view visable
            emptyView.setVisibility(View.VISIBLE);
            //clear the adapter of previous data
            mAdapter.clear();
            // If there is a valid list of Books, then add them to the adapters data set, this will trigger the ListView to update.
            if (data != null && !data.isEmpty()) {
                mAdapter.addAll(data);
            }
        }
    }

    //called when empty state should say that no books could be found
    public void NoBooks() {
        emptyImage.setImageResource(R.drawable.baseline_search_black_48);
        emptyHeading.setText(R.string.empty_search_heading);
        emptySubHeading.setText(R.string.empty_search_subheading);
    }

    //called when empty state should say that there is no internet
    public void NoInternet() {
        emptyImage.setImageResource(R.drawable.baseline_error_black_48);
        emptyHeading.setText(R.string.no_connection_heading);
        emptySubHeading.setText(R.string.no_connection_subheading);
    }
}
