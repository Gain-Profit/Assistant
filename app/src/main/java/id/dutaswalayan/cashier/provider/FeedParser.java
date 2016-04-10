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

package id.dutaswalayan.cashier.provider;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FeedParser {

    public List<Product> parse(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readProductsArray(reader);
        } finally {
            reader.close();
        }
    }

    public List<Product> readProductsArray(JsonReader reader) throws IOException {
        List<Product> Products = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            Products.add(readProduct(reader));
        }
        reader.endArray();
        return Products;
    }

    public Product readProduct(JsonReader reader) throws IOException {
        String id = "";
        String description = "";
        String unit = "";
        long price = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("pid")) {
                id = reader.nextString();
            } else if (name.equals("description")) {
                description = reader.nextString();
            } else if (name.equals("unit")) {
                unit = reader.nextString();
            } else if (name.equals("price")) {
                price = reader.nextLong();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Product(id, description, unit, price);
    }

    public static class Product {
        public final String id;
        public final String description;
        public final String unit;
        public final long price;

        public Product(String id, String description, String unit, long price) {
            this.id = id;
            this.description = description;
            this.unit = unit;
            this.price = price;
        }
    }
}