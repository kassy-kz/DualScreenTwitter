package orz.kassy.dts.twitter;

import orz.kassy.dts.image.ImageCache;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.kyocera.dualscreen.DualScreen;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import orz.kassy.dts.twitter.R;
import orz.kassy.dts.twitter.AppUtils;
import orz.kassy.dts.twitter.color.ColorThemeGreen;
import orz.kassy.dts.twitter.color.ColorThemeRed;

/**
 * メインアクティビティー
 * @author kashimoto
 */
public class MainActivity extends FragmentActivity 
                          implements TimelineListFragment.OnTimelineListItemClickListener {
    
    private static final String INTENT_ACTION_SLIDE = "com.kyocera.intent.action.SLIDE_OPEN";
    CustomReceiver mReceiver;
    private static MainActivity self;
    
	private static final int TWITTER_AUTHORIZE = 0;
    private static final String TAG = null;
	
	private Twitter mTwitter = null;
	private RequestToken mToken = null;
	
	private AccessToken mAccessToken = null;
	private TweetStatusAdapter mAdapter = null;
	private String mAuthorizeUrl = "";
	
	private ProgressDialog mDialog = null;
	private Handler mHandler = new Handler();
	
	private boolean mIsAuthorized = false;
	private ListView mListView1;
	private ListView mListView2;

	private AuthAsyncTask mTask;

	private static final int MENU_ID_MENU1 = (Menu.FIRST + 1);
    private static final int MENU_ID_MENU2 = (Menu.FIRST + 2);

    // スタイルチェンジの時に引き継ぐ値たちね
    // スタイルチェンジではActivityが生成され直すから、staticにする必要があるのよ
    private static int sTweetModeFlag;
    private static long sSelectedStatusId;
    private static String sSelectedStatusUserName;
    private static String sSelectedStatusText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        //setContentView(R.layout.main);        

        // Echo DTS Setting 
        DualScreen.restrictOrientationAtFullScreen( this, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED );
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        
        // Register intent receiver.
        mReceiver = new CustomReceiver();
        IntentFilter slideFilter = new IntentFilter(INTENT_ACTION_SLIDE);
        registerReceiver(mReceiver, slideFilter);

        // 保存したAccessToken取得
        mAccessToken = AppUtils.loadAccessToken(this);

        // 認証してない場合だけ認証処理するよ
        if(mAccessToken == null) {
            // 認証処理（非同期）
            mTask = new AuthAsyncTask(this);
            mTask.execute(0);
        // 認証してる時はいきなりタイムライン流すよ あ、でも縦のときだけね
        } else {
            // 表示処理を行います
            setScreenLayout();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        sTweetModeFlag = AppUtils.TWEET_MODE_NORMAL;
        sSelectedStatusId = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mTwitter != null) mTwitter.shutdown();
        ImageCache.clear();
    }
    
    /**
     * オプションメニュー生成（最初の一度だけ）
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューアイテムを追加します
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    /**
     *  オプションメニューが表示される度に呼び出されます
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     *  オプションメニューアイテムが選択された時に呼び出されます
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
        case R.id.create_new:
            Log.i(TAG,"menu create new ");
            FragmentManager fm = ((FragmentActivity) self).getSupportFragmentManager();
            TimelineListFragment timelineFragmentR = (TimelineListFragment)fm.findFragmentById(R.id.timelineFragmentR);
            timelineFragmentR.setColorTheme(new ColorThemeRed());

            
            ret = true;
            break;
        case R.id.menu_id_tmp1:
            Log.i(TAG,"menu tmp 1");
            ret = true;
            break;
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        }
        return ret;
    }
    
	/**
	 * 端末の向きが変わった時
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG,"onConfigurationChange");
        super.onConfigurationChanged(newConfig);
        setScreenLayout();
    }

	/**
	 *  認証処理(WebView)から帰ってきたとき
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == TWITTER_AUTHORIZE) {
			if(resultCode == 0) {
				// 認証成功
				final String pincode = data.getExtras().getString("pincode");
				
				mDialog = new ProgressDialog(this);
				mDialog.setMessage(getString(R.string.wait_timeline_message));
				mDialog.setIndeterminate(true);
				mDialog.show();
				
				new Thread() {
					@Override
					public void run() {
						try {
						    // 認証が成功したあとの処理
						    // アクセストークン取得
							mAccessToken = mTwitter.getOAuthAccessToken(mToken, pincode);
							// Preferenceに保存
                            // 本番モードのみ
                            AppUtils.saveAccessToken(self, mAccessToken);
							
							// アクセス・トークンが取得できたら、リソース解放して、インスタンス再生成
							mTwitter.shutdown();
							mTwitter = null;
							
							TwitterFactory factory = new TwitterFactory();
							mTwitter = factory.getInstance();
							mTwitter.setOAuthConsumer(AppUtils.CONSUMER_KEY, AppUtils.CONSUMER_SECRET);
							mTwitter.setOAuthAccessToken(mAccessToken);
							
						} catch(TwitterException e) {
							Log.d("TEST", "Exception", e);
						}
						mHandler.post(mRunnable_List_update);
					}
				}.start();					
			}
		}
	}
	
	/**
	 * WebViewAuthの後の認証処理の後処理　TwitterタイムラインをListViewに表示するよ
	 */
	private Runnable mRunnable_List_update = new Runnable() {
	    @Override
	    public void run() {
	        setScreenLayout();
	    }
	};

	/**
	 *  Echo のスタイルが変化するたびに呼ばれる。
	 *  各形態に合わせた表示処理を行う　(setContentViewを呼ぶ)
	 *  UIスレッドで呼ぶこと
	 */
    private void setScreenLayout() {
        
        DualScreen dualscreen = new DualScreen(getApplicationContext());
        int screen_mode = dualscreen.getScreenMode();

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        if( screen_mode == DualScreen.FULL ) {
            // full tate (D|D)
            if( width > height ) {
                setContentView(R.layout.main_full_tate);

                // 左右のフラグメントでタイムライン更新だぜ
                FragmentManager fm = ((FragmentActivity) self).getSupportFragmentManager();
                TimelineListFragment timelineFragmentL = (TimelineListFragment)fm.findFragmentById(R.id.timelineFragmentL);
                timelineFragmentL.updateHomeTimeLine(mAccessToken);
                TimelineListFragment timelineFragmentR = (TimelineListFragment)fm.findFragmentById(R.id.timelineFragmentR);
                timelineFragmentR.updateMentions(mAccessToken);
                timelineFragmentR.setColorTheme(new ColorThemeGreen());
                
            } else {
                Log.e(TAG,"change state activity");
                //full yoko (＝)
//                setContentView(R.layout.main_full_yoko);
//                Button btn = (Button)findViewById(R.id.btnsend);
//                btn.setOnClickListener(this);
                //AdMob setting
//                AdView adView = new AdView(this, AdSize.BANNER, "a14e939d84dfe72");
//                LinearLayout layout = (LinearLayout)findViewById(R.id.admob);
//                layout.addView(adView);
//                AdRequest request = new AdRequest();
//                adView.loadAd(request);
                Intent intent = new Intent(MainActivity.this, MainYokoActivity.class);
                switch(sTweetModeFlag) {
                    case AppUtils.TWEET_MODE_MENTION:
                        Log.e(TAG,"mention");
                        intent.putExtra(AppUtils.TWEET_MODE, AppUtils.TWEET_MODE_MENTION);
                        intent.putExtra(AppUtils.IN_REPLY_TO_STATUS_ID, sSelectedStatusId);
                        intent.putExtra(AppUtils.IN_REPLY_TO_STATUS_TEXT, sSelectedStatusText);
                        intent.putExtra(AppUtils.IN_REPLY_TO_STATUS_USERNAME, sSelectedStatusUserName);

                        break;
                    case AppUtils.TWEET_MODE_REPLY:
                        Log.e(TAG,"reply");
                        intent.putExtra(AppUtils.TWEET_MODE, AppUtils.TWEET_MODE_REPLY);
                        intent.putExtra(AppUtils.IN_REPLY_TO_STATUS_ID, sSelectedStatusId);
                        intent.putExtra(AppUtils.IN_REPLY_TO_STATUS_TEXT, sSelectedStatusText);
                        intent.putExtra(AppUtils.IN_REPLY_TO_STATUS_USERNAME, sSelectedStatusUserName);
                        break;
                    default:
                        Log.e(TAG,"normal");
                        intent.putExtra(AppUtils.TWEET_MODE, AppUtils.TWEET_MODE_NORMAL);
                        break;
                }
                unregisterReceiver(mReceiver);
                startActivity(intent);
                finish();
            }
        } else {
            // for normal screen
            if( width > height ) {
                // normal yoko
                setContentView(R.layout.main_half_yoko);
            } else {
                // normal tate
                setContentView(R.layout.main_half_tate);
                FragmentManager fm = ((FragmentActivity) self).getSupportFragmentManager();
                TimelineListFragment timelineFragmentHalf = (TimelineListFragment)fm.findFragmentById(R.id.timelineFragmentHalf);
                timelineFragmentHalf.updateHomeTimeLine(mAccessToken);
            }
        }
    }
	
    /**
     * 端末状態変化した時のブロードキャストレシーバー
     */
    class CustomReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ( action.equals(INTENT_ACTION_SLIDE)) {
                boolean slideOpen = intent.getBooleanExtra("OPEN",false);
                if(slideOpen) {
//                    Toast.makeText(DualScreenApiSampleActivity.this, R.string.msg_slide_opened, Toast.LENGTH_SHORT).show();
                    //Utils.showToast(context, "slide open");
                } else {
//                    Toast.makeText(DualScreenApiSampleActivity.this, R.string.msg_slide_closed, Toast.LENGTH_SHORT).show();
                    //Utils.showToast(context, "slide close");
                }
            }
        }
    }
    
    /**
     * 認証処理の非同期タスク
     * ワーカースレッドでアクセス初回認証処理（トークン取得とか）やります
     * 後処理でWebViewのあるAuthページに飛んで認証処理に入ります
     */
    public class AuthAsyncTask extends AsyncTask<Integer, Void, Integer>{
        private Activity mActivity;
        private static final int RESULT_OK = 0;
        private static final int RESULT_NG = -1;
        private static final int RESULT_AUTHED = 1;

        public AuthAsyncTask(Activity activity) {
            mActivity = activity;
        }

        /**
         *  前処理 これはUIスレッドでの処理ね
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // ダイアログを表示
            mDialog = new ProgressDialog(mActivity);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setMessage("認証処理をしています...");
            mDialog.setCancelable(true);
            mDialog.setOnDismissListener(mDismissListener);
            mDialog.show();
        }
        
        /**
         *  これはワーカースレッドでの処理ね
         */
        @Override
        protected Integer doInBackground(Integer... arg0) {

            // 保存した情報を呼び出すのだぜ
            SharedPreferences shPref = mActivity.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);

            // 保存したAccessToken取得
            mAccessToken = AppUtils.loadAccessToken(mActivity);
            
            // accesstoken がない場合（認証まだな場合）
            if(mAccessToken == null) {
                // 初回のアプリ認証処理
                mTwitter = new TwitterFactory().getInstance();
                mTwitter.setOAuthConsumer(AppUtils.CONSUMER_KEY, AppUtils.CONSUMER_SECRET);
                try {
                    mToken = mTwitter.getOAuthRequestToken();
                    mAuthorizeUrl = mToken.getAuthorizationURL();
                    return RESULT_OK;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return RESULT_NG;
                }
            // すでに認証できてる場合
            } else {
                return RESULT_AUTHED;
            }
        }

        /**
         *  後処理 これはUIスレッドでの処理ね
         */
        @Override
        protected void onPostExecute (Integer result) {
            super.onPostExecute(result);
            // 初回アプリ認証処理が成功した場合ね
            if(result == RESULT_OK) {
                // ダイアログ消して
                if(mDialog != null) {
                    mDialog.dismiss();
                }
                // ユーザー認証画面(WebView)に遷移するよ
                // インテントには認証用URLの情報を入れとくよ
                Intent intent = new Intent(mActivity, TwitterAuthorizeActivity.class);
                intent.putExtra(AppUtils.AUTH_URL, mAuthorizeUrl);
                mActivity.startActivityForResult(intent, TWITTER_AUTHORIZE);
            // 初回アプリ認証失敗した場合
            } else if(result == RESULT_NG) {
                if(mDialog != null) {
                    mDialog.dismiss();
                }
                // トーストだけだしてゴメンナサイ「認証に失敗しました」
                Toast.makeText(mActivity, R.string.twitter_auth_error, Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * ダイアログ消去リスナー
         * ダイアログが消えるとき、アクティビティも消えるのです。
         */
        private DialogInterface.OnDismissListener mDismissListener = new DialogInterface.OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        };
    }

    /**
     * タイムラインのアイテムがクリックされたとき、呼ばれますよ
     */
    @Override
    public void onTimelineListItemClick(Status status) {
        //Log.e(TAG,"Clicked Timeline item" + statusId);
        sSelectedStatusId = status.getId();
        sSelectedStatusUserName = status.getUser().getScreenName();
        sSelectedStatusText = status.getText();
        sTweetModeFlag = AppUtils.TWEET_MODE_REPLY;
        
        // カスタムトーストを表示しますお
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.custom_toast_lead_rotate, null);
        Toast mToast = new Toast(this);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.setView(v);
        mToast.show();

    }
}