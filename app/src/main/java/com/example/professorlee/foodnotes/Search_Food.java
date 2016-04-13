package com.example.professorlee.foodnotes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Search_Food extends AppCompatActivity {

    private static final String TAG = "Search_Food";
    @Bind(R.id.txtLocation)
    TextView txtLocation;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.Spdistance)
    Spinner Spdistance;

    private String[] name;
    private String[] location;
    private String[] distance;

    private String[] place_id;

    String LocalAddress = "";

    OkHttpClient okHttpClient = new OkHttpClient();

    double lat, lng;

    private List<TableItem_search_food> ListtableItem = new ArrayList<>();

    int arrayLength;
    int radius =  3000;
    private static final int Request_Location = 2;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_food);
        ButterKnife.bind(this);
        Logger.init();
        if (Build.VERSION.SDK_INT < 23) {
            refreshData();
            Log.i(TAG, "onCreate: 23以下");

        }else {
            checkLocationPermission();
            Log.i(TAG, "onCreate: res ");
        }


        chosedistance();
        getLocation();
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple, android.R.color.holo_blue_bright, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);



        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh: onclick");
                refreshData();

            }
        });


    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(swipeRefreshLayout, "需要給予權限", Snackbar.LENGTH_INDEFINITE)
                        .setAction("確認", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(Search_Food.this, PERMISSIONS_LOCATION, Request_Location);
                            }
                        }).show();
            }else {
                ActivityCompat.requestPermissions(Search_Food.this, PERMISSIONS_LOCATION, Request_Location);
            }
        }else {
            Search_Food.this.runOnUiThread(()-> refreshData());
            Log.i(TAG, "checkLocationPermission: refresh()");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult: " + requestCode + "," +grantResults[0]);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onRequestPermissionsResult: refresh()");
            refreshData();
        }else {
            Toast.makeText(getApplicationContext(), "請給予位置權限", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void chosedistance() {

        String[] Items = getResources().getStringArray(R.array.distance);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spdistance.setAdapter(adapter);
        Spdistance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemSelected: " + position);
                switch (position) {
                    case 0:
                        Log.i(TAG, "onItemSelected: ");
                        break;
                    case 1 :
                        radius = 5000;
                        refreshData();
                        break;
                    case 2 :
                        radius = 7000;
                        refreshData();
                        break;
                    case 3 :
                        radius = 11000;
                        refreshData();
                        break;
                    case 4 :
                        radius = 13000;
                        refreshData();
                        break;
                    case 5 :
                        radius = 15000;
                        refreshData();
                        break;

                }
                Log.i(TAG, "onItemSelected: " + radius);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void refreshData() {
        Log.i(TAG, "refresh: refresh()Data");
        ListtableItem.clear();
        search_food();

    }

    private void search_food() {
        Log.i(TAG, "search_food: 搜尋");
        swipeRefreshLayout.setRefreshing(true);
        final Request request = new Request.Builder()
                .url("https://maps.googleapis.com/maps/api/place/nearbysearch/json?language=zh-TW&location="
                        + lat + "," + lng + "&radius=" + radius/2 + "&types=restaurant|food&key=AIzaSyAv6Z_x-ssH1UhWoWbaZzAGCQN0i1T5t-w")
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //Log.i(TAG, "onResponse:" + response.code());
                String strResponse = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    //Logger.json(strResponse);
                    if ("OK".equals(jsonObject.getString("status"))) {
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        arrayLength = jsonArray.length();
                        name = new String[arrayLength];

                        location = new String[arrayLength];
                        distance = new String[arrayLength];
                        place_id = new String[arrayLength];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json = jsonArray.getJSONObject(i);
                            Map<String, String> map = new HashMap();
                            map.put("name", json.getString("name"));
                            map.put("place_id", json.getString("place_id"));
                            name[i] = map.get("name");
                            place_id[i] = map.get("place_id");
                            getDetail(place_id[i], i);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void getDetail(final String id, final int i) {

        final Request request = new Request.Builder()
                .url("https://maps.googleapis.com/maps/api/place/details/json?language=zh-TW&placeid="
                        + id + "&key=AIzaSyAv6Z_x-ssH1UhWoWbaZzAGCQN0i1T5t-w")
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                double latitude;
                double longitude;
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string()).getJSONObject("result");
                    location[i] = jsonObject.getString("formatted_address");

                    JSONObject a = jsonObject.getJSONObject("geometry");
                    JSONArray b = a.getJSONArray("access_points");
                    JSONObject c = b.getJSONObject(0).getJSONObject("location");
                    latitude = Double.parseDouble(c.getString("lat"));
                    longitude = Double.parseDouble(c.getString("lng"));

                    Logger.d(latitude + "," + longitude);
                    Logger.d(location[i]);


                    getDistance(latitude, longitude, i);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void getDistance(double latitude, double longitude, final int i) {

        Request request = new Request.Builder()
                .url("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
                        lat + "," + lng + "&destinations=" + latitude + "," + longitude +
                        "&language=zh-TW&key=AIzaSyAv6Z_x-ssH1UhWoWbaZzAGCQN0i1T5t-w")
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray a = jsonObject.getJSONArray("rows");
                    JSONObject b = a.getJSONObject(0);
                    JSONArray c = b.getJSONArray("elements");
                    JSONObject d = c.getJSONObject(0);
                    JSONObject e = d.getJSONObject("distance");
                    distance[i] = e.getString("text");
                    Log.i(TAG, "onResponse: " + distance[i]);

                    TableItem_search_food table = new TableItem_search_food(
                            name[i], location[i], distance[i]);

                    ListtableItem.add(table);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Search_Food.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.d("array" + arrayLength + "," + i);
                        adapterRecycleView();

                    }
                });

            }
        });
    }

    private void adapterRecycleView() {

        swipeRefreshLayout.setRefreshing(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(Search_Food.this);
        recyclerView.setLayoutManager(layoutManager);

        TableItem_searchRecycleAdapter adapter = new TableItem_searchRecycleAdapter(ListtableItem, Search_Food.this);
        recyclerView.setAdapter(adapter);

    }



    private void getLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = null;
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        provider = mLocationManager.getBestProvider(criteria, true);

        if (provider != null) {
            Log.i(TAG, "getLocation: " + provider);
            Location location = mLocationManager.getLastKnownLocation(provider);

            Log.i(TAG, "getLocation: " + getLatitude(location) + "," + getLongitude(location));
            lat = getLatitude(location);
            lng = getLongitude(location);

            Geocoder geocoder = new Geocoder(this, Locale.TAIWAN);

            if (getLatitude(location) != 0 && getLongitude(location) != 0) {

                try {
                    List<Address> addresses = geocoder.getFromLocation(getLatitude(location), getLongitude(location), 1);
                    LocalAddress = addresses.get(0).getAdminArea() +
                            addresses.get(0).getLocality() + addresses.get(0).getThoroughfare() +
                            addresses.get(0).getFeatureName() + "號";
                    Log.i(TAG, "getLocation: " + LocalAddress);
                    txtLocation.setText(LocalAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }


    private double getLongitude(Location location) {
        return location.getLongitude();
    }

    private double getLatitude(Location location) {
        return location.getLatitude();
    }


}
