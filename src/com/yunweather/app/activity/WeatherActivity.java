package com.yunweather.app.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yunweather.app.R;
import com.yunweather.app.service.AutoUpdateService;
import com.yunweather.app.util.CustomScrollView;
import com.yunweather.app.util.HttpCallbackListener;
import com.yunweather.app.util.HttpUtil;
import com.yunweather.app.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener{
	private RelativeLayout weatherInfoLayout;
	/**
	* ������ʾ������
	* 
	* */
	private TextView cityNameText;
	/**
	* ������ʾ����ʱ��
	*/
	private TextView publishText;
	/**
	* ������ʾ����������Ϣ
	*/
	private TextView weatherDespText;
	/**
	* ������ʾ��������
	*/
	private TextView temp1Text;
	/**
	* ������ʾ��������
	*/
	private TextView day2Text;
	/**
	* ������ʾ��ǰ����
	*/
	private TextView currentDateText;
	/**
	* �л����а�ť
	*/
	private Button switchCity;
	/**
	* ����������ť
	*/
	private Button refreshWeather;
	
	//private TextView LocationResult;
	
	private ImageView weatherImage;
	
	private TextView cruTemp;
	
	private CustomScrollView scrollView;
	private TextView mTextView;
	private int pageCount = 0;
	private LayoutInflater inflater;
	private LinearLayout mLinearLayout; 
	private LinearLayout.LayoutParams param;
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_main);
		setupView();
		
		//setContentView(R.layout.weather_layout);
		// ��ʼ�����ؼ�
		weatherInfoLayout = (RelativeLayout)findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		cruTemp   = (TextView) findViewById(R.id.temp_cur);
		day2Text = (TextView) findViewById(R.id.text_day2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		weatherImage = (ImageView) findViewById(R.id.image_weather);
		weatherImage.setVisibility(View.INVISIBLE);
		String countyCode = getIntent().getStringExtra("county_code");
		
		//LocationResult = (TextView) findViewById(R.id.textView1);
		//LocationResult.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		
		if (!TextUtils.isEmpty(countyCode)) {
			// ���ؼ�����ʱ��ȥ��ѯ����
			publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// û���ؼ�����ʱ��ֱ����ʾ��������
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("ͬ����...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	* ��ѯ�ؼ���������Ӧ���������š�
	*/
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}
	
	/**
	* ��ѯ������������Ӧ��������
	*/
	private void queryWeatherInfo(String weatherCode) {
		//String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";  //�й�������api;json����
		String address = "http://wthrcdn.etouch.cn/weather_mini?citykey=" + weatherCode;  //������ ����API�ӿ�; json����
		queryFromServer(address, "weatherCode");
	}
	
	/**
	* ���ݴ���ĵ�ַ������ȥ���������ѯ�������Ż���������Ϣ��
	*/
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(final String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// �ӷ��������ص������н�������������
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// ������������ص�������Ϣ
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showWeather();
				}
			});
		}
	}
			
	@Override
	public void onError(Exception e) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				publishText.setText("ͬ��ʧ��");
			}
			});
			}
		});
	}
	
	private void showWeatherPicture(String pic) {
		if (pic.equals("��")) {
			weatherImage.getDrawable().setLevel(0);
			weatherImage.setVisibility(View.VISIBLE);
		}else if (pic.equals("����")) {
			weatherImage.getDrawable().setLevel(2);
			weatherImage.setVisibility(View.VISIBLE);
		}else if (pic.equals("С��") || pic.equals("����")) {
			weatherImage.getDrawable().setLevel(1);
			weatherImage.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	* ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ�������ϡ�
	*/
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText( prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("tempLow0", "") + " ~" + prefs.getString("tempHigh0", ""));
		day2Text.setText("���� " + prefs.getString("tempLow1", "") + " ~" + prefs.getString("tempHigh1", ""));
		cruTemp.setText(prefs.getString("temp_cur", "")+ "��");
		weatherDespText.setText(prefs.getString("weatherConditions0", ""));
		showWeatherPicture(prefs.getString("weatherConditions0", ""));
		publishText.setText("���췢��");
		currentDateText.setText(prefs.getString("weatherDate0", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
	

	private void setupView() { 
		scrollView = (CustomScrollView) findViewById(R.id.definedview); 
		mTextView = (TextView) findViewById(R.id.text_page); 
		pageCount = 2;
	  
		mTextView.setText(1 + "/" + pageCount); 
	  
		for (int i = 0; i < pageCount; i++) { 
			param = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT); 
			inflater = this.getLayoutInflater(); 
		  
			if (i == 0) { 
				final View addview = inflater.inflate( R.layout.weather_info_main, null); 
				mLinearLayout = new LinearLayout(this); 
				mLinearLayout.addView(addview, param); 
				scrollView.addView(mLinearLayout); 
			} else { 
				View addview = inflater.inflate(R.layout.activity_main_two, null); 		  
				mLinearLayout = new LinearLayout(this); 
				mLinearLayout.addView(addview, param); 
				scrollView.addView(mLinearLayout); 
			} 
		  
	} 

	scrollView.setPageListener(new CustomScrollView.PageListener() {
		@Override
		public void page(int page) { 
		setCurPage(page); 
		} 
	}); 
} 
  
	private void setCurPage(int page) { 
		mTextView.setText((page + 1) + "/" + pageCount); 
	} 
  
}
