package com.mocentury.fis.util;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Wayne on 5/5/2016.
 */
public class CVUtil {

    private static HashMap<Integer, ArrayList<Double>> verticals;
    private static Rect bounds;

    public static void processContours(List<MatOfPoint> contours, int width, int height) {

        // Compute reduced/optimized bounds for the image after edge detection
        double minX = width;
        double maxX = 0.0;
        double minY = height; // Bug fix
        double maxY = 0.0;
        verticals = new HashMap<>();
        for(MatOfPoint pt: contours) {
            List<Point> list = pt.toList();
            for(Point p: list) {
                if(p.x < minX) minX = p.x;
                if(p.x > maxX) maxX = p.x;
                if(p.y < minY) minY = p.y;
                if(p.y > maxY) maxY = p.y;

                // Individual x value and its y values (1-to-many)
                int key = Double.valueOf(p.x).intValue();
                ArrayList<Double> value = verticals.get(key);
                if(value != null) value.add(p.y);
                else {
                    value = new ArrayList<>();
                    verticals.put(key, value);
                }
            }
        }

        bounds = new Rect((int)minX, (int)minY, (int)(maxX-minX), (int)(maxY-minY));
        //System.out.println(minX+" "+maxX+" "+minY+" "+maxY);
    }

    public static Rect getBounds() {

        return bounds;
    }

    public static HashMap<Integer, ArrayList<Double>> getVerticals() {

        return verticals;
    }
}
