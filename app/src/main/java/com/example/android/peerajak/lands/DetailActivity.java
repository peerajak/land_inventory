package com.example.android.peerajak.lands;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.peerajak.lands.data.LandsContract;

import java.io.File;

public class DetailActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>   {
    LandsDbhelper mDbHelper;
    /** TextView field to enter the land's name */
    private TextView mNameTextView;
    private TextView mPhoneNumber;
    /** TextView field to enter the land's breed */
    private TextView mDescTextView;
    private int mCurrentId=0;
    /** TextView field to enter the land's weight */
    private TextView mSizeTextView;

    /** TextView field to enter the land's province */
    private Spinner mProvinceSpinner;
    private final int mLoaderManagerId=1;
    private Uri mCurrentLandUri=null;
    private ImageView mImageView;
    private ImageView mIconPhoneView;
    private String mCurrentPhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        setTitle(R.string.detail_activity_title_edit_land);
        Intent catalogIntent = getIntent();
        mCurrentLandUri = catalogIntent.getData();
        Log.i("DetailActivity", mCurrentLandUri.toString());
        getSupportLoaderManager().initLoader(mLoaderManagerId, null, this);
        mNameTextView = (TextView) findViewById(R.id.detail_land_name);
        mDescTextView = (TextView) findViewById(R.id.detail_land_desc);
        mSizeTextView = (TextView) findViewById(R.id.detail_land_size);
        mProvinceSpinner = (Spinner) findViewById(R.id.detailspinner_province);
        mImageView = (ImageView) findViewById(R.id.detail_land_image);
        mPhoneNumber = (TextView) findViewById(R.id.detail_phone_number);
        setupSpinner();
        mDbHelper = new LandsDbhelper(this);
        mIconPhoneView = (ImageView) findViewById(R.id.detail_phone_icon);
        mIconPhoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Click", "Shop Telephone ImageView clicked");
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                phoneIntent.setData(Uri.parse("tel:" + mCurrentPhoneNumber));
                v.getContext().startActivity(phoneIntent);
            }
        });

    }
    private void setupSpinner() {
        ArrayAdapter provinceSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_province_options, android.R.layout.simple_spinner_item);
        provinceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mProvinceSpinner.setAdapter(provinceSpinnerAdapter);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i("DetailActivity","onCreateLoader");
        String[] projection = {
                LandsContract.LandEntry._ID,
                LandsContract.LandEntry.COLUMN_LAND_NAME,
                LandsContract.LandEntry.COLUMN_LAND_DESC,
                LandsContract.LandEntry.COLUMN_LAND_PROVINCE,
                LandsContract.LandEntry.COLUMN_LAND_SIZE,
                LandsContract.LandEntry.COLUMN_LAND_PHONE,
                //LandsContract.LandEntry.COLUMN_LAND_HOMEPRICE,
                //LandsContract.LandEntry.COLUMN_LAND_HOMEQUANTITY,
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
        Log.i("DetailActivity","onLoadFinished");
        if (cursor.moveToFirst()) {
            // Find the columns of land attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(LandsContract.LandEntry.COLUMN_LAND_NAME);
            int descColumnIndex = cursor.getColumnIndex(LandsContract.LandEntry.COLUMN_LAND_DESC);
            int provinceColumnIndex = cursor.getColumnIndex(LandsContract.LandEntry.COLUMN_LAND_PROVINCE);
            int sizeraiColumnIndex = cursor.getColumnIndex(LandsContract.LandEntry.COLUMN_LAND_SIZE);
            String image_path = cursor.getString(cursor.getColumnIndexOrThrow(LandsContract.LandEntry.COLUMN_LAND_IMAGE));
            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String desc = cursor.getString(descColumnIndex);
            int province = cursor.getInt(provinceColumnIndex);
            int sizerai = cursor.getInt(sizeraiColumnIndex);
            mCurrentPhoneNumber = cursor.getString(cursor.getColumnIndex(LandsContract.LandEntry.COLUMN_LAND_PHONE));
            mNameTextView.setText(name);
            mDescTextView.setText(desc);
            mSizeTextView.setText(Integer.toString(sizerai));
            mPhoneNumber.setText(mCurrentPhoneNumber);

            switch (province) {
                case LandsContract.LandEntry.PROVINCE_AROUNDBANGKOK:
                    mProvinceSpinner.setSelection(1);
                    break;
                case LandsContract.LandEntry.PROVINCE_THREEHOURS:
                    mProvinceSpinner.setSelection(2);
                    break;
                default:
                    mProvinceSpinner.setSelection(0);
                    break;
            }
            mProvinceSpinner.setEnabled(false);
            Log.i("MainActivity","image_path="+image_path);
            if(image_path!=null) {
                File file = new File(image_path);
                Uri uri = Uri.fromFile(file);
                Glide.with(this)
                        .load(uri) // Uri of the picture
                        .into(mImageView);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.i("DetailActivity", "Menu Selected");
        Intent editorIntent = new Intent(DetailActivity.this, EditorActivity.class);
        Log.i("CatalogActivity","onClickListener:"+mCurrentLandUri.toString());
        editorIntent.setData(mCurrentLandUri);
        startActivity(editorIntent);
        return super.onOptionsItemSelected(item);
    }

}
