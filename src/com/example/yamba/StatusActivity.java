package com.example.yamba;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends BaseActivity implements OnClickListener,
		TextWatcher, OnSharedPreferenceChangeListener, LocationListener {
	private static final String TAG = "StatusActivity";
	private static final long LOCATION_MIN_TIME = 3600000; // One hour
	private static final float LOCATION_MIN_DISTANCE = 1000; // One kilometer
	private static String provider;
	EditText editText;
	Button updateButton;
	Twitter twitter;
	TextView textCount;
	SharedPreferences prefs;
	LocationManager locationManager;
	Location location;

	/** activity首次创建时调用 **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);

		// 获取对view的引用
		editText = (EditText) findViewById(R.id.editText);
		updateButton = (Button) findViewById(R.id.buttonUpdate);
		updateButton.setOnClickListener(this);

		// 获取textCount
		textCount = (TextView) findViewById(R.id.textCount);
		textCount.setText(Integer.toString(140));
		textCount.setTextColor(Color.GREEN);
		editText.addTextChangedListener(this);

		// 设置 preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Setup location information
		provider = ((YambaApplication) yamba).getProvider();

		if (!YambaApplication.LOCATION_PROVIDER_NONE.equals(provider)) {
			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		}

		if (locationManager != null) {
			location = locationManager.getLastKnownLocation(provider);
			locationManager.requestLocationUpdates(provider, LOCATION_MIN_TIME,
					LOCATION_MIN_DISTANCE, this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	// 异步发送到twitter
	class PostToTwitter extends AsyncTask<String, Integer, String> {
		// 在触发时发起后台操作
		@Override
		protected String doInBackground(String... statuses) {
			try {
				YambaApplication yamba = ((YambaApplication) getApplication());

				if (location != null) {
					double latlong[] = { location.getLatitude(),
							location.getLongitude() };
					yamba.getTwitter().setMyLocation(latlong);
				}

				Twitter.Status status = yamba.getTwitter().updateStatus(
						statuses[0]);
				return status.text;
			} catch (TwitterException e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
				return "Failed to post";
			}
		}

		// 在更新状态时触发
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			Log.e(TAG, values.toString());
		}

		// 在后台任务执行完毕时触发
		protected void onPostExecute(String result) {
			Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG)
					.show();

		}
	}

	@Override
	public void onClick(View v) {
		String status = editText.getText().toString();
		new PostToTwitter().execute(status);
		Log.d(TAG, "onClicked");
		/**
		 * try { getTwitter().setStatus(editText.getText().toString()); } catch
		 * (TwitterException e) { Log.d(TAG, "Twitter setStatus failed: " + e);
		 * }
		 **/
	}

	// TextWatcher methods
	@Override
	public void afterTextChanged(Editable statusText) {
		int count = 140 - statusText.length();
		textCount.setText(Integer.toString(count));
		textCount.setTextColor(Color.GREEN);

		if (count < 10) {
			textCount.setTextColor(Color.YELLOW);
		}

		if (count < 0) {
			textCount.setTextColor(Color.RED);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		twitter = null;
	}

	// LocationListener methods
	public void onLocationChanged(Location location) {
		this.location = location;
	}

	public void onProviderDisabled(String provider) {
		if (this.provider.equals(provider))
			locationManager.removeUpdates(this);
	}

	public void onProviderEnabled(String provider) {
		if (this.provider.equals(provider))
			locationManager.requestLocationUpdates(this.provider,
					LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, this);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

}
