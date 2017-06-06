package com.aware.tests;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.StrictMode;
import android.util.Log;

import com.aware.Aware;
import com.aware.ESM;
import com.aware.ui.esms.ESMFactory;
import com.aware.ui.esms.ESM_Checkbox;
import com.aware.ui.esms.ESM_DRAW;
import com.aware.ui.esms.ESM_DateTime;
import com.aware.ui.esms.ESM_Freetext;
import com.aware.ui.esms.ESM_IMAGE_Freetext;
import com.aware.ui.esms.ESM_ImageManipulation;
import com.aware.ui.esms.ESM_Image_Draw;
import com.aware.ui.esms.ESM_Likert;
import com.aware.ui.esms.ESM_Notice;
import com.aware.ui.esms.ESM_Number;
import com.aware.ui.esms.ESM_PAM;
import com.aware.ui.esms.ESM_QuickAnswer;
import com.aware.ui.esms.ESM_Radio;
import com.aware.ui.esms.ESM_Scale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * Created by denzil on 04/03/16.
 */
public class TestESM implements AwareTest {

    @Override
    public void test(Context context) {
        testESMS(context);
//        trialESMS(context);
//        testFlow(context);
//        testTimeoutQueue(context);
//        testNumeric(context);
//        testDateTime(context);
//        testPAM(context);
//        testOptionsOverflow(context);
//        testNotificationRetries(context);
    }

    /**
     * This tests the notification re-trigger x times after y seconds have elapsed.
     * @param context
     */
    private void testNotificationRetries(Context context) {
        ESMFactory factory = new ESMFactory();
        try {

            ESM_Number number = new ESM_Number();
            number.setNotificationTimeout(5*60) //5 minutes
                    .setNotificationRetry(3) //notify the user 3 times, so notification alive for 3 * 5 minutes = 15 minutes
                    .setTitle("Lucky number?")
                    .setInstructions("Pick one.");

            factory.addESM(number);

            ESM.queueESM(context, factory.build());

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void testOptionsOverflow(Context context) {
        ESMFactory factory = new ESMFactory();
        try {
            ESM_Radio q2 = new ESM_Radio();
            q2.addRadio("1")
                    .addRadio("2")
                    .addRadio("3")
                    .addRadio("4")
                    .addRadio("5")
                    .addRadio("6")
                    .addRadio("7")
                    .addRadio("8")
                    .addRadio("9")
                    .addRadio("10")
                    .addRadio("11")
                    .addRadio("12")
                    .addRadio("13")
                    .addRadio("14")
                    .addRadio("15")
                    .addRadio("16")
                    .addRadio("17")
                    .addRadio("18")
                    .addRadio("19")
                    .setTitle("Too many options!!!")
                    .setSubmitButton("Visible?");

            ESM_Checkbox q3 = new ESM_Checkbox();
            q3.addCheck("1")
                    .addCheck("2")
                    .addCheck("3")
                    .addCheck("4")
                    .addCheck("5")
                    .addCheck("6")
                    .addCheck("7")
                    .addCheck("8")
                    .addCheck("9")
                    .addCheck("10")
                    .addCheck("11")
                    .addCheck("12")
                    .addCheck("13")
                    .addCheck("14")
                    .addCheck("15")
                    .addCheck("16")
                    .addCheck("17")
                    .addCheck("18")
                    .addCheck("19")
                    .setTitle("Too many options!!!")
                    .setSubmitButton("Visible?");

            factory.addESM(q2);
            factory.addESM(q3);

            ESM.queueESM(context, factory.build());

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void testPAM(Context context) {
        ESMFactory factory = new ESMFactory();

        try {
            ESM_PAM q1 = new ESM_PAM();
            q1.setTitle("PAM")
                    .setInstructions("Pick the closest to how you feel right now.")
                    .setSubmitButton("OK")
                    .setNotificationTimeout(10)
                    .setTrigger("AWARE Test");

            factory.addESM(q1);

            Log.d(Aware.TAG, factory.build());

            ESM.queueESM(context, factory.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void testDateTime(Context context) {
        ESMFactory factory = new ESMFactory();

        try {
            ESM_DateTime q1 = new ESM_DateTime();
            q1.setTitle("Date and time")
                    .setInstructions("When did this happen?")
                    .setSubmitButton("OK")
                    .setTrigger("AWARE Test");

            factory.addESM(q1);

            Log.d(Aware.TAG, factory.build());

            Intent queue = new Intent(ESM.ACTION_AWARE_QUEUE_ESM);
            queue.putExtra(ESM.EXTRA_ESM, factory.build());
            context.sendBroadcast(queue);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void testNumeric(Context context) {
        ESMFactory factory = new ESMFactory();

        try {
            ESM_Number q1 = new ESM_Number();
            q1.setTitle("Number")
                    .setInstructions("We only accept a number!")
                    .setSubmitButton("OK")
                    .setTrigger("AWARE Test");

            factory.addESM(q1);

            Log.d(Aware.TAG, factory.build());

            Intent queue = new Intent(ESM.ACTION_AWARE_QUEUE_ESM);
            queue.putExtra(ESM.EXTRA_ESM, factory.build());
            context.sendBroadcast(queue);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void testFlow(Context context) {
        ESMFactory factory = new ESMFactory();

        try {
            ESM_PAM q1 = new ESM_PAM();
            q1.setTitle("Your mood")
                    .setInstructions("Choose the closest to how you feel right now.")
                    .setSubmitButton("Thanks!");

            ESM_Radio q2 = new ESM_Radio();
            q2.addRadio("Eating")
                    .addRadio("Working")
                    .addRadio("Not alone")
                    .setTitle("Why is that?")
                    .setSubmitButton("Thanks!");

            ESM_QuickAnswer q0 = new ESM_QuickAnswer();
            q0.addQuickAnswer("Yes")
                    .addQuickAnswer("No")
                    .setTitle("Is this a good time to answer?")
                    .addFlow("Yes", q1.build())
                    .addFlow("No", q2.build());

            factory.addESM(q0);

            ESM.queueESM(context, factory.build());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void testESMS(Context context) {
        ESMFactory factory = new ESMFactory();
        try {
            ESM_Freetext esmFreetext = new ESM_Freetext();
            esmFreetext.setTitle("Freetext")
                    .setTrigger("test")
                    .setReplaceQueue(true)
                    .setSubmitButton("OK")
                    .setInstructions("Freetext ESM");

            ESM_Checkbox esmCheckbox = new ESM_Checkbox();
            esmCheckbox.addCheck("Check 1")
                    .addCheck("Check 2")
                    .addCheck("Other")
                    .setTitle("Checkbox")
                    .setTrigger("test")
                    .setSubmitButton("OK")
                    .setInstructions("Checkbox ESM");

            ESM_Likert esmLikert = new ESM_Likert();
            esmLikert.setLikertMax(7)
                    .setLikertMaxLabel("Great")
                    .setLikertMinLabel("Poor")
                    .setLikertStep(1)
                    .setTitle("Likert 3")
                    .setInstructions("Likert ESM")
                    .setTrigger("test")
                    .setSubmitButton("OK");

            ESM_QuickAnswer esmQuickAnswer = new ESM_QuickAnswer();
            esmQuickAnswer.addQuickAnswer("Yes")
                    .addQuickAnswer("No")
                    .setTrigger("test")
                    .setInstructions("Quick Answers ESM");

            ESM_Radio esmRadio = new ESM_Radio();
            esmRadio.addRadio("Radio 1")
                    .addRadio("Radio 2")
                    .setTitle("Radios")
                    .setInstructions("Radios ESM")
                    .setSubmitButton("OK");

            ESM_Scale esmScale = new ESM_Scale();
            esmScale.setScaleMax(100)
                    .setScaleMin(0)
                    .setScaleStart(50)
                    .setScaleMaxLabel("Perfect")
                    .setScaleMinLabel("Poor")
                    .setScaleStep(10)
                    .setTitle("Scale")
                    .setInstructions("Scale ESM")
                    .setSubmitButton("OK");

            ESM_DateTime esmDate = new ESM_DateTime();
            esmDate.setTitle("Date and Time")
                    .setTrigger("AWARE Test")
                    .setInstructions("Specify date and time")
                    .setSubmitButton("OK");

            ESM_PAM esmPAM = new ESM_PAM();
            esmPAM.setTitle("PAM")
                    .setInstructions("Pick the closest to how you feel right now.")
                    .setSubmitButton("OK")
                    .setTrigger("AWARE Test");

            ESM_DRAW esmDraw = new ESM_DRAW();
            esmDraw.setTitle("Draw")
                    .setInstructions("Please draw a circle")
                    .setSubmitButton("OK")
                    .setTrigger("AWARE Test");

            // Image Url function requires Thread Policy adjustments for networking
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            ESM_IMAGE_Freetext Image_Freetext = new ESM_IMAGE_Freetext();
            Image_Freetext.setTitle("Image Freetext")
                    .setInstructions(
                            (new JSONObject())
                                    .put("Text","Instruction text")
                                    .put("ImageUrl","https://i.imgur.com/r31yNMx.jpg")
                                    .toString())
                    .setSubmitButton("OK")
                    .setTrigger("AWARE Test");

            ESM_Image_Draw Image_Draw = new ESM_Image_Draw();
            Image_Draw.setTitle("Image Draw")
                    .setInstructions(
                            (new JSONObject())
                                    .put("Text","Please copy the Image")
                                    .put("ImageUrl","https://i.imgur.com/zNZv3sj.png")
                                    .toString())
                    .setSubmitButton("OK")
                    .setTrigger("AWARE Test");

            ESM_Notice esmNotice = new ESM_Notice();
            esmNotice.setTitle("Notice")
                    .setInstructions("Test notice")
                    .setSubmitButton("OK")
                    .setTrigger("AWARE Test");

            ESM_Notice esm_tts = new ESM_Notice();
            esm_tts.setTitle("TTS")
                    .setInstructions("This is a test of text to speech functionality")
                    .setSubmitButton("OK")
                    .setTrigger("AWARE Test")
                    .setSpeakInstructions(true);

            CustomGame imageManipulation = new CustomGame();
            imageManipulation.setTitle("Image Manipulation")
                    .setESM_Class(CustomGame.class.toString())
                    .setInstructions(
                            (new JSONObject())
                                    .put("Text","Please place the circles on top of each other")
                                    .put("Shapes", (new JSONArray())
                                            .put((new JSONObject())
                                                    .put("type","Circle")
                                                    .put("xPos",200)
                                                    .put("yPos",200)
                                                    .put("radius",100)
                                                    .put("color", Color.RED))
                                            .put((new JSONObject())
                                                    .put("type","Circle")
                                                    .put("xPos",200)
                                                    .put("yPos",100)
                                                    .put("radius",100)
                                                    .put("color", Color.GREEN))
                                            .put((new JSONObject())
                                                    .put("type","Circle")
                                                    .put("xPos",400)
                                                    .put("yPos",500)
                                                    .put("radius",100)
                                                    .put("color", Color.BLUE))
                                    )
                                    .toString())
                    .setSubmitButton("OK")
                    .setTrigger("AWARE Test");

            /*factory.addESM(esmFreetext);
            factory.addESM(esmCheckbox);
            factory.addESM(esmLikert);
            factory.addESM(esmQuickAnswer);
            factory.addESM(esmRadio);
            factory.addESM(esmScale);
            factory.addESM(esmPAM);
            factory.addESM(esmDate);
            factory.addESM(esmDraw);
            factory.addESM(Image_Freetext);
            factory.addESM(Image_Draw);
            factory.addESM(esmNotice);
            factory.addESM(esm_tts);*/
            factory.addESM(imageManipulation);

            ESM.queueESM(context, factory.build());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void trialESMS(Context context) {
        ESMFactory factory = new ESMFactory();
        try {
            ESM_Freetext esmFreetext = new ESM_Freetext();
            esmFreetext.setTitle("Freetext")
                    .setTrigger("test")
                    .setExpirationThreshold(0)
                    .setSubmitButton("OK")
                    .setInstructions("Freetext ESM");

            ESM_Checkbox esmCheckbox = new ESM_Checkbox();
            esmCheckbox.addCheck("Check 1")
                    .addCheck("Check 2")
                    .addCheck("Other")
                    .setTitle("Checkbox")
                    .setTrigger("test")
                    .setExpirationThreshold(0)
                    .setSubmitButton("OK")
                    .setInstructions("Checkbox ESM");

            ESM_Likert esmLikert = new ESM_Likert();
            esmLikert.setLikertMax(5)
                    .setLikertMaxLabel("Great")
                    .setLikertMinLabel("Poor")
                    .setLikertStep(1)
                    .setTitle("Likert")
                    .setInstructions("Likert ESM")
                    .setTrigger("test")
                    .setExpirationThreshold(0)
                    .setSubmitButton("OK");

            ESM_QuickAnswer esmQuickAnswer = new ESM_QuickAnswer();
            esmQuickAnswer.addQuickAnswer("Yes")
                    .addQuickAnswer("No")
                    .setTrigger("test")
                    .setExpirationThreshold(0)
                    .setSubmitButton("OK")
                    .setInstructions("Quick Answers ESM");

            ESM_Radio esmRadio = new ESM_Radio();
            esmRadio.addRadio("Radio 1")
                    .addRadio("Radio 2")
                    .setTitle("Radios")
                    .setInstructions("Radios ESM")
                    .setExpirationThreshold(0)
                    .setSubmitButton("OK");

            ESM_Scale esmScale = new ESM_Scale();
            esmScale.setScaleMax(100)
                    .setScaleMin(0)
                    .setScaleStart(50)
                    .setScaleMaxLabel("Perfect")
                    .setScaleMinLabel("Poor")
                    .setScaleStep(10)
                    .setTitle("Scale")
                    .setInstructions("Scale ESM")
                    .setExpirationThreshold(0)
                    .setSubmitButton("OK");

            factory.addESM(esmFreetext);
            factory.addESM(esmCheckbox);
            factory.addESM(esmLikert);
            factory.addESM(esmQuickAnswer);
            factory.addESM(esmRadio);
            factory.addESM(esmScale);

            Intent queue = new Intent(ESM.ACTION_AWARE_TRY_ESM);
            queue.putExtra(ESM.EXTRA_ESM, factory.build());
            context.sendBroadcast(queue);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void testTimeoutQueue(Context context) {
        ESMFactory factory = new ESMFactory();
        try {
            ESM_Freetext esmFreetext = new ESM_Freetext();
            esmFreetext.setTitle("Freetext")
                    .setTrigger("test")
                    .setExpirationThreshold(0)
                    .setNotificationTimeout(10)
                    .setSubmitButton("OK")
                    .setInstructions("Freetext ESM");

            ESM_Checkbox esmCheckbox = new ESM_Checkbox();
            esmCheckbox.addCheck("Check 1")
                    .addCheck("Check 2")
                    .addCheck("Other")
                    .setTitle("Checkbox")
                    .setTrigger("test")
                    .setExpirationThreshold(0)
                    .setNotificationTimeout(10)
                    .setSubmitButton("OK")
                    .setInstructions("Checkbox ESM");

            ESM_Likert esmLikert = new ESM_Likert();
            esmLikert.setLikertMax(5)
                    .setLikertMaxLabel("Great")
                    .setLikertMinLabel("Poor")
                    .setLikertStep(1)
                    .setTitle("Likert")
                    .setInstructions("Likert ESM")
                    .setTrigger("test")
                    .setExpirationThreshold(0)
                    .setNotificationTimeout(10)
                    .setSubmitButton("OK");

            ESM_QuickAnswer esmQuickAnswer = new ESM_QuickAnswer();
            esmQuickAnswer.addQuickAnswer("Yes")
                    .addQuickAnswer("No")
                    .setTrigger("test")
                    .setExpirationThreshold(0)
                    .setNotificationTimeout(10)
                    .setInstructions("Quick Answers ESM");

            ESM_Radio esmRadio = new ESM_Radio();
            esmRadio.addRadio("Radio 1")
                    .addRadio("Radio 2")
                    .setTitle("Radios")
                    .setInstructions("Radios ESM")
                    .setExpirationThreshold(0)
                    .setNotificationTimeout(10)
                    .setSubmitButton("OK");

            ESM_Scale esmScale = new ESM_Scale();
            esmScale.setScaleMax(100)
                    .setScaleMin(0)
                    .setScaleStart(50)
                    .setScaleMaxLabel("Perfect")
                    .setScaleMinLabel("Poor")
                    .setScaleStep(10)
                    .setTitle("Scale")
                    .setInstructions("Scale ESM")
                    .setExpirationThreshold(0)
                    .setNotificationTimeout(10)
                    .setSubmitButton("OK");

            factory.addESM(esmFreetext);
            factory.addESM(esmCheckbox);
            factory.addESM(esmLikert);
            factory.addESM(esmQuickAnswer);
            factory.addESM(esmRadio);
            factory.addESM(esmScale);

            Intent queue = new Intent(ESM.ACTION_AWARE_QUEUE_ESM);
            queue.putExtra(ESM.EXTRA_ESM, factory.build());
            context.sendBroadcast(queue);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class CustomGame extends ESM_ImageManipulation {

        private int xThreshold = 10;
        private int yThreshold = 10;

        public CustomGame() throws JSONException {
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
}
