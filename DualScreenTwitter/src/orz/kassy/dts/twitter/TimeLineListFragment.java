package orz.kassy.dts.twitter;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * TimeLineリストを表示するフラグメントね
 * @author kashimoto
 */
public class TimeLineListFragment extends ListFragment{
    private OnTimeLineListItemClickListener mListener;
    private StatusAdapter mAdapter = null;
    private Handler mHandler = new Handler();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnTimeLineListItemClickListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPhotoListItemClickListener");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    public interface OnTimeLineListItemClickListener {
        void onPhotoListItemClick(int resId);
    }
    
    /**
     * タイムラインの取得（公式AsyncTwitter）
     */
    private void getAsyncTimeLine() {
        
        // 前処理　ダイアログ 表示

        // 後処理 ダイアログ消去とか　実はこれワーカースレッドぽい... ???
        TwitterListener listener = new TwitterAdapter() {
            @Override
            public void gotHomeTimeline(ResponseList<Status> statuses) {
                mAdapter = new StatusAdapter(getActivity(), statuses);
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        setListAdapter(mAdapter);                   
                    }
                });
            }
            @Override
            public void onException(TwitterException ex, TwitterMethod method) {
            }
        };
        
        //mAccessToken = AppUtils.loadAccessToken(this);
        AsyncTwitterFactory factory = new AsyncTwitterFactory(listener);
        AsyncTwitter asyncTwitter = factory.getInstance();
        asyncTwitter.setOAuthConsumer(AppUtils.CONSUMER_KEY, AppUtils.CONSUMER_SECRET);
       // asyncTwitter.setOAuthAccessToken(mAccessToken);
        asyncTwitter.getHomeTimeline();
    }


}
