package com.mocentury.fis.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by lumtwj on 23/4/16.
 */
public class ImageUtil {
    public static Bitmap checkPhoto(Context context, Uri contentURI, Bitmap bitmap) throws IOException {
        String photoPath = getRealPathFromURI(context, contentURI);
        ExifInterface ei = new ExifInterface(photoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
        }

        return bitmap;
    }

    public static void saveBitmapToFile(Bitmap bm) throws IOException {
        FileOutputStream out = null;
        String fileName = getFileName();

        try {
            out = new FileOutputStream(fileName);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
        } finally {
            if (out != null)
                out.close();
        }
    }

    public static Bitmap generateQRCode(String data) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();

        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 500, 500);
        Bitmap bm = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < 500; i++) { //width
            for (int j = 0; j < 500; j++) { //height
                bm.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }

        return bm;
    }

    private static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);

        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }

        return result;
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    private static String getFileName() {
        File outputDir = new File(Environment.getExternalStorageDirectory(), "fis");
//        Log.d(getPackageName(), outputDir.getAbsolutePath());

        if (!outputDir.exists())
            outputDir.mkdir();

        return new File(outputDir, outputDir.list().length + ".png").getAbsolutePath();
    }
}
