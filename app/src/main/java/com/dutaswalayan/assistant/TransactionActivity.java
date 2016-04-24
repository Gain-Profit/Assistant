package com.dutaswalayan.assistant;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.dutaswalayan.assistant.common.db.TransactionContract;

public class TransactionActivity extends AppCompatActivity {
    TransactionContract.TransactionDatabase mDbTransaction;
    ListView list;
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
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
        SQLiteDatabase db = mDbTransaction.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM transaction",null);
    }
}
