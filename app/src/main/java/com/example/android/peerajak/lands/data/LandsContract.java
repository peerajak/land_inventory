package com.example.android.peerajak.lands.data;

import android.net.Uri;
import android.provider.BaseColumns;
import android.content.ContentResolver;
/**
 * Created by peerajak on 11/15/17.
 */

public final class LandsContract {

    private LandsContract(){}//An empty private constructor makes sure that the class is not going to be initialised.
    public static final String CONTENT_AUTHORITY = "com.example.android.peerajak.lands";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_LANDS = "lands";


    public static final class LandEntry implements BaseColumns {

        public static final String TABLE_NAME = "lands";
        public static final String COLUMN_LAND_NAME = "name";
        public static final String COLUMN_LAND_SIZE = "size";
        public static final String COLUMN_LAND_PROVINCE = "province";
        public static final String COLUMN_LAND_DESC = "description";
        public static final String COLUMN_LAND_IMAGE = "photo";
        public static final String COLUMN_LAND_LATITUDE = "latitude";
        public static final String COLUMN_LAND_LONGITUDE = "longitude";
        public static final String COLUMN_LAND_PHONE = "phone";
        public static final String COLUMN_LAND_HOMEPRICE = "homeprice";
        public static final String COLUMN_LAND_HOMEQUANTITY = "honequantity";

        public static final int PROVINCE_AROUNDBANGKOK = 1;
        public static final int PROVINCE_THREEHOURS = 2;
        public static final int PROVINCE_FARAWAY = 0;


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_LANDS);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LANDS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LANDS;


        public static boolean isValidDrink(int drinktype) {
            if (drinktype == PROVINCE_AROUNDBANGKOK || drinktype == PROVINCE_THREEHOURS || drinktype == PROVINCE_FARAWAY) {
                return true;
            }
            return false;
        }
    }

}

