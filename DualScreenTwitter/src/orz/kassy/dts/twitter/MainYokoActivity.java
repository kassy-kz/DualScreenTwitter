package orz.kassy.dts.twitter;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainYokoActivity extends Activity implements OnClickListener {

    private static final String TAG = "MainYoko";

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

        // View setting
        Button btn = (Button)findViewById(R.id.btnsend);
        btn.setOnClickListener(this);

        //AdMob setting
        AdView adView = new AdView(this, AdSize.BANNER, "a14e939d84dfe72");
        LinearLayout layout = (LinearLayout)findViewById(R.id.admob);
        layout.addView(adView);
        AdRequest request = new AdRequest();
        adView.loadAd(request);
    }

    @Override
    public void onClick(View v) {

    }
}
