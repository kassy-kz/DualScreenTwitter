package orz.kassy.dts.twitter;

import java.util.List;

import orz.kassy.dts.image.ImageCache;
import orz.kassy.dts.image.ThumbnailTask;
import orz.kassy.dts.twitter.R;
import orz.kassy.dts.twitter.color.ColorTheme;
import orz.kassy.dts.twitter.color.ColorThemeWhite;
import twitter4j.Status;
import twitter4j.UserList;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TwitterUserListAdapter extends ArrayAdapter<UserList> {
	private LayoutInflater inflater;
	private ColorTheme mColorTheme = new ColorThemeWhite();
	
	/**
	 * コンストラクタ
	 */
	public TwitterUserListAdapter(Context context, List<UserList> objects) {
		this(context, 0, objects);
	}
	
	/**
	 * コンストラクタ
	 */
	public TwitterUserListAdapter(Context context, int textViewResourceId, List<UserList> objects) {
		super(context, textViewResourceId, objects);
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * カラーの設定
	 */
	public void setColorTheme(ColorTheme colorTheme) {
	    mColorTheme = colorTheme;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		if(convertView == null) {
			convertView = inflater.inflate(R.layout.timeline_item, null);
			holder = new ViewHolder();
			holder.img_icon = (ImageView)convertView.findViewById(R.id.img_icon);
			holder.lbl_screenname = (TextView)convertView.findViewById(R.id.lbl_screenname);
//            holder.lbl_name = (TextView)convertView.findViewById(R.id.lbl_name);
			holder.lbl_tweet = (TextView)convertView.findViewById(R.id.lbl_tweet);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}

		// 色の設定だ
		holder.lbl_screenname.setTextColor(mColorTheme.getTextColor());
        //holder.lbl_name.setTextColor(mColorTheme.getTextColor());
        holder.lbl_tweet.setTextColor(mColorTheme.getTextColor());
		
		UserList userList = this.getItem(position);
		if(userList != null) {
			String strKey = userList.getUser().getScreenName();
			String strName = userList.getUser().getName();
            holder.lbl_screenname.setText(strKey);
			holder.lbl_tweet.setText(userList.getName());
			if(ImageCache.getImage(strKey) == null) {
				if(holder.task == null) {
					holder.task = new ThumbnailTask(holder.img_icon);
					holder.task.execute(userList.getUser().getProfileImageURL().toString(), strKey);
				} else {
					holder.img_icon.setImageBitmap(null);
					if(holder.task.getStatus() == AsyncTask.Status.FINISHED) {
						// 受信処理が失敗してたら再度リクエストする。
						holder.task = new ThumbnailTask(holder.img_icon);
						holder.task.execute(userList.getUser().getProfileImageURL().toString(), strKey);						
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
		TextView lbl_name;
		TextView lbl_tweet;
		ThumbnailTask task;
	}
}
