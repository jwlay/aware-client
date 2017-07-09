package com.aware.ui.esms;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * Created by Jan Wohlfahrt-Laymann on 2017-06-03.
 */

public class ThreeStepCommand extends ESM_ImageManipulation {

    private int xThreshold = 10;
    private int yThreshold = 10;

    public ThreeStepCommand() throws JSONException {
        super();
    }

    @Override
    public JSONObject createJsonOutput(CanvasView feedback) throws JSONException {
        JSONObject response = super.createJsonOutput(feedback);
        JSONObject answer = new JSONObject();
        JSONObject circles = new JSONObject();
        boolean TestPass = true;
        int[] x_values = new int[3];
        int[] y_values = new int[3];
        int radius = 0; //radius should be the same for all
        int i = 0;
        for (CanvasView.CircleArea circle : feedback.getmCircles()) {
            x_values[i] = circle.centerX;
            y_values[i] = circle.centerY;
            radius = circle.radius;
            i++;
            circles.put("circle",circle.toString());
        }
        Arrays.sort(x_values);
        int x_ans = 0;
        if (abs(x_values[0] - x_values[1]) < xThreshold)
            x_ans++;
        if (abs(x_values[1] - x_values[2]) < xThreshold)
            x_ans++;

        switch (x_ans) {
            case 0:
                answer.put("x-values","The circles were not correctly aligned on the x-axis");
                TestPass = false;
                break;
            case 1:
                answer.put("x-values","Only two circles aligned on the x-axis");
                TestPass = false;
                break;
            case 2:
                answer.put("x-values","The circles were correctly placed along the x-axis");
                break;
        }

        Arrays.sort(y_values);
        int y_ans = 0;
        if (abs(y_values[0] - y_values[1]) < yThreshold)
            y_ans++;
        if (abs(y_values[1] - y_values[2]) < yThreshold)
            y_ans++;

        switch (y_ans) {
            case 0:
                answer.put("y-values", "The circles were not correctly aligned on the y-axis");
                TestPass = false;
                break;
            case 1:
                answer.put("y-values", "Only two circles aligned on the y-axis");
                TestPass = false;
                break;
            case 2:
                answer.put("y-values", "The circles were correctly placed along the y-axis");
                break;
        }

        int radius_ans = 0;
        if (abs(y_values[0] - y_values[1]) > radius)
            y_ans++;
        if (abs(y_values[1] - y_values[2]) > radius)
            y_ans++;

        switch (radius_ans) {
            case 0:
                answer.put("overlap", "The circles are overlapping");
                TestPass = false;
                break;
            case 1:
                answer.put("overlap", "Two circles are overlapping");
                TestPass = false;
                break;
            case 2:
                answer.put("overlap", "The circles are not overlapping");
                break;
        }

        response.put("test passed",TestPass);
        response.put("evaluation",answer);
        return response;
    }
}
