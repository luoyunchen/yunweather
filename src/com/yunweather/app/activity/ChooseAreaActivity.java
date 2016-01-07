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
	* ʡ�б�
	*/
	private List<Province> provinceList;
	/**
	* ���б�
	*/
	private List<City> cityList;
	/**
	* ���б�
	*/
	private List<County> countyList;
	/**
	* ѡ�е�ʡ��
	*/
	private Province selectedProvince;
	/**
	* ѡ�еĳ���
	*/
	private City selectedCity;
	/**
	* ��ǰѡ�еļ���
	*/
	private int currentLevel;
	
	/**
	* �Ƿ��WeatherActivity����ת������
	*/
	private boolean isFromWeatherActivity;
	
	/**
	* ��¼�Ӱٶȵõ���ʡ���У��ػ��������ơ�
	*/
	public class AutoLocation {
		private String provinceName;
		private String cityName;
		private String countyName;
	}
	
	/**
	* AutoLocation�����á�
	*/
	private AutoLocation autoLocation;
	
	/**
	* �Զ���λ��ʡ��
	*/
	private Province locationProvince;
	/**
	* �Զ���λ�ĵĳ���
	*/
	private City locationCity;
	/**
	* �Զ���λ�ĵ���/��
	*/
	private County locationCounty;
	/**
	* �Ƿ����Զ���λ��
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
		queryProvinces(); // ����ʡ������
		Log.d("cxw", "go to LocationActivity"); 
		if (!isFromWeatherActivity) { //������Ǵ�WeatherActivity��ת�����ģ��Ϳ���ִ��
			Intent intent = new Intent(ChooseAreaActivity.this, LocationActivity.class);
			startActivityForResult(intent, 1);
		}
	}
	
	/**
	* �����ͽ����Ӱٶȵ��������ݣ�Ȼ��ӷ�������ѯ��county��ID�����ڲ�ѯ�Զ���λ�ĳ�������
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
	* �����ʹ����ٶȷ��������صĵ�ַ����
	*/
	
	public static void  handleAdrrResponse(String response, AutoLocation autoLocation) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split("��");
			if (!TextUtils.isEmpty(allCounties[1])) {
				String[] Province = allCounties[1].split("ʡ");
				autoLocation.provinceName = Province[0];
				if (!TextUtils.isEmpty(Province[1])) {
					String[] City = Province[1].split("��");
					autoLocation.cityName = City[0];
					if (!TextUtils.isEmpty(City[1])) {
						String[] County = City[1].split("��");
						if (!TextUtils.isEmpty(County[0])) {
							autoLocation.countyName = County[0];
						}
						County = City[1].split("��");
						if (!TextUtils.isEmpty(County[0])) {
							autoLocation.countyName = County[0];
						}
					} 
				} 
			}
		}
	}
	
	/**
	* ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
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
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}
	
	/**
	* ��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
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
	* ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
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
	* ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ�������ݡ�
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
					// ͨ��runOnUiThread()�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								if (locationFlag) { //�Զ���λʱ����ѯ����Ӧ������Ϣ��Ȼ���ѯ�����µ���/����Ϣ
									yunWeatherDB.queryCitys(locationCity);
									Log.d("cxw", "cxdssfse" + locationCity.getCityCode());
									selectedCity = locationCity;
									queryFromServer(selectedCity.getCityCode(), "county");
								}
								queryCities();
							} else if ("county".equals(type)) {
								if (locationFlag) { //�Զ���λʱ����ѯ����Ӧ��/����Ϣ����county_code����WeatherActivity������ת
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
				// ͨ��runOnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	/**
	* ��ʾ���ȶԻ���
	*/
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	* �رս��ȶԻ���
	*/
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	* ����Back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б���ʡ�б�������ֱ���˳���
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