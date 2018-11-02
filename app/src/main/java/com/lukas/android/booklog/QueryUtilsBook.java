package com.lukas.android.booklog;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public final class QueryUtilsBook {

    //tag for log messages
    private static final String LOG_TAG = QueryUtilsBook.class.getSimpleName();

    //object instant of QuerryUtilsBooks is not needed bacause it is only  meant to hold static variables and mathods
    private QueryUtilsBook() {
    }

    //query google dataset and return list of book objects
    public static List<Book> fetchBookData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        //perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        //extract relevant fields from the JSON response and create a list of {@link Book}s
        List<Book> books = extractFeatureFromJson(jsonResponse);

        //return the list of books
        return books;
    }

    //creates url from String url
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }


    //Make an HTTP request to the given URL and return a String as the response
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        //if the URL is null, then return early
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            //we only want to get data from url
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //if the request was successful (response code 200), then get the info
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {

                inputStream.close();
            }
        }
        return jsonResponse;
    }


    //convert the inputstream into a string of the JSON response of the server
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    //return a list of book objects that has been built up from the JSON response
    private static List<Book> extractFeatureFromJson(String bookJSON) {
        //if the JSON string is empty or null, return early
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // create an empty ArrayList that we can start adding books to
        List<Book> books = new ArrayList<>();

        //try to get the JSON respons if no, throw it so app does not crash
        try {
            //create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            //get JSONArray assosiated with key "items"
            JSONArray bookArray = baseJsonResponse.getJSONArray("items");

            //for each book in "items"
            for (int i = 0; i < bookArray.length(); i++) {
                //get a single book at position "i" within the list of books
                JSONObject currentBook = bookArray.getJSONObject(i);

                //get JSONObject assosiated with key "volumeInfo"
                JSONObject info = currentBook.getJSONObject("volumeInfo");

                //get value assosiated with key "title"
                String bookTitle = info.getString("title");

                //get array of authors assosiated with key "authors"
                JSONArray authorArray = info.getJSONArray("authors");
                //look for 1st author (in allmost all cases the 1st is the only relevant author)
                String bookAuthor = authorArray.getString(0);

                String bookThumbnail = null;
                //check if image is provided
                if (info.has("imageLinks")) {
                    //get JSONObject assosiated with key "imageLinks"
                    JSONObject imageObjects = info.getJSONObject("imageLinks");
                    //get value assosiated with key "smallThumbnail"
                    bookThumbnail = imageObjects.getString("smallThumbnail");
                }

                //create Book object with the gathered information and add it to list of books
                Book book = new Book(bookTitle, bookAuthor, bookThumbnail);
                books.add(book);
            }

        } catch (JSONException e) {
            //if there was error in try block, catch error so app does not crash and print message
            Log.e("QueryUtilsBook", "Problem parsing the book JSON results", e);
        }

        //return list of books
        return books;
    }
}
