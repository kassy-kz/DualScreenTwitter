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
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
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
    private LinearLayout mFirstProgress;

    // フラグメントモード　ホームタイムラインか、メンションか、とか...
    private int mFragmentMode;
    private int mPageCount=1;
    // リストへの追記モードか否か... 
    private boolean mListAddFlag = false;
    // このフラグメントのID　いろいろ使うよ でもgetIdで取れるからいらなかった...
    //private int mThisFragmentId = 0;

    private boolean mExistList = false;


    /**
     * 初期処理１
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
        
        AsyncTwitterFactory factory = new AsyncTwitterFactory();
        mAsyncTwitter = factory.getInstance();
        mAsyncTwitter.addListener(mAsyncTwitterListener);
        mAsyncTwitter.setOAuthConsumer(AppUtils.CONSUMER_KEY, AppUtils.CONSUMER_SECRET);
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
    }
    
    /**
     * 初期処理２
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            return ;
        }
        if(savedInstanceState.getBoolean(AppUtils.BUNDLE_IS_EXIST_LIST)) {
            mExistList = true;
            ResponseList<Status> statuses = (ResponseList<Status>) savedInstanceState.getSerializable(AppUtils.BUNDLE_RESPONSE_LIST);
            //mAdapter = new TweetStatusAdapter(getActivity(), statuses);
            Log.e(TAG,"statuses = ");
        }
    }

    /**
     * 初期処理３
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
        
        mFirstProgress = (LinearLayout)mView.findViewById(R.id.timeline_first_progress);
        return mView;
    }
    
    /**
     * 初期処理６
     */
    @Override 
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        setTimelineType(AppUtils.loadPainType(getActivity(),getId()));
        updateTimeline(AppUtils.getAccessToken());
    }
    
    /**
     * 初期処理7
     */
    @Override 
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mAdapter == null) {
            return ;
        }
        outState.putBoolean(AppUtils.BUNDLE_IS_EXIST_LIST, true);
        outState.putSerializable(AppUtils.BUNDLE_RESPONSE_LIST, (ResponseList<Status>)mAdapter.getList());
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
     * そしてヘッダとかも書き換えるから、まぁよそ者はコレを呼んどけ
     * @param ぶっちゃけnullでもいい
     */
    public void updateTimeline(AccessToken accessToken) {
        if(accessToken == null) {
            accessToken = AppUtils.getAccessToken();
        }
        mAccessToken = accessToken;
        mAsyncTwitter.setOAuthAccessToken(mAccessToken);
        mIsTimelimeLimit = false;
        if(mListView.getFooterViewsCount() ==0) {
            mFooterView.findViewById(R.id.timelineNormalFooter).setVisibility(View.VISIBLE);
            mFooterView.findViewById(R.id.timelineLimitFooter).setVisibility(View.GONE);
        }

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
                getAsyncFavoritesFirst();
                break;
            case AppUtils.TIMELINE_TYPE_USERLIST:
                // タイトル文字列の設定
                mHeaderNormalText.setText(R.string.timelineTitleUserList);
                mHeaderProgressText.setText(R.string.timelineTitleUserList);
                break;
            case AppUtils.TIMELINE_TYPE_PROFILELINE:
                // タイトル文字列の設定
                mHeaderNormalText.setText(R.string.timelineTitleProfileLine);
                mHeaderProgressText.setText(R.string.timelineTitleProfileLine);
                getAsyncProfileLineFirst();                
                break;
        }
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
     * リストアイテムが押された時のリスナー
     */
    private OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> view, View arg1, int position, long id) {
            Log.e(TAG,"on item click "+position );
            Status status = (Status) mListView.getItemAtPosition(position);
            mTimelineListener.onTimelineListItemClick(getId(), position, status);
        }
    };
    
    /**
     * タイムラインを選択状態にします
     * @param position　ポジション、選択解除には-1とか入れてね
     */
    public void setSelected(int position) {
        if(mAdapter != null) {
            mAdapter.setSelected(position);
        }
    }
    
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
     * Mentionラインの取得（公式AsyncTwitter）
     */
    private void getAsyncProfileLineFirst() {
        mPageCount=1;        
        Paging paging = new Paging(1, 20);
        getAsyncProfileLine(paging, false);
        mPageCount++;        
    }
    private void getAsyncProfileLine(Paging paging, boolean listAddFlag) {
        if(!mIsUpdating){
            mHeaderNormal.setVisibility(View.GONE);
            mHeaderProgress.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(10);
            mAsyncTwitter.getUserTimeline(paging);
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
            setStatusesToListView(statuses);
        }
        
        @Override
        public void gotMentions(ResponseList<Status> statuses) {
            setStatusesToListView(statuses);
        }

        @Override
        public void gotFavorites(ResponseList<Status> statuses) {
            setStatusesToListView(statuses);
        }
        
        @Override
        public void gotUserTimeline(ResponseList<Status> statuses) {
            setStatusesToListView(statuses);
        }

        @Override
        public void onException(TwitterException ex, TwitterMethod method) {
            // 後処理UI
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFirstProgress.setVisibility(View.GONE);
                    Utils.showToast(getActivity(), R.string.msg_gettweetError);
                }
            });
        }
    };

    private boolean mIsTimelimeLimit;

    
    /**
     * 取得したstatusをlistviewにセットする処理
     * みんな一緒っぽいから共通化しようかなって
     */
    private void setStatusesToListView(ResponseList<Status> statuses) {
        mIsUpdating = false;
        mProgressBar.setProgress(90);
        
        // これ以上取得できない場合
        if(statuses.size() <10) {
            Log.e(TAG," limit kita----- ");
            mIsTimelimeLimit = true;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Utils.showToast(getActivity(), R.string.msg_getTweetLimit);
                    mFooterView.findViewById(R.id.timelineNormalFooter).setVisibility(View.GONE);
                    mFooterView.findViewById(R.id.timelineLimitFooter).setVisibility(View.VISIBLE);
                }
            });
        } 
        
        // 追加取得の場合
        if(mListAddFlag) {
            mAdapter.addAll(statuses);
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    mFirstProgress.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    completeProgressBar();
                }
            });
        // 新規取得の場合
        } else {
            mAdapter = new TweetStatusAdapter(getActivity(), statuses);
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    mFirstProgress.setVisibility(View.GONE);
                    mListView.setAdapter(mAdapter);                   
                    completeProgressBar();
                }
            });
        }
    }

    
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
            // mSettingListener.onSettingButtonClick(mThisFragmentId);
            createSettingFragment();
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
        //Log.i(TAG,"list scroll "+ firstVisibleItem + ", " + visibleItemCount + ", " + totalItemCount );
        if(totalItemCount == 0) return;
        if(mIsTimelimeLimit) return;
        
        // 一番下までスクロールしたら
        if((firstVisibleItem + visibleItemCount) >= totalItemCount) {
            //Log.i(TAG, "list scroll bottom");
            if(mFragmentMode == AppUtils.TIMELINE_TYPE_HOME){
                Paging paging = new Paging(mPageCount, 20);
                getAsyncHomeTimeline(paging, true);
                mPageCount++;
            } else if(mFragmentMode == AppUtils.TIMELINE_TYPE_MENTION) {
                Paging paging = new Paging(mPageCount, 20);
                getAsyncMentions(paging, true);            
                mPageCount++;
            } else if(mFragmentMode == AppUtils.TIMELINE_TYPE_FAVORITE) {
                Paging paging = new Paging(mPageCount, 20);
                getAsyncFavorites(paging, true);            
                mPageCount++;
            } else if(mFragmentMode == AppUtils.TIMELINE_TYPE_PROFILELINE) {
                Paging paging = new Paging(mPageCount, 20);
                getAsyncProfileLine(paging, true);            
                mPageCount++;
            }
        }
    }
    
    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {
        // 特に何もしない
    }

    /**
     * リストをクリックした時の処理、Activityに渡すほうね 
     * @author kashimoto
     */
    public interface OnTimelineListItemClickListener {
        // 押された奴の表示を更新する処理
        void onTimelineListItemClick(int fragmentId, int position,  Status statusId);
    }

    /**
     * 更新ボタンをクリックした時の処理、Activityに渡すほうね 
     * @author kashimoto
     */
    public interface OnSettingButtonClickListener {
        /**
         * 設定ボタン押されたら呼ばれるよ
         * @param fragmentId ボタン押されたフラグメントのID、　設定されてないなら０を返すよ
         */
        void onSettingButtonClick(int fragmentId);
    }
    
    
    private void createSettingFragment() {
//        if(MainActivity.getIsSetting()) {
//            Utils.showToast(getActivity(), R.string.toast_cannot_create_setting);
//            return;
//        }
        String backStack = "tmp";
        FragmentManager fm = ((FragmentActivity) getActivity()).getSupportFragmentManager();
        TimelineListFragment tlFragment = (TimelineListFragment)fm.findFragmentById(getId());

        // バックスタックを指定する
        if(getId() == R.id.timelineFragmentL){
            backStack = "LEFT";
        }else if(getId() == R.id.timelineFragmentR){
            backStack = "RIGHT";
        }
        
        SettingTimelineFragment sfragment = new SettingTimelineFragment(getId(), tlFragment);

        // FragmentTransactionインスタンスを取得する
        android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        //ft.replace(getId(), sfragment);
        ft.add(getId(), sfragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        // バックスタックに入れる
        //ft.addToBackStack(null);
        Log.w("backstack",backStack);
        //ft.addToBackStack(backStack);
        // Transactionを実行する
        sfragment.commitId = ft.commit();
        MainActivity.setIsSetting(true);
        
    }
}
