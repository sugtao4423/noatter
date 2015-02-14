package com.tao.noatter;

import twitter4j.Status;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;


public class CustomAdapter extends ArrayAdapter<Status>{
	private LayoutInflater mInflater;
	public CustomAdapter(Context context){
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	static class ViewHolder{
		TextView name, text;
		SmartImageView icon;
	}
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder;
		if (convertView == null){
			convertView = mInflater.inflate(R.layout.list_item_tweet, null);
			TextView name = (TextView) convertView.findViewById(R.id.name_screenName);
			TextView text = (TextView) convertView.findViewById(R.id.tweetText);
			SmartImageView icon = (SmartImageView) convertView.findViewById(R.id.icon);
			
			holder = new ViewHolder();
			holder.name = name;
			holder.text = text;
			holder.icon = icon;
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		Status item = getItem(position);
		holder.name.setText(item.getUser().getName() + " - @" + item.getUser().getScreenName());
		holder.text.setText(item.getText());
		holder.icon.setImageUrl(item.getUser().getProfileImageURL());
		return convertView;
	}
}