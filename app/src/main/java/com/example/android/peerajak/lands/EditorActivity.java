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
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.example.android.peerajak.lands.data.LandsContract;
import com.example.android.peerajak.lands.data.LandsContract.LandEntry;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Allows user to create a new land or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> ,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener , LocationListener{
    private LandsDbhelper mDbHelper;
    /** EditText field to enter the land's name */
    private EditText mNameEditText;

    /** EditText field to enter the land's breed */
    private EditText mDescEditText;
    private int mCurrentId=0;
    private final int start_home_quantity=0;
    /** EditText field to enter the land's weight */
    private EditText mSizeEditText;
    private int mQuantity=0;
    /** EditText field to enter the land's province */
    private Spinner mProvinceSpinner;
    private final int mLoaderManagerId=1;
    private Uri mCurrentLandUri=null;
    private boolean mIsNewInsert;
    private boolean mLandHasChanged = false;
    private ImageView mImageView;
    private Button mTakePhoto;
    private EditText mPhoneEditText;
    private EditText mHomePrice;
    private TextView mQuantityTextView;
    /**
     * Gender of the land. The possible values are:
     * 0 for unknown province, 1 for male, 2 for female.
     */
    private int mProvince = 0;
    String mCurrentPhotoPath;

    double mLatitude=0;
    double mLongitude=0;
    private GoogleApiClient googleApiClient;
    Location mCurrentLocation=null;
    
    
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent catalogIntent = getIntent();
        mCurrentLandUri = catalogIntent.getData();
        mTakePhoto = (Button) findViewById(R.id.takephoto);
        if(mCurrentLandUri==null)
        {
            setTitle(R.string.editor_activity_title_new_land);
            Log.i("CatalogActivity","intent add");
            mIsNewInsert = true;
            setTitle(getString(R.string.editor_activity_title_new_land));
            invalidateOptionsMenu();
        }else{
            setTitle(R.string.editor_activity_title_edit_land);
            //mTakePhoto.setVisibility(View.GONE);
            Log.i("EditorActivity",mCurrentLandUri.toString());
            mIsNewInsert = false;
            getSupportLoaderManager().initLoader(mLoaderManagerId, null, this);
            String[] parts = mCurrentLandUri.toString().split("/");
            mCurrentId = Integer.parseInt(parts[4]);
            Log.i("EditorActivity",mCurrentLandUri.toString()+":"+mCurrentId);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_land_name);
        mDescEditText = (EditText) findViewById(R.id.edit_land_desc);
        mSizeEditText = (EditText) findViewById(R.id.edit_land_size);
        mProvinceSpinner = (Spinner) findViewById(R.id.spinner_province);
        mPhoneEditText = (EditText) findViewById(R.id.edit_phone_number);
        mHomePrice = (EditText) findViewById(R.id.home_price);
        mQuantityTextView = (TextView) findViewById(R.id.quantity_text_view);
        setupSpinner();
        mDbHelper = new LandsDbhelper(this);
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescEditText.setOnTouchListener(mTouchListener);
        mSizeEditText.setOnTouchListener(mTouchListener);
        mProvinceSpinner.setOnTouchListener(mTouchListener);
        mImageView = (ImageView) findViewById(R.id.camphoto);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mLandHasChanged = true;
            return false;
        }
    };
    @Override
    public void onBackPressed() {
        // If the land hasn't changed, continue with handling back button press
        if (!mLandHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i("MainActivity","onStart");
        googleApiClient.connect();
        if( googleApiClient.isConnected())
        {
            Toast.makeText(this,"google Location API connected",Toast.LENGTH_SHORT).show();
            Log.i("MainActivity","google Location API connected");
        }else{
            Log.i("MainActivity","google Location API NOT connected");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("MainActivity","onStop");
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        Log.i("MainActivity","onConnected");
        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);

        if (locationAvailability.isLocationAvailable()) {
            // Call Location Services
            LocationRequest locationRequest = new LocationRequest()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000);
            Log.i("MainActivity","onConnected2");
            //if(locationRequest!=null)
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }catch (Exception e){
                Log.e("MainActivity", "Error in onConnected.  "+e.getMessage());
            }
            Log.i("MainActivity","onConnected3");
        } else {
            // Do something when Location Provider not available
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("MainActivity","OnLocationChanged");
        TextView locationStatus = (TextView) findViewById(R.id.location_status);
        locationStatus.setText("Status: CONNECTED");
        mCurrentLocation = location;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentdata)  {
        Log.i("MainActivity", "onActivityResult");
        if (requestCode == REQUEST_IMAGE_CAPTURE ){
            Log.i("MainActivity", "ImageRequest:"+resultCode);
            if( resultCode == RESULT_OK) {

                File file = new File(mCurrentPhotoPath);
                //String currentThumbnailPath = new StringBuilder(mCurrentPhotoPath).insert(mCurrentPhotoPath.length()-4, "thumbnail").toString();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(file));
                    Bitmap thumbnail = crupAndScale(bitmap.copy(bitmap.getConfig(),true),150);
                    mImageView.setImageBitmap(thumbnail);
                    //File fileThumbnail = new File(currentThumbnailPath); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
                    //OutputStream fOut = new FileOutputStream(file);
                    //thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title" , "Description");
                } catch (Exception e) {
                    Log.e("MainActivity","Create bitmap exception");
                }
                //addImageToGallery(mCurrentPhotoPath,MainActivity.this);

            }
            if( resultCode == RESULT_CANCELED)
            {
                Log.i("MainActivity", "RESULT_CANCELED");
            }
        }
    }
    private void showUnsavedChangesDialog(
        DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the land.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    /**
     * Setup the dropdown spinner that allows the user to select the province of the land.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter provinceSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_province_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        provinceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mProvinceSpinner.setAdapter(provinceSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mProvinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.province_aroundbangkok))) {
                        mProvince = LandEntry.PROVINCE_AROUNDBANGKOK; // Male
                    } else if (selection.equals(getString(R.string.province_threehours))) {
                        mProvince = LandEntry.PROVINCE_THREEHOURS; // Female
                    } else {
                        mProvince = LandEntry.PROVINCE_FARAWAY; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mProvince = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case android.R.id.home:
                // If the land hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.

                if (!mLandHasChanged) {
                    Log.i("EditActivity","onOptionsItemSelected");
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case R.id.action_save:
                if(mIsNewInsert)
                     insertLand();
                else
                    saveLand();
                finishInsert();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                //finishInsert();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            //case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
               // NavUtils.navigateUpFromSameTask(this);
               // return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertLand(){
        String name = mNameEditText.getText().toString();
        String desc = mDescEditText.getText().toString();
        String sizerai_str = mSizeEditText.getText().toString();
        String currentPhone = mPhoneEditText.getText().toString();
        String homeprice_str = mHomePrice.getText().toString();


        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sizerai_str) || TextUtils.isEmpty(currentPhone) || TextUtils.isEmpty(homeprice_str)){
            return;
        }
        Log.i("EditorActivity","insertLand");
        int sizerai = Integer.parseInt(sizerai_str.trim());
        int homeprice =Integer.parseInt(homeprice_str.trim());
        ContentValues values = new ContentValues();
        Log.i("EditorActivity","insertLand");
        values.put(LandEntry.COLUMN_LAND_NAME,name.trim());
        values.put(LandEntry.COLUMN_LAND_DESC,desc.trim());
        values.put(LandEntry.COLUMN_LAND_PROVINCE,mProvince);
        values.put(LandEntry.COLUMN_LAND_SIZE,sizerai);
        values.put(LandEntry.COLUMN_LAND_LATITUDE,mLatitude);
        values.put(LandEntry.COLUMN_LAND_LONGITUDE,mLongitude);
        values.put(LandEntry.COLUMN_LAND_IMAGE,mCurrentPhotoPath);
        values.put(LandEntry.COLUMN_LAND_PHONE,currentPhone);
        values.put(LandEntry.COLUMN_LAND_HOMEQUANTITY,start_home_quantity);
        values.put(LandEntry.COLUMN_LAND_HOMEPRICE,homeprice);
        values.put(LandEntry.COLUMN_LAND_HOMEQUANTITY,mQuantity);
        Log.i("EditorActivity","insertLand");
        Uri newUri = getContentResolver().insert(LandEntry.CONTENT_URI, values);
        Log.i("EditorActivity","insertLand");
        if(newUri == null) {
            Toast.makeText(this,"Error insertion",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,"1 row added",Toast.LENGTH_SHORT).show();
            // Log.i("CatalogActivity","1 row added");
        }
    }
    private void saveLand(){

        String name = mNameEditText.getText().toString();
        String desc = mDescEditText.getText().toString();
        String sizerai_str = mSizeEditText.getText().toString();
        String currentPhone = mPhoneEditText.getText().toString();
        String homeprice_str = mHomePrice.getText().toString();


        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sizerai_str) || TextUtils.isEmpty(currentPhone) || TextUtils.isEmpty(homeprice_str)){
            return;
        }

        int sizerai = Integer.parseInt(sizerai_str.trim());
        int homeprice =Integer.parseInt(homeprice_str.trim());
        ContentValues values = new ContentValues();

        values.put(LandEntry.COLUMN_LAND_NAME,name.trim());
        values.put(LandEntry.COLUMN_LAND_DESC,desc.trim());
        values.put(LandEntry.COLUMN_LAND_PROVINCE,mProvince);
        values.put(LandEntry.COLUMN_LAND_SIZE,sizerai);
        values.put(LandEntry.COLUMN_LAND_LATITUDE,mLatitude);
        values.put(LandEntry.COLUMN_LAND_LONGITUDE,mLongitude);
        values.put(LandEntry.COLUMN_LAND_PHONE,currentPhone);
        values.put(LandEntry.COLUMN_LAND_HOMEPRICE,homeprice);
        values.put(LandEntry.COLUMN_LAND_HOMEQUANTITY,mQuantity);
        values.put(LandEntry.COLUMN_LAND_IMAGE,mCurrentPhotoPath);


        int num_effect = getContentResolver().update(mCurrentLandUri, values,null,null);
        if(num_effect == 0) {
            Toast.makeText(this,"Error insertion",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,num_effect+" row updated",Toast.LENGTH_SHORT).show();
            // Log.i("CatalogActivity","1 row added");
        }
    }
    private void finishInsert(){
        Intent intent = new Intent( EditorActivity.this,CatalogActivity.class);
        startActivity(intent);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                LandEntry._ID,
                LandsContract.LandEntry.COLUMN_LAND_NAME,
                LandsContract.LandEntry.COLUMN_LAND_DESC,
                LandsContract.LandEntry.COLUMN_LAND_PROVINCE,
                LandsContract.LandEntry.COLUMN_LAND_SIZE,
                LandsContract.LandEntry.COLUMN_LAND_PHONE,
                LandsContract.LandEntry.COLUMN_LAND_HOMEPRICE,
                LandsContract.LandEntry.COLUMN_LAND_HOMEQUANTITY,
                LandsContract.LandEntry.COLUMN_LAND_IMAGE};


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentLandUri,         // Query the content URI for the current land
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Find the columns of land attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(LandEntry.COLUMN_LAND_NAME);
            int descColumnIndex = cursor.getColumnIndex(LandEntry.COLUMN_LAND_DESC);
            int provinceColumnIndex = cursor.getColumnIndex(LandEntry.COLUMN_LAND_PROVINCE);
            int sizeraiColumnIndex = cursor.getColumnIndex(LandEntry.COLUMN_LAND_SIZE);
            int phoneColumnIndex = cursor.getColumnIndex(LandEntry.COLUMN_LAND_PHONE);
            int homepriceColumnIndex = cursor.getColumnIndex(LandEntry.COLUMN_LAND_HOMEPRICE);
            int quantityColumnIndex = cursor.getColumnIndex(LandEntry.COLUMN_LAND_HOMEQUANTITY);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String desc = cursor.getString(descColumnIndex);
            String phone_str = cursor.getString(phoneColumnIndex);
            int province = cursor.getInt(provinceColumnIndex);
            int sizerai = cursor.getInt(sizeraiColumnIndex);
            String homeprice_str = cursor.getString(homepriceColumnIndex).toString().trim();
            String quantity_str = cursor.getString(quantityColumnIndex).toString().trim();
            mCurrentPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow(LandEntry.COLUMN_LAND_IMAGE));
            try{
                mQuantity = Integer.parseInt(quantity_str);
            }catch (Exception e){
                Log.e("EditorActivity","Quantity Parse Int Error");
            }

            mNameEditText.setText(name);
            mDescEditText.setText(desc);
            mSizeEditText.setText(Integer.toString(sizerai));
            mPhoneEditText.setText(phone_str);
            mHomePrice.setText(homeprice_str);
            mQuantitydisplay();
            switch (province) {
                case LandEntry.PROVINCE_AROUNDBANGKOK:
                    mProvinceSpinner.setSelection(1);
                    break;
                case LandEntry.PROVINCE_THREEHOURS:
                    mProvinceSpinner.setSelection(2);
                    break;
                default:
                    mProvinceSpinner.setSelection(0);
                    break;
            }
            if(mCurrentPhotoPath!=null) {
                File file = new File(mCurrentPhotoPath);
                Uri uri = Uri.fromFile(file);
                Glide.with(this)
                        .load(uri) // Uri of the picture
                        .into(mImageView);
            }

        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new land, hide the "Delete" menu item.
        if (mCurrentLandUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the land.
                deleteLand();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the land.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the land in the database.
     */
    private void deleteLand() {
        int num_row_effect= getContentResolver().delete(mCurrentLandUri, null, null);

        if(num_row_effect == 0) {
            Toast.makeText(this,"Error deletion",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,num_row_effect+" row deleted",Toast.LENGTH_SHORT).show();
            // Log.i("CatalogActivity","1 row added");
        }
    }


    public static  Bitmap crupAndScale (Bitmap source, int scale){
        int factor = source.getHeight() <= source.getWidth() ? source.getHeight(): source.getWidth();
        int longer = source.getHeight() >= source.getWidth() ? source.getHeight(): source.getWidth();
        int x = source.getHeight() >= source.getWidth() ?0:(longer-factor)/2;
        int y = source.getHeight() <= source.getWidth() ?0:(longer-factor)/2;
        source = Bitmap.createBitmap(source, x, y, factor, factor);
        source = Bitmap.createScaledBitmap(source, scale, scale, false);
        return source;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("MainActivity","create photoFile error");
            }
            if (photoFile != null) {
                Uri uri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

            //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.i("MainActivity",mCurrentPhotoPath.toString());
        return image;
    }

    public void takePhoto(View view){
        dispatchTakePictureIntent();
    }
    public void getGPSclicked(View view){

        if(mCurrentLocation != null) {
            mLatitude = mCurrentLocation.getLatitude();
            mLongitude = mCurrentLocation.getLongitude();
            TextView latitude_text = (TextView) findViewById(R.id.latitude);
            latitude_text.setText("" + mLatitude);
            TextView longitude_text = (TextView) findViewById(R.id.longitude);
            longitude_text.setText("" + mLongitude);
        }
    }
    public void mQuantitydisplay(){
        mQuantityTextView.setText("" + mQuantity);
    }
    public void incrementQuantity(View view) {
        mQuantity++;
        mQuantitydisplay();

    }
    public void decrementQuantity(View view) {

        if(mQuantity>0) {
            mQuantity--;
            mQuantitydisplay();
        }else{
            Toast.makeText(this,"Quantity must be non-negative value.", Toast.LENGTH_SHORT).show();
        }
    }

}