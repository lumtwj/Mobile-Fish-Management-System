package com.mocentury.fis.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Classifier {
    //Threshold settings for yellow filtering
    private static final int COLOR_RG_DIFF_MAX = 70;    // Maximum different value between red and green
    private static final int COLOR_RG_DIFF_MIN = -10;    // Minimum different value between red and green
    private static final int COLOR_YB_DIFF_MAX = 30;    // Maximum different value between yellow and blue
    private static final int HUE_BLUE_MIN = 180;                // Minimum hue value
    private static final int HUE_BLUE_MAX = 260;                // Maximum hue value
    private static final int HUE_YELLOW_MIN = 40;                // Minimum hue value
    private static final int HUE_YELLOW_MAX = 80;                // Maximum hue value

    public static final String COLOR_CLASS_TYPE_BLUE = "BLUE";
    public static final String COLOR_CLASS_TYPE_YELLOW = "YELLOW";

    private static final double THRESHOLD_YELLOW_VALUE = 0.5d;
    private static final double THRESHOLD_BLUE_VALUE = 0.15d;
    private static final double THRESHOLD_BILL_GRADIENT = 0.1d;

    public static final int FISH_TYPE_SWORDFISH = 1;
    private static final int FISH_TYPE_YELLOW_FIN = 2;
    private static final int FISH_TYPE_BLUE_FIN = 3;
//    public static final int FISH_TYPE_BIG_EYE = 4;

    private static final int CONFIRMATION_SIZE = 5;

    public static int isSwordFish(HashMap<Integer, Double> diffMap, double minX, double maxX) {

        // Bold assumption: the bill is 30-40% of total length
        // therefore gradient checking starts at 20% from tip
        int midpt = (int) (0.5 * (maxX - minX));
        int tipX = (int) minX;
        int billend = (int) minX;
        int startX = (int) (tipX + 0.01 * (maxX - minX));
        Log.d("com.mocentury.fis", "tipX: " + tipX + " startX: " + startX);
        int min_req = (int) (tipX + 0.2 * (maxX - minX));

        int tipY = 0;
        for (int i = tipX; i < midpt; i++) {
            if (diffMap.get(i) != null) {
                tipX = i;
                tipY = diffMap.get(i).intValue();
                break;
            }
        }

        ArrayList<Integer> confirmation = new ArrayList<>();
        for (int j = startX; j < midpt; j++) {
            if (diffMap.get(j) != null) {
                double gradient = billGradient(tipX, tipY, j, diffMap.get(j).intValue());
                if (gradient > THRESHOLD_BILL_GRADIENT) {
                    confirmation.add(j);
                    if (confirmation.size() == CONFIRMATION_SIZE) {
                        billend = confirmation.get(0);
                        break;
                    }
                } else {
                    confirmation.clear();
                }
            }
        }

        Log.d("com.mocentury.fis", billend + " " + min_req);
        Log.d("com.mocentury.fis", "Estimated bill ending pixel: " + billend);


        // Estimated pixel of the end of the bill is beyond 20% of the total length
        if ((double) billend > min_req) {
            return billend;
        } else {
            return 0;
        }
    }

    // Compute gradient of the bill
    private static double billGradient(int x1, int y1, int x2, int y2) {

        return (double) (y2 - y1) / (double) (2 * (x2 - x1));
    }

    // Get pixel of the segment where it is the narrowest
    public static int getTailSegment(HashMap<Integer, Double> diffMap, double maxX) {
        // Assumption: the tail resides within 75-90 percentile of total length
        // Tail segmentation starting from the tail end
        double min_depth = maxX;
        int narrowest = 0;
        int startpt = (int) (0.90 * maxX);
        int endpt = (int) (0.75 * maxX);

        ArrayList<Integer> confirmation = new ArrayList<>();
        for (int k = startpt; k > endpt; k--) {
            Double band = diffMap.get(k);
            if (band != null) {
                if (band < min_depth) {
                    min_depth = band;
                    narrowest = k;
                    confirmation.clear();
                } else {
                    confirmation.add(k);
                }
            }

            if (confirmation.size() == CONFIRMATION_SIZE) {
                break;
            }
        }

        return narrowest;
    }

    public static int getFishClassType(Bitmap image, String colorType,
                                       int minX, int maxX, int minY, int maxY) throws IOException {

        int hitValue = 0;
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                int clr = image.getPixel(x, y);
                int red = (clr & 0x00ff0000) >> 16;
                int green = (clr & 0x0000ff00) >> 8;
                int blue = clr & 0x000000ff;

                float hsb[] = new float[3];

                Color.RGBToHSV(red, green, blue, hsb);
                int hue = (int) (hsb[0]);

                switch (colorType) {
                    case COLOR_CLASS_TYPE_BLUE:
                        int saturation = (int) (hsb[1]);
                        int brightness = (int) (hsb[2]);

//                        Log.d("com.mocentury.fis", "Hue: " + hue + ", Saturation: " + saturation + ", Brightness: " + brightness);

                        if (hue >= HUE_BLUE_MIN && hue <= HUE_BLUE_MAX && saturation >= 30 && brightness <= 150) {
                            hitValue++;
                        }
                        break;
                    case COLOR_CLASS_TYPE_YELLOW:
                        int rgDifValue = red - green;

                        if (rgDifValue <= COLOR_RG_DIFF_MAX && rgDifValue >= COLOR_RG_DIFF_MIN && (red - blue >= COLOR_YB_DIFF_MAX || green - blue >= COLOR_YB_DIFF_MAX) && (hue >= HUE_YELLOW_MIN && hue <= HUE_YELLOW_MAX)) {
                            hitValue++;
                        }
                        break;
                }
            }
        }

        // create image from color model and data
        int w = maxX - minX;
        int h = maxY - minY;
        double threshold = ((double) hitValue / (w * h)) * 100;

        switch (colorType) {
            case COLOR_CLASS_TYPE_YELLOW:
                if (threshold >= THRESHOLD_YELLOW_VALUE) return FISH_TYPE_YELLOW_FIN;
                break;
            case COLOR_CLASS_TYPE_BLUE:
                if (threshold >= THRESHOLD_BLUE_VALUE) return FISH_TYPE_BLUE_FIN;
                break;
        }

        return -1;
    }

    public static String getFishString(int type) {

        switch (type) {
            case 1:
                return "SWORDFISH";
            case 2:
                return "YELLOWFIN TUNA";
            case 3:
                return "BLUEFIN TUNA";
//            case 4:
//                return "BIG EYE TUNA";
            default:
                return "UNKNOWN TYPE";
        }
    }
}