package orz.kassy.dts.twitter;

import orz.kassy.dts.twitter.color.ColorTheme;
import orz.kassy.dts.twitter.color.ColorThemeWhite;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.conf.Configuration;
import twitter4j.http.AccessToken;
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
    private TweetStatusAdapter mAdapter = null;
    private Handler mHandler = new Handler();
    private AccessToken mAccessToken = null;
    private ColorTheme mColorTheme = new ColorThemeWhite();
    private AsyncTwitter mAsyncTwitter;

    /**
     * リストをクリックした時の処理、　なにするかは決めてない 
     * @author kashimoto
     */
    public interface OnTimeLineListItemClickListener {
        void onPhotoListItemClick(int resId);
    }
    
    /**
     * このフラグメント内のリストビューをメインタイムラインで更新しますよ 
     */
    public void updateHomeTimeLine(AccessToken accessToken) {
        mAccessToken = accessToken;
        getAsyncHomeTimeLine();
    }
    
    /**
     * このフラグメント内のリストビューをMentionsでラインで更新しますよ 
     */
    public void updateMentions(AccessToken accessToken) {
        mAccessToken = accessToken;
        getAsyncMentions();
    }

    /**
     * カラー変更しますよ
     */
    public void setColorTheme(ColorTheme colorTheme){
        mColorTheme  = colorTheme;
        getListView().setBackgroundColor(colorTheme.getBackgroundColor());
        if(mAdapter != null) {
            mAdapter.setColorTheme(colorTheme);
            mAdapter.notifyDataSetChanged();
        }
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setScrollingCacheEnabled(false); 
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
        
        AsyncTwitterFactory factory = new AsyncTwitterFactory(mAsyncTwitterListener);
        mAsyncTwitter = factory.getInstance();
        mAsyncTwitter.setOAuthConsumer(AppUtils.CONSUMER_KEY, AppUtils.CONSUMER_SECRET);
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    
    /**
     * タイムラインの取得（公式AsyncTwitter）
     */
    private void getAsyncHomeTimeLine() {
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
        mAsyncTwitter.getHomeTimeline();
    }

    /**
     * Mentionラインの取得（公式AsyncTwitter）
     */
    private void getAsyncMentions() {
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
        mAsyncTwitter.getMentions();
    }
    
    
    
    // 後処理 ダイアログ消去とか　実はこれワーカースレッドぽい... ???
    TwitterListener mAsyncTwitterListener = new TwitterAdapter() {
        @Override
        public void gotHomeTimeline(ResponseList<Status> statuses) {
            mAdapter = new TweetStatusAdapter(getActivity(), statuses);
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    setListAdapter(mAdapter);                   
                }
            });
        }
        
        @Override
        public void gotMentions(ResponseList<Status> statuses) {
            mAdapter = new TweetStatusAdapter(getActivity(), statuses);
            mAdapter.setColorTheme(mColorTheme);
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
}
