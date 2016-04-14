package com.lu.luandroid.foodnotes;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lu.luandroid.foodnotes.config.ipconfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FoodList extends AppCompatActivity {

    private static final String TAG = "FoodList";

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.textview)
    TextView textview;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private String[] name;
    private String[] location;
    private String[] image;

    private List<TableItem> listTableItem = new ArrayList<>();

    private OkHttpClient httpClient = new OkHttpClient();


    private String account;

    public static Activity activity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        ButterKnife.bind(this);

        SharedPreferences settings = getSharedPreferences("login", 0);
        account = settings.getString("account", "");

        refresh();
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple, android.R.color.holo_blue_bright, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refresh();
                Log.i(TAG, "onRefresh: ");
            }
        });

        activity = this;


    }

    private void searchFoodList() {

        RequestBody requestBody = new FormBody.Builder()
                .add("account", account)
                .build();
        Request request = new Request.Builder()
                .post(requestBody)
                .url(ipconfig.search_food)
                .build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
                FoodList.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setVisibility(View.GONE);
                        textview.setVisibility(View.VISIBLE);
                        textview.setText("請重新整理");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: " + response.code());
                String strResponse = response.body().string();


                int success;
                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    Log.i(TAG, "onResponse: " + strResponse);
                    success = jsonObject.getInt("success");
                    if (success == 1) {
                        JSONArray jsonArray = jsonObject.getJSONArray("table");
                        name = new String[jsonArray.length()];
                        location = new String[jsonArray.length()];
                        image = new String[jsonArray.length()];

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json = jsonArray.getJSONObject(i);
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("name", json.getString("name"));
                            map.put("location", json.getString("location"));
                            map.put("image", json.getString("image"));

                            name[i] = map.get("name");
                            location[i] = map.get("location");
                            image[i] = map.get("image");

                            TableItem tableItem = new TableItem(name[i], location[i], image[i]);
                            listTableItem.add(tableItem);
                            if (jsonArray.length() == i) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                        FoodList.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapterRecycleView();


                            }
                        });
                    } else {
                        FoodList.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textview.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);

                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void adapterRecycleView() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(FoodList.this);
        recyclerView.setLayoutManager(layoutManager);

        TableRecycleAdapter adapter = new TableRecycleAdapter(listTableItem, FoodList.this);
        recyclerView.setAdapter(adapter);

        adapter.setItemClickListener(new TableRecycleAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view) {
                Log.i(TAG, "onItemClick: " + recyclerView.getChildAdapterPosition(view));
                int id = recyclerView.getChildAdapterPosition(view);
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("shopname", name[id]);
                intent.setClass(FoodList.this, FoodDetail.class);
                intent.putExtras(bundle);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refresh();
    }

    private void refresh() {
        listTableItem.clear();
        searchFoodList();
        swipeRefreshLayout.setRefreshing(true);
    }


}
