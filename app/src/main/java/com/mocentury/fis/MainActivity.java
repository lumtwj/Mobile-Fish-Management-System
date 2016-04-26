package com.mocentury.fis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ViewFlipper;

import com.mocentury.fis.util.Classifier;
import com.mocentury.fis.util.ImageUtil;
import com.mocentury.fis.util.LocationUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class MainActivity extends AppCompatActivity {
    private final int PICK_IMAGE = 1;
    private final int TAKE_PHOTO = 2;

    private static float FOV = 2.0f;
    public int RES_WIDTH = 5312;

    ImageViewTouch ivTest;
    ViewFlipper vfContainer;
    Snackbar sb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivTest = (ImageViewTouch) findViewById(R.id.ivTest);
        vfContainer = (ViewFlipper) findViewById(R.id.vfContainer);

        System.loadLibrary("opencv_java3");
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mOpenCVCallBack);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

//        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.bluefinf);
//
//        try {
//            init(bm);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            if (requestCode == PICK_IMAGE || requestCode == TAKE_PHOTO) {
                try {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                    if (requestCode == TAKE_PHOTO)
                        bm = ImageUtil.checkPhoto(this, uri, bm);

                    init(bm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void init(Bitmap input) throws IOException {
        Log.d(getPackageName(), "Bitmap (width * height): " + input.getWidth() + " * " + input.getHeight());
        input = downscaleBitmap(input);

        vfContainer.setDisplayedChild(1);

        Mat mat = new Mat();

        Utils.bitmapToMat(input, mat);

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 0);
        Imgproc.Canny(mat, mat, 10, 100);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Bitmap edges = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, edges);

        //
        int height = input.getHeight();
        int width = input.getWidth();

        //
        RES_WIDTH = width;
        //

        // Compute reduced/optimized bounds for the image after edge detection
        double minX = width;
        double maxX = 0.0;
        double minY = height;
        double maxY = 0.0;
        HashMap<Integer, ArrayList<Double>> bitMap = new HashMap<>();

        for (MatOfPoint pt : contours) {
            List<Point> list = pt.toList();

            for (Point p : list) {
                if (p.x < minX) minX = p.x;
                if (p.x > maxX) maxX = p.x;
                if (p.y < minY) minY = p.y;
                if (p.y > maxY) maxY = p.y;

                // Individual x value and its y values (1-to-many)
                int key = (Double.valueOf(p.x)).intValue();
                ArrayList<Double> value = bitMap.get(key);

                if (value != null) value.add(p.y);
                else {
                    value = new ArrayList<>();
                    bitMap.put(key, value);
                }
            }
        }

        HashMap<Integer, Double> diffMap = new HashMap<>();

        for (Integer k : bitMap.keySet()) {
            if (bitMap.get(k).size() > 2) {
                double minimumY = Collections.min(bitMap.get(k));
                double maximumY = Collections.max(bitMap.get(k));
                double diff = maximumY - minimumY;
                diffMap.put(k, diff);
            }
        }

        int tail_pixel = Classifier.getTailSegment(diffMap, maxX);

        int classType;
        int billend = Classifier.isSwordFish(diffMap, minX, maxX);
        // Swordfish checking. Zero: Not swordfish, Non-zero: Swordfish
        if (billend != 0) {
            classType = Classifier.FISH_TYPE_SWORDFISH;
        } else {
            billend = (int) minX;
            classType = Classifier.getFishClassType(input,
                    Classifier.COLOR_CLASS_TYPE_YELLOW,
                    (int) minX, (int) maxX, (int) minY, (int) maxY);
            if (classType == -1) {
                classType = Classifier.getFishClassType(input,
                        Classifier.COLOR_CLASS_TYPE_BLUE,
                        (int) minX, (int) maxX, (int) minY, (int) maxY);
//                if (classType == -1) classType = Classifier.FISH_TYPE_BIG_EYE;
            }
        }

        // Fork length in pixel
        int fork_pixel = tail_pixel - billend; // billend to tail segment

        Log.d(getPackageName(), "Fish type: " + Classifier.getFishString(classType));
        Log.d(getPackageName(), "Fish length: " + computeLength(fork_pixel) + " inch");


        ivTest.setImageBitmap(input);
        new LocationUtil(this, Classifier.getFishString(classType), computeLength(fork_pixel)).connect();
    }

    public double computeLength(double pxwidth) {
        return meterToInch(pxwidth / RES_WIDTH * FOV);
    }

    public double meterToInch(double meter) {
        return meter * 39.37;
    }

    public void takePhoto(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private Bitmap downscaleBitmap(Bitmap bm) {
        double height = bm.getHeight() * (1024.0 / bm.getWidth());
        Log.d(getPackageName(), ">>" + height);
        return Bitmap.createScaledBitmap(bm, 1024, (int) height, false);
    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(getPackageName(), "OpenCV loaded successfully");
                    break;
                }
                case LoaderCallbackInterface.INIT_FAILED: {
                    Log.d(getPackageName(), "OpenCV failed to init");
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };
}
