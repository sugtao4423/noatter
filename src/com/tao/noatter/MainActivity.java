package com.tao.noatter;

import java.util.List;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	static EditText sousin_saki, kougeki_saki, backLetter;
	static Spinner spinner;
	static List<Status> mentions;
	static String text, CK, CS, MyScreenName;
	static SharedPreferences pref;
	static CustomAdapter HomeAdapter, MentionAdapter;
	
	static Twitter twitter;
	static TwitterFactory twitterFactory;
	static TwitterStream stream;
	static AccessToken accessToken;
	static Configuration jconf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sousin_saki = (EditText)findViewById(R.id.editText1);
		kougeki_saki = (EditText)findViewById(R.id.editText2);
		backLetter = (EditText)findViewById(R.id.editText3);
		spinner = (Spinner)findViewById(R.id.spinner1);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(pref.getString("AccessToken", "").equals(""))
			startActivity(new Intent(this, OAuth.class));
		else{
			accessToken = new AccessToken(pref.getString("AccessToken", "")
					, pref.getString("AccessTokenSecret", ""));
			if(pref.getString("CustomCK", "").equals("")){
				CK = getResources().getString(R.string.CK);
				CS = getResources().getString(R.string.CS);
			}else{
				CK = pref.getString("CustomCK", "");
				CS = pref.getString("CustomCS", "");
			}
			login();
		}
	}
	
	public void onResume(){
		super.onResume();
		//デフォルト有効
		if(pref.getBoolean("enable_default", false)){
			sousin_saki.setText(pref.getString("sousin_saki", ""));
			kougeki_saki.setText(pref.getString("kougeki_saki", ""));
			backLetter.setText(pref.getString("back_letter", ""));
		}else{ //無効
			sousin_saki.setText("@sarasty_noah");
			kougeki_saki.setHint("@flum_");
			backLetter.setHint("のあちゃんに攻撃");
		}
	}
	
	private void login(){
		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(String... params) {
				try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(CK)
				.setOAuthConsumerSecret(CS);
				jconf = builder.build();
				twitterFactory = new TwitterFactory(jconf);
				twitter = twitterFactory.getInstance(accessToken);
				MyScreenName = "@" + twitter.getScreenName();
				} catch (Exception e) {
					showToast(e.toString());
				}
				return true;
			}
			@Override
            protected void onPostExecute(Boolean result) {
                if (result){
                    try {
						TimeLine();
						Streaming();
					} catch (Exception e) {
						showToast(e.toString());
					}
                }
            }
		};
		task.execute();
	}
	
	public void sousin(View v){
		text = sousin_saki.getText().toString() + " " + spinner.getSelectedItem().toString()
				+ " " + kougeki_saki.getText().toString() + " " + backLetter.getText().toString();
		tweet();
		background(v);
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
	
	public void TimeLine() throws InterruptedException{
		final ListView HomeList = (ListView)findViewById(R.id.listView1);
		final ListView MentionList = (ListView)findViewById(R.id.listView2);
		final int count = Integer.valueOf(pref.getString("TimeLineCount", "50"));
		HomeAdapter = new CustomAdapter(this);
		MentionAdapter = new CustomAdapter(this);
		
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try{
				Paging paging = new Paging(1, count);
				ResponseList<twitter4j.Status> home = twitter.getUserTimeline(MyScreenName.substring(1), paging);
				ResponseList<twitter4j.Status> mention = twitter.getMentionsTimeline(paging);
                for (twitter4j.Status status : home){
                	HomeAdapter.add(status);
                }
                for (twitter4j.Status status : mention)
                    MentionAdapter.add(status);
                return true;
                }catch(Exception e){
					showToast("取得失敗");
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result){
					background(HomeList);
					HomeList.setAdapter(HomeAdapter);
					MentionList.setAdapter(MentionAdapter);
				}
			}
		};
		task.execute();
	}
	
	public void Streaming(){
		try{
		
		final TwitterStreamFactory streamFactory = new TwitterStreamFactory(jconf);
		stream = streamFactory.getInstance(accessToken);
		final Handler handler = new Handler();
		UserStreamAdapter streamadapter = new UserStreamAdapter(){
			public void onStatus(final twitter4j.Status status){
				if(status.getUser().getScreenName().equals(MyScreenName.substring(1))){
					handler.post(new Runnable(){
						public void run(){
							HomeAdapter.insert(status, 0);
						}
					});
				}
				if(status.getText().matches(".*" + MyScreenName + ".*")){
					handler.post(new Runnable(){
						public void run(){
							MentionAdapter.insert(status, 0);
						}
					});
				}
			}
		};
		stream.addListener(streamadapter);
		stream.user();
		showToast("connect");
		
		}catch(Exception e){
			showToast(e.toString());
		}
	}
	
	public void rentou(View v){
		startActivity(new Intent(this, rentou.class));
	}
	
	public void background(View v){
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	stream.shutdown();
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
		if (id == R.id.oauth) {
			startActivity(new Intent(this, OAuth.class));
			return true;
		}if (id == R.id.setting){
			startActivity(new Intent(this, Preferences.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
