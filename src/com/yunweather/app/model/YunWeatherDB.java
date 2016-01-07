package com.yunweather.app.model;

import java.util.ArrayList;
import java.util.List;

import android.R.bool;
import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yunweather.app.activity.ChooseAreaActivity.AutoLocation;
import com.yunweather.app.db.YunWeatherOpenHelper;

public class YunWeatherDB {
	/**
	* 数据库名
	*/
	public static final String DB_NAME = "yun_weather";
	
	/**
	* 数据库版本
	*/
	public static final int VERSION = 1;
	private static YunWeatherDB yunWeatherDB;
	private SQLiteDatabase db;
	
	/**
	* 将构造方法私有化
	*/
	private YunWeatherDB(Context context) {
		YunWeatherOpenHelper dbHelper = new YunWeatherOpenHelper(context, DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}
	
	/**
	* 获取CoolWeatherDB的实例。
	*/
	public synchronized static YunWeatherDB getInstance(Context context) {
		if (yunWeatherDB == null) {
			yunWeatherDB = new YunWeatherDB(context);
		}
		return yunWeatherDB;
	}
	
	/**
	* 将Province实例存储到数据库。
	*/
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	
	/**
	* 从数据库读取全国所有的省份信息。
	*/
	public List<Province> loadProvinces() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				list.add(province);
			} while (cursor.moveToNext());
		}
		return list;
	}
	
	/**
	* 将City实例存储到数据库。
	*/
	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}
	
	/**
	* 从数据库读取某省下所有的城市信息。
	*/
	public List<City> loadCities(int provinceId) {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id = ?", new String[] { String.valueOf(provinceId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			} while (cursor.moveToNext());
		}
		return list;
	}
	
	/**
	* 将County实例存储到数据库。
	*/
	public void saveCounty(County county) {
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
		}
	}
	
	/**
	* 从数据库读取某城市下所有的县信息。
	*/
	public List<County> loadCounties(int cityId) {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id = ?", new String[] { String.valueOf(cityId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
				county.setCityId(cityId);
				list.add(county);
			} while (cursor.moveToNext());
		}
		return list;
	}
	
	/**
	* 通过省的名字查询相应的省份ID。
	*/
	public void queryProvinces(Province locationProvince) {
		Cursor cursor = db.query("Province", null, "province_name = ?", new String[] { locationProvince.getProvinceName() }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				locationProvince.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				locationProvince.setId(cursor.getInt(cursor.getColumnIndex("id")));
			} while (cursor.moveToNext());
		}
	}
	
	/**
	* 通过市的名字查询相应的市ID。
	*/
	public void queryCitys(City locationCity) {
		Cursor cursor = db.query("City", null, "city_name = ?", new String[] { locationCity.getCityName() }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				locationCity.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				locationCity.setId(cursor.getInt(cursor.getColumnIndex("id")));
			} while (cursor.moveToNext());
		}
	}
	
	/**
	* 通过县/区的名字查询相应的ID。
	*/
	public void queryCounties(County locationCounty, City locationCity) {
		Cursor cursor = db.query("County", null, "county_name = ?", new String[] { locationCounty.getCountyName() }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				locationCounty.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
				locationCounty.setId(cursor.getInt(cursor.getColumnIndex("id")));
			} while (cursor.moveToNext());
		}else {
			cursor = db.query("County", null, "county_name = ?", new String[] { locationCity.getCityName() }, null, null, null);
			if (cursor.moveToFirst()) {
				do {
					locationCounty.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
					locationCounty.setId(cursor.getInt(cursor.getColumnIndex("id")));
				} while (cursor.moveToNext());
			}
		}
	}
	
	
}
