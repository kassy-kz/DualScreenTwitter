package orz.kassy.dts.twitter;

import orz.kassy.dts.image.ImageCache;
import orz.kassy.dts.image.ThumbnailTask;
import orz.kassy.dts.twitter.color.ColorTheme;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.http.AccessToken;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainYokoActivity extends Activity implements OnClickListener {

    private static final int DIALOG_YES_NO_MESSAGE = 1;
    
    private static final String TAG = "MainYoko";
    private EditText mTweetEditText;
    private long mInReplyTo;
    private static ProgressDialog mDialog ;
    private AccessToken mAccessToken = null;
    private TextView mTweetStrCountText;
    private Button mSendBtn;
    private TextView mTweetRefText;
    private MainYokoActivity mSelf;
    private final Handler mHandler = new Handler();
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        
        // 縦向きだったら、すかさずステートチェンジします
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        if( width > height ) {
            Intent intent = new Intent(MainYokoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // 横向きなら、改めて頑張りましょう
        setContentView(R.layout.main_full_yoko);
        mSelf = this;
        //Utils.showToast(this, R.string.toast_rotate_info_yoko);

        
        // 保存したAccessToken取得
        mAccessToken = AppUtils.loadAccessToken(this);

        // View setting
        mSendBtn = (Button)findViewById(R.id.sendButton);
        mSendBtn.setOnClickListener(this);
        mTweetEditText = (EditText)findViewById(R.id.tweetEditText);
        mTweetEditText.addTextChangedListener(mTextWatcher);
        mTweetStrCountText = (TextView)findViewById(R.id.tweetStrCount);
        //mTweetRefText = (TextView)findViewById(R.id.tweetRefText);
        
        int mode = getIntent().getIntExtra(AppUtils.TWEET_MODE, AppUtils.TWEET_MODE_NORMAL);
        String targetUserName = null;
        
        switch(mode) {
            case AppUtils.TWEET_MODE_NORMAL:
                setTitle(R.string.title_tweet_normal);
                RelativeLayout rl = (RelativeLayout) findViewById(R.id.tweetRefView);
                rl.setVisibility(View.GONE);
                break;
            case AppUtils.TWEET_MODE_REPLY:
                setTitle(R.string.title_tweet_reply);
                // ここ非同期にするとややこいので断念な
                // ちなみに非同期にするときはshowStatusメソッドを使うよ
                mInReplyTo = getIntent().getLongExtra(AppUtils.IN_REPLY_TO_STATUS_ID, 0);
                targetUserName  = getIntent().getStringExtra(AppUtils.IN_REPLY_TO_STATUS_USERNAME);
                String refText = getIntent().getStringExtra(AppUtils.IN_REPLY_TO_STATUS_TEXT);
                String iconUrl = getIntent().getStringExtra(AppUtils.IN_REPLY_TO_STATUS_ICONURL);
                setTweetHeaderView(targetUserName, refText, iconUrl);
                mTweetEditText.setText("@"+targetUserName+" ");
                mTweetEditText.setSelection(mTweetEditText.getText().length());
                break;
            case AppUtils.TWEET_MODE_MENTION:
                setTitle(R.string.title_tweet_mention);
                mInReplyTo = 0;
                targetUserName = getIntent().getStringExtra(AppUtils.IN_REPLY_TO_STATUS_USERNAME);
                mTweetEditText.setText("@"+targetUserName+" ");
                mTweetEditText.setSelection(mTweetEditText.getText().length());
                break;
            default:
                setTitle(R.string.title_tweet_normal);
                break;
        }

        //AdMob setting
        AdView adView = new AdView(this, AdSize.BANNER, "a14e939d84dfe72");
        LinearLayout layout = (LinearLayout)findViewById(R.id.admob);
        layout.addView(adView);
        layout.setBackgroundColor(0xff888888);
        AdRequest request = new AdRequest();
        adView.loadAd(request);
    }

    private void setTweetHeaderView(String targetName, String refText, String iconUrl) {
        // 追加
        LinearLayout header = (LinearLayout)findViewById(R.id.tweetHeaderItem);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.tweetRefView);
        rl.setVisibility(View.VISIBLE);
        TextView lbl_screenname = (TextView)header.findViewById(R.id.tweetRefName);
        lbl_screenname.setTextColor(0xff000000);
        lbl_screenname.setText(targetName);
        TextView lbl_tweet = (TextView)header.findViewById(R.id.tweetRefMessage);
        lbl_tweet.setTextColor(0xff000000);
        lbl_tweet.setText(refText);
        // 画像どしよ...
        ImageView imgIcon = (ImageView) findViewById(R.id.tweetRefImg);
//        if(bmp != null) {
//            imgIcon.setImageBitmap(bmp);
//        }
        ThumbnailTask task;
        task = new ThumbnailTask(imgIcon);
        task.execute(iconUrl, targetName);
    }
    
    @Override
    public void onResume() {
        super.onResume();

        // キーボード出すよ
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.sendButton :
                sendTweet();
                break;
            default :
                break;
        }
    }

    /**
     * ツイートします
     */
    private void sendTweet() {
        String tweetStr = mTweetEditText.getText().toString();
        StatusUpdate statusUpdate = new StatusUpdate(tweetStr);
        statusUpdate.setInReplyToStatusId(mInReplyTo);
        
        // ツイート処理、公式のAsyncTwitter使って
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("sending tweet");
        mDialog.setIndeterminate(true);
        mDialog.setOnDismissListener(mDismissListener);
        mDialog.show();
        
        AsyncTwitterFactory factory = new AsyncTwitterFactory(mAsyncTwitterListener);
        AsyncTwitter asyncTwitter = factory.getInstance();
        asyncTwitter.setOAuthConsumer(AppUtils.CONSUMER_KEY, AppUtils.CONSUMER_SECRET);
        asyncTwitter.setOAuthAccessToken(mAccessToken);
        asyncTwitter.updateStatus(statusUpdate);
    }
    
    /**
     *  ツイッター処理の後処理 ダイアログ消去とかするよ
     *  実はこれワーカースレッドぽい... 
     *  このメソッド内ではトーストを出すこともできません
     *  EditTextの編集すら無理
     */
    TwitterListener mAsyncTwitterListener = new TwitterAdapter() {
        @Override
        public void updatedStatus(Status statuses) {
            Log.e(TAG,"send success");
            mDialog.dismiss();  
            // 後処理UI
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTweetEditText.setText("");
                    Utils.showToast(mSelf, "tweet success");
                }
            });
        }
        @Override
        public void onException(TwitterException ex, TwitterMethod method) {
            mDialog.dismiss();
            Log.e(TAG,"send error");
            // 後処理UI
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Utils.showToast(mSelf, "Error failed to send");
                }
            });
            // トースト出せない
            // Utils.showToast(mSelf, "Error failed to send");
        }
    };

    /**
     * ダイアログが消えた時のリスナーですよ
     */
    DialogInterface.OnDismissListener mDismissListener = new DialogInterface.OnDismissListener(){
        @Override
        public void onDismiss(DialogInterface dialog) { 
//            finish(); 
        }
    };

    /**
     * テキストウォッチャー、　ツイート文言が変わったときに呼ばれますよ
     */
    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable arg0) {
        }
        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //文言が変わった時、数字を変えますよ
            int editExitCount = mTweetEditText.getText().length();
            int i = 140-(editExitCount);
            mTweetStrCountText.setText(Integer.toString(i));
            if(i<0) {
                mTweetStrCountText.setTextColor(0xffff0000);
                mSendBtn.setEnabled(false);
            } else {
                mTweetStrCountText.setTextColor(0xff000000);
                mSendBtn.setEnabled(true);                
            }
        }
    };
    
    
    /**
     * オプションメニュー生成（最初の一度だけ）
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューアイテムを追加します
        getMenuInflater().inflate(R.menu.main_yoko, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    /**
     *  オプションメニューアイテムが選択された時に呼び出されます
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret;
        switch (item.getItemId()) {
        case R.id.menu_yoko_clear:
            showDialog(DIALOG_YES_NO_MESSAGE);
            ret = true;
            break;
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        }
        return ret;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_YES_NO_MESSAGE:
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_clear_tweet)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 消すお
                        setTitle(R.string.title_tweet_normal);
                        RelativeLayout rl = (RelativeLayout) findViewById(R.id.tweetRefView);
                        rl.setVisibility(View.GONE);
                        mTweetEditText.setText("");
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // なにもしないよ
                    }
                })
                .create();
        }
        return null;
    }


}
