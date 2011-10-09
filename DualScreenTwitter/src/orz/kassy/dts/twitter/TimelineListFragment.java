package orz.kassy.dts.twitter;

import orz.kassy.dts.twitter.color.ColorTheme;
import orz.kassy.dts.twitter.color.ColorThemeWhite;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Paging;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


/**
 * TimeLineリストを表示するフラグメントね
 * @author kashimoto
 */
public class TimelineListFragment extends Fragment implements OnClickListener, OnScrollListener{
    
    private static final String TAG = "TimeLineFragment";
    private OnTimelineListItemClickListener mListener;
    private TweetStatusAdapter mAdapter = null;
    private Handler mHandler = new Handler();
    private AccessToken mAccessToken = null;
    private ColorTheme mColorTheme = new ColorThemeWhite();
    private AsyncTwitter mAsyncTwitter;
    private ListView mListView;
    private View mView;
    private Button mUpdateButton;
    private View mFooterView;
    private boolean mIsUpdating = false;
    private boolean mListAddFlag = false;
    
    /**
     * リストをクリックした時の処理、　なにするかは決めてない 
     * @author kashimoto
     */
    public interface OnTimelineListItemClickListener {
        void onPhotoListItemClick(int resId);
    }
    
    /**
     * このフラグメント内のリストビューをメインタイムラインで更新しますよ 
     */
    public void updateHomeTimeLine(AccessToken accessToken) {
        mAccessToken = accessToken;
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
        getAsyncHomeTimeLine();
    }
    
    /**
     * このフラグメント内のリストビューをMentionsでラインで更新しますよ 
     */
    public void updateMentions(AccessToken accessToken) {
        mAccessToken = accessToken;
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
        getAsyncMentions();
    }

    /**
     * カラー変更しますよ
     */
    public void setColorTheme(ColorTheme colorTheme){
        mColorTheme  = colorTheme;
        mListView.setBackgroundColor(colorTheme.getBackgroundColor());
        if(mAdapter != null) {
            mAdapter.setColorTheme(colorTheme);
            mAdapter.notifyDataSetChanged();
        }
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.timelinelist_fragment, container);
        mListView = (ListView) mView.findViewById(R.id.timelineListView);
        mListView.setScrollingCacheEnabled(false);
        mListView.setOnItemClickListener(listItemClickListener);
        mUpdateButton = (Button)mView.findViewById(R.id.timelineUpdateButton);
        mUpdateButton.setOnClickListener(this);
        mFooterView = inflater.inflate(R.layout.timeline_footer, null);
        mListView.addFooterView(mFooterView);
        mListView.setOnScrollListener(this);
        return mView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnTimelineListItemClickListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPhotoListItemClickListener");
        }
        
        AsyncTwitterFactory factory = new AsyncTwitterFactory(mAsyncTwitterListener);
        mAsyncTwitter = factory.getInstance();
        mAsyncTwitter.setOAuthConsumer(AppUtils.CONSUMER_KEY, AppUtils.CONSUMER_SECRET);
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
    }
    
    private OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> view, View arg1, int position, long id) {
            Log.e(TAG,"on item click "+position );
        }
    };
    
    /**
     * タイムラインの取得（公式AsyncTwitter）
     */
    private void getAsyncHomeTimeLine() {
        mAsyncTwitter.getHomeTimeline();
        mIsUpdating = true;
    }

    /**
     * Mentionラインの取得（公式AsyncTwitter）
     */
    private void getAsyncMentions() {
        mAsyncTwitter.getMentions();
        mIsUpdating = true;
    }
    
    // 後処理 ダイアログ消去とか　実はこれワーカースレッドぽい... ???
    TwitterListener mAsyncTwitterListener = new TwitterAdapter() {
        @Override
        public void gotHomeTimeline(ResponseList<Status> statuses) {
            mIsUpdating = false;
            mAdapter = new TweetStatusAdapter(getActivity(), statuses);
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    mListView.setAdapter(mAdapter);                   
                }
            });
        }
        
        @Override
        public void gotMentions(ResponseList<Status> statuses) {
            mIsUpdating = false;
            mAdapter = new TweetStatusAdapter(getActivity(), statuses);
            mAdapter.setColorTheme(mColorTheme);
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    mListView.setAdapter(mAdapter);                   
                }
            });
        }

        @Override
        public void onException(TwitterException ex, TwitterMethod method) {
        }
    };

    /**
     * ボタン押下リスナー
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
        // 更新ボタン
        case R.id.timelineUpdateButton:
            Paging paging = new Paging(1, 20);
            mAsyncTwitter.getHomeTimeline(paging);
            mIsUpdating = true;
            break;
        }
    }

    /**
     * スクロールリスナー
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.i(TAG,"list scroll "+ firstVisibleItem + ", " + visibleItemCount + ", " + totalItemCount );
        // 一番下までスクロールしたら
        if((firstVisibleItem + visibleItemCount) >= totalItemCount) {
            Log.i(TAG, "list scroll bottom");
            Paging paging = new Paging(2, 20);
            mAsyncTwitter.getHomeTimeline(paging);
            mIsUpdating = true;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {
        // TODO Auto-generated method stub
        
    }
}
