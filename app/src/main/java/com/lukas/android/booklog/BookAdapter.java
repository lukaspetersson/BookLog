package com.lukas.android.booklog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

//makes BookAdapter, an ArrayAdapter provided with the Book object created
//displayed in the search activity
public class BookAdapter extends ArrayAdapter<Book> {

    //create BookAdapter object
    public BookAdapter(Context context, ArrayList<Book> words) {
        super(context, 0, words);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_search_result, parent, false);
        }

        //get the Book object located at this position in the list
        Book currentBook = getItem(position);

        //find the TextView with view id title
        TextView titleView = (TextView) listItemView.findViewById(R.id.title);
        //display the title of the current book in that TextView
        titleView.setText(currentBook.getTitle());

        //find the TextView with view id title
        TextView authorView = (TextView) listItemView.findViewById(R.id.author);
        //display the author of the current book in that TextView
        authorView.setText(currentBook.getAuthor());

        ImageView thumbnailView = listItemView.findViewById(R.id.thumbnail_display);

        if (currentBook.getThumbnail() == null) {
            //set defualt image if no URL is provided
            thumbnailView.setImageResource(R.drawable.defualt_book_big);
        } else {
            //dowload image and display it in the view
            //edit URL so that image is bigger and curl taken away
            Picasso.get()
                    .load(currentBook.getThumbnail().replace("zoom=5", "zoom=2").replace("edge=curl&", ""))
                    .placeholder(R.drawable.defualt_book_small)
                    .into(thumbnailView);
        }

        //return the whole list item layout so that it can be shown in the ListView
        return listItemView;
    }
}

