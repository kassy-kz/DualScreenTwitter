package orz.kassy.dts.twitter;


import orz.kassy.dts.twitter.R;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class Utils {
	
	private static NotificationManager sNM;
	
	public static void showNotification(Context con, int iconResource, String startText, String titleText, String bodyText) {
		sNM=(NotificationManager)con.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		PendingIntent contentIntent = PendingIntent.getActivity(con, 0, intent, 0);

		Notification notification = new Notification(R.drawable.icon,"tekitouText",System.currentTimeMillis());
		notification.setLatestEventInfo(con, titleText, bodyText, contentIntent);
		
		sNM.notify(R.string.app_name, notification);
	}
	
	public static void deleteNotification(){
	    sNM.cancel(R.string.app_name);
	}
	
	public static String getStrAddress(int ip){
		String ipStr = ((ip>>0) &0xFF)+"."+
					   ((ip>>8) &0xFF)+"."+
					   ((ip>>16)&0xFF)+"."+
					   ((ip>>24)&0xFF);
		return ipStr;
	}
	
	public static String geStrAddress(Context con){
		WifiManager wfm = (WifiManager)con.getSystemService(Context.WIFI_SERVICE);
		int ip = wfm.getConnectionInfo().getIpAddress();
		String ipStr = ((ip>>0) &0xFF)+"."+
		   ((ip>>8) &0xFF)+"."+
		   ((ip>>16)&0xFF)+"."+
		   ((ip>>24)&0xFF);
		return ipStr;
	}
	
	/**
	 * シンプルアラートダイアログ出すよ 
	 * @param con
	 * @param title
	 * @param message
	 * @param okListener
	 * @param cancelListener
	 */
	public static void showSimpleAlertDialog(Context con, 
	                                            String title, 
	                                            String message, 
	                                            OnClickListener okListener, 
	                                            OnClickListener cancelListener ){

	    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(con);
        // アラートダイアログのタイトル、メッセージを設定
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        
        // ボタン押しコールバックリスナーを登録します
        alertDialogBuilder.setPositiveButton("OK", okListener);
        alertDialogBuilder.setNegativeButton("キャンセル", cancelListener);

        // アラートダイアログのキャンセルが可能かどうかを設定
        alertDialogBuilder.setCancelable(true);

        // アラートダイアログを表示
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();	    
	}

	/**
	 * IPアドレスを取得するよ　整数型で
	 * @param con
	 * @return
	 */
	public static int geIntAddress(Context con){
		WifiManager wfm = (WifiManager)con.getSystemService(Context.WIFI_SERVICE);
		int ip = wfm.getConnectionInfo().getIpAddress();
		return ip;
	}
	
	/**
	 * トーストを表示するよ
	 * @param con　コンテキスト
	 * @param message　メッセージ
	 */
	public static void showToast(Context con, String message){
	    Toast.makeText(con, message, Toast.LENGTH_LONG).show();
	}
	
}
