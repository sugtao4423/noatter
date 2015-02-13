package com.tao.noatter;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Preferences extends PreferenceActivity {
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content,  new MyPreferencesFragment()).commit();
		ActionBar actionbar = getActionBar();
		actionbar.setHomeButtonEnabled(true);
	}
	
	public static class MyPreferencesFragment extends PreferenceFragment {
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference);
		}
	}
}
