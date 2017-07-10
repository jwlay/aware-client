package com.aware.ui.esms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.ESM;
import com.aware.R;
import com.aware.providers.ESM_Provider;
import com.aware.utils.Aware_TTS;
import com.aware.utils.ESM_ImageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

/**
 * Created by Jan Wohlfahrt-Laymann on 2017-05-30.
 */

public class ESM_ImageManipulation extends ESM_Question {

    public ESM_ImageManipulation() throws JSONException {
        this.setType(ESM.TYPE_ESM_IMAGE_MANIPULATION);
    }

    @Override
    protected ESM_Question setID(int id) {
        if (this.getClass().equals(ESM_ImageManipulation.class)) {
            try {
                Class<?> c = Class.forName(getESM_Class());
                ESM_ImageManipulation subclass = (ESM_ImageManipulation) c.newInstance();
                subclass.rebuild(this.esm).setID(id);
                return subclass;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return super.setID(id);
    }

    public class ImageInstructionSpeaker extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... inst) {
            Intent speak = new Intent(Aware_TTS.ACTION_AWARE_TTS_SPEAK);
            try {
                JSONObject instructions = new JSONObject(inst[0]);
                speak.putExtra(Aware_TTS.EXTRA_TTS_TEXT, instructions.getString("Text"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            speak.putExtra(Aware_TTS.EXTRA_TTS_REQUESTER, getContext().getApplicationContext().getPackageName());
            getActivity().sendBroadcast(speak);
            return null;
        }
    }

    @Override
    public void sayInstructions() throws JSONException {
        ImageInstructionSpeaker imageInstructionSpeaker = new ImageInstructionSpeaker();
        imageInstructionSpeaker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getInstructions());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ui = inflater.inflate(R.layout.esm_imagemanipulation, null);
        builder.setView(ui);

        esm_dialog = builder.create();
        esm_dialog.setCanceledOnTouchOutside(false);

        try {

            TextView esm_title = (TextView) ui.findViewById(R.id.esm_title);
            esm_title.setText(getTitle());

            TextView esm_instructions = (TextView) ui.findViewById(R.id.esm_instructions);
            JSONObject jsonInstruction = new JSONObject(getInstructions());
            esm_instructions.setText(jsonInstruction.getString("Text"));

            final CanvasView feedback = (CanvasView) ui.findViewById(R.id.esm_draw);
            feedback.requestFocus();
            feedback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (getExpirationThreshold() > 0 && expire_monitor != null)
                            expire_monitor.cancel(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            esm_dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            JSONArray shapes = jsonInstruction.getJSONArray("Shapes");
            try {
                for (int i = 0; i < shapes.length(); i++) {
                    feedback.addShape(shapes.getJSONObject(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Button cancel_text = (Button) ui.findViewById(R.id.esm_cancel);
            cancel_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    esm_dialog.cancel();
                }
            });

            Button submit_text = (Button) ui.findViewById(R.id.esm_submit);
            submit_text.setText(getSubmitButton());
            submit_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (getExpirationThreshold() > 0 && expire_monitor != null)
                            expire_monitor.cancel(true);

                        ContentValues rowData = new ContentValues();
                        rowData.put(ESM_Provider.ESM_Data.ANSWER_TIMESTAMP, System.currentTimeMillis());
                        rowData.put(ESM_Provider.ESM_Data.ANSWER, createJsonOutput(feedback).toString());
                        rowData.put(ESM_Provider.ESM_Data.STATUS, ESM.STATUS_ANSWERED);

                        getActivity().getContentResolver().update(ESM_Provider.ESM_Data.CONTENT_URI, rowData, ESM_Provider.ESM_Data._ID + "=" + getID(), null);

                        Intent answer = new Intent(ESM.ACTION_AWARE_ESM_ANSWERED);
                        answer.putExtra(ESM.EXTRA_ANSWER, rowData.getAsString(ESM_Provider.ESM_Data.ANSWER));
                        getActivity().sendBroadcast(answer);

                        if (Aware.DEBUG) Log.d(Aware.TAG, "Answer:" + rowData.toString());

                        esm_dialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return esm_dialog;
    }

    public JSONObject createJsonOutput(CanvasView feedback) throws JSONException {
        JSONObject response = new JSONObject();
        response.put("Image",ESM_ImageUtils.bitmapToString(feedback.getDrawing()));
        return response;
    }

    public static class CanvasView extends View {
        private static final String TAG = "CirclesDrawingView";

        /**
         * Main bitmap
         */
        private Bitmap mBitmap = null;

        private Rect mMeasuredRect;

        public void addShape(JSONObject shape) throws JSONException {
            String shape_type = shape.getString("type");
            int shape_x = shape.getInt("xPos");
            int shape_y = shape.getInt("yPos");
            int shape_radius = shape.getInt("radius");
            int shape_color = shape.getInt("color");
            initialShapes.add(new CanvasView.CircleArea(shape_x,shape_y,shape_radius,shape_color));
        }

        /**
         * Stores data about single circle
         */
        public static class CircleArea {
            public int radius;
            public int centerX;
            public int centerY;
            public int color;

            CircleArea(int centerX, int centerY, int radius, int color) {
                this.radius = radius;
                this.centerX = centerX;
                this.centerY = centerY;
                this.color = color;
            }

            @Override
            public String toString() {
                return "Circle[" + centerX + ", " + centerY + ", " + radius + ", " + color + "]";
            }
        }

        private final Random mRadiusGenerator = new Random();
        // Radius limit in pixels
        private final static int RADIUS_LIMIT = 100;

        private static final int CIRCLES_LIMIT = 10;

        /**
         * All available circles
         */
        private HashSet<Object> initialShapes = new HashSet<Object>(CIRCLES_LIMIT);
        private HashSet<CircleArea> mCircles = new HashSet<CircleArea>(CIRCLES_LIMIT);
        private SparseArray<CircleArea> mCirclePointer = new SparseArray<CircleArea>(CIRCLES_LIMIT);

        public HashSet<CircleArea> getmCircles() {
            return mCircles;
        }

        /**
         * Default constructor
         *
         * @param ct {@link android.content.Context}
         */
        public CanvasView(final Context ct) {
            super(ct);

            init(ct);
        }

        public CanvasView(final Context ct, final AttributeSet attrs) {
            super(ct, attrs);

            init(ct);
        }

        public CanvasView(final Context ct, final AttributeSet attrs, final int defStyle) {
            super(ct, attrs, defStyle);

            init(ct);
        }

        private void init(final Context ct) {
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

            //Add the circles to the windows
            for (Object shape : initialShapes) {
                if (shape instanceof CircleArea) {
                    CircleArea circle = (CircleArea) shape;

                    //Place the object in bounds of the window if the x and y pos. are incorrect
                    circle.centerX = xBound(circle.centerX);
                    circle.centerY = yBound(circle.centerY);
                    mCircles.add(circle);
                }
            }
        }

        private int xBound(int xPos) {
            if (xPos < 0 ) return 0;
            if (xPos > getWidth()) return getWidth();
            return xPos;
        }

        private int yBound(int yPos) {
            if (yPos < 0) return 0;
            if (yPos > getHeight()) return getHeight();
            return yPos;
        }

        @Override
        public void onDraw(final Canvas canv) {
            // background bitmap to cover all area
            canv.drawBitmap(mBitmap, null, mMeasuredRect, null);

            for (CircleArea circle : mCircles) {
                Paint mCirclePaint = new Paint();

                mCirclePaint.setColor(circle.color);
                mCirclePaint.setStrokeWidth(40);
                mCirclePaint.setStyle(Paint.Style.FILL);
                canv.drawCircle(circle.centerX, circle.centerY, circle.radius, mCirclePaint);
            }
        }

        @Override
        public boolean onTouchEvent(final MotionEvent event) {
            boolean handled = false;

            CircleArea touchedCircle;
            int xTouch;
            int yTouch;
            int pointerId;
            int actionIndex = event.getActionIndex();

            // get touch event coordinates and make transparent circle from it
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // it's the first pointer, so clear all existing pointers data
                    clearCirclePointer();

                    xTouch = (int) event.getX(0);
                    yTouch = (int) event.getY(0);

                    // check if we've touched inside some circle
                    touchedCircle = getTouchedCircle(xTouch, yTouch);

                    if (touchedCircle == null)
                        return true; //return if no object is touched

                    touchedCircle.centerX = xTouch;
                    touchedCircle.centerY = yTouch;
                    mCirclePointer.put(event.getPointerId(0), touchedCircle);

                    invalidate();
                    handled = true;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.w(TAG, "Pointer down");
                    // It secondary pointers, so obtain their ids and check circles
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);

                    if (xTouch < 0 || xTouch > getWidth() || yTouch < 0 || yTouch > getHeight())
                        return true;

                    // check if we've touched inside some circle
                    touchedCircle = getTouchedCircle(xTouch, yTouch);

                    if (touchedCircle == null)
                        return true; //return if no object is touched

                    mCirclePointer.put(pointerId, touchedCircle);
                    touchedCircle.centerX = xTouch;
                    touchedCircle.centerY = yTouch;
                    invalidate();
                    handled = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    final int pointerCount = event.getPointerCount();

                    Log.w(TAG, "Move");

                    for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                        // Some pointer has moved, search it by pointer id
                        pointerId = event.getPointerId(actionIndex);

                        xTouch = (int) event.getX(actionIndex);
                        yTouch = (int) event.getY(actionIndex);

                        if (xTouch < 0 || xTouch > getWidth() || yTouch < 0 || yTouch > getHeight())
                            return true;

                        touchedCircle = mCirclePointer.get(pointerId);

                        if (null != touchedCircle) {
                            touchedCircle.centerX = xTouch;
                            touchedCircle.centerY = yTouch;
                        }
                    }
                    invalidate();
                    handled = true;
                    break;

                case MotionEvent.ACTION_UP:
                    clearCirclePointer();
                    invalidate();
                    handled = true;
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    // not general pointer was up
                    pointerId = event.getPointerId(actionIndex);

                    mCirclePointer.remove(pointerId);
                    invalidate();
                    handled = true;
                    break;

                case MotionEvent.ACTION_CANCEL:
                    handled = true;
                    break;

                default:
                    // do nothing
                    break;
            }

            return super.onTouchEvent(event) || handled;
        }

        /**
         * Clears all CircleArea - pointer id relations
         */
        private void clearCirclePointer() {
            Log.w(TAG, "clearCirclePointer");

            mCirclePointer.clear();
        }

        /**
         * Determines touched circle
         *
         * @param xTouch int x touch coordinate
         * @param yTouch int y touch coordinate
         * @return {@link CircleArea} touched circle or null if no circle has been touched
         */
        private CircleArea getTouchedCircle(final int xTouch, final int yTouch) {
            CircleArea touched = null;

            for (CircleArea circle : mCircles) {
                if ((circle.centerX - xTouch) * (circle.centerX - xTouch) + (circle.centerY - yTouch) * (circle.centerY - yTouch) <= circle.radius * circle.radius) {
                    touched = circle;
                    break;
                }
            }

            return touched;
        }

        public Bitmap getDrawing() {
            this.setDrawingCacheEnabled(true);
            this.buildDrawingCache();
            Bitmap bmp = Bitmap.createBitmap(this.getDrawingCache());
            this.setDrawingCacheEnabled(false);


            return bmp;
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            mMeasuredRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
        }
    }
}
