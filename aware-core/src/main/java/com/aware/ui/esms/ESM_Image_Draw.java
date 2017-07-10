package com.aware.ui.esms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.ESM;
import com.aware.R;
import com.aware.providers.ESM_Provider;
import com.aware.utils.Aware_TTS;
import com.aware.utils.ESM_ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Jan Wohlfahrt-Laymann on 2017-05-07.
 */

public class ESM_Image_Draw extends ESM_Question {

    public ESM_Image_Draw() throws JSONException {
        this.setType(ESM.TYPE_ESM_IMAGE_DRAW);
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
        View ui = inflater.inflate(R.layout.esm_image_draw, null);
        builder.setView(ui);

        esm_dialog = builder.create();
        esm_dialog.setCanceledOnTouchOutside(false);

        try {
            TextView esm_title = (TextView) ui.findViewById(R.id.esm_title);
            esm_title.setText(getTitle());

            ImageView esm_Imageinstructions = (ImageView) ui.findViewById(com.aware.R.id.esm_Imageinstructions);
            TextView esm_instructions = (TextView) ui.findViewById(R.id.esm_instructions);
            JSONObject instructions = new JSONObject(getInstructions());

            if (instructions.has("ImageUrl")) {
                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                esm_Imageinstructions.setImageBitmap(ESM_ImageUtils.getBitmapFromURL(instructions.getString("ImageUrl")));
            } else if (instructions.has("encodedImage"))
                esm_Imageinstructions.setImageBitmap(ESM_ImageUtils.StringToBitMap(instructions.getString("encodedImage")));
            esm_instructions.setText(instructions.getString("Text"));

            final ESM_DRAW.CanvasView feedback = (ESM_DRAW.CanvasView) ui.findViewById(R.id.esm_draw);
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
                        rowData.put(ESM_Provider.ESM_Data.ANSWER, ESM_ImageUtils.bitmapToString(feedback.getDrawing()));
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
}
