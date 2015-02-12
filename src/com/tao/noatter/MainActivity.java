package com.tao.noatter;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	static EditText sousin_saki, kougeki_saki, backLetter;
	static Spinner spinner;
	static List<Status> mentions;
	static String text, CK, CS;
	static AccessToken accessToken;
	
	static Twitter twitter;
	static TwitterFactory twitterFactory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sousin_saki = (EditText)findViewById(R.id.editText1);
		kougeki_saki = (EditText)findViewById(R.id.editText2);
		backLetter = (EditText)findViewById(R.id.editText3);
		spinner = (Spinner)findViewById(R.id.spinner1);
	}
	
	public void onStart(){
		super.onStart();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if(pref.getString("AccessToken", "").equals("")){
			startActivity(new Intent(this, OAuth.class));
		}else{
			accessToken = new AccessToken(pref.getString("AccessToken", "")
					, pref.getString("AccessTokenSecret", ""));
		}
		if(pref.getString("CustomCK", "").equals("")){
			CK = getResources().getString(R.string.CK);
			CS = getResources().getString(R.string.CS);
		}else{
			CK = pref.getString("CustomCK", "");
			CS = pref.getString("CustomCS", "");
		}
		login();
	}
	
	private void login(){
		AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>(){
			@Override
			protected Void doInBackground(String... params) {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(CK)
				.setOAuthConsumerSecret(CS);
				Configuration jconf = builder.build();
				twitterFactory = new TwitterFactory(jconf);
				twitter = twitterFactory.getInstance(accessToken);
				return null;
			}
		};
		task.execute();
	}
	
	public void sousin(View v){
		text = sousin_saki.getText().toString() + " " + spinner.getSelectedItem().toString()
				+ " " + kougeki_saki.getText().toString() + " " + backLetter.getText().toString();
		tweet();
	}
	
	public void tweet() {
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    twitter.updateStatus(params[0]);
                    return true;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result) {
                    showToast("ツイート送信失敗");
                }
            }
        };
        task.execute(text);
    }
	
	public void mention(View v) throws InterruptedException{
		final ListView list = (ListView)findViewById(R.id.listView1);
		final ArrayList<String> arrayList = new ArrayList<String>();
		AsyncTask<Void, Void, List<String>> task = new AsyncTask<Void, Void, List<String>>(){
			@Override
			protected List<String> doInBackground(Void... params) {
				try{
				ResponseList<twitter4j.Status> mention = twitter.getMentionsTimeline(new Paging(1, 50));
                for (twitter4j.Status status : mention) {
                    arrayList.add(status.getText());
                }
				}catch(Exception e){
					showToast("取得失敗");
				}
                return null;
			}
		};
		task.execute();
		Thread.sleep(2000);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
		list.setAdapter(adapter);
	}
	
	public void rentou(View v){
		startActivity(new Intent(this, rentou.class));
	}
	
	public void background(View v){
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, OAuth.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
