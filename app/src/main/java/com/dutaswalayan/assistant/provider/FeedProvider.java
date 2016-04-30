/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dutaswalayan.assistant.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.dutaswalayan.assistant.common.db.SelectionBuilder;

public class FeedProvider extends ContentProvider {
    private static final String TAG = "FeedProvider";
    FeedDatabase mDatabaseHelper;

    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = FeedContract.CONTENT_AUTHORITY;

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /products
     */
    public static final int ROUTE_PRODUCTS = 1;

    /**
     * URI ID for route: /products/{ID}
     */
    public static final int ROUTE_PRODUCTS_ID = 2;

    public static final int SEARCH_SUGGEST = 3;
    private static final int REFRESH_SHORTCUT = 4;
    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "products", ROUTE_PRODUCTS);
        sUriMatcher.addURI(AUTHORITY, "products/*", ROUTE_PRODUCTS_ID);

        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,SEARCH_SUGGEST);
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

        /* The following are unused in this implementation, but if we include
         * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
         * could expect to receive refresh queries when a shortcutted suggestion is displayed in
         * Quick Search Box, in which case, the following Uris would be provided and we
         * would return a cursor with a single item representing the refreshed suggestion data.
         */
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);

    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new FeedDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_PRODUCTS:
                return FeedContract.Product.CONTENT_TYPE;
            case ROUTE_PRODUCTS_ID:
                return FeedContract.Product.CONTENT_ITEM_TYPE;
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new UnsupportedOperationException("uri tidak diketahui: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Log.i(TAG,"Uri: "+ uri);
        Cursor c;
        Context ctx;
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case SEARCH_SUGGEST:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                            "selectionArgs must be provided for the Uri: " + uri);
                }
                Log.i(TAG,"route suggest...");
                String[] columns = new String[] {
                        FeedContract.Product._ID,
                        FeedContract.Product.COLUMN_DESCRIPTION,
                        FeedContract.Product.COLUMN_PRICE};
                // Return all known entries.
                builder.table(FeedContract.Product.TABLE_NAME)
                        .where(FeedContract.Product.COLUMN_DESCRIPTION + " LIKE ?", "%"+ selectionArgs[0] + "%")
                        .map(FeedContract.Product._ID,FeedContract.Product._ID)
                        .map(FeedContract.Product.COLUMN_DESCRIPTION,SearchManager.SUGGEST_COLUMN_TEXT_1)
                        .map(FeedContract.Product.COLUMN_PRICE,SearchManager.SUGGEST_COLUMN_TEXT_2);

                c = builder.query(db, columns, sortOrder);
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_PRODUCTS_ID:
                Log.i(TAG,"route products id");
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(FeedContract.Product._ID + "=?", id);
            case ROUTE_PRODUCTS:
                Log.i(TAG,"route products");
                // Return all known entries.
                builder.table(FeedContract.Product.TABLE_NAME)
                        .where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_PRODUCTS:
                long id = db.insertOrThrow(FeedContract.Product.TABLE_NAME, null, values);
                result = Uri.parse(FeedContract.Product.CONTENT_URI + "/" + id);
                break;
            case ROUTE_PRODUCTS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_PRODUCTS:
                count = builder.table(FeedContract.Product.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_PRODUCTS_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(FeedContract.Product.TABLE_NAME)
                        .where(FeedContract.Product._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an etry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_PRODUCTS:
                count = builder.table(FeedContract.Product.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_PRODUCTS_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(FeedContract.Product.TABLE_NAME)
                        .where(FeedContract.Product._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * SQLite backend for @{link FeedProvider}.
     *
     * Provides access to an disk-backed, SQLite datastore which is utilized by FeedProvider. This
     * database should never be accessed by other parts of the application directly.
     */
    static class FeedDatabase extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 2;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "product.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";
        /** SQL statement to create "entry" table. */
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedContract.Product.TABLE_NAME + " (" +
                        FeedContract.Product._ID + " INTEGER PRIMARY KEY," +
                        FeedContract.Product.COLUMN_PRODUCT_ID + TYPE_TEXT + COMMA_SEP +
                        FeedContract.Product.COLUMN_DESCRIPTION    + TYPE_TEXT + COMMA_SEP +
                        FeedContract.Product.COLUMN_UNIT + TYPE_TEXT + COMMA_SEP +
                        FeedContract.Product.COLUMN_PRICE + TYPE_INTEGER + COMMA_SEP +
                        FeedContract.Product.COLUMN_BARCODE + TYPE_TEXT + COMMA_SEP +
                        FeedContract.Product.COLUMN_UPDATED + TYPE_TEXT + ")";

        /** SQL statement to drop "entry" table. */
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedContract.Product.TABLE_NAME;

        public FeedDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}