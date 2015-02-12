package com.tao.noatter;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.ResponseList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class rentou extends Activity {
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rentou);
	}
	
	public void start(View v){
		EditText text, min, max;
		text = (EditText)findViewById(R.id.editText1);
		min = (EditText)findViewById(R.id.editText2);
		max = (EditText)findViewById(R.id.editText3);
		try{
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
	
	public void mention(View v) throws InterruptedException{
		Toast.makeText(this, "2秒お待ちください", Toast.LENGTH_SHORT).show();
		final ListView list = (ListView)findViewById(R.id.listView1);
		final ArrayList<String> arrayList = new ArrayList<String>();
		AsyncTask<Void, Void, List<String>> task = new AsyncTask<Void, Void, List<String>>(){
			@Override
			protected List<String> doInBackground(Void... params) {
				try{
				ResponseList<twitter4j.Status> mention = MainActivity.twitter.getMentionsTimeline(new Paging(1, 50));
                for (twitter4j.Status status : mention) {
                    arrayList.add(status.getText());
                }
				}catch(Exception e){
					Toast.makeText(getApplicationContext(), "取得失敗", Toast.LENGTH_SHORT).show();
				}
                return null;
			}
		};
		task.execute();
		Thread.sleep(2000);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
		list.setAdapter(adapter);
	}
	
	public void back(View v){
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}
	
	public void background(View v){
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
}
