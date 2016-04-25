package com.dutaswalayan.assistant;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.dutaswalayan.assistant.common.db.TransactionContract;
import com.dutaswalayan.assistant.common.db.TransactionContract.Transaction;

public class TransactionActivity extends AppCompatActivity {
    private SimpleCursorAdapter mAdapter;
    TransactionContract.TransactionDatabase mDbTransaction;
    ListView list;

    private static final String[] PROJECTION = new String[]{
            Transaction._ID,
            Transaction.COLUMN_PRODUCT_ID,
            Transaction.COLUMN_DESCRIPTION,
            Transaction.COLUMN_UNIT,
            Transaction.COLUMN_PRICE,
            Transaction.COLUMN_BARCODE,
            Transaction.COLUMN_QTY};

    private static final String[] FROM_COLUMNS = new String[]{
            Transaction.COLUMN_DESCRIPTION,
            Transaction.COLUMN_PRICE
    };

    private static final int[] TO_FIELDS = new int[]{
            android.R.id.text1,
            android.R.id.text2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = mDbTransaction.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(Transaction.COLUMN_PRODUCT_ID, "123");
                values.put(Transaction.COLUMN_DESCRIPTION, "KOREK");
                values.put(Transaction.COLUMN_BARCODE, "123456789");
                values.put(Transaction.COLUMN_UNIT, "PIECES");
                values.put(Transaction.COLUMN_PRICE, 5000);
                values.put(Transaction.COLUMN_QTY, 1);

                long newRowId;
                newRowId = db.insert(
                        Transaction.TABLE_NAME,
                        null,
                        values);
                mAdapter.changeCursor(getAllData());

                Snackbar.make(view, "add data ID :"+ newRowId +"to database", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDbTransaction = new TransactionContract.TransactionDatabase(this);
        list = (ListView) findViewById(R.id.listTransaction);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter = new SimpleCursorAdapter(
                this,       // Current context
                android.R.layout.simple_list_item_activated_2,  // Layout for individual rows
                getAllData(),                // Cursor
                FROM_COLUMNS,        // Cursor columns to use
                TO_FIELDS,           // Layout fields to use
                0                    // No flags
        );
        list.setAdapter(mAdapter);
    }

    public Cursor getAllData() {
        SQLiteDatabase db = mDbTransaction.getReadableDatabase();
        Cursor c = db.query(Transaction.TABLE_NAME,
                this.PROJECTION,
                null,
                null,
                null,
                null,
                Transaction.COLUMN_DESCRIPTION + " asc");
        return c;
    }
}
