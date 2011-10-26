package orz.kassy.dts.twitter;

import java.util.List;

import orz.kassy.dts.async.ThumbnailTask;
import orz.kassy.dts.image.ImageCache;
import orz.kassy.dts.twitter.R;
import orz.kassy.dts.twitter.color.ColorTheme;
import orz.kassy.dts.twitter.color.ColorThemeWhite;
import twitter4j.ResponseList;
import twitter4j.Status;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetStatusAdapter extends ArrayAdapter<Status> {
	private static final String TAG = "TweetStatusAdapter";
    private LayoutInflater mInflater;
	private ColorTheme mColorTheme = new ColorThemeWhite();
    private List<Status> mStatusList;
	private int mSelectedPosition = -1;
    
	/**
	 * コンストラクタ
	 */
	public TweetStatusAdapter(Context context, List<Status> objects) {
		this(context, 0, objects);
	}
	
	/**
	 * コンストラクタ
	 */
	public TweetStatusAdapter(Context context, int textViewResourceId, List<Status> statusList) {
		super(context, textViewResourceId, statusList);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mStatusList = statusList;
	}

	/**
	 * カラーの設定
	 */
	public void setColorTheme(ColorTheme colorTheme) {
	    mColorTheme = colorTheme;
	}
	
	/**
	 *　リストに追加
	 * @param statuses
	 */
    public void addAll(ResponseList<Status> statuses) {
        mStatusList.addAll(statuses);
    }
    
    /**
     * リスト返してもらう
     */
    public List<Status> getList() {
        return mStatusList;
    }
    
    /**
     * 選択箇所を色変更するよ
     * 選択解除するときは引数に-1とか入れるべし
     */
    public void setSelected(int position){
        mSelectedPosition = position;
        notifyDataSetChanged();
    }
    
    @Override
    public Status getItem(int position) {
        Log.i(TAG,"position = "+position);
        return mStatusList.get(position);
    }
        
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		// Viewを取得するよ
		if(convertView == null) {
		    // 新規に作る方
			convertView = mInflater.inflate(R.layout.timeline_item, null);
			holder = new ViewHolder();
			holder.img_icon = (ImageView)convertView.findViewById(R.id.img_icon);
			holder.lbl_screenname = (TextView)convertView.findViewById(R.id.lbl_screenname);
			// holder.lbl_name = (TextView)convertView.findViewById(R.id.lbl_name);
			holder.lbl_tweet = (TextView)convertView.findViewById(R.id.lbl_tweet);
			convertView.setTag(holder);
		} else {
		    // 使いまわす方
			holder = (ViewHolder)convertView.getTag();
		}

		// 色の設定をするよ
		holder.lbl_screenname.setTextColor(mColorTheme.getTextColor());
        holder.lbl_tweet.setTextColor(mColorTheme.getTextColor());
        convertView.setBackgroundColor(0x00000000);
        // 選択されてる奴は特別だぜ
        if(position == mSelectedPosition){
            holder.lbl_screenname.setTextColor(0xffff0000);
            holder.lbl_tweet.setTextColor(0xffff0000);
            convertView.setBackgroundColor(0xff00ff00);
        }
		
        // 文言を入れ込むよ
		Status status = this.getItem(position);
		if(status == null) {
		    return convertView;
		}
		String strKey = status.getUser().getScreenName();
		String strName = status.getUser().getName();
		// holder.lbl_screenname.setText(strName);
        holder.lbl_screenname.setText(strKey);
        // holder.lbl_name.setText(strKey);
		holder.lbl_tweet.setText(status.getText());

		// 画像を取得して入れ込むよ
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
