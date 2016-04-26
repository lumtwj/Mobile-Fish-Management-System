package com.mocentury.fis.object;

import com.mocentury.fis.util.LocationUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lumtwj on 23/4/16.
 */
public class Data {
    long id;
    String species;
    double length, lat, lng;
    String time;

    public Data(String species, double length, double lat, double lng) {
        this.species = species;
        this.length = length;
        this.lat = lat;
        this.lng = lng;
        this.time = LocationUtil.getCurrentDate();
    }

    public Data(long id, String species, double length, double lat, double lng) {
        this.id = id;
        this.species = species;
        this.length = length;
        this.lat = lat;
        this.lng = lng;
        this.time = LocationUtil.getCurrentDate();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        JSONObject data = new JSONObject();
        JSONObject fish = new JSONObject();

        try {
            data.put("lat", lat);
            data.put("lng", lng);
            data.put("time", time);

            fish.put("species", species);
            fish.put("length", length);

            data.put("fish", fish);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data.toString();
    }
}
