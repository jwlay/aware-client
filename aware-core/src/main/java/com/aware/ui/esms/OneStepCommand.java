package com.aware.ui.esms;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jan Wohlfahrt-Laymann on 2017-06-22.
 */

public class OneStepCommand extends ESM_ImageManipulation {

    public OneStepCommand() throws JSONException {
        super();
    }

    @Override
    public JSONObject createJsonOutput(CanvasView feedback) throws JSONException {
        JSONObject response = super.createJsonOutput(feedback);
        JSONObject answer = new JSONObject();
        boolean TestPass = true;
        ESM_ImageManipulation.CanvasView.CircleArea circle = feedback.getmCircles().iterator().next();
        Bitmap image = feedback.getDrawing();

        if (circle.centerX < (image.getWidth() * .5 )) {
            answer.put("explanation", "The x-value of the circle is not on the right");
            answer.put("circle.centerX","The x-value of the circle is: "+String.valueOf(circle.centerX));
            answer.put("Image Width", "The image width is: "+String.valueOf(image.getWidth() * .5));
            TestPass = false;
        }

        response.put("test passed",TestPass);
        response.put("evaluation",answer);
        return response;
    }
}
