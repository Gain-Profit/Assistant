package com.dutaswalayan.assistant.contract;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class FeedContract {
    private FeedContract(){
    }


    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.dutaswalayan.assistant";

    /**
     * Base URI. (content://com.dutaswalayan.assistant)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "product"-type resources..
     */
    private static final String PATH_PRODUCTS = "products";

    /**
     * Columns supported by "products" records.
     */
    public static class Product implements BaseColumns {
        /**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.assistant.products";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.assistant.product";

        /**
         * Fully qualified URI for "entry" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCTS).build();

        /**
         * Table name where records are stored for "product" resources.
         */
        public static final String TABLE_NAME = "product";
        /**
         * Atom ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_PRODUCT_ID = "product_id";
        /**
         * Product Description
         */
        public static final String COLUMN_DESCRIPTION = "description";
        /**
         * Unit for Product "PIECES, BOX, etc."
         */
        public static final String COLUMN_UNIT = "unit";
        /**
         * Price for Product.
         */
        public static final String COLUMN_PRICE = "price";
        /**
         * Barcode Product
         */
        public static final String COLUMN_BARCODE = "barcode";
        /**
         * last Updated product
         */
        public static final String COLUMN_UPDATED = "updated";
    }
}
