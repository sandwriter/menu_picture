package com.menupicture.menupicture;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TabHost;

import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;


public class MainActivity extends Activity {
    public enum Mode {Touch, Highlight};

    private Map<Mode, Integer> icon_id_map;

    private static final String TAG = "MainActivity";

    private GridView gridView;

    public static GridViewAdapter gridAdapter;

    public static final ReadWriteLock imageListLock = new ReentrantReadWriteLock();
    public static ArrayList<ImageItem> imageList = new ArrayList<ImageItem>();

    private ImageViewTouch touchView;
    private HighlightView highlightView;
    private FloatingActionButton fab;
    private FloatingActionButton fab_touch;
    private FloatingActionButton fab_highlight;
    private FloatingActionButton fab_camera;
    private FrameLayout fab_framelayout;
    private ImageView full_img;

    private boolean fab_expand = false;

    //Animations
    private Animation show_fab_eye;
    private Animation hide_fab_eye;
    private Animation show_fab_highlight;
    private Animation hide_fab_highlight;
    private Animation show_fab_camera;
    private Animation hide_fab_camera;

    // The Bitmap representing the menu image.
    private static Bitmap bitmap;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    static final int REQUEST_CODE_ASK_CAMERA_PERMISSIONS = 3;

    private String menuPicturePath;

    private Matrix displayMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        icon_id_map = new HashMap<Mode, Integer>();
        icon_id_map.put(Mode.Touch, R.drawable.eye);
        icon_id_map.put(Mode.Highlight, R.drawable.highlight);

        displayMatrix = new Matrix();

        touchView = (ImageViewTouch) findViewById(R.id.touch_image);
        highlightView = (HighlightView) findViewById(R.id.highlight_tab);
        // Main floating action button.
        fab = (FloatingActionButton) findViewById(R.id.fab);
        // Full image view that open on click.
        full_img = (ImageView) findViewById(R.id.full_img);
        full_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                full_img.setVisibility(View.GONE);
            }
        });

        // Set of smaller floating action button triggered by the main one.
        fab_touch = (FloatingActionButton)findViewById(R.id.fab_eye);
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
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageItem item = (ImageItem)gridView.getAdapter().getItem(position);
                full_img.setVisibility(View.VISIBLE);
                Ion.with(full_img).placeholder(R.drawable.loading).load(item.getImageUrl());
            }
        });

        // Touch view by default. Use a stock image.
        // TODO(wenjiesha) Camera by default.
        touchView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        InitializeBitmap();
        touchView.setImageBitmap(bitmap);
        highlightView.setVisibility(View.GONE);

        // Expand/hide the mini floating action buttons when necessary.
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                displayMatrix = touchView.getDisplayMatrix();

                if (!fab_expand) {
                    fab_expand = true;
                    expandFAB();
                }else{
                    fab_expand = false;
                    hideFAB();
                }
            }
        });

        // Touch/eye/zoom/pan view is chose.
        fab_touch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                touchView.setImageBitmap(getImageBitmap(), displayMatrix, -1, -1);
                touchView.setVisibility(View.VISIBLE);
                highlightView.setVisibility(View.GONE);
                hideFAB(Mode.Touch);
            }
        });

        // Highlight view is chosen.
        fab_highlight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                highlightView.setImageBitmap(getImageBitmap());
                highlightView.setMatrix(displayMatrix);
                highlightView.setVisibility(View.VISIBLE);
                touchView.setVisibility(View.GONE);
                hideFAB(Mode.Highlight);
            }
        });

        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Log.v(TAG, "Error create image file stub: " + ex.getMessage());
                    }
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });
    }

    private void InitializeBitmap() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inScaled = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.indian2, options);
    }

    public static Bitmap GetMenuBitmap(){
        return bitmap;
    }

    private void expandFAB() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fab_touch.getLayoutParams();
        layoutParams.rightMargin += (int) (fab_touch.getWidth() * 1.7);
        layoutParams.bottomMargin += (int) (fab_touch.getHeight() * 0.25);
        fab_touch.setLayoutParams(layoutParams);
        fab_touch.startAnimation(show_fab_eye);
        fab_touch.setClickable(true);

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
        hideFAB(null);
    }

    private void hideFAB(Mode mode) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fab_touch.getLayoutParams();
        layoutParams.rightMargin -= (int) (fab_touch.getWidth() * 1.7);
        layoutParams.bottomMargin -= (int) (fab_touch.getHeight() * 0.25);
        fab_touch.setLayoutParams(layoutParams);
        fab_touch.startAnimation(hide_fab_eye);
        fab_touch.setClickable(false);

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

        fab_expand = false;
        if(mode != null) {
            fab.setImageResource(icon_id_map.get(mode));

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        int hasWriteExternalStoragePermission= checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_CAMERA_PERMISSIONS);
            return null;
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        menuPicturePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO){
            if (resultCode == RESULT_OK){
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                File menuPictureFile = new File(menuPicturePath);
                if (menuPictureFile.exists()) {
                    Log.v(TAG, "menu picture path: "+ menuPictureFile);
                    bitmap = BitmapFactory.decodeFile(menuPicturePath, options);
                    if (bitmap == null){
                        Log.v(TAG, "bit map is empty!!!");
                    }
                    // Reset all transformation for new image.
                    displayMatrix = new Matrix();
                    fab_touch.callOnClick();
                }
            }
        }else if(requestCode == REQUEST_CODE_ASK_CAMERA_PERMISSIONS){
            if(resultCode == RESULT_OK){
                fab_camera.callOnClick();
            }
        }
    }

    public Bitmap getImageBitmap() {
        return bitmap;
    }
}
