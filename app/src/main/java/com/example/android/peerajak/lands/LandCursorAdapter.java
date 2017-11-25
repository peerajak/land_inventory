package com.example.android.peerajak.lands;

import android.content.ContentValues;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CursorAdapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.peerajak.lands.data.LandsContract.LandEntry;
import com.example.android.peerajak.lands.data.LandsContract;

import java.io.File;

/**
 * Created by peerajak on 11/17/17.
 */

public class LandCursorAdapter extends CursorAdapter {


    public LandCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent ,false);

        ViewHolderItem holder = new ViewHolderItem(view);
        view.setTag(holder);

        return view;
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolderItem holder = (ViewHolderItem) view.getTag();
        final int position = cursor.getPosition();
        final long id = getItemId(position);
        String name_meal = cursor.getString(cursor.getColumnIndexOrThrow(LandEntry.COLUMN_LAND_NAME));
        String meal_desc = cursor.getString(cursor.getColumnIndexOrThrow(LandEntry.COLUMN_LAND_DESC));
        String image_path = cursor.getString(cursor.getColumnIndexOrThrow(LandEntry.COLUMN_LAND_IMAGE));
        double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LandEntry.COLUMN_LAND_LATITUDE));
        double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LandEntry.COLUMN_LAND_LONGITUDE));
        String price_str = cursor.getString(cursor.getColumnIndexOrThrow(LandEntry.COLUMN_LAND_HOMEPRICE));
        String quantity_str = cursor.getString(cursor.getColumnIndexOrThrow(LandEntry.COLUMN_LAND_HOMEQUANTITY));
        if (TextUtils.isEmpty(meal_desc)) {
            meal_desc = "No Description";
        }
        holder.name_txtview.setText(name_meal);
        holder.desc_txtview.setText("Description: "+meal_desc);
        holder.latitude_text.setText("Latitude: "+latitude);
        holder.longitude_text.setText("Longitude: "+longitude);
        holder.quantity_text.setText("Quantity: "+quantity_str);

        final int quantity = Integer.parseInt(quantity_str);
        holder.quantity_text.setText("Quantity: "+quantity);

        holder.buy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quantity>0) {
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(LandsContract.LandEntry.COLUMN_LAND_HOMEQUANTITY, quantity-1);
                    int num_effect = v.getContext().getContentResolver().update(LandsContract.LandEntry.CONTENT_URI, updateValues, LandsContract.LandEntry._ID+"="+id, null);
                    if(num_effect == 1) {
                        Toast.makeText(v.getContext(),"ORDER COMPLETED SUCCESSFULLY", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(v.getContext(),"ORDER NOT COMPLETED", Toast.LENGTH_SHORT).show();
                    }


                    holder.quantity_text.setText("Quantity: "+quantity);
                }else{
                    Toast.makeText(v.getContext(),"OUT OF STOCK", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Log.i("MainActivity","image_path="+image_path);
        if(image_path!=null) {
            File file = new File(image_path);
            Uri uri = Uri.fromFile(file);
            Glide.with(context)
                    .load(uri) // Uri of the picture
                    .into(holder.image_imgview);
        }
    }

    static class ViewHolderItem{
        TextView name_txtview;
        TextView desc_txtview;
        ImageView image_imgview;
        TextView latitude_text;
        TextView longitude_text;
        TextView quantity_text;
        TextView price_text;
        Button buy_button;

        public ViewHolderItem(View convertView){
            name_txtview = (TextView) convertView.findViewById(R.id.name);
            desc_txtview = (TextView) convertView.findViewById(R.id.description);
            image_imgview = (ImageView) convertView.findViewById(R.id.item_image);
            latitude_text = (TextView) convertView.findViewById(R.id.out_latitude);
            longitude_text = (TextView) convertView.findViewById(R.id.out_longitude);
            quantity_text = (TextView) convertView.findViewById(R.id.out_quantity);
            price_text = (TextView) convertView.findViewById(R.id.out_price);
            buy_button = (Button) convertView.findViewById(R.id.buy_item);
        }
    }
}
