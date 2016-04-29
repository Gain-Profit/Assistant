package com.dutaswalayan.assistant;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.dutaswalayan.assistant.common.db.TransactionContract;
import com.dutaswalayan.assistant.common.db.TransactionContract.Transaction;

public class TransactionActivity extends AppCompatActivity {
    private static final String TAG = "TransactionActivity";
    private SimpleCursorAdapter mAdapter;
    TransactionContract.TransactionDatabase mDbTransaction;
    ListView list;
    private TextView cartTotal;

    private static final String[] PROJECTION = new String[]{
            Transaction._ID,
            Transaction.COLUMN_PRODUCT_ID,
            Transaction.COLUMN_DESCRIPTION,
            Transaction.COLUMN_UNIT,
            Transaction.COLUMN_PRICE,
            Transaction.COLUMN_BARCODE,
            Transaction.COLUMN_QTY};

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_PRODUCT_ID = 1;
    private static final int COLUMN_DESCRIPTION = 2;
    private static final int COLUMN_UNIT = 3;
    private static final int COLUMN_PRICE = 4;
    private static final int COLUMN_BARCODE = 5;
    private static final int COLUMN_QTY = 6;

    private static final String[] FROM_COLUMNS = new String[]{
            Transaction._ID,
            Transaction.COLUMN_DESCRIPTION,
            Transaction.COLUMN_UNIT,
            Transaction.COLUMN_PRICE,
            Transaction.COLUMN_QTY,
            Transaction.COLUMN_PRICE
    };

    private static final int[] TO_FIELDS = new int[]{
            R.id.product_delete,
            R.id.product_name,
            R.id.product_unit_value,
            R.id.product_price_value,
            R.id.product_qty,
            R.id.product_total_value
    };

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
                values.put(Transaction.COLUMN_QTY, 5);

                long insertRowId;
                insertRowId = db.insert(
                        Transaction.TABLE_NAME,
                        null,
                        values);
                mAdapter.changeCursor(getAllData());

                Snackbar.make(view, "add data ID :" + insertRowId + " to database", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDbTransaction = new TransactionContract.TransactionDatabase(this);
        list = (ListView) findViewById(R.id.listTransaction);

        cartTotal = (TextView) findViewById(R.id.cartTotal);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter = new SimpleCursorAdapter(
                this,       // Current context
                R.layout.custom_list_transaction,  // Layout for individual rows
                getAllData(),                // Cursor
                FROM_COLUMNS,        // Cursor columns to use
                TO_FIELDS,           // Layout fields to use
                0                    // No flags
        );

        mAdapter.setViewBinder(new TransactionBinder());

        list.setAdapter(mAdapter);
    }

    public Cursor getAllData() {
        SQLiteDatabase db = mDbTransaction.getReadableDatabase();
        Cursor cTotal = db.rawQuery("SELECT SUM(" + Transaction.COLUMN_QTY +" * " +
                Transaction.COLUMN_PRICE + ") FROM " + Transaction.TABLE_NAME + ";",null);
        cTotal.moveToFirst();

        cartTotal.setText("Rp. " + String.format("%,d",cTotal.getLong(0)));

        Cursor c = db.query(Transaction.TABLE_NAME,
                this.PROJECTION,
                null,
                null,
                null,
                null,
                Transaction.COLUMN_DESCRIPTION + " asc");
        return c;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transaction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_clear_transaction) {
            SQLiteDatabase db = mDbTransaction.getWritableDatabase();
            long deleted = db.delete(
                    Transaction.TABLE_NAME,
                    null,
                    null);
            mAdapter.changeCursor(getAllData());

            Snackbar.make(getWindow().getDecorView(),
                    deleted + " Record Has Been Deleted", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private class TransactionBinder implements SimpleCursorAdapter.ViewBinder{

        @Override
        public boolean setViewValue(View view, Cursor cursor, int i) {
            final String id = cursor.getString(COLUMN_ID);
            if (view.getId() == R.id.product_delete){
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteProduct(id);
                    }
                });
                return true;
            } else if (view.getId() == R.id.product_price_value){
                ((TextView) view).setText("Rp. " + String.format("%,d",cursor.getLong(COLUMN_PRICE)));
                return true;
            } else if (view.getId() == R.id.product_total_value){
                Long total = cursor.getLong(COLUMN_PRICE) * cursor.getLong(COLUMN_QTY);
                ((TextView) view).setText("Rp. " + String.format("%,d",total));
                return true;
            } else if (view.getId() == R.id.product_qty){
                final int value = cursor.getInt(COLUMN_QTY);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showNumberPicker(v, id, value);
                    }
                });
                return false;
            } else {
                return false;
            }
        }
    }

    private void deleteProduct(String id) {
        SQLiteDatabase db = mDbTransaction.getWritableDatabase();

        db.delete(Transaction.TABLE_NAME,
                Transaction._ID + " = ?",
                new String[]{id});

        mAdapter.changeCursor(getAllData());

        Log.i(TAG,"delete data for id: "+ id);
    }

    private void showNumberPicker(final View btnView, final String id, int oldValue) {
        final Dialog d = new Dialog(TransactionActivity.this);
        d.setTitle("Change Qty");
        d.setContentView(R.layout.number_picker);
        Button b1 = (Button) d.findViewById(R.id.btnSet);
        Button b2 = (Button) d.findViewById(R.id.btnCancel);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);
        np.setMaxValue(100);
        np.setMinValue(1);
        np.setValue(oldValue);
        np.setWrapSelectorWheel(false);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                ((Button) btnView).setText(String.valueOf(np.getValue()));
                changeQty(id,np.getValue());
                Log.i(TAG,"value picker is: " + np.getValue());
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private void changeQty(String id, int value) {
        SQLiteDatabase db = mDbTransaction.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Transaction.COLUMN_QTY, value);

        db.update(Transaction.TABLE_NAME,
                values,
                Transaction._ID + " = ?",
                new String[]{id});

        mAdapter.changeCursor(getAllData());

        Log.i(TAG,"change qty for id: "+ id +" to: "+ value);
    }
}
