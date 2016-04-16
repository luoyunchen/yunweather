package com.yunweather.app.model;

import android.util.Log;

public class WeatherInfo {
	private String windDirection;
	private String windForce;
	private String tempHigh;
	private String tempLow;
	private String weatherConditions;
	private String weatherDate;
	
	public void setWindDirection(String windDirection) {
		Log.d("cxw", "in setWindDirection ...........");
		this.windDirection = windDirection;
		Log.d("cxw", "in setWindDirection ...........over");
	}
	
	public String getWindDirection() {
		return windDirection;
	}
	
	public void setWindForce(String windForce) {
		this.windForce = windForce;
	}
	
	public String getWindForce() {
		return windForce;
	}
	
	public void setTempHigh(String tempHigh) {
		this.windForce = tempHigh;
	}
	
	public String getTempHigh() {
		return tempHigh;
	}
	
	public void setTempLow(String tempLow) {
		this.tempLow = tempLow;
	}
	
	public String getTempLow() {
		return tempLow;
	}
	
	public void setWeatherConditions(String weatherConditions) {
		this.weatherConditions = weatherConditions;
	}
	
	public String getWeatherConditions() {
		return weatherConditions;
	}
	
	public void setWeatherDate(String weatherDate) {
		this.weatherDate = weatherDate;
	}
	
	public String getWeatherDate() {
		return weatherDate;
	}
}
