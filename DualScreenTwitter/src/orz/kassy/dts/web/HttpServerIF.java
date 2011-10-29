package orz.kassy.dts.web;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class HttpServerIF {
	public static final int UNKNOWN_HOST = -1;
	private static final String LOG_TAG = "HttpServerIF";
	
	private Bitmap mResultBitmap;
	private String mResultText;
	
	public Bitmap getResBitmap() {
		return mResultBitmap;
	}
	
	public String getResText() {
		return mResultText;
	}
	
	public int requestText(String url) {
		int iRet = UNKNOWN_HOST;
		mResultText = "";								// メンバ変数の内容を初期化する。

		log(url);
		
		// 引数に記述されたURLに対してGET METHODでのリクエストを送信する。
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		
		HttpResponse res = null;
		HttpEntity entity = null;
		try {
			res = client.execute(httpGet);
			if(res != null) {
				iRet = res.getStatusLine().getStatusCode();
				if(iRet == HttpStatus.SC_OK) {
					// リクエスト成功。レスポンスを取得
					entity = res.getEntity();
					mResultText = EntityUtils.toString(entity);
				}
			}
		} catch(UnknownHostException uhe) {
			log("UnknownHost");
		} catch(Exception e) {
			log(e);
		} finally {
			// HttpEntityのリソースを解放する
			try {
				if(entity != null) entity.consumeContent();
			} catch (IOException ie) {
				log(ie);
			}
			
			// クライアントを終了させる。
			client.getConnectionManager().shutdown();
		}
		
		return iRet;
	}
	
	/**
	 * 画像をサーバーから取得してmResultBitmapに格納
	 * @param url  URL
	 * @return Http Result
	 */
	public int requestImage(String url) {
		int iRet = UNKNOWN_HOST;
		mResultBitmap = null;							// メンバ変数の内容を初期化する。
		log(url);
		
		// 引数に記述されたURLに対してGET METHODでのリクエストを送信する。
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		
		HttpResponse res = null;
		try {
			res = client.execute(httpGet);
			if(res != null) {
				iRet = res.getStatusLine().getStatusCode();
				if(iRet == HttpStatus.SC_OK) {
					// リクエスト成功
					try {
						// 画像へのリクエストを前提にするので、レスポンスを直接Bitmapに変換する。
						Bitmap bmp = BitmapFactory.decodeStream(res.getEntity().getContent());
						if(bmp != null) {
							// 受信後にメンバ変数に退避
							mResultBitmap = bmp;
						}
					} catch(IOException ie) {
						log(ie);
					}
				}
			}
		} catch(UnknownHostException uhe) {
			log("UnknownHost");
		} catch(Exception e) {
			log(e);
		} finally {
			// クライアントを終了させる。
			client.getConnectionManager().shutdown();
		}

		return iRet;
	}
		
	private static void log(String strLog) {
//		Log.d(LOG_TAG, strLog);
	}
	
	private static void log(Exception e) {
//		Log.d(LOG_TAG, "", e);
	}
}
