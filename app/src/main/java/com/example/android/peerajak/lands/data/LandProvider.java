package com.example.android.peerajak.lands.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import com.example.android.peerajak.lands.data.LandsContract.LandEntry;
import android.database.sqlite.SQLiteDatabase;
import com.example.android.peerajak.lands.LandsDbhelper;


import android.util.Log;

public class LandProvider extends ContentProvider {

    private LandsDbhelper mDbHelper;
    public static final String LOG_TAG = LandProvider.class.getSimpleName();
    private static final int LANDS = 100;
    private static final int LAND_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        sUriMatcher.addURI(com.example.android.peerajak.lands.data.LandsContract.CONTENT_AUTHORITY, com.example.android.peerajak.lands.data.LandsContract.PATH_LANDS,LANDS);
        sUriMatcher.addURI(com.example.android.peerajak.lands.data.LandsContract.CONTENT_AUTHORITY, com.example.android.peerajak.lands.data.LandsContract.PATH_LANDS+"/#",LAND_ID);
    }

    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a LandsDbHelper object to gain access to the pets database.
        mDbHelper = new LandsDbhelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case LANDS:
                cursor = database.query(LandEntry.TABLE_NAME,projection,selection,selectionArgs
                        ,null,null,sortOrder);
                break;
            case LAND_ID:
                selection = LandEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(LandEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LANDS:
                return insertLand(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertLand(Uri uri, ContentValues values) {
        Log.i(LOG_TAG,"Provider insertLand");
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        String name = values.getAsString(LandEntry.COLUMN_LAND_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Land requires a name");
        }
        Integer provincetype = values.getAsInteger(LandEntry.COLUMN_LAND_PROVINCE);
        if (provincetype == null || !LandEntry.isValidDrink(provincetype)) {
            throw new IllegalArgumentException("Land requires valid provincetype");
        }
        Integer weight = values.getAsInteger(LandEntry.COLUMN_LAND_SIZE);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Land requires valid weight");
        }
        long id = database.insert(LandEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        Log.i("LandProvider","update:"+uri.toString());
        switch (match) {
            case LANDS:
                return updateLand(uri, contentValues, selection, selectionArgs);
            case LAND_ID:
                selection = LandEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateLand(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }


    private int updateLand(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // TODO: Update the selected pets in the pets database table with the given ContentValues
        if (values.containsKey(LandEntry.COLUMN_LAND_NAME)) {
            String name = values.getAsString(LandEntry.COLUMN_LAND_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Land requires a name");
            }
        }
        if (values.containsKey(LandEntry.COLUMN_LAND_PROVINCE)) {
            Integer provincetype = values.getAsInteger(LandEntry.COLUMN_LAND_PROVINCE);
            if (provincetype == null || !LandEntry.isValidDrink(provincetype)) {
                throw new IllegalArgumentException("Land requires valid provincetype");
            }
        }
        if (values.containsKey(LandEntry.COLUMN_LAND_SIZE)) {

            Integer weight = values.getAsInteger(LandEntry.COLUMN_LAND_SIZE);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Land requires valid weight");
            }
        }

        if (values.size() == 0) {
            return 0;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        return database.update(LandEntry.TABLE_NAME,values,selection,selectionArgs);
    }




    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {



        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LANDS:
                return deleteLand(uri, selection, selectionArgs);
            case LAND_ID:
                selection = LandEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return deleteLand(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

    }
    private int deleteLand(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int num_deleted = database.delete(LandEntry.TABLE_NAME, selection, selectionArgs);
        if (num_deleted!=0)
            getContext().getContentResolver().notifyChange(uri,null);
        return num_deleted;

    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LANDS:
                return LandEntry.CONTENT_LIST_TYPE;
            case LAND_ID:
                return LandEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

