package com.leo.spatial;

/**
 * Created by LX on 2017/8/28.
 */
public class CityGeoInfo {

    /** 城市id **/
    private int cityId;
    /** 城市名称 **/
    private String name;
    /** 城市经度 **/
    private double lnt;
    /** 城市纬度 **/
    private double lat;


    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLnt() {
        return lnt;
    }

    public void setLnt(double lnt) {
        this.lnt = lnt;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
