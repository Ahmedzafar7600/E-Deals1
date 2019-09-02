package com.tominc.prustyapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

/**
 * Created by shubham on 14/1/16.
 */
public class ProductListAdapter extends BaseAdapter {
    Context c;
    ArrayList<Product> items;

    private final String IMAGE_DOWNLOAD_BASE = Config.BASE_URL + "products/";

    public ProductListAdapter(Context c, ArrayList<Product> items){
        this.c = c;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View row;

        //TODO: make staggered gridview

        if(view==null){
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_item, null);
        } else{
            row=view;
        }

        TextView title = (TextView) row.findViewById(R.id.product_name);
        TextView cost = (TextView) row.findViewById(R.id.product_cost);
        ImageView pic = (ImageView) row.findViewById(R.id.product_pic);

        int no_images = items.get(i).getImageCount();

        if(no_images==0){
            pic.setImageResource(R.drawable.material);
        } else{
//            String madeUrl = IMAGE_DOWNLOAD_BASE + items.get(i).getEmail() + "/" + items.get(i).getName()+"/image0.png";

//            Glide.with(c)
//                    .load(madeUrl)
//                    .into(pic);

//            DownloadImage download = new DownloadImage(pic, madeUrl);
//            download.execute();
        }

        CardView card = (CardView) row.findViewById(R.id.list_item_card);

        title.setText(items.get(i).getName());
        cost.setText(items.get(i).getPrice());

        final View tempView = row;
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(c, "click", Toast.LENGTH_SHORT).show();
//                Snackbar.make(tempView, "Clicked", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                Intent in = new Intent(c, ShowProductActivity.class);
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                in.putExtra("prod", items.get(i));
                c.startActivity(in);
            }
        });


        return row;
    }

    private class DownloadImage extends AsyncTask<Void, Void, Bitmap>{
        ImageView imageId;
        String url;

        public DownloadImage(ImageView imageId, String url) {
            this.imageId = imageId;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            Resources r = c.getResources();
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, r.getDisplayMetrics());

            Bitmap bmp=null;
            try {
                bmp = Glide.with(c)
                        .load(url)
                        .asBitmap()
                        .centerCrop()
                        .into(height, height)
                        .get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageId.setImageBitmap(bitmap);
        }
    }


}
