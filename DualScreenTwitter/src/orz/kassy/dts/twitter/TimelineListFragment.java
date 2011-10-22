package orz.kassy.dts.twitter;

import java.util.List;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageButton;

/**
 * TimeLineリストを表示するフラグメントね
 * @author kashimoto
 */
public class TimelineListFragment extends Fragment implements OnClickListener, OnScrollListener{
    
    private static final String TAG = "TimeLineFragment";

//    private static final int TIMELINE_TYPE_HOME = 0;
//    private static final int AppUtils.TIMELINE_TYPE_MENTION = 1;

    private OnTimelineListItemClickListener mTimelineListener;
    private OnSettingButtonClickListener mSettingListener;
    private TweetStatusAdapter mAdapter = null;
    private Handler mHandler = new Handler();
    private AccessToken mAccessToken = null;
    private ColorTheme mColorTheme = new ColorThemeWhite();
    private AsyncTwitter mAsyncTwitter;
    private ListView mListView;
    private View mView;
    private ImageButton mUpdateButton;
    private ImageButton mSettingButton;
    private View mFooterView;
    private boolean mIsUpdating = false;
    private ProgressBar mProgressBar;
    private View mHeaderNormal;
    private TextView mHeaderNormalText;
    private View mHeaderProgress;
    private TextView mHeaderProgressText;

    // フラグメントモード　ホームタイムラインか、メンションか、とか...
    private int mFragmentMode;
    private int mPageCount=1;
    // リストへの追記モードか否か... 
    private boolean mListAddFlag = false;
    // このフラグメントのID　いろいろ使うよ
    private int mThisFragmentId = 0;

    
    /**
     * 初期処理
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.timelinelist_fragment, container);
        mListView = (ListView) mView.findViewById(R.id.timelineListView);
        mListView.setScrollingCacheEnabled(false);
        mListView.setOnItemClickListener(listItemClickListener);
        mUpdateButton = (ImageButton)mView.findViewById(R.id.timelineUpdateButton);
        mUpdateButton.setOnClickListener(this);
        mSettingButton = (ImageButton)mView.findViewById(R.id.timelineSettingButton);
        mSettingButton.setOnClickListener(this);

        // リストビューの設定
        mFooterView = inflater.inflate(R.layout.timeline_footer, null);
        mListView.addFooterView(mFooterView);
        mListView.setOnScrollListener(this);

        // プログレスバーの設定
        mProgressBar = (ProgressBar)mView.findViewById(R.id.timelineProgressBar);
        mProgressBar.setMax(100); // 水平プログレスバーの最大値を設定
        //mProgressBar.setProgress(20); // 水平プログレスバーの値を設定
        //mProgressBar.setSecondaryProgress(60); // 水平プログレスバーのセカンダリ値を設定
        mHeaderNormal = mView.findViewById(R.id.timelineNormalHeader);
        mHeaderNormalText = (TextView) mView.findViewById(R.id.timelineTitleTextView);
        mHeaderProgress = mView.findViewById(R.id.timelineProgressHeader);
        mHeaderProgressText = (TextView) mView.findViewById(R.id.timelineProgressTitle);
        return mView;
    }
    
    /**
     * 初期処理
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mTimelineListener = (OnTimelineListItemClickListener)activity;
            mSettingListener = (OnSettingButtonClickListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPhotoListItemClickListener");
        }
        
        AsyncTwitterFactory factory = new AsyncTwitterFactory(mAsyncTwitterListener);
        mAsyncTwitter = factory.getInstance();
        mAsyncTwitter.setOAuthConsumer(AppUtils.CONSUMER_KEY, AppUtils.CONSUMER_SECRET);
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
    }

    /**
     * このフラグメントのタイムラインのタイプを設定しますよ
     * @param type タイプ AppUtils.TIMELINE_TYPE_HOME... etc
     */
    public void setTimelineType(int type) {
        // 状態フィールド更新
        mFragmentMode = type;
        // タイムライン更新
        // updateTimeline(AppUtils.getAccessToken());
    }
    
    /**
     * このフラグメントのタイムラインの更新をします
     * 状態フィールドを読んで、どのタイプか判断して更新しますよ
     */
    public void updateTimeline(AccessToken accessToken) {

        mAccessToken = accessToken;
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);

        switch(mFragmentMode){
            case AppUtils.TIMELINE_TYPE_HOME:
                // タイトル文字列の設定
                mHeaderNormalText.setText(R.string.timelineTitleHome);
                mHeaderProgressText.setText(R.string.timelineTitleHome);
                getAsyncHomeTimelineFirst();
                break;
            case AppUtils.TIMELINE_TYPE_MENTION:
                // タイトル文字列の設定
                mHeaderNormalText.setText(R.string.timelineTitleMention);
                mHeaderProgressText.setText(R.string.timelineTitleMention);
                getAsyncMentionsFirst();                
                break;
            case AppUtils.TIMELINE_TYPE_FAVORITE:
                // タイトル文字列の設定
                mHeaderNormalText.setText(R.string.timelineTitleFavorite);
                mHeaderProgressText.setText(R.string.timelineTitleFavorite);

                break;
            case AppUtils.TIMELINE_TYPE_USERLIST:
                // タイトル文字列の設定
                mHeaderNormalText.setText(R.string.timelineTitleUserList);
                mHeaderProgressText.setText(R.string.timelineTitleUserList);
                
                break;
        }
    }
    
    /**
     * このフラグメント内のリストビューをメインタイムラインで更新しますよ 
     * @deprecated
     */
    public void updateHomeTimeLine(AccessToken accessToken) {
        mAccessToken = accessToken;
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
        mFragmentMode = AppUtils.TIMELINE_TYPE_HOME;

        // タイトル文字列の設定
        mHeaderNormalText.setText(R.string.timelineTitleHome);
        mHeaderProgressText.setText(R.string.timelineTitleHome);

        getAsyncHomeTimelineFirst();
    }
    
    /**
     * このフラグメント内のリストビューをMentionsでラインで更新しますよ 
     * @deprecated
     */
    public void updateMentions(AccessToken accessToken) {
        mAccessToken = accessToken;
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
        mFragmentMode = AppUtils.TIMELINE_TYPE_MENTION;

        // タイトル文字列の設定
        mHeaderNormalText.setText(R.string.timelineTitleMention);
        mHeaderProgressText.setText(R.string.timelineTitleMention);

        getAsyncMentionsFirst();
    }

    /**
     * このフラグメント内のリストビューをお気に入りタイムラインで更新しますよ 
     */
    public void updateFavorites(AccessToken accessToken) {
        mAccessToken = accessToken;
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
        mFragmentMode = AppUtils.TIMELINE_TYPE_HOME;

        // タイトル文字列の設定
        mHeaderNormalText.setText(R.string.timelineTitleFavorite);
        mHeaderProgressText.setText(R.string.timelineTitleFavorite);

        getAsyncFavoritesFirst();
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
    
    /**
     * このフラグメントのレイアウトIDをセットします。
     * onclicklistenerでどのフラグメントが叩かれたか把握するためにね
     */
    public void setFragmentId(int fragmentId) {
        mThisFragmentId  = fragmentId;
    }
    
    /**
     * リストアイテムが押された時のリスナー
     */
    private OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> view, View arg1, int position, long id) {
            Log.e(TAG,"on item click "+position );
            Status status = (Status) mListView.getItemAtPosition(position);
            mTimelineListener.onTimelineListItemClick(status);
        }
    };
    
    /**
     * タイムラインの取得（公式AsyncTwitter）
     */
    private void getAsyncHomeTimelineFirst() {
        mPageCount=1;        
        Paging paging = new Paging(1, 20);
        getAsyncHomeTimeline(paging, false);
        mPageCount++;        
    }
    private void getAsyncHomeTimeline(Paging paging, boolean listAddFlag) {
        if(!mIsUpdating){
            mAsyncTwitter.getHomeTimeline(paging);
            mHeaderNormal.setVisibility(View.GONE);
            mHeaderProgress.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(10);
            mIsUpdating = true;            
            mListAddFlag = listAddFlag;
        }
    }

    /**
     * Mentionラインの取得（公式AsyncTwitter）
     */
    private void getAsyncMentionsFirst() {
        mPageCount=1;        
        Paging paging = new Paging(1, 20);
        getAsyncMentions(paging, false);
        mPageCount++;        
    }
    private void getAsyncMentions(Paging paging, boolean listAddFlag) {
        if(!mIsUpdating){
            mHeaderNormal.setVisibility(View.GONE);
            mHeaderProgress.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(10);
            mAsyncTwitter.getMentions(paging);
            mIsUpdating = true;
            mListAddFlag = listAddFlag;
        }
    }

    /**
     * Favoritesタイムラインの取得
     */
    private void getAsyncFavoritesFirst() {
        mPageCount=1;        
        Paging paging = new Paging(1, 20);
        getAsyncFavorites(paging, false);
        mPageCount++;        
    }
    private void getAsyncFavorites(Paging paging, boolean listAddFlag) {
        if(!mIsUpdating){
            mHeaderNormal.setVisibility(View.GONE);
            mHeaderProgress.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(10);
            mAsyncTwitter.getFavorites(mPageCount);
            mIsUpdating = true;
            mListAddFlag = listAddFlag;
        }
    }

    
    /**
     *  後処理 ダイアログ消去とか　実はこれワーカースレッドぽいから注意な
     */
    TwitterListener mAsyncTwitterListener = new TwitterAdapter() {
        @Override
        public void gotHomeTimeline(ResponseList<Status> statuses) {
            mIsUpdating = false;
            mProgressBar.setProgress(90);

            // 追加取得の場合
            if(mListAddFlag) {
                mAdapter.addAll(statuses);
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        completeProgressBar();
                    }

                });
//                mAdapter = new TweetStatusAdapter(getActivity(), statuses);
            // 新規取得の場合
            } else {
                mAdapter = new TweetStatusAdapter(getActivity(), statuses);
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        mListView.setAdapter(mAdapter);
                        completeProgressBar();
                    }
                });
            }
        }
        
        
        @Override
        public void gotMentions(ResponseList<Status> statuses) {
            mIsUpdating = false;
            mProgressBar.setProgress(90);

            // 追加取得の場合
            if(mListAddFlag) {
                mAdapter.addAll(statuses);
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        completeProgressBar();
                    }
                });
            // 新規取得の場合
            } else {
                mAdapter = new TweetStatusAdapter(getActivity(), statuses);
//                mAdapter.setColorTheme(mColorTheme);
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        mListView.setAdapter(mAdapter);                   
                        completeProgressBar();
                    }
                });
            }
        }

        @Override
        public void gotFavorites(ResponseList<Status> statuses) {
            mIsUpdating = false;
            mAdapter = new TweetStatusAdapter(getActivity(), statuses);
            mAdapter.setColorTheme(mColorTheme);
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    mListView.setAdapter(mAdapter);                   
                    mProgressBar.setProgress(100);
                    mHeaderNormal.setVisibility(View.VISIBLE);
                    mHeaderProgress.setVisibility(View.GONE);
                }
            });
        }
        
        @Override
        public void onException(TwitterException ex, TwitterMethod method) {
            // 後処理UI
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Utils.showToast(getActivity(), R.string.msg_tweetError);
                }
            });
        }
    };

    /**
     * プログレスバーをコンプリート（進捗１００）にする処理
     */
    private void completeProgressBar() {
        mProgressBar.setProgress(100);
        mHeaderNormal.setVisibility(View.VISIBLE);
        mHeaderProgress.setVisibility(View.GONE);
    }

    /**
     * ボタン押下リスナー(更新ボタンとかあるよ)
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
        // 更新ボタン
        case R.id.timelineUpdateButton:
            if(mFragmentMode == AppUtils.TIMELINE_TYPE_HOME) {
                getAsyncHomeTimelineFirst();
            } else if(mFragmentMode == AppUtils.TIMELINE_TYPE_MENTION) {
                getAsyncMentionsFirst();                
            }
            break;
        case R.id.timelineSettingButton:
            Log.e(TAG,"onSettingButton pressed");
            mSettingListener.onSettingButtonClick(mThisFragmentId);
            break;
        default:
            break;
        }
    }

    /**
     * スクロールリスナー　一番下までいったら続き取得とかするよ
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.i(TAG,"list scroll "+ firstVisibleItem + ", " + visibleItemCount + ", " + totalItemCount );
        if(totalItemCount == 0) return;
        // 一番下までスクロールしたら
        if((firstVisibleItem + visibleItemCount) >= totalItemCount) {
            Log.i(TAG, "list scroll bottom");
            if(mFragmentMode == AppUtils.TIMELINE_TYPE_HOME){
                Paging paging = new Paging(mPageCount, 20);
                getAsyncHomeTimeline(paging, true);
                mPageCount++;
            } else if(mFragmentMode == AppUtils.TIMELINE_TYPE_MENTION) {
                Paging paging = new Paging(mPageCount, 20);
                getAsyncMentions(paging, true);            
                mPageCount++;
            }
        }
    }
    
    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {
        // 特に何もしない
    }

    /**
     * リストをクリックした時の処理、　なにするかは決めてない 
     * @author kashimoto
     */
    public interface OnTimelineListItemClickListener {
        void onTimelineListItemClick(Status statusId);
    }

    /**
     * 更新ボタンをクリックした時の処理、　なにするかは決めてない 
     * @author kashimoto
     */
    public interface OnSettingButtonClickListener {
        /**
         * 設定ボタン押されたら呼ばれるよ
         * @param fragmentId ボタン押されたフラグメントのID、　設定されてないなら０を返すよ
         */
        void onSettingButtonClick(int fragmentId);
    }

}
