package com.example.professorlee.foodnotes;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PagerFragment extends Fragment {

    private static final String TAG = "PagerFragment";

    private static final String IMAGE = "IMAGE";

    private int resImageId = 0;

    public static final PagerFragment newInstance(int resImageId) {
        PagerFragment pf = new PagerFragment();
        Bundle b = new Bundle();
        b.putInt(IMAGE, resImageId);

        pf.setArguments(b);

        return pf;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resImageId = getArguments().getInt(IMAGE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView image = (ImageView) view.findViewById(R.id.imageView1);
        image.setAdjustViewBounds(true);
        image.setImageResource(resImageId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
