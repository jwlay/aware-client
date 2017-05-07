package com.aware.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jan Wohlfahrt-Laymann on 2017-04-30.
 */

/*
  Helper class to convert image to base64 string
  and retrieve bitmaps from url
 */
public class ESM_ImageUtils {

    /*
    * Method to encode bitmap to base64 string
    * @param bitmap Bitmap to convert
    * @return String the base64 encoded string
     */
    public static String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    /*
    * Method to decode base64 encoded string to bitmap
    * @param encodedString the base64 encoded string
    * @return Bitmap the decoded bitmap
     */
    public static Bitmap StringToBitMap(String encodedString) {
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    /*
    * Retrieve a bitmap from image from a url string
    * @param src the url
    * @return Bitmap
     */
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } else {
                Log.e("ESM_IMAGE_Freetext","Error connection to url: "+connection.getResponseCode()+ " " + connection.getResponseMessage());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
