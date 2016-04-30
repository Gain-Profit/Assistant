package com.dutaswalayan.assistant;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.dutaswalayan.assistant.common.db.TransactionContract;
import com.dutaswalayan.assistant.common.db.TransactionContract.Transaction;
import com.dutaswalayan.assistant.provider.FeedContract;

public class SearchableProduct extends AppCompatActivity {
    private static final String TAG = "SearchableProduct";
    ListView mListViewSearch;
    TransactionContract.TransactionDatabase mDbTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable_product);
        mListViewSearch = (ListView) findViewById(R.id.lvSearchProduct);
        mDbTransaction = new TransactionContract.TransactionDatabase(this);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Because this activity has set launchMode="singleTop", the system calls this method
        // to deliver the intent if this activity is currently the foreground activity when
        // invoked again (when the user executes a search from this activity, we don't create
        // a new instance of this activity, so the system delivers the search intent here)
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.i(TAG,"action view");
            // handles a click on a search suggestion; launches activity to show word
//            Intent wordIntent = new Intent(this, TransactionActivity.class);
//            wordIntent.setData(intent.getData());
//            startActivity(wordIntent);
            Uri uri = intent.getData();
            Log.i(TAG,"uri: " + uri);
            Cursor cursor = managedQuery(uri, null, null, null, null);

            if (cursor == null) {
                finish();
            } else {
                cursor.moveToFirst();

                SQLiteDatabase db = mDbTransaction.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(Transaction.COLUMN_PRODUCT_ID, cursor.getString(MainActivity.COLUMN_PRODUCT_ID));
                values.put(Transaction.COLUMN_DESCRIPTION, cursor.getString(MainActivity.COLUMN_DESCRIPTION));
                values.put(Transaction.COLUMN_BARCODE, cursor.getString(MainActivity.COLUMN_BARCODE));
                values.put(Transaction.COLUMN_UNIT, cursor.getString(MainActivity.COLUMN_UNIT));
                values.put(Transaction.COLUMN_PRICE, cursor.getLong(MainActivity.COLUMN_PRICE));
                values.put(Transaction.COLUMN_QTY, 1);

                db.insert(Transaction.TABLE_NAME,null,values);
                Log.i(TAG,"insert: " + values);
                Intent wordIntent = new Intent(this, TransactionActivity.class);
                wordIntent.putExtra("DATA_INSERTED",values.getAsString(Transaction.COLUMN_DESCRIPTION));
                startActivity(wordIntent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchProduct).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchProduct:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }
}
