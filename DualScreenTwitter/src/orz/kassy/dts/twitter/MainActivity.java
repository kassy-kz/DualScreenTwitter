package orz.kassy.dts.twitter;

import orz.kassy.dts.image.ImageCache;
import com.kyocera.dualscreen.DualScreen;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
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
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import orz.kassy.dts.twitter.R;

public class MainActivity extends Activity implements OnClickListener {
    
    private static final String INTENT_ACTION_SLIDE = "com.kyocera.intent.action.SLIDE_OPEN";
    CustomReceiver mReceiver;
    private static MainActivity self;
    
	private static final int TWITTER_AUTHORIZE = 0;
    private static final String TAG = null;
	
	private Twitter mTwitter = null;
	private RequestToken mToken = null;
	
	private AccessToken mAccessToken = null;
	private StatusAdapter mAdapter = null;
	private String mAuthorizeUrl = "";
	
	private ProgressDialog mDialog = null;
	private Handler mHandler = new Handler();
	
	private boolean mIsAuthorized = false;
	private ListView mListView1;
	private ListView mListView2;

	private AuthAsyncTask mTask;

	// Receive thread
	private Runnable mRunnable_normal = new Runnable() {
	    @Override
		public void run() {
			mDialog.dismiss();
			Intent intent = new Intent(MainActivity.this, TwitterAuthorizeActivity.class);
			intent.putExtra("authurl", mAuthorizeUrl);
			startActivityForResult(intent, TWITTER_AUTHORIZE);
		}
	};

	private Runnable mRunnable_error = new Runnable() {
		@Override
		public void run() {
			mDialog.dismiss();
			Toast.makeText(MainActivity.this, R.string.twitter_auth_error, Toast.LENGTH_SHORT).show();
		}
	};
	
	private Runnable mRunnable_List_update = new Runnable() {
		@Override
		public void run() {
			// TimeLineをListViewに表示
			try {
				mAdapter = new StatusAdapter(MainActivity.this, mTwitter.getHomeTimeline());
				mDialog.dismiss();
			} catch(TwitterException e) {
				
			}
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        setContentView(R.layout.main);        

        // Echo DTS Setting 
        DualScreen.restrictOrientationAtFullScreen( this, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED );
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setScreenLayout();
        // Register intent receiver.
        mReceiver = new CustomReceiver();
        IntentFilter slideFilter = new IntentFilter(INTENT_ACTION_SLIDE);
        registerReceiver(mReceiver, slideFilter);

        // 保存したAccessToken取得
        mAccessToken = AppUtils.loadAccessToken(this);

        // 認証してない場合だけ認証処理するよ
        if(mAccessToken == null) {
            mTask = new AuthAsyncTask(this);
            mTask.execute(0);
        }
    }
    
	@Override
	protected void onStop() {
		super.onStop();
		
		if(mTwitter != null) mTwitter.shutdown();
		
		ImageCache.clear();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG,"onConfigurationChange");
        super.onConfigurationChanged(newConfig);
        setScreenLayout();
    }

	
	// 認証処理から帰ってきたとき
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
	
	@Override
	protected void onResume() {
	    super.onResume();
	    setScreenLayout();
	}

	/**
	 *  Echo のスタイルが変化するたびに呼ばれる。
	 *  表示変化等の処理を行う　
	 */
    private void setScreenLayout() {
        
        DualScreen dualscreen = new DualScreen(getApplicationContext());
        int screen_mode = dualscreen.getScreenMode();

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        if( screen_mode == DualScreen.FULL ) {
            if( width > height ) {
                //full tate 
                setContentView(R.layout.main_full_tate);
                //Utils.showToast(this, "tate");
                if(mAdapter != null) {
                    mListView1 = (ListView) findViewById(R.id.listview1);
                    mListView1.setAdapter(mAdapter);
                    mListView2 = (ListView) findViewById(R.id.listview2);
                    mListView2.setAdapter(mAdapter);
                }
            } else {
                //full yoko (Input style !!)
                setContentView(R.layout.main_full_yoko);
                //Utils.showToast(this, "yoko");
                Button btn = (Button)findViewById(R.id.btnsend);
                btn.setOnClickListener(this);
                EditText editText = (EditText)findViewById(R.id.editText1);
                // 入力状態にする（キーボードを出現させる）
                editText.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
                inputMethodManager.showSoftInput(editText, 0);  
            }
        } else {
            // for normal screen
            if( width > height ) {
                // normal yoko
                setContentView(R.layout.main_normal_yoko);
            } else {
                // normal tate
                setContentView(R.layout.main_normal_tate);
                mListView1 = (ListView) findViewById(R.id.listview1);
                mListView1.setAdapter(mAdapter);
            }
        }
    }
	
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

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnsend){
            Utils.showSimpleAlertDialog(this,"Send","send completed",oklistener,oklistener);
        }
    }
    
    DialogInterface.OnClickListener oklistener = new DialogInterface.OnClickListener(){

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            setScreenLayout();
        }
    };
    
    /**
     * 認証処理の非同期タスク
     */
    public class AuthAsyncTask extends AsyncTask<Integer, Void, Integer>{
        private Activity mActivity;
        private static final int RESULT_OK = 0;
        private static final int RESULT_NG = -1;
        private static final int RESULT_AUTHED = 1;

        public AuthAsyncTask(Activity activity) {
            mActivity = activity;
        }

        // 前処理 これはUIスレッドでの処理ね
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
        
        // ワーカースレッドでの処理ね
        @Override
        protected Integer doInBackground(Integer... arg0) {

            // 保存した情報を呼び出すのだぜ
            SharedPreferences shPref = mActivity.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);

            // 保存したAccessToken取得
            mAccessToken = AppUtils.loadAccessToken(mActivity);
            
            // accesstoken がない場合（認証まだな場合）
            if(mAccessToken == null) {
                // 初回の認証処理
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

        // 後処理 これはUIスレッドでの処理ね
        @Override
        protected void onPostExecute (Integer result) {
            super.onPostExecute(result);
            if(result == RESULT_OK) {
                if(mDialog != null) {
                    mDialog.dismiss();
                }
                Intent intent = new Intent(mActivity, TwitterAuthorizeActivity.class);
                intent.putExtra(AppUtils.AUTH_URL, mAuthorizeUrl);
                mActivity.startActivityForResult(intent, TWITTER_AUTHORIZE);
            } else if(result == RESULT_NG) {
                if(mDialog != null) {
                    mDialog.dismiss();
                }
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
}