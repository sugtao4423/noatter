package com.tao.noatter;

import java.util.ArrayList;

import twitter4j.Paging;
import twitter4j.ResponseList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class rentou extends Activity {
	
	static EditText text, min, max;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rentou);
		
		text = (EditText)findViewById(R.id.editText1);
		min = (EditText)findViewById(R.id.editText2);
		max = (EditText)findViewById(R.id.editText3);
	}
	
	public void onResume(){
		super.onResume();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		//デフォルト有効
		if(pref.getBoolean("enable_default", true)){
			text.setText("@sarasty_noah attack @");
			min.setHint("1");
			max.setHint("10");
		}else{ //無効
			text.setText(pref.getString("rentou_letter", ""));
			min.setText(pref.getString("rentou_min", ""));
			max.setText(pref.getString("rentou_max", ""));
		}
	}
	
	public void start(View v){
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
		final ListView list = (ListView)findViewById(R.id.listView1);
		final ArrayList<String> arrayList = new ArrayList<String>();
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try{
				ResponseList<twitter4j.Status> mention = MainActivity.twitter.getMentionsTimeline(new Paging(1, 50));
                for (twitter4j.Status status : mention)
                    arrayList.add(status.getText());
                return true;
				}catch(Exception e){
					Toast.makeText(getApplicationContext(), "取得失敗", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result)
					mentionFinish(list, arrayList);
			}
		};
		task.execute();
	}
	
	private void mentionFinish(ListView list, ArrayList<String> arrayList){
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
		list.setAdapter(adapter);
		background(list);
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
