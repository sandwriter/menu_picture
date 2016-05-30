package com.menupicture.menupicture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private GridView gridView;

    public static GridViewAdapter gridAdapter;

    public static final ReadWriteLock imageListLock = new ReentrantReadWriteLock();
    public static ArrayList<ImageItem> imageList = new ArrayList<ImageItem>();

    private ImageViewTouch touchView;
    private HighlightView highlightView;
    private FloatingActionButton fab;
    private FloatingActionButton fab_eye;
    private FloatingActionButton fab_highlight;
    private FloatingActionButton fab_camera;
    private FrameLayout fab_framelayout;

    //Animations
    private Animation show_fab_eye;
    private Animation hide_fab_eye;
    private Animation show_fab_highlight;
    private Animation hide_fab_highlight;
    private Animation show_fab_camera;
    private Animation hide_fab_camera;

    private boolean touch_mode;
    private boolean fab_expand;

    private static Bitmap menu_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab_expand = false;

        fab_eye = (FloatingActionButton)findViewById(R.id.fab_eye);
        fab_highlight = (FloatingActionButton)findViewById(R.id.fab_highlight);
        fab_camera = (FloatingActionButton)findViewById(R.id.fab_camera);
        fab_framelayout = (FrameLayout)findViewById(R.id.fab_framelayout);

        show_fab_eye = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_eye_show);
        hide_fab_eye = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_eye_hide);
        show_fab_highlight = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_highlight_show);
        hide_fab_highlight = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_highlight_hide);
        show_fab_camera = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_camera_show);
        hide_fab_camera = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_camera_hide);

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
        menu_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.indian2, options);

        touchView = (ImageViewTouch) findViewById(R.id.touch_image);
        touchView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        touchView.setImageBitmap(menu_bitmap, null, -1, -1);

        highlightView = (HighlightView) findViewById(R.id.highlight_tab);
        highlightView.setMenuBitmap(menu_bitmap);
        highlightView.setMatrix(touchView.getDisplayMatrix());

        touch_mode = true;

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!fab_expand) {
                    fab_expand = true;
                    expandFAB();
                }else{
                    fab_expand = false;
                    hideFAB();
                }
/*
                if (touch_mode) {
                    Log.v(TAG, touchView.getDisplayMatrix().toShortString());
                    highlightView.setMatrix(touchView.getDisplayMatrix());
                    highlightView.bringToFront();
                    fab.setImageResource(R.drawable.highlight);
                    touch_mode = false;
                }else{
                    highlightView.reset();
                    touchView.setImageBitmap(highlightView.getFinalBitmap(), touchView.getDisplayMatrix(), -1, -1);
                    touchView.bringToFront();
                    fab.setImageResource(R.drawable.eye);
                    touch_mode = true;
                }
*/
            }
        });

        fab_eye.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.v(TAG, "eye clicked.");

                highlightView.reset();
                touchView.setImageBitmap(highlightView.getFinalBitmap(), touchView.getDisplayMatrix(), -1, -1);
                touchView.bringToFront();
                fab.setImageResource(R.drawable.eye);
                touch_mode = true;
                hideFAB();
                fab_expand = false;
            }
        });

        fab_highlight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                highlightView.setMatrix(touchView.getDisplayMatrix());
                highlightView.bringToFront();
                fab.setImageResource(R.drawable.highlight);
                touch_mode = false;
                hideFAB();
                fab_expand = false;
            }
        });

        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFAB();
                fab_expand = false;
                Log.v(TAG, "fab_camera clicked.");
            }
        });
    }

    public static Bitmap GetMenuBitmap(){
        return menu_bitmap;
    }

    private void expandFAB() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fab_eye.getLayoutParams();
        layoutParams.rightMargin += (int) (fab_eye.getWidth() * 1.7);
        layoutParams.bottomMargin += (int) (fab_eye.getHeight() * 0.25);
        fab_eye.setLayoutParams(layoutParams);
        fab_eye.startAnimation(show_fab_eye);
        fab_eye.setClickable(true);

        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) fab_highlight.getLayoutParams();
        layoutParams2.rightMargin += (int) (fab_highlight.getWidth() * 1.5);
        layoutParams2.bottomMargin += (int) (fab_highlight.getHeight() * 1.5);
        fab_highlight.setLayoutParams(layoutParams2);
        fab_highlight.startAnimation(show_fab_highlight);
        fab_highlight.setClickable(true);

        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) fab_camera.getLayoutParams();
        layoutParams3.rightMargin += (int) (fab_camera.getWidth() * 0.25);
        layoutParams3.bottomMargin += (int) (fab_camera.getHeight() * 1.7);
        fab_camera.setLayoutParams(layoutParams3);
        fab_camera.startAnimation(show_fab_camera);
        fab_camera.setClickable(true);

        fab_framelayout.bringToFront();

    }

    private void hideFAB() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fab_eye.getLayoutParams();
        layoutParams.rightMargin -= (int) (fab_eye.getWidth() * 1.7);
        layoutParams.bottomMargin -= (int) (fab_eye.getHeight() * 0.25);
        fab_eye.setLayoutParams(layoutParams);
        fab_eye.startAnimation(hide_fab_eye);
        fab_eye.setClickable(false);

        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) fab_highlight.getLayoutParams();
        layoutParams2.rightMargin -= (int) (fab_highlight.getWidth() * 1.5);
        layoutParams2.bottomMargin -= (int) (fab_highlight.getHeight() * 1.5);
        fab_highlight.setLayoutParams(layoutParams2);
        fab_highlight.startAnimation(hide_fab_highlight);
        fab_highlight.setClickable(false);

        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) fab_camera.getLayoutParams();
        layoutParams3.rightMargin -= (int) (fab_camera.getWidth() * 0.25);
        layoutParams3.bottomMargin -= (int) (fab_camera.getHeight() * 1.7);
        fab_camera.setLayoutParams(layoutParams3);
        fab_camera.startAnimation(hide_fab_camera);
        fab_camera.setClickable(false);
    }
}
