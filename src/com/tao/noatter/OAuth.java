package com.tao.noatter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class OAuth extends Activity{
	
	static Twitter twitter;
	static TwitterFactory twitterFactory;
	
	static RequestToken rt;
	
	static EditText pin, CK, CS;
	static String CustomCK, CustomCS;
	static SharedPreferences pref;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth);
        
        pin = (EditText)findViewById(R.id.editText1);
        CK = (EditText)findViewById(R.id.editText2);
        CS = (EditText)findViewById(R.id.editText3);
        
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
        
        CK.setText(pref.getString("CustomCK", ""));
        CS.setText(pref.getString("CustomCS", ""));
    }

    public void ninsyo(View v) {
    	if(CK.getText().toString().equals("")){
        	CustomCK = getResources().getString(R.string.CK);
        	CustomCS = getResources().getString(R.string.CS);
        }else{
        	CustomCK = CK.getText().toString();
        	CustomCS = CS.getText().toString();
        	pref.edit().putString("CustomCK", CustomCK).putString("CustomCS", CustomCS).commit();
        }
    	AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(CustomCK)
				.setOAuthConsumerSecret(CustomCS);
				Configuration jconf = builder.build();
				twitterFactory = new TwitterFactory(jconf);
				twitter = twitterFactory.getInstance();
				
				try {
					rt = twitter.getOAuthRequestToken();
					String url = rt.getAuthorizationURL();
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		task.execute();
    }
    
    public void pin(View v){
    	AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try{
					AccessToken accessToken = twitter.getOAuthAccessToken(rt, pin.getText().toString());
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					pref.edit().putString("AccessToken", accessToken.getToken())
					.putString("AccessTokenSecret", accessToken.getTokenSecret()).commit();
					return true;
				}catch(Exception e){
					Toast.makeText(getApplicationContext(), "おかしい", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result){
					startActivity(new Intent(getApplicationContext(), MainActivity.class));
					finish();
				}
			}
    	};
    	task.execute();
    }
    
    
    public void background(View v){
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
}