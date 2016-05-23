package com.menupicture.menupicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.StreamingContent;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequest;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Vertex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by wenjie on 5/15/16.
 */
public class HighlightView extends View {
    private class HighlightRectF{
        public HighlightRectF(RectF rectf, boolean on, String word){
            this.rectf = rectf;
            this.on = on;
            this.word = word;
        }

        public RectF rectf;
        public boolean on;
        public boolean touched;
        public String word;
    }

    private Bitmap bitmap;
    // canvas is used to store all committed previous path.
    private Canvas canvas;
    private Paint bitmap_paint;
    private Paint rectf_on_paint;
    private Paint rectf_off_paint;
    private Bitmap menu_bitmap;

    // Path current point.
    private float x;
    private float y;
    private static final float TOUCH_TOLERANCE = 4;

    private static final String CLOUD_VISION_API_KEY = "AIzaSyBFoF4sDsSj6FV8O-cYsyHbU9stfIrACJg";
    private static final String SCE_ID = "000057874177480001711:2ywzhtb3u6q";

    private static final String TAG = "HighlightView";

    private final ReadWriteLock bound_lock = new ReentrantReadWriteLock();
    private final Lock bound_rLock = bound_lock.readLock();
    private final Lock bound_wLock = bound_lock.writeLock();


    private final ReadWriteLock matrix_lock = new ReentrantReadWriteLock();
    private final Lock matrix_rLock = matrix_lock.readLock();
    private final Lock matrix_wLock = matrix_lock.writeLock();

    private Matrix transformation;

    private java.util.ArrayList<HighlightRectF> rect_list = new java.util.ArrayList<HighlightRectF>();

    public HighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);

        bitmap_paint = new Paint();
        bitmap_paint.setAntiAlias(true);
        bitmap_paint.setFilterBitmap(true);
        bitmap_paint.setDither(true);

        rectf_off_paint = new Paint(Paint.DITHER_FLAG);
        rectf_off_paint.setAntiAlias(true);
        rectf_off_paint.setColor(Color.YELLOW);
        rectf_off_paint.setStyle(Paint.Style.FILL);
        rectf_off_paint.setAlpha(15);

        rectf_on_paint = new Paint(Paint.DITHER_FLAG);
        rectf_on_paint.setAntiAlias(true);
        rectf_on_paint.setColor(Color.YELLOW);
        rectf_on_paint.setStyle(Paint.Style.FILL);
        rectf_on_paint.setAlpha(100);


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inScaled = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        menu_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.two_line, options);

        new AsyncTask<Object, Void, List<EntityAnnotation>>() {
            @Override
            protected List<EntityAnnotation> doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(new
                            VisionRequestInitializer(CLOUD_VISION_API_KEY));
                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        menu_bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("TEXT_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    Log.d(TAG, "response: " + response.toPrettyString());
                    return convertToRectList(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                } catch (Exception e) {
                    Log.d(TAG, "Cloud Vision API request failed. Check logs for details. " + e.getMessage());
                }
                return new ArrayList<EntityAnnotation>();
            }

            protected void onPostExecute(List<EntityAnnotation> result) {
                bound_wLock.lock();
                try {
                    rect_list.clear();
                    for (EntityAnnotation entity : result) {
                        rect_list.add(new HighlightRectF(GetRectF(entity.getBoundingPoly().getVertices()), false, entity.getDescription()));
                    }
                } finally {
                    bound_wLock.unlock();
                }
                invalidate();
            }

            private RectF GetRectF(List<Vertex> vertices) {
                RectF rectf = new RectF();
                int left = vertices.get(0).getX();
                int top = vertices.get(0).getY();
                int right = vertices.get(2).getX();
                int bottom = vertices.get(2).getY();
                rectf.set(left, top, right, bottom);

                matrix_rLock.lock();
                try{
                    transformation.mapRect(rectf);
                }finally {
                    matrix_rLock.unlock();
                }
                return rectf;
            }

            private List<EntityAnnotation> convertToRectList(BatchAnnotateImagesResponse response) {
                List<EntityAnnotation> annotations = response.getResponses().get(0).getTextAnnotations();
                return annotations.subList(1, annotations.size());

            }

        }.execute();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        float scale = 1.0f;
        float dx = 0.0f;
        float dy = 0.0f;
        float menu_width = menu_bitmap.getWidth();
        float menu_height = menu_bitmap.getHeight();
        if (menu_width / w >= menu_height / h) {
            // scale with aspect ratio to width.
            scale = w / menu_width;
            dy = (h - menu_height * scale) / 2.0f;
        } else {
            // scale by height.
            scale = h / menu_height;
            dx = (w - menu_width * scale) / 2.0f;
        }

        matrix_wLock.lock();
        try {
            transformation = new Matrix();
            transformation.preScale(scale, scale);
            transformation.postTranslate(dx, dy);
        }finally {
            matrix_wLock.unlock();
        }

        canvas.drawBitmap(menu_bitmap, transformation, bitmap_paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bitmap, 0, 0, bitmap_paint);

        bound_rLock.lock();
        try {
            for (HighlightRectF rect : rect_list) {
                boolean on = rect.on ^ rect.touched;
                if (on) {
                    canvas.drawRect(rect.rectf, rectf_on_paint);
                }else{
                    canvas.drawRect(rect.rectf, rectf_off_paint);
                }
            }
        } finally {
            bound_rLock.unlock();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;

    }

    private void touch(float x, float y){
        bound_wLock.lock();
        try{
            for (HighlightRectF rect : rect_list){
                if (rect.rectf.contains(x, y)){
                    rect.touched = true;
                }
            }
        }finally {
            bound_wLock.unlock();
        }
    }

    private void touch_start(float x, float y) {
        touch(x, y);
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - this.x);
        float dy = Math.abs(y - this.y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            touch(x, y);
        }
    }

    private void touch_up() {
        // clear all changed bits and permanent the change.
        bound_wLock.lock();
        try{
            for (HighlightRectF rect : rect_list){
                if (rect.touched){
                    rect.on = rect.on ^ rect.touched;
                    rect.touched = false;
                }
            }
        }finally {
            bound_wLock.unlock();
        }

        search_picture();
    }

    private void search_picture() {
        new AsyncTask<Object, Void, ArrayList<ImageItem>>(){
            @Override
            protected ArrayList<ImageItem> doInBackground(Object... params) {
                ArrayList<ImageItem> image_list = new ArrayList<ImageItem>();
                try{
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Customsearch.Builder builder = new Customsearch.Builder(httpTransport, jsonFactory, null);
                    builder.setCustomsearchRequestInitializer(new CustomsearchRequestInitializer(){
                        @Override
                        protected void initializeCustomsearchRequest(CustomsearchRequest<?> request) throws IOException {
                            super.initializeCustomsearchRequest(request);
                            request.setKey(CLOUD_VISION_API_KEY);
                            request.set("cx", SCE_ID);
                            request.set("searchType", "image");
                            request.set("safe", "high");
                            request.set("num", new Long(3));
                        }
                    });

                    Customsearch customSearch = builder.build();

                    ArrayList<String> menu_list = getMenuList(rect_list);

                    for (String menu: menu_list) {
                        Search searchResult = customSearch.cse().list(menu).execute();
                        image_list.addAll(getImageList(searchResult, menu));
                    }
                }catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                } catch (Exception e) {
                    Log.d(TAG, "CustomSearch API request failed. Check logs for details. " + e.getMessage());
                }
                return image_list;
            }

            private ArrayList<String> getMenuList(ArrayList<HighlightRectF> rect_list) {
                ArrayList<String> menu_list = new ArrayList<String>();
                menu_list.add("grilled steak");
                menu_list.add("roasted pork");
                return menu_list;
            }

            protected void onPostExecute(ArrayList<ImageItem> image_list) {
                MainActivity.imageListLock.writeLock().lock();
                try {
                    MainActivity.imageList.clear();
                    MainActivity.imageList.addAll(image_list);
                }finally {
                    MainActivity.imageListLock.writeLock().unlock();
                }
                MainActivity.gridAdapter.notifyDataSetChanged();
            }

            private ArrayList<ImageItem> getImageList(Search searchResult, String menu) {
                ArrayList<ImageItem> image_list = new ArrayList<ImageItem>();

                for (Result result : searchResult.getItems()) {
                    image_list.add(new ImageItem(result.getLink(), menu));
                }

                return image_list;
            }
        }.execute();
    }
}
