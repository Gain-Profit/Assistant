package com.dutaswalayan.assistant.common.db;

import android.provider.BaseColumns;

public class TransactionContract {
    private TransactionContract(){}

    public static class Transaction implements BaseColumns {
        public static final String TABLE_NAME = "transaction";
        public static final String COLUMN_PRODUCT_ID = "product_id";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_UNIT = "unit";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_BARCODE = "barcode";
        public static final String COLUMN_QTY = "qty";
    }
}
