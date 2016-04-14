package com.lu.luandroid.foodnotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lu.luandroid.foodnotes.config.ipconfig;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    String account;

    ProgressDialog pDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        ButterKnife.bind(this);

        Logger.init();
        SharedPreferences settings = getSharedPreferences("login", 0);
        account = settings.getString("account", "");

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
        }else if (id == R.id.btnDelete) {
            showDialog();
            deleteFood();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteFood() {
        OkHttpClient okhttpclient = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("shopname", getShopName())
                .add("account", account)
                .build();

        Request request = new Request.Builder()
                .url(ipconfig.deleteFood)
                .post(requestBody)
                .build();

        Call call = okhttpclient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "請檢查網路是否連接成功", Toast.LENGTH_SHORT).show();
                FoodDetail.this.runOnUiThread(()-> hideDialog());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String strResponse = response.body().string();
                Log.i(TAG, "onResponse: " +strResponse);
                FoodDetail.this.runOnUiThread(()-> hideDialog());
                int success;
                try {
                    JSONObject json = new JSONObject(strResponse);
                    success = json.getInt("success");
                    if (success == 1) {

                        startActivity(new Intent(FoodDetail.this, FoodList.class));
                        pDialog.dismiss();
                        FoodList.activity.finish();
                        FoodDetail.this.runOnUiThread(()-> Toast.makeText(getApplicationContext(), "刪除成功", Toast.LENGTH_SHORT).show());
                        finish();
                    }else {
                        FoodDetail.this.runOnUiThread(()-> Toast.makeText(getApplicationContext(), "刪除失敗", Toast.LENGTH_SHORT).show());

                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getFoodDetail() {
        runOnUiThread(()-> showDialog());
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
                runOnUiThread(()-> hideDialog());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String strResponse = response.body().string();
                int success;
                pDialog.dismiss();
                runOnUiThread(()-> hideDialog());
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
                                Glide.with(FoodDetail.this).load("http://skychi.no-ip.org/Lu/food_note/upload/" + image).fitCenter().into(foodimage);
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

    private void showDialog() {
        pDialog = new ProgressDialog(this);
        if (!pDialog.isShowing()) {
            pDialog.setCancelable(false);
            pDialog.show();
        }

    }

    private void hideDialog() {
        if (pDialog.isShowing()) {
            pDialog.hide();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }
}
