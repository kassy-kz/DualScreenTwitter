package orz.kassy.dts.twitter;

import orz.kassy.dts.twitter.color.ColorTheme;
import orz.kassy.dts.twitter.color.ColorThemeGreen;
import orz.kassy.dts.twitter.color.ColorThemeRed;
import orz.kassy.dts.twitter.color.ColorThemeWhite;

import com.kyocera.dualscreen.DualScreen;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.view.Display;
import android.view.WindowManager;
import twitter4j.http.AccessToken;

public class AppUtils {
    
    public static final String PREF_FILE_NAME = "pref_file";
    public static final String CONSUMER_KEY = "IfHeDiZaU5APV4MLxfA";
    public static final String CONSUMER_SECRET = "y4GC9SSsTttftO42hMmv7XZnmiCNZoJLxT6855klI";
    public static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    public static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
    public static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
    
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ACCESS_TOKEN_SECRET = "access_token_secret";
    public static final String AUTH_URL = "authurl";
    
    public static final String TWEET_MODE = "tweet_mode";
    public static final int TWEET_MODE_NORMAL = 0;
    public static final int TWEET_MODE_MENTION = 1;
    public static final int TWEET_MODE_DIRECT = 2;
    public static final int TWEET_MODE_REPLY = 3;
    public static final int TWEET_MODE_QT_REPLY = 4;
    public static final int TWEET_MODE_QT_RETWEET = 5;
    public static final int TWEET_MODE_RETWEET = 6;
    public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
    public static final String IN_REPLY_TO_STATUS = "in_reply_to_status";
    public static final String IN_REPLY_TO_STATUS_USERNAME = "in_reply_to_status_user_name";
    public static final String IN_REPLY_TO_STATUS_TEXT = "in_reply_to_status_text";
    public static final String IN_REPLY_TO_STATUS_ICONBMP = "in_reply_to_status_icon_bmp";
    public static final String IN_REPLY_TO_STATUS_ICONURL = "in_reply_to_status_icon_url";

    // バンドルにまつわる値
    public static final String BUNDLE_RESPONSE_LIST = "bundle_response_list";
    public static final String BUNDLE_IS_EXIST_LIST = "bundle_is_exist_list";

    
    // カラーにまつわる値
    private static final String LEFT_PAIN_COLOR = "left_pain_color";
    private static final String RIGHT_PAIN_COLOR = "right_pain_color";
    private static final String HALF_PAIN_COLOR = "half_pain_color";
    public static final int COLOR_WHITE = 0;
    public static final int COLOR_GREEN = 1;
    public static final int COLOR_RED = 2;
    private static ColorTheme colorThemeGreen = new ColorThemeGreen();
    private static ColorTheme colorThemeWhite = new ColorThemeWhite();
    private static ColorTheme colorThemeRed = new ColorThemeRed();
    public static final ColorTheme[] COLOR_THEME_LIST = {colorThemeWhite, 
                                                         colorThemeGreen, 
                                                         colorThemeRed
                                                        };
    // タイプ（ホームとかメンションとか）にまつわる値
    private static final String LEFT_PAIN_TYPE = "left_pain_type";
    private static final String RIGHT_PAIN_TYPE = "right_pain_type";
    private static final String HALF_PAIN_TYPE = "half_pain_type";    
    public static final int TIMELINE_TYPE_HOME = 0;
    public static final int TIMELINE_TYPE_MENTION = 1;
    public static final int TIMELINE_TYPE_FAVORITE = 2;
    public static final int TIMELINE_TYPE_USERLIST = 3;
    public static final int[] TIMELINE_TYPE_LIST = {TIMELINE_TYPE_HOME, 
                                                    TIMELINE_TYPE_MENTION, 
                                                    TIMELINE_TYPE_FAVORITE,
                                                    TIMELINE_TYPE_USERLIST
                                                    };

    // アクセストークンを保持してみる（仮だからな）
    private static AccessToken sAccessToken = null;
    /**
     * アクセストークンをここからゲットするようにしてみよう
     */
    public static AccessToken getAccessToken() {
        return sAccessToken;
    }
    
    /**
     * タイムラインのカラーを取得するよ left rightは広げた時、halfは閉じた時のやつね
     * @param context
     * @return カラーの値ね、AppUtils.COLOR_WHITE...
     */
    public static int loadLeftPainColor(Context context) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        int leftPainColor       = shPref.getInt(LEFT_PAIN_COLOR, 0);
        return leftPainColor;
    }
    public static int loadRightPainColor(Context context) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        int rightPainColor       = shPref.getInt(RIGHT_PAIN_COLOR, 0);
        return rightPainColor;
    }
    public static int loadHalfPainColor(Context context) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        int halfPainColor       = shPref.getInt(HALF_PAIN_COLOR, 0);
        return halfPainColor;
    }
    /**
     * タイムラインのカラーを保存するよ
     * @param context 
     * @param color カラーの値ね、AppUtils.COLOR_WHITE...
     */
    public static void saveLeftPainColor(Context context, int color) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        Editor e = shPref.edit();
        e.putInt(LEFT_PAIN_COLOR, color);
        e.commit();
    }
    public static void saveRightPainColor(Context context, int color) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        Editor e = shPref.edit();
        e.putInt(RIGHT_PAIN_COLOR, color);
        e.commit();
    }
    public static void saveHalfPainColor(Context context, int color) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        Editor e = shPref.edit();
        e.putInt(HALF_PAIN_COLOR, color);
        e.commit();
    }

    /**
     * タイムラインのタイプをロードするよ left rightは広げた時、halfは閉じた時のやつね
     * @param context
     * @return タイプの値ね、AppUtils.TIMELINE_TYPE_HOME...etc
     */
    public static int loadPainType(Context context, int fragmentId) {
        int type = 0;
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        switch(fragmentId) {
            case R.id.timelineFragmentL:
                type  = shPref.getInt(LEFT_PAIN_TYPE, 0);
                break;
            case R.id.timelineFragmentR:
                type  = shPref.getInt(RIGHT_PAIN_TYPE, 1);
                break;
            case R.id.timelineFragmentHalf:
                type  = shPref.getInt(HALF_PAIN_TYPE, 0);
                break;
            default:
                break;
        }
        return type;
    }
    /**
     * @deprecated
     */
    public static int loadLeftPainType(Context context) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        int type  = shPref.getInt(LEFT_PAIN_TYPE, 0);
        return type;
    }
    /**
     * @deprecated
     */
    public static int loadRightPainType(Context context) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        int type  = shPref.getInt(RIGHT_PAIN_TYPE, 1);
        return type;
    }
    /**
     * @deprecated
     */
    public static int loadHalfPainType(Context context) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        int type  = shPref.getInt(HALF_PAIN_TYPE, 0);
        return type;
    }

    /**
     * タイムラインのタイプを保存するよ
     * @param context 
     * @param color タイプの値ね、AppUtils.TIMELINE_TYPE_HOME...etc
     */
    public static void saveLeftPainType(Context context, int type) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        Editor e = shPref.edit();
        e.putInt(LEFT_PAIN_TYPE, type);
        e.commit();
    }
    public static void saveRightPainType(Context context, int type) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        Editor e = shPref.edit();
        e.putInt(RIGHT_PAIN_TYPE, type);
        e.commit();
    }
    public static void saveHalfPainType(Context context, int type) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        Editor e = shPref.edit();
        e.putInt(HALF_PAIN_TYPE, type);
        e.commit();
    }

    
    /**
     * アクセストークンをロードします
     * @param context
     * @return
     */
    public static AccessToken loadAccessToken(Context context) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        String token       = shPref.getString(ACCESS_TOKEN, null);
        String tokenSecret = shPref.getString(ACCESS_TOKEN_SECRET, null);

        if(token != null && tokenSecret != null) {
             // 仮のstatic変数
            sAccessToken = new AccessToken(token, tokenSecret);
            return new AccessToken(token, tokenSecret);
        } else {
            return null;
        }
    }

    public static void saveAccessToken(Context context, AccessToken accessToken) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        String token       = accessToken.getToken();
        String tokenSecret = accessToken.getTokenSecret();

        // 仮のstatic変数
        sAccessToken = accessToken;

        Editor e = shPref.edit();
        e.putString(AppUtils.ACCESS_TOKEN, token);
        e.putString(AppUtils.ACCESS_TOKEN_SECRET, tokenSecret);
        e.commit();
    }

    /**
     * エコーが縦か横か判定するよ　
     * @return
     */
    public static boolean isEchoTate(Activity act){
        DualScreen dualscreen = new DualScreen(act.getApplicationContext());
        int screen_mode = dualscreen.getScreenMode();
        Display display = act.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        if(width > height) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEchoYoko(Activity act){
        DualScreen dualscreen = new DualScreen(act.getApplicationContext());
        int screen_mode = dualscreen.getScreenMode();
        Display display = act.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        if(width > height) {
            return false;
        } else {
            return true;
        }
    }
    
    public static void showKeyboard(Activity act) {
        act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);        
    }
    
}
