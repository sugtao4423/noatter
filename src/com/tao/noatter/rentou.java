package com.tao.noatter;

import java.util.ArrayList;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class rentou extends Activity {
	
	static EditText text, min, max;
	static ArrayAdapter<String> HomeAdapter, MentionAdapter;
	static TwitterStream stream;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rentou);
		
		text = (EditText)findViewById(R.id.editText1);
		min = (EditText)findViewById(R.id.editText2);
		max = (EditText)findViewById(R.id.editText3);
		try{
		TimeLine();
		Streaming();
		}catch(Exception e){
			showToast(e.toString());
		}
	}
	
	public void onResume(){
		super.onResume();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		//デフォルト有効
		if(pref.getBoolean("enable_default", false)){
			text.setText(pref.getString("rentou_letter", ""));
			min.setText(pref.getString("rentou_min", ""));
			max.setText(pref.getString("rentou_max", ""));
		}else{ //無効
			text.setText("@sarasty_noah attack @");
			min.setHint("1");
			max.setHint("10");
		}
	}
	
	public void start(View v){
		try{
			background(v);
			String Text = text.getText().toString();
			int Min = Integer.valueOf(min.getText().toString());
			int Max = Integer.valueOf(max.getText().toString());
			for( ; Min <= Max; Min++){
				MainActivity.text = Text + " " + Min;
				new MainActivity().tweet();
			}
			}catch(Exception e){
				Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			}
	}
	
	public void TimeLine() throws InterruptedException{
		final ListView HomeList = (ListView)findViewById(R.id.listView1);
		final ListView MentionList = (ListView)findViewById(R.id.listView2);
		final ArrayList<String> HomeArrayList = new ArrayList<String>();
		final ArrayList<String> MentionArrayList = new ArrayList<String>();
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try{
				Paging paging = new Paging(1, 50);
				ResponseList<twitter4j.Status> home = MainActivity.twitter.getUserTimeline(MainActivity.MyScreenName.substring(1), paging);
				ResponseList<twitter4j.Status> mention = MainActivity.twitter.getMentionsTimeline(paging);
				for (twitter4j.Status status : home)
					HomeArrayList.add(status.getText());
                for (twitter4j.Status status : mention)
                    MentionArrayList.add("@" + status.getUser().getScreenName() + " : " + status.getText());
                return true;
				}catch(Exception e){
					Toast.makeText(getApplicationContext(), "取得失敗", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result){
					HomeFinish(HomeList, HomeArrayList);
					MentionFinish(MentionList, MentionArrayList);
				}
			}
		};
		task.execute();
	}
	
	private void HomeFinish(ListView list, ArrayList<String> arrayList){
		HomeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
		list.setAdapter(HomeAdapter);
		background(list);
	}
	private void MentionFinish(ListView list, ArrayList<String> arrayList){
		MentionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
		list.setAdapter(MentionAdapter);
		background(list);
	}
	
	public void HomeListViewadd(String text){
		HomeAdapter.insert(text, 0);
	}
	public void MentionListViewadd(String text){
		MentionAdapter.insert(text, 0);
	}
	
	public void Streaming(){
		final TwitterStreamFactory streamFactory = new TwitterStreamFactory(MainActivity.jconf);
		stream = streamFactory.getInstance(MainActivity.accessToken);
		final Handler handler = new Handler();
		UserStreamAdapter streamadapter = new UserStreamAdapter(){
			public void onStatus(final twitter4j.Status status){
				if(status.getUser().getScreenName().equals(MainActivity.MyScreenName.substring(1))){
					handler.post(new Runnable(){
						public void run(){
							HomeListViewadd(status.getText());
						}
					});
				}
				if(status.getText().matches(".*" + MainActivity.MyScreenName + ".*")){
					handler.post(new Runnable(){
						public void run(){
							MentionListViewadd("@" + status.getUser().getScreenName() + " : " + status.getText());
						}
					});
				}
			}
		};
		stream.addListener(streamadapter);
		stream.user();
		showToast("connect");
	}
	
	public void back(View v){
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}
	
	public void showToast(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	public void background(View v){
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	public void onDestroy(){
		super.onDestroy();
		stream.shutdown();
	}
}