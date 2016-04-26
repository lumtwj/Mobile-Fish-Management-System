package com.mocentury.fis.util;

import android.os.AsyncTask;

import com.google.zxing.WriterException;
import com.mocentury.fis.MainActivity;
import com.mocentury.fis.object.Data;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by lumtwj on 23/4/16.
 */
public class AddFishApi extends AsyncTask<Data, Void, String> {
    public static final String SERVER_URL = "http://52.74.203.190";
    public static final int SERVER_PORT = 80;
    public static final String ENDPOINT = "Fishackathon/fish";
    public static final String ADD_FISH = String.format("%s:%d/%s", SERVER_URL, SERVER_PORT, ENDPOINT);

    MainActivity activity;
    Data data;

    public AddFishApi(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Data... d) {
        String result = null;
        data = d[0];

        try {
            Document resp = Jsoup.connect(ADD_FISH)
                    .header("Content-Type",
                            "application/x-www-form-urlencoded;charset=UTF-8")
                    .data("species", data.getSpecies())
                    .data("length", String.valueOf(data.getLength()))
                    .data("lat", String.valueOf(data.getLat()))
                    .data("lng", String.valueOf(data.getLng()))
                    .data("recorded_time", data.getTime())
                    .ignoreContentType(true)
                    .timeout(0)
                    .post();

            result = resp.text();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject arr = new JSONObject(result);
            data.setId(arr.getLong("id"));

            //            Bitmap map = ImageUtil.generateQRCode(d.toString());\
//            ImageUtil.saveBitmapToFile(map);

            try {
                new FishInfoDialog(activity).loadData(data);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
