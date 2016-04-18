package com.dutaswalayan.assistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailProduct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);

        TextView tvPid = (TextView) findViewById(R.id.data_pid);
        TextView tvDescription = (TextView) findViewById(R.id.data_description);
        TextView tvBarcode = (TextView) findViewById(R.id.data_barcode);
        TextView tvUnit = (TextView) findViewById(R.id.data_unit);
        TextView tvPrice = (TextView) findViewById(R.id.data_price);
        TextView tvUpdated = (TextView) findViewById(R.id.data_updated);

        Intent intent = getIntent();
        String pid = intent.getStringExtra("Products.PID");
        String description = intent.getStringExtra("Products.DESCRIPTION");
        String barcode = intent.getStringExtra("Products.BARCODE");
        String unit = intent.getStringExtra("Products.UNIT");
        Long price = intent.getLongExtra("Products.PRICE",0);
        String updated = intent.getStringExtra("Products.UPDATED");

        tvPid.setText(pid);
        tvDescription.setText(description);
        tvBarcode.setText(barcode);
        tvUnit.setText(unit);
        tvPrice.setText(String.format("%,d",price));
        tvUpdated.setText(updated);
    }
}
