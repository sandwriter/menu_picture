package com.menupicture.menupicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
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
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by wenjie on 5/15/16.
 */
public class HighlightView extends View {
    private Bitmap bitmap;
    // canvas is used to store all committed previous path.
    private Canvas canvas;
    private Path path;
    private Paint highlight_paint;
    private Paint bitmap_paint;
    private Bitmap menu_bitmap;

    // Path current point.
    private float x;
    private float y;
    private static final float TOUCH_TOLERANCE = 4;

    private static final String CLOUD_VISION_API_KEY = "AIzaSyBFoF4sDsSj6FV8O-cYsyHbU9stfIrACJg";

    private static final String TAG = "HighlightView";

    public HighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);

        path = new Path();
        highlight_paint = new Paint(Paint.DITHER_FLAG);
        highlight_paint.setAntiAlias(true);
        highlight_paint.setColor(Color.RED);
        highlight_paint.setStyle(Paint.Style.STROKE);
        highlight_paint.setStrokeJoin(Paint.Join.ROUND);
        highlight_paint.setStrokeCap(Paint.Cap.SQUARE);
        highlight_paint.setStrokeWidth(12);

        bitmap_paint = new Paint();
        bitmap_paint.setAntiAlias(true);
        bitmap_paint.setFilterBitmap(true);
        bitmap_paint.setDither(true);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inScaled = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        menu_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.two_line, options);

        new AsyncTask<Object, Void, String>(){
            @Override
            protected String doInBackground(Object... params) {
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
                    return response.toPrettyString();

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
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
        if (menu_width/w >= menu_height/h){
            // scale with aspect ratio to width.
            scale = w/menu_width;
            dy = (h - menu_height * scale) / 2.0f;
        }else{
            // scale by height.
            scale = h/menu_height;
            dx = (w - menu_width * scale) / 2.0f;
        }

        Matrix transformation = new Matrix();
        transformation.preScale(scale, scale);
        transformation.postTranslate(dx, dy);

        canvas.drawBitmap(menu_bitmap, transformation, bitmap_paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bitmap, 0, 0, bitmap_paint);
        canvas.drawPath(path, highlight_paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
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

    private void touch_start(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        this.x = x;
        this.y = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x-this.x);
        float dy = Math.abs(y-this.y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            path.quadTo(this.x, this.y, (x+this.x)/2, (y+this.y)/2);
            this.x = x;
            this.y = y;
        }
    }

    private void touch_up() {
        path.lineTo(this.x, this.y);
        canvas.drawPath(path, highlight_paint);

        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        Log.v(TAG, bounds.toShortString());

        path.reset();
    }
}
