package com.example.professorlee.foodnotes;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.professorlee.foodnotes.config.ipconfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FoodDetail extends AppCompatActivity {

    private static final String TAG = "FoodDetail";

    @Bind(R.id.shopname)
    TextView txtshopname;
    @Bind(R.id.date)
    TextView txtdate;
    @Bind(R.id.type)
    TextView txttype;
    @Bind(R.id.location)
    Button btnlocation;
    @Bind(R.id.foodimage)
    ImageView foodimage;

    String location;

    String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        ButterKnife.bind(this);

        getFoodDetail();

        Log.i(TAG, "onCreate: " + getShopName());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.food_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.btnEdit) {
            Bundle bundle = new Bundle();
            bundle.putString("shopName", txtshopname.getText().toString());
            bundle.putString("Date", txtdate.getText().toString());
            bundle.putString("FoodType", txttype.getText().toString());
            bundle.putString("Location", btnlocation.getText().toString());
            bundle.putString("image", image);
            Intent intent = new Intent();
            intent.setClass(FoodDetail.this, Edit_FoodDetail.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getFoodDetail() {
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("shopname", getShopName())
                .build();
        Request request = new Request.Builder()
                .url(ipconfig.getFoodDetail)
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String strResponse = response.body().string();
                int success;
                try {
                    JSONObject json = new JSONObject(strResponse);
                    success = json.getInt("success");
                    if (success == 1) {
                        location = getJson(json, "location");
                        final String time = getJson(json, "time");
                        final String foodtype = getJson(json, "foodtype");
                        image = getJson(json, "image");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtshopname.setText(getShopName());
                                btnlocation.setText(location);
                                txtdate.setText(time);
                                txttype.setText(foodtype);
                                Glide.with(FoodDetail.this).load("http://163.17.9.116/Lu/food_note/upload/" + image).fitCenter().into(foodimage);
                            }
                        });


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getJson(JSONObject json, String location) throws JSONException {
        return json.getString(location);
    }


    private String getShopName() {
        Bundle bundle = this.getIntent().getExtras();

        return bundle.getString("shopname");
    }


    public void btnToMap(View view) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.co.in/maps?q=" + location));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);

    }
}
