package orz.kassy.dts.twitter;

import orz.kassy.dts.adapter.StatusAdapter;
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
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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

    
	private static final int TWITTER_AUTHORIZE = 0;
	private static final String CONSUMER_KEY = "0puDNPmm5z6ZKtIGwjzgow";
	private static final String CONSUMER_SECRET = "CZQ4abCLZpkOJ1QjFTRl9bcez8Y8PqazgLnqIwfNtw";
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
        setContentView(R.layout.main);        

        // DTS Setting
        DualScreen.restrictOrientationAtFullScreen( this, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED );
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setScreenLayout();
        // Register intent receiver.
        mReceiver = new CustomReceiver();
        IntentFilter slideFilter = new IntentFilter(INTENT_ACTION_SLIDE);
        registerReceiver(mReceiver, slideFilter);
        
        // Twitter Setting
		mDialog = new ProgressDialog(this);
		mDialog.setMessage(getString(R.string.auth_wait_message));
		mDialog.setIndeterminate(true);
		mDialog.show();
        new Thread() {
        	@Override
        	public void run() {
        		try {
        			mTwitter = new TwitterFactory().getInstance();
        			mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        			mToken = mTwitter.getOAuthRequestToken();
	        		mAuthorizeUrl = mToken.getAuthorizationURL();
	        		mHandler.post(mRunnable_normal);
        		} catch(TwitterException e) {
        			Log.d("TEST", "Exception", e);
        			mHandler.post(mRunnable_error);
        		}
        	}
        }.start();
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
							mAccessToken = mTwitter.getOAuthAccessToken(mToken, pincode);
							
							// アクセス・トークンが取得できたら、リソース解放して、インスタンス再生成
							mTwitter.shutdown();
							mTwitter = null;
							
							TwitterFactory factory = new TwitterFactory();
							mTwitter = factory.getInstance();
							mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
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
}