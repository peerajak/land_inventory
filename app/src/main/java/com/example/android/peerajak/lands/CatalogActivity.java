/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.peerajak.lands;

import android.content.ContentUris;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.net.Uri;
import android.widget.Toast;

import com.example.android.peerajak.lands.data.LandsContract;
import com.example.android.peerajak.lands.data.LandsContract.LandEntry;



/**
 * Displays list of lands that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    LandsDbhelper mDbHelper;
    private final int mLoaderManagerId=1;
    LandCursorAdapter mLandcursor_adapter;
    private String[] mProjection = {
            LandEntry._ID,
            LandEntry.COLUMN_LAND_NAME,
            LandEntry.COLUMN_LAND_DESC,
            LandEntry.COLUMN_LAND_PROVINCE,
            LandEntry.COLUMN_LAND_SIZE,
            LandEntry.COLUMN_LAND_LATITUDE,
            LandEntry.COLUMN_LAND_LONGITUDE,
            LandEntry.COLUMN_LAND_IMAGE

    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id){
            case mLoaderManagerId:
                return new CursorLoader(this, LandEntry.CONTENT_URI, mProjection,
                        null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mLandcursor_adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLandcursor_adapter.changeCursor(null);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        mDbHelper = new LandsDbhelper(this);
        //displayDatabaseInfo();
        mLandcursor_adapter = new LandCursorAdapter(this,null);
        ListView list_lands = (ListView) findViewById(R.id.list);
        list_lands.setAdapter(mLandcursor_adapter);
        getSupportLoaderManager().initLoader(mLoaderManagerId, null, this);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        list_lands.setEmptyView(emptyView);

        list_lands.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent editIntent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentLandUri = ContentUris.withAppendedId(LandEntry.CONTENT_URI,id);
                Log.i("CatalogActivity",currentLandUri.toString());
                editIntent.setData(currentLandUri);
                startActivity(editIntent);
            }
        });

    }

    private void insertLands(){


        // Create and/or open a database to read from it
        //SQLiteDatabase db = mDbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(LandEntry.COLUMN_LAND_NAME,"Lili");
        values.put(LandEntry.COLUMN_LAND_DESC,"Pom");
        values.put(LandEntry.COLUMN_LAND_PROVINCE,2);
        values.put(LandEntry.COLUMN_LAND_SIZE,4);
        values.put(LandEntry.COLUMN_LAND_LATITUDE,13.12);
        values.put(LandEntry.COLUMN_LAND_LONGITUDE,105.12);
        //values.put(LandEntry.COLUMN_LAND_IMAGE,mCurrentPhotoPath);
        Log.i("CatalogActivity","before add");
        Uri newUri = getContentResolver().insert(LandEntry.CONTENT_URI, values);
        Log.i("CatalogActivity","1 row added");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertLands();

                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllLands();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllLands() {
        int num_row_effect= getContentResolver().delete(LandEntry.CONTENT_URI, null, null);

        if(num_row_effect == 0) {
            Toast.makeText(this,"Error deletion",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,num_row_effect+" row deleted",Toast.LENGTH_SHORT).show();
            // Log.i("CatalogActivity","1 row added");
        }
    }
}
