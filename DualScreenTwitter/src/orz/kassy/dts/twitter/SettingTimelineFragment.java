package orz.kassy.dts.twitter;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

public class  SettingTimelineFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{        

    private static final String TAG = "SettingTimelineFrag";
    private static int NUM_OF_VIEWS = 2;
    private static int[] mViewColor = {Color.RED, Color.BLACK, Color.BLUE, Color.GREEN, Color.DKGRAY};
    private Activity mContext;
    private MyPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private View mSetColorView;
    private View mSetTimelineTypeView;
    private RadioGroup mColorRadioGroup;
    private RadioGroup mTypeRadioGroup;
    private int mTimelineId;
    
    /**
     * コンストラクタ
     * @param timelineId　設定先のtimelinefragmentのリソースID
     */
    public SettingTimelineFragment(int timelineId) {
        super();
        mTimelineId = timelineId;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.set_timeline_fragment, container, false);
        mContext = getActivity();

        // ViewPagerの設定
        mPagerAdapter = new MyPagerAdapter();
        mViewPager = (ViewPager) view.findViewById(R.id.setTimelineViewPager);
        mViewPager.setAdapter(mPagerAdapter);
        
        // ViewPagerの各ページのView
        mSetColorView = inflater.inflate(R.layout.set_timeline_color, null);
        mSetTimelineTypeView = inflater.inflate(R.layout.set_timeline_type, null);

        mColorRadioGroup = (RadioGroup) mSetColorView.findViewById(R.id.setTimelineColorRadio);
        mColorRadioGroup.setOnCheckedChangeListener(this);
        mTypeRadioGroup = (RadioGroup) mSetTimelineTypeView.findViewById(R.id.setTimelineTypeRadio);
        mTypeRadioGroup.setOnCheckedChangeListener(this);

        return view;
    }
    
    private class MyPagerAdapter extends PagerAdapter{
        
        private View mPageView0;

        @Override
        public int getCount() {
            //Pagerに登録したビューの数を返却。サンプルは固定なのでNUM_OF_VIEWS
            return NUM_OF_VIEWS;
        }
 
        /**
         * ページを生成する
         * position番目のViewを生成し返却するために利用
         * @param container: 表示するViewのコンテナ
         * @param position : インスタンス生成位置
         * @return ページを格納しているコンテナを返却すること。サンプルのようにViewである必要は無い。
         */
        @Override
        public Object instantiateItem(View collection, int position) {
            View view = null;
            switch(position) {
                case 0:
//                    view = mPageView0;
                    view = mSetTimelineTypeView;
                    ((ViewPager) collection).addView(mSetTimelineTypeView,0);
                    break;
                case 1:
                    view = mSetColorView;
                    ((ViewPager) collection).addView(mSetColorView,0);
                    break;
                case 2:
                    break;
            }
            return view;
//            TextView tv = new TextView(mContext);
//            tv.setText("Hello, world! myPostion :" + position);
//            tv.setTextColor(Color.WHITE);
//            tv.setTextSize(30);
//            tv.setBackgroundColor(mViewColor[position]);
// 
//            ((ViewPager) collection).addView(tv,0);
//            return tv;
        }
        
        /**
         * ページを破棄する。
         * postion番目のViweを削除するために利用
         * @param container: 削除するViewのコンテナ
         * @param position : インスタンス削除位置
         * @param object   : instantiateItemメソッドで返却したオブジェクト
         */
        @Override
        public void destroyItem(View collection, int position, Object view) {
            //ViewPagerに登録していたTextViewを削除する
            ((ViewPager) collection).removeView((TextView) view);
        }
        @Override
        public void finishUpdate(View arg0) {
            // TODO Auto-generated method stub
            
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            //表示するViewがコンテナに含まれているか判定する(表示処理のため)
            //objecthainstantiateItemメソッドで返却したオブジェクト。
            //今回はTextViewなので以下の通りオブジェクト比較
            return view==((View)object);
        }
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            // TODO Auto-generated method stub
        }

        @Override
        public Parcelable saveState() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
            // TODO Auto-generated method stub
            
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Log.i(TAG,"radio button pressed checked group = "+group+"id = "+checkedId);
        switch(checkedId){
            case R.id.setTypeHomeTimeline:
                Log.i(TAG,"selected home time line");
                if(mTimelineId == R.id.timelineFragmentL) {
                    AppUtils.saveLeftPainType(getActivity(), AppUtils.TIMELINE_TYPE_HOME);
                }else if(mTimelineId == R.id.timelineFragmentR) {
                    AppUtils.saveRightPainType(getActivity(), AppUtils.TIMELINE_TYPE_HOME);
                }else if(mTimelineId == R.id.timelineFragmentHalf) {
                    AppUtils.saveHalfPainType(getActivity(), AppUtils.TIMELINE_TYPE_HOME);
                }
                break;
            case R.id.setTypeFavorites:
                Log.i(TAG,"selected favorite");
                if(mTimelineId == R.id.timelineFragmentL) {
                    AppUtils.saveLeftPainType(getActivity(), AppUtils.TIMELINE_TYPE_FAVORITE);
                }else if(mTimelineId == R.id.timelineFragmentR) {
                    AppUtils.saveRightPainType(getActivity(), AppUtils.TIMELINE_TYPE_FAVORITE);
                }else if(mTimelineId == R.id.timelineFragmentHalf) {
                    AppUtils.saveHalfPainType(getActivity(), AppUtils.TIMELINE_TYPE_FAVORITE);
                }
                break;
            case R.id.setTypeMentions:
                Log.i(TAG,"selected mention");
                if(mTimelineId == R.id.timelineFragmentL) {
                    AppUtils.saveLeftPainType(getActivity(), AppUtils.TIMELINE_TYPE_MENTION);
                }else if(mTimelineId == R.id.timelineFragmentR) {
                    AppUtils.saveRightPainType(getActivity(), AppUtils.TIMELINE_TYPE_MENTION);
                }else if(mTimelineId == R.id.timelineFragmentHalf) {
                    AppUtils.saveHalfPainType(getActivity(), AppUtils.TIMELINE_TYPE_MENTION);
                }
                break;
            case R.id.setTypeUserList:
                Log.i(TAG,"selected user list");
                if(mTimelineId == R.id.timelineFragmentL) {
                    AppUtils.saveLeftPainType(getActivity(), AppUtils.TIMELINE_TYPE_USERLIST);
                }else if(mTimelineId == R.id.timelineFragmentR) {
                    AppUtils.saveRightPainType(getActivity(), AppUtils.TIMELINE_TYPE_USERLIST);
                }else if(mTimelineId == R.id.timelineFragmentHalf) {
                    AppUtils.saveHalfPainType(getActivity(), AppUtils.TIMELINE_TYPE_USERLIST);
                }
                break;
            case R.id.setColorWhite:
                if(mTimelineId == R.id.timelineFragmentL) {
                    AppUtils.saveLeftPainColor(getActivity(), AppUtils.COLOR_WHITE);
                }else if(mTimelineId == R.id.timelineFragmentR) {
                    AppUtils.saveRightPainColor(getActivity(), AppUtils.COLOR_WHITE);
                }else if(mTimelineId == R.id.timelineFragmentHalf) {
                    AppUtils.saveHalfPainColor(getActivity(), AppUtils.COLOR_WHITE);
                }
                break;
            case R.id.setColorGreen:
                if(mTimelineId == R.id.timelineFragmentL) {
                    AppUtils.saveLeftPainColor(getActivity(), AppUtils.COLOR_GREEN);
                }else if(mTimelineId == R.id.timelineFragmentR) {
                    AppUtils.saveRightPainColor(getActivity(), AppUtils.COLOR_GREEN);
                }else if(mTimelineId == R.id.timelineFragmentHalf) {
                    AppUtils.saveHalfPainColor(getActivity(), AppUtils.COLOR_GREEN);
                }
                break;
            case R.id.setColorRed:
                if(mTimelineId == R.id.timelineFragmentL) {
                    AppUtils.saveLeftPainColor(getActivity(), AppUtils.COLOR_RED);
                }else if(mTimelineId == R.id.timelineFragmentR) {
                    AppUtils.saveRightPainColor(getActivity(), AppUtils.COLOR_RED);
                }else if(mTimelineId == R.id.timelineFragmentHalf) {
                    AppUtils.saveHalfPainColor(getActivity(), AppUtils.COLOR_RED);
                }
                break;
                
        }
    }
}

