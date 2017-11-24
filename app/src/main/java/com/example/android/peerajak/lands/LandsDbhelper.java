package com.example.android.peerajak.lands;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.peerajak.lands.data.LandsContract.LandEntry;

/**
 * Created by peerajak on 11/15/17.
 */

public class LandsDbhelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lands.db";
    private static final int DATABASE_VERSION=1;

    public LandsDbhelper(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlCreateLandsTable = "CREATE TABLE "+ LandEntry.TABLE_NAME+"("+
        LandEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                LandEntry.COLUMN_LAND_NAME + " TEXT NOT NULL, "+
                LandEntry.COLUMN_LAND_SIZE + " INTEGER NOT NULL, "+
                LandEntry.COLUMN_LAND_PROVINCE+ " INTEGER NOT NULL DEFAULT 0, "+
                LandEntry.COLUMN_LAND_DESC + " TEXT,"+
                LandEntry.COLUMN_LAND_LATITUDE + " DOUBLE,"+
                LandEntry.COLUMN_LAND_LONGITUDE + " DOUBLE,"+
                LandEntry.COLUMN_LAND_PHONE + " TEXT NOT NULL,"+
                LandEntry.COLUMN_LAND_HOMEPRICE + " INTEGER NOT NULL ,"+
                //LandEntry.COLUMN_LAND_HOMEQUANTITY + " INTEGER NOT NULL,"+
                LandEntry.COLUMN_LAND_IMAGE + " TEXT);";
        Log.i("Dbhelper","just befor exec create db.8");
        db.execSQL(sqlCreateLandsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
