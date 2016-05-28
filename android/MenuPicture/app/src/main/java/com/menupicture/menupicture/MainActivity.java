package com.menupicture.menupicture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.GridView;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;


public class MainActivity extends Activity {

    private GridView gridView;

    public static GridViewAdapter gridAdapter;

    public static final ReadWriteLock imageListLock = new ReentrantReadWriteLock();
    public static ArrayList<ImageItem> imageList = new ArrayList<ImageItem>();

    private ImageViewTouch touchView;
    private HighlightView highlightView;
    private FloatingActionButton fab;

    private boolean touch_mode;

    private static Bitmap menu_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tab_host = (TabHost)findViewById(R.id.tabHost);
        tab_host.setup();

        //Highlight, zoom, pan around
        TabHost.TabSpec spec = tab_host.newTabSpec("Highlight Tab");
        spec.setContent(R.id.frame_layout);
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

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inScaled = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        menu_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.two_line, options);

        touchView = (ImageViewTouch) findViewById(R.id.touch_image);
        touchView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);

        touchView.setImageBitmap(menu_bitmap, null, -1, -1);

        highlightView = (HighlightView) findViewById(R.id.highlight_tab);
        highlightView.setMenuBitmap(menu_bitmap);

        touch_mode = true;

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (touch_mode) {
                    highlightView.bringToFront();
                    fab.setImageResource(R.drawable.highlight);
                    touch_mode = false;
                }else{
                    touchView.bringToFront();
                    fab.setImageResource(R.drawable.eye);
                    touch_mode = true;
                }
            }
        });
    }

    public static Bitmap GetMenuBitmap(){
        return menu_bitmap;
    }
}
