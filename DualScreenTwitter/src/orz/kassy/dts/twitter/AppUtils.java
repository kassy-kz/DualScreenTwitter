package orz.kassy.dts.twitter;

import com.kyocera.dualscreen.DualScreen;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
    
    public static AccessToken loadAccessToken(Context context) {
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        String token       = shPref.getString(ACCESS_TOKEN, null);
        String tokenSecret = shPref.getString(ACCESS_TOKEN_SECRET, null);

        if(token != null && tokenSecret != null) {
            return new AccessToken(token, tokenSecret);
        } else {
            return null;
        }
    }

    public static void saveAccessToken(Context context, AccessToken accessToken) {
        
        SharedPreferences shPref = context.getSharedPreferences(AppUtils.PREF_FILE_NAME,Context.MODE_PRIVATE);
        String token       = accessToken.getToken();
        String tokenSecret = accessToken.getTokenSecret();

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
