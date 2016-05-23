package com.menupicture.menupicture;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class MainActivity extends Activity {

    private GridView gridView;

    public static GridViewAdapter gridAdapter;

    public static final ReadWriteLock imageListLock = new ReentrantReadWriteLock();
    public static ArrayList<ImageItem> imageList = new ArrayList<ImageItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tab_host = (TabHost)findViewById(R.id.tabHost);
        tab_host.setup();

        //Highlight, zoom, pan around
        TabHost.TabSpec spec = tab_host.newTabSpec("Highlight Tab");
        spec.setContent(R.id.highlight_tab);
        spec.setIndicator(getResources().getString(R.string.highlight_tab));
        tab_host.addTab(spec);

        //Picture View
        spec = tab_host.newTabSpec("Picture Tab");
        spec.setContent(R.id.picture_tab);
        spec.setIndicator(getResources().getString(R.string.picture_tab));
        tab_host.addTab(spec);

        gridView = (GridView) findViewById(R.id.picture_tab);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item, imageList);
        gridView.setAdapter(gridAdapter);
    }
}
