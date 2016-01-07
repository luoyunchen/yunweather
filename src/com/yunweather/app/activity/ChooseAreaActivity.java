package com.yunweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.R.bool;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yunweather.app.R;
import com.yunweather.app.model.City;
import com.yunweather.app.model.County;
import com.yunweather.app.model.Province;
import com.yunweather.app.model.YunWeatherDB;
import com.yunweather.app.util.HttpCallbackListener;
import com.yunweather.app.util.HttpUtil;
import com.yunweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private YunWeatherDB yunWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	/**
	* 省列表
	*/
	private List<Province> provinceList;
	/**
	* 市列表
	*/
	private List<City> cityList;
	/**
	* 县列表
	*/
	private List<County> countyList;
	/**
	* 选中的省份
	*/
	private Province selectedProvince;
	/**
	* 选中的城市
	*/
	private City selectedCity;
	/**
	* 当前选中的级别
	*/
	private int currentLevel;
	
	/**
	* 是否从WeatherActivity中跳转过来。
	*/
	private boolean isFromWeatherActivity;
	
	/**
	* 纪录从百度得到的省，市，县或区的名称。
	*/
	public class AutoLocation {
		private String provinceName;
		private String cityName;
		private String countyName;
	}
	
	/**
	* AutoLocation的引用。
	*/
	private AutoLocation autoLocation;
	
	/**
	* 自动定位的省份
	*/
	private Province locationProvince;
	/**
	* 自动定位的的城市
	*/
	private City locationCity;
	/**
	* 自动定位的的县/区
	*/
	private County locationCounty;
	/**
	* 是否在自动定位中
	*/
	private boolean locationFlag = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		yunWeatherDB = YunWeatherDB.getInstance(this);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounties();
				}else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces(); // 加载省级数据
		Log.d("cxw", "go to LocationActivity"); 
		if (!isFromWeatherActivity) { //如果不是从WeatherActivity跳转过来的，就可以执行
			Intent intent = new Intent(ChooseAreaActivity.this, LocationActivity.class);
			startActivityForResult(intent, 1);
		}
	}
	
	/**
	* 处理和解析从百度得来的数据，然后从服务器查询到county的ID，用于查询自动定位的城市天气
	*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {
				String returnedData = data.getStringExtra("data_return");
				autoLocation = new AutoLocation();
				handleAdrrResponse(returnedData, autoLocation);
				locationFlag = true;
				locationProvince = new Province();
				locationCity = new City();
				locationCounty = new County();
				
				locationProvince.setProvinceName(autoLocation.provinceName);
				locationCity.setCityName(autoLocation.cityName);
				locationCounty.setCountyName(autoLocation.countyName);
				
				yunWeatherDB.queryProvinces(locationProvince);
				
				selectedProvince = locationProvince;
				queryFromServer(selectedProvince.getProvinceCode(), "city");					
			}
		break;
			default:
		}
	}
	
	/**
	* 解析和处理百度服务器返回的地址数据
	*/
	
	public static void  handleAdrrResponse(String response, AutoLocation autoLocation) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split("国");
			if (!TextUtils.isEmpty(allCounties[1])) {
				String[] Province = allCounties[1].split("省");
				autoLocation.provinceName = Province[0];
				if (!TextUtils.isEmpty(Province[1])) {
					String[] City = Province[1].split("市");
					autoLocation.cityName = City[0];
					if (!TextUtils.isEmpty(City[1])) {
						String[] County = City[1].split("县");
						if (!TextUtils.isEmpty(County[0])) {
							autoLocation.countyName = County[0];
						}
						County = City[1].split("区");
						if (!TextUtils.isEmpty(County[0])) {
							autoLocation.countyName = County[0];
						}
					} 
				} 
			}
		}
	}
	
	/**
	* 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
	*/
	private void queryProvinces() {
		provinceList = yunWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}
	
	/**
	* 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
	*/
	private void queryCities() {
		cityList = yunWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/**
	* 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
	*/
	private void queryCounties() {
		countyList = yunWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	/**
	* 根据传入的代号和类型从服务器上查询省市县数据。
	*/
	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(yunWeatherDB, response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(yunWeatherDB, response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(yunWeatherDB, response, selectedCity.getId());
				}
				if (result) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								if (locationFlag) { //自动定位时，查询到对应城市信息，然后查询城市下的县/区信息
									yunWeatherDB.queryCitys(locationCity);
									Log.d("cxw", "cxdssfse" + locationCity.getCityCode());
									selectedCity = locationCity;
									queryFromServer(selectedCity.getCityCode(), "county");
								}
								queryCities();
							} else if ("county".equals(type)) {
								if (locationFlag) { //自动定位时，查询到对应县/区信息，把county_code传入WeatherActivity，并跳转
									locationFlag = false;
									yunWeatherDB.queryCounties(locationCounty, locationCity);
									Log.d("cxw", "county_code=" + locationCounty.getCountyCode());
									Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
									intent.putExtra("county_code", locationCounty.getCountyCode());
									startActivity(intent);
									finish();		
								}
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	/**
	* 显示进度对话框
	*/
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	* 关闭进度对话框
	*/
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	* 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出。
	*/
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
