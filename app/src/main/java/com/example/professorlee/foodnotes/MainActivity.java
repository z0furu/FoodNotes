package com.example.professorlee.foodnotes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{

    private static final String TAG = "MainActivity";
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.gridview)
    GridView gridview;
    @Bind(R.id.viewpager)
    ViewPager viewpager;
    @Bind(R.id.viewGroup)
    LinearLayout viewGroup;
    @Bind(R.id.viewpager_RelativeLayout)
    RelativeLayout viewpagerRelativeLayout;

    private int[] images = {
            R.drawable.cat, R.drawable.flower, R.drawable.hippo,
            R.drawable.monkey, R.drawable.mushroom, R.drawable.panda};

    private String[] txtImages = {
            "新增美食", "查看記錄", "test", "test", "Test", "test"};

    private int dotCount;
    private ImageView[] dots;
    private PagerAdapter adapter;
    private Handler handle = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);



        setSupportActionBar(toolbar);
        initViewPager();
        storeGrid();

        handle.postDelayed(run, 2000);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handle.postDelayed(run, 2000);
    }

    private void initViewPager() {
        List<Fragment> fragments = getFragment();

        adapter = new PagerAdapter(getSupportFragmentManager(), fragments);
        viewpager.setAdapter(adapter);
        viewpager.addOnPageChangeListener(this);
        setUiPageViewController();

    }

    private List<Fragment> getFragment() {
        List<Fragment> fragments = new ArrayList<>();

        fragments.add(PagerFragment.newInstance(R.drawable.cat));
        fragments.add(PagerFragment.newInstance(R.drawable.flower));
        fragments.add(PagerFragment.newInstance(R.drawable.panda));

        return fragments;

    }

    private void storeGrid() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < images.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("image", images[i]);
            item.put("text", txtImages[i]);
            items.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(this,
                items, R.layout.grid_item, new String[]{"image", "text"},
                new int[]{R.id.image, R.id.text});


        gridview.setNumColumns(3);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0 :
                        startActivity(new Intent(MainActivity.this, AddFood.class));
                        break;
                    case 1 :
                        startActivity(new Intent(MainActivity.this, FoodList.class));
                        break;
                    case 2 :
                        startActivity(new Intent(MainActivity.this, Search_Food.class));
                        break;
                }
            }
        });
    }

    private void setUiPageViewController() {
        dotCount = adapter.getCount();
        dots = new ImageView[dotCount];
        for (int i = 0; i < dotCount; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4,4,4,4);
            viewGroup.addView(dots[i], params);
        }
        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot)); //預設一進畫面，是被選中的
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < dotCount; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));
        }
        Log.i(TAG, "onPageSelected: " + position);
        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private Runnable run = new Runnable() {
        @Override
        public void run() {
            if (viewpager.getCurrentItem() < dotCount - 1) {
                viewpager.setCurrentItem(viewpager.getCurrentItem() + 1);
                handle.postDelayed(run, 2000);
            }else {
                viewpager.setCurrentItem(0);
                handle.postDelayed(run, 2000);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (handle != null) {
            handle.removeCallbacks(run);
        }
    }
}
