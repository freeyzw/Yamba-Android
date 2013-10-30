package com.example.yamba;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {//PrefsActivity继承PreferenceActivity类

    @SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {//重载onCreate()方法初始化activity

        super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.prefs);//preference activity通过调用addPreferencesFromResource()设置自身内容

	}

}
