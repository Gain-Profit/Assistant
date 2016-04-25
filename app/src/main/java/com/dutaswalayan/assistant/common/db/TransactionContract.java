package com.dutaswalayan.assistant.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class TransactionContract {
    private TransactionContract(){}

    public static class Transaction implements BaseColumns {
        public static final String TABLE_NAME = "shopping";
        public static final String COLUMN_PRODUCT_ID = "product_id";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_UNIT = "unit";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_BARCODE = "barcode";
        public static final String COLUMN_QTY = "qty";
    }

    public static class TransactionDatabase extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 1;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "Belanja.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";
        /** SQL statement to create "transaction" table. */
        private static final String SQL_CREATE_TRANSACTION =
                "CREATE TABLE " + Transaction.TABLE_NAME + " (" +
                        Transaction._ID + " INTEGER PRIMARY KEY," +
                        Transaction.COLUMN_PRODUCT_ID + TYPE_TEXT + COMMA_SEP +
                        Transaction.COLUMN_DESCRIPTION    + TYPE_TEXT + COMMA_SEP +
                        Transaction.COLUMN_UNIT + TYPE_TEXT + COMMA_SEP +
                        Transaction.COLUMN_PRICE + TYPE_INTEGER + COMMA_SEP +
                        Transaction.COLUMN_BARCODE + TYPE_TEXT + COMMA_SEP +
                        Transaction.COLUMN_QTY + TYPE_INTEGER + ")";

        /** SQL statement to drop "entry" table. */
        private static final String SQL_DELETE_TRANSACTION =
                "DROP TABLE IF EXISTS " + Transaction.TABLE_NAME;

        public TransactionDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TRANSACTION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_TRANSACTION);
            onCreate(db);
        }
    }
}
