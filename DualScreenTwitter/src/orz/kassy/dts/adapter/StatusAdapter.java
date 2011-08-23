package orz.kassy.dts.adapter;

import java.util.List;

import orz.kassy.dts.async.ThumbnailTask;
import orz.kassy.dts.image.ImageCache;
import orz.kassy.dts.twitter.R;
import twitter4j.Status;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusAdapter extends ArrayAdapter<Status> {
	private LayoutInflater inflater;
	
	public StatusAdapter(Context context, List<Status> objects) {
		this(context, 0, objects);
	}
	
	public StatusAdapter(Context context, int textViewResourceId, List<Status> objects) {
		super(context, textViewResourceId, objects);
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		if(convertView == null) {
			convertView = inflater.inflate(R.layout.timeline_layout, null);
			holder = new ViewHolder();
			holder.img_icon = (ImageView)convertView.findViewById(R.id.img_icon);
			holder.lbl_screenname = (TextView)convertView.findViewById(R.id.lbl_screenname);
			holder.lbl_tweet = (TextView)convertView.findViewById(R.id.lbl_tweet);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		Status status = this.getItem(position);
		if(status != null) {
			String strKey = status.getUser().getScreenName();
			String strName = status.getUser().getName();
			holder.lbl_screenname.setText(strName);
			holder.lbl_tweet.setText(status.getText());
			if(ImageCache.getImage(strKey) == null) {
				if(holder.task == null) {
					holder.task = new ThumbnailTask(holder.img_icon);
					holder.task.execute(status.getUser().getProfileImageURL().toString(), strKey);
				} else {
					holder.img_icon.setImageBitmap(null);
					if(holder.task.getStatus() == AsyncTask.Status.FINISHED) {
						// 受信処理が失敗してたら再度リクエストする。
						holder.task = new ThumbnailTask(holder.img_icon);
						holder.task.execute(status.getUser().getProfileImageURL().toString(), strKey);						
					}
				}
			} else {
				holder.img_icon.setImageBitmap(ImageCache.getImage(strKey));
			}

		}
		return convertView;
	}
	
	class ViewHolder {
		ImageView img_icon;
		TextView lbl_screenname;
		TextView lbl_tweet;
		ThumbnailTask task;
	}
}
