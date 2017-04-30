package com.aware.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Jan Wohlfahrt-Laymann on 2017-04-30.
 */

/*
Base 64 Bitmap converter helper class
can encode bitmap to base64 string
and decode
 */
public class BASE64_bitmap_converter {

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
}
