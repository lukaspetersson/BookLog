package com.lukas.android.booklog;

public class Book {

    //title of book
    private String mTitle;

    //author of book
    private String mAuthor;

    //thumbnail of book
    private String mThumbnail;

    //make Book object
    public Book(String title, String author, String thumbnail) {
        mTitle = title;
        mAuthor = author;
        mThumbnail = thumbnail;
    }




    //returns the title of book.
    public String getTitle() {
        return mTitle;
    }

    //returns the author of book.
    public String getAuthor() {
        return mAuthor;
    }

    //returns the thumbnail link of book.
    public String getThumbnail() {
        return mThumbnail;
    }

}
