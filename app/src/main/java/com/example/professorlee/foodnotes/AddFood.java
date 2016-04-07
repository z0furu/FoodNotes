package com.example.professorlee.foodnotes;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.professorlee.foodnotes.config.ipconfig;
import com.google.android.gms.games.internal.constants.RequestStatus;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddFood extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener {

    private static final String TAG = "AddFood";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.name)
    EditText name;
    @Bind(R.id.btnTime)
    Button btnTime;
    @Bind(R.id.btnFoodType)
    Button btnFoodType;
    @Bind(R.id.btnPhoto)
    ImageButton btnPhoto;
    @Bind(R.id.btnCamera)
    ImageButton btnCamera;
    @Bind(R.id.imageview)
    ImageView imageview;
    @Bind(R.id.btnclc)
    ImageButton btnclc;
    @Bind(R.id.location)
    EditText edtLocation;
    @Bind(R.id.btnLoadLocation)
    Button btnLoadLocation;

    String[] foodType = {"早餐", "午餐", "晚餐", "消夜"};

    private static final int TYPE_PHOTO = 1;
    private static final int TYPE_CAMERA = 2;
    private static final int MEDIA_TYPE_IMAGE = 3;
    @Bind(R.id.relativeLayout)
    RelativeLayout relativeLayout;

    private DisplayMetrics mPhone;
    private Uri fileUri; //相機路徑

    double lat = 0, lon = 0;


    private final OkHttpClient client = new OkHttpClient();
    RequestBody requestBody;

    String LocalAddress = null, shopname = null, time = null, foodtype = null;
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static String imgname = null;
    boolean isCamera = false, isPhote = false;

    private ProgressDialog pDialog;

    private String account;


    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private static String[] PERMISSIONS_CAMERA = {Manifest.permission.CAMERA};
    private static final int Request_Camera = 0;
    private static final int Request_Storage = 1;
    private static final int Request_Location = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);
        ButterKnife.bind(this);

        SharedPreferences setting = getSharedPreferences("login", 0);
        account = setting.getString("account", "");

        mPhone = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mPhone);
        setToolbar();
        BtnListener();


    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle("新增美食");
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddFood.this, MainActivity.class));
                finish();
            }
        });

    }


    private void BtnListener() {
        btnTime.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnFoodType.setOnClickListener(this);
        btnPhoto.setOnClickListener(this);
        btnclc.setOnClickListener(this);
        btnLoadLocation.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_food, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.btnGone) {
            shopname = name.getText().toString();
            if ("".equals(shopname) || "".equals(edtLocation.getText().toString()) || time == null || foodtype == null || imgname == null) {
                Toast.makeText(getApplicationContext(), "請輸入完整資訊", Toast.LENGTH_SHORT).show();
            } else {
                AddFood.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDialog();

                    }
                });
                Log.i(TAG, "onOptionsItemSelected: " + "相機" + isCamera + "相簿" + isPhote);
                if (isCamera) {
                    String file = fileUri.getPath();
                    Log.i(TAG, "onOptionsItemSelected: " + file);
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("account", account)
                            .addFormDataPart("location", edtLocation.getText().toString())
                            .addFormDataPart("shopname", shopname)
                            .addFormDataPart("time", time)
                            .addFormDataPart("foodtype", foodtype)
                            .addFormDataPart("addimage", file,
                                    RequestBody.create(MEDIA_TYPE_PNG, new File(file)))
                            .build();


                } else if (isPhote) {

                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("account", account)
                            .addFormDataPart("location", edtLocation.getText().toString())
                            .addFormDataPart("shopname", shopname)
                            .addFormDataPart("time", time)
                            .addFormDataPart("foodtype", foodtype)
                            .addFormDataPart("addimage", imgname,
                                    RequestBody.create(MEDIA_TYPE_PNG, new File(imgname)))
                            .build();
                }


                Request request = new Request.Builder()
                        .url(ipconfig.addFood)
                        .post(requestBody)
                        .build();

                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "onFailure: " + e.toString());
                        AddFood.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideDialog();
                            }
                        });

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        Log.i(TAG, "onResponse: " + result);
                        AddFood.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideDialog();
                            }
                        });
                        try {
                            JSONObject json = new JSONObject(result);
                            int success = json.getInt("success");
                            if (success == 1) {
                                AddFood.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "新增成功", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                startActivity(new Intent(AddFood.this, MainActivity.class));
                                finish();
                            } else if (success == 2) {
                                AddFood.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "此地點已新增過", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                AddFood.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "新增失敗", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        time = year + "/" + String.valueOf(monthOfYear + 1) + "/" + dayOfMonth;

        btnTime.setText(time);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTime:
                Calendar now = Calendar.getInstance();
                DatePickerDialog pickerDialog = DatePickerDialog.newInstance(
                        AddFood.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH));
                pickerDialog.setThemeDark(true);
                pickerDialog.show(getFragmentManager(), "請選擇日期");
                break;
            case R.id.btnCamera:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        Snackbar.make(relativeLayout, "需要給予權限", Snackbar.LENGTH_INDEFINITE)
                                .setAction("確認", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(AddFood.this, PERMISSIONS_CAMERA, Request_Camera);
                                    }
                                }).show();
                    }else {
                        ActivityCompat.requestPermissions(this, PERMISSIONS_CAMERA, Request_Camera);
                    }
                } else {
                    Intent cameraintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);  //儲存相片
                    cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); //儲存到相簿預設位置
                    startActivityForResult(cameraintent, TYPE_CAMERA);
                    isCamera = true;
                    isPhote = false;
                }

                break;
            case R.id.btnPhoto:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Snackbar.make(relativeLayout, "需要給予權限", Snackbar.LENGTH_INDEFINITE)
                                .setAction("確認", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(AddFood.this, PERMISSIONS_STORAGE, Request_Storage);
                                    }
                                }).show();
                    }else {
                        ActivityCompat.requestPermissions(AddFood.this,  PERMISSIONS_STORAGE, Request_Storage);
                    }
                }else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, TYPE_PHOTO);
                    isPhote = true;
                    isCamera = false;
                }

                break;
            case R.id.btnFoodType:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setSingleChoiceItems(foodType, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        btnFoodType.setText("早餐");
                                        foodtype = "早餐";
                                        break;
                                    case 1:
                                        btnFoodType.setText("午餐");
                                        foodtype = "午餐";
                                        break;
                                    case 2:
                                        btnFoodType.setText("晚餐");
                                        foodtype = "晚餐";
                                        break;
                                    case 3:
                                        btnFoodType.setText("消夜");
                                        foodtype = "消夜";
                                        break;
                                }
                            }
                        })
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
            case R.id.btnclc:
                imageview.setImageResource(0);
                btnPhoto.setVisibility(View.VISIBLE);
                btnCamera.setVisibility(View.VISIBLE);
                imageview.setVisibility(View.GONE);
                btnclc.setVisibility(View.GONE);
                break;
            case R.id.btnLoadLocation:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            Snackbar.make(relativeLayout, "需要給予權限", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("確認", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ActivityCompat.requestPermissions(AddFood.this, PERMISSIONS_LOCATION, Request_Location);
                                        }
                                    }).show();
                        }else {
                            ActivityCompat.requestPermissions(AddFood.this, PERMISSIONS_LOCATION, Request_Location);
                        }
                }else {
                    getLocation();
                }

                break;
        }
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
            lat = location.getLatitude();
            lon = location.getLongitude();
            Log.i(TAG, "getLocation: " + lat + "," + lon);

            Geocoder geocoder = new Geocoder(this, Locale.TAIWAN);

            if (lat != 0 && lon != 0) {

                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                    LocalAddress = addresses.get(0).getAdminArea() +
                            addresses.get(0).getLocality() + addresses.get(0).getThoroughfare() +
                            addresses.get(0).getFeatureName() + "號";
                    Log.i(TAG, "getLocation: " + LocalAddress);
                    edtLocation.setText(LocalAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TYPE_PHOTO && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            uriImage(uri);
            btnPhoto.setVisibility(View.GONE);
            btnCamera.setVisibility(View.GONE);
            imageview.setVisibility(View.VISIBLE);
            btnclc.setVisibility(View.VISIBLE);

            ContentResolver cr = getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                //判斷照片為橫向或者為直向，並進入ScalePic判斷圖片是否要進行縮放
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    ScalePic(bitmap, mPhone.heightPixels);
                } else {
                    ScalePic(bitmap, mPhone.widthPixels);
                }
                Log.i(TAG, "onActivityResult: " + uri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == TYPE_CAMERA && resultCode == RESULT_OK) {
            btnPhoto.setVisibility(View.GONE);
            btnCamera.setVisibility(View.GONE);
            imageview.setVisibility(View.VISIBLE);
            btnclc.setVisibility(View.VISIBLE);
            ContentResolver cr = getContentResolver();
            try {
                Bitmap image = BitmapFactory.decodeStream(cr.openInputStream(fileUri));
                if (image.getWidth() > image.getHeight()) {
                    ScalePic(image, mPhone.heightPixels);
                } else {
                    ScalePic(image, mPhone.widthPixels);
                }
                Log.i(TAG, "onActivityResult: " + fileUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void ScalePic(Bitmap b, int phone) {
        float mScale = 1;
        //如果圖片大小大於手機寬度就進行縮放
        if (b.getWidth() > phone) {
            mScale = (float) phone / (float) b.getWidth();
            Log.i(TAG, "ScalePic: " + mScale + " phone :" + phone + " b:" + b.getWidth());
            Matrix matrix = new Matrix();
            matrix.setScale(mScale, mScale);
            Bitmap scaleBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
            imageview.setImageBitmap(scaleBitmap);
        } else {
            imageview.setImageBitmap(b);
        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FoodNotes");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.i(TAG, "getOutputMediaFile: " + "資料夾未被創建");
            }
        }

        String timeImage = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.TAIWAN).format(new Date());

        File media;
        media = new File(mediaStorageDir + File.separator + timeImage + ".jpg");

        return media;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void uriImage(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cs = getContentResolver().query(uri, filePathColumn, null, null, null);
        cs.moveToFirst();

        int columnIndex = cs.getColumnIndex(filePathColumn[0]);
        imgname = cs.getString(columnIndex);
        Log.i(TAG, "uriImage: " + imgname);
        cs.close();
        if (imgname == null) {
            String wholeId = DocumentsContract.getDocumentId(uri);
            String id = wholeId.split(":")[1];
            String[] column = {MediaStore.Images.Media.DATA};

            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);
            int Index = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                imgname = cursor.getString(Index);
            }
            cursor.close();
            Log.i(TAG, "uriImage: " + imgname);
        }
    }

    private void showDialog() {
        pDialog = new ProgressDialog(this);
        if (!pDialog.isShowing()) {

            pDialog.setMessage("新增中");
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
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }
}
