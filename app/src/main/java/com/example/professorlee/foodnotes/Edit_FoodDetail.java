package com.example.professorlee.foodnotes;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Edit_FoodDetail extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "Edit_FoodDetail";
    @Bind(R.id.foodimage)
    ImageView foodimage;
    @Bind(R.id.shopname)
    EditText edtShopname;
    @Bind(R.id.date)
    Button edtDate;
    @Bind(R.id.type)
    Button edtType;
    @Bind(R.id.location)
    EditText edtLocation;
    @Bind(R.id.linearLayout)
    LinearLayout linearLayout;

    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static String[] PERMISSIONS_CAMERA = {Manifest.permission.CAMERA};
    private static final int Request_Camera = 0;
    private static final int Request_Storage = 1;

    private Uri fileUri; //相機路徑


    private static final int TYPE_PHOTO = 1;
    private static final int TYPE_CAMERA = 2;
    private static final int MEDIA_TYPE_IMAGE = 3;

    private static String imgname = null;
    private DisplayMetrics mPhone;
    private String account;
    boolean isCamera = false, isPhote = false;

    String[] foodType = {"早餐", "午餐", "晚餐", "消夜"};
    int year, month, date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_food_detail);
        ButterKnife.bind(this);

        SharedPreferences setting = getSharedPreferences("login", 0);
        account = setting.getString("account", "");

        mPhone = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mPhone);

        initLayout();  //接受美食數值

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_food_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.btnEdit) {

        }
        return super.onOptionsItemSelected(item);
    }


    private void initLayout() {
        Bundle bundle = this.getIntent().getExtras();
        String strShopName = bundle.getString("shopName");
        edtShopname.setText(strShopName);

        String strDate = bundle.getString("Date");
        String[] split = strDate.split("/");
        year = Integer.parseInt(split[0]);
        month = Integer.parseInt(split[1]) - 1;
        date = Integer.parseInt(split[2]);
        Log.i(TAG, "initLayout: " + split[0] + "," + split[1] +"," + split[2]);
        edtDate.setText(strDate);


        String strFoodType = bundle.getString("FoodType");
        edtType.setText(strFoodType);


        String strImage = bundle.getString("image");
        Glide.with(Edit_FoodDetail.this).load("http://163.17.9.116/Lu/food_note/upload/" + strImage).fitCenter().into(foodimage);

        String strLocation = bundle.getString("Location");
        edtLocation.setText(strLocation);
    }

    @OnClick(R.id.date) void choseDate() {
        DatePickerDialog pickerDialog = DatePickerDialog.newInstance(
                Edit_FoodDetail.this,
                year,
                month,
                date);
        pickerDialog.setThemeDark(true);
        pickerDialog.show(getFragmentManager(), "請選擇日期");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

    }

    @OnClick(R.id.type) void choseType() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setSingleChoiceItems(foodType, -1, ((dialog, which) -> {
                    switch (which) {
                        case 0:
                            edtType.setText("早餐");
                            break;
                        case 1:
                            edtType.setText("午餐");
                            break;
                        case 2:
                            edtType.setText("晚餐");
                            break;
                        case 3:
                            edtType.setText("消夜");
                            break;
                    }
                }))
                .setPositiveButton("確認", ((dialog1, which1) -> dialog1.dismiss()))
                .show();
    }


    @OnClick(R.id.foodimage)
    void choseImage() {
        new AlertDialog.Builder(this)
                .setPositiveButton("相簿", ((dialog, which) -> ToAlbums()))
                .setNeutralButton("相機", ((dialog1, which1) -> ToCamera()))
                .show();
    }

    private void ToCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Snackbar.make(linearLayout, "需要給予權限", Snackbar.LENGTH_INDEFINITE)
                        .setAction("確認", view -> ActivityCompat.requestPermissions(Edit_FoodDetail.this, PERMISSIONS_CAMERA, Request_Camera)).show();
            } else {
                ActivityCompat.requestPermissions(Edit_FoodDetail.this, PERMISSIONS_CAMERA, Request_Camera);
            }
        }else {
            IntentCamera();
            isCamera = true;
        }
    }

    private void IntentCamera() {
        Intent cameraintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);  //儲存相片
        cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); //儲存到相簿預設位置
        startActivityForResult(cameraintent, TYPE_CAMERA);
    }

    private Uri getOutputMediaFileUri(int mediaTypeImage) {
        return Uri.fromFile(getOutputMediaFile(mediaTypeImage));
    }

    private File getOutputMediaFile(int type) {
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

    private void ToAlbums() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(linearLayout, "需要給予權限", Snackbar.LENGTH_INDEFINITE)
                        .setAction("確認", view -> ActivityCompat.requestPermissions(Edit_FoodDetail.this, PERMISSIONS_STORAGE, Request_Storage)).show();
            }else {
                ActivityCompat.requestPermissions(Edit_FoodDetail.this, PERMISSIONS_STORAGE, Request_Storage);
            }
        }else {
            IntentAlbums();
            isPhote = true;
        }
    }

    private void IntentAlbums() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, TYPE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult: " + requestCode + "," + grantResults[0] +"," + grantResults[1]);
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            IntentCamera();
        }else if (requestCode == 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            IntentAlbums();
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TYPE_PHOTO && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            uriImage(uri);
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
        }else if (requestCode == TYPE_CAMERA && resultCode == RESULT_OK) {
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
            foodimage.setImageBitmap(scaleBitmap);
        } else {
            foodimage.setImageBitmap(b);
        }
    }
}
