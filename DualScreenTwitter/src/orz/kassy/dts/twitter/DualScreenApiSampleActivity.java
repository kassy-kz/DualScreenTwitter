package orz.kassy.dts.twitter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import orz.kassy.dts.twitter.R;
import com.kyocera.dualscreen.DualScreen;

public class DualScreenApiSampleActivity extends Activity implements OnClickListener {

    private static final String INTENT_ACTION_SLIDE = "com.kyocera.intent.action.SLIDE_OPEN";
    private static final String TAG = "DTS kassy";
    CustomReceiver mReceiver;

    private TextView mTvScreenMode;
    private TextView mTvOrientation;
    private TextView mTvRotation;
    private ImageView mIvSecondary;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        DualScreen.restrictOrientationAtFullScreen( this, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED );
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        setScreenLayout();

        // Register intent receiver.
        mReceiver = new CustomReceiver();
        IntentFilter slideFilter = new IntentFilter(INTENT_ACTION_SLIDE);
        registerReceiver(mReceiver, slideFilter);


        displayStatus();
    }
    
    @Override
    public void onDestroy() {
        
        super.onDestroy();
        // Unregister intent receiver.
        unregisterReceiver(mReceiver);
    }
    
    public void onClick(View v) {
        
        if( v == findViewById(R.id.RadioNone) ) {
            DualScreen.restrictOrientationAtFullScreen( this, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED );
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else if( v == findViewById(R.id.RadioPortrait) ) {
            DualScreen.restrictOrientationAtFullScreen( this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if( v == findViewById(R.id.RadioLandscape) ) {
            DualScreen.restrictOrientationAtFullScreen( this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
    
    private void setScreenLayout() {
        
        DualScreen dualscreen = new DualScreen(getApplicationContext());
        int screen_mode = dualscreen.getScreenMode();

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        if( screen_mode == DualScreen.FULL ) {
            // for full screen
            if( width > height ) {
                setContentView(R.layout.fullscreen_landscape);
            } else {
                setContentView(R.layout.fullscreen_portrait);
                
                // Primary and secondary screen heights calculation.
                //  (Full screen-portrait mode only) 
                View layout_primary = findViewById(R.id.LayoutPrimaryScreen);
                View layout_secondary = findViewById(R.id.LayoutSecondaryScreen);
                
                LayoutParams layoutparams_primary = layout_primary.getLayoutParams();
                LayoutParams layoutparams_secondary = layout_secondary.getLayoutParams();

                // get notification bar height
                Drawable phone_call_icon = getResources().getDrawable(android.R.drawable.stat_sys_phone_call);
                int height_notification = phone_call_icon.getIntrinsicHeight();

                // calculate heights
                layoutparams_primary.height = height / 2 - height_notification;
                layoutparams_secondary.height = height / 2;
                
                layout_primary.setLayoutParams(layoutparams_primary);
                layout_secondary.setLayoutParams(layoutparams_secondary);
            }

        } else {
            // for normal screen
            if( width > height ) {
                setContentView(R.layout.normalscreen_landscape);
            } else {
                setContentView(R.layout.normalscreen_portrait);
            }
        }
        
        // Initialize view/widget instances
        mTvScreenMode = (TextView)findViewById(R.id.TvScreenMode);
        mTvOrientation = (TextView)findViewById(R.id.TvOrientation);
        mTvRotation = (TextView)findViewById(R.id.TvRotation);
        if( screen_mode == DualScreen.FULL ) {
            mIvSecondary = (ImageView)findViewById(R.id.IvSecondary);
        } else {
            mIvSecondary = null;
        }
        
        findViewById(R.id.RadioNone).setOnClickListener(this);
        findViewById(R.id.RadioPortrait).setOnClickListener(this);
        findViewById(R.id.RadioLandscape).setOnClickListener(this);
        
    }
    
    private void displayStatus() {
        
        DualScreen dualscreen = new DualScreen(getApplicationContext());
        int screen_mode = dualscreen.getScreenMode();

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        
        // Screen mode
        int res_mode;
        if( screen_mode == DualScreen.FULL ) {
            res_mode = R.string.mode_fullscreen;
        } else {
            res_mode = R.string.mode_normalscreen;
        }
        mTvScreenMode.setText(res_mode);

        // Screen orientation
        int res_orientation = R.string.orientation_portrait;
        if( width > height ) {
            res_orientation = R.string.orientation_landscape;
        }
        String disp_orientation = this.getString(res_orientation);
        mTvOrientation.setText(disp_orientation);

        // Rotation degrees
        int res_degrees = 0;

        switch( display.getRotation() ) {
        case Surface.ROTATION_0:
            res_degrees = R.string.rotation_0;
            break;
        case Surface.ROTATION_90:
            res_degrees = R.string.rotation_90;
            break;
        case Surface.ROTATION_180:
            res_degrees = R.string.rotation_180;
            break;
        case Surface.ROTATION_270:
            res_degrees = R.string.rotation_270;
            break;
        }
        mTvRotation.setText(res_degrees);

        // Orientation setting
        int screen_orientation = getRequestedOrientation();
        switch( screen_orientation ) {
        case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
            ((RadioButton)findViewById(R.id.RadioPortrait)).setChecked(true);
            break;
        case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
            ((RadioButton)findViewById(R.id.RadioLandscape)).setChecked(true);
            break;
        default:
            ((RadioButton)findViewById(R.id.RadioNone)).setChecked(true);
            break;
        }
        
        // Secondary animation
        if( mIvSecondary != null ) {
            AlphaAnimation alpha = new AlphaAnimation(0, 2);
            alpha.setDuration(1000);
            mIvSecondary.startAnimation(alpha);
        }

    }
    
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG,"onConfigurationChange");
        super.onConfigurationChanged(newConfig);
        setScreenLayout();
        displayStatus();
    }

    
    // -------------------------------------------------------------
    //  Broadcast intent custom receiver
    // -------------------------------------------------------------
    class CustomReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            
        	String action = intent.getAction();
            if ( action.equals(INTENT_ACTION_SLIDE)) {
                boolean slideOpen = intent.getBooleanExtra("OPEN",false);
                if(slideOpen) {
                    Toast.makeText(DualScreenApiSampleActivity.this, R.string.msg_slide_opened, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DualScreenApiSampleActivity.this, R.string.msg_slide_closed, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
}