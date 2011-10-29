package orz.kassy.dts.image;

import org.apache.http.HttpStatus;

import orz.kassy.dts.web.HttpServerIF;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ThumbnailTask extends AsyncTask<String, Void, Integer> {
	private Bitmap mBitmap = null;
	private ImageView mImageView = null;
	
	public ThumbnailTask(ImageView imageView) {
		mImageView = imageView;
	}
	
	/**
	 * ワーカースレッドの処理
	 * 引数：　params URL, key
	 */
	@Override
	protected Integer doInBackground(String... params) {
		String url = params[0];
		String strKey = params[1];
		int iRet = 0;
		if(url != null && url.length() > 0) {
			HttpServerIF svr = new HttpServerIF();
			iRet = svr.requestImage(url);
			if(iRet == HttpStatus.SC_OK) {
				mBitmap = svr.getResBitmap();
				if(mBitmap != null) {
					ImageCache.setImage(strKey, mBitmap);
				}
			}
		}
		return new Integer(iRet);
	}

    /**
     * 後処理　（UIスレッド）
     */
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if(result == HttpStatus.SC_OK) {
			mImageView.setImageBitmap(mBitmap);
		}
	}

}
