package com.example.yamba;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class UpdaterService extends IntentService {
	private static final String TAG = "UpdaterService";
	public static final String NEW_STATUS_INTENT = "com.example.yamba.NEW_STATUS";
	public static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
	public static final String RECEIVE_TIMELINE_NOTIFICATIONS = "com.example.yamba.RECEIVE_TIMELINE_NOTIFICATIONS";
	private NotificationManager notificationManager;
	private Notification notification;

	public UpdaterService() { // http://dev.icybear.net/learning-android-cn/images/2.png
		super(TAG);
		Log.d(TAG, "UpdaterService constructed");
	}

	@Override
	protected void onHandleIntent(Intent inIntent) { // http://dev.icybear.net/learning-android-cn/images/3.png
		Intent intent;
		this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		this.notification = new Notification(
				android.R.drawable.stat_notify_chat, "", 0);

		Log.d(TAG, "onHandleIntent'ing");

		YambaApplication yamba = (YambaApplication) getApplication();

		int newUpdates = yamba.fetchStatusUpdates();

		if (newUpdates > 0) { // http://dev.icybear.net/learning-android-cn/images/4.png

			Log.d(TAG, "We have a new status");
			intent = new Intent(NEW_STATUS_INTENT);
			intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates);
			sendBroadcast(intent, RECEIVE_TIMELINE_NOTIFICATIONS);
			sendTimelineNotification(newUpdates);
		}

	}

	/**
	 * 
	 * Creates a notification in the notification bar telling user there are new
	 * 
	 * messages
	 * 
	 * 
	 * 
	 * @param timelineUpdateCount
	 * 
	 *            Number of new statuses
	 */

	private void sendTimelineNotification(int timelineUpdateCount) {
		Log.d(TAG, "sendTimelineNotification'ing");
		PendingIntent pendingIntent = PendingIntent.getActivity(this, -1,
				new Intent(this, TimelineActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		this.notification.when = System.currentTimeMillis();
		this.notification.flags |= Notification.FLAG_AUTO_CANCEL;

		CharSequence notificationTitle = this
				.getText(R.string.msgNotificationTitle);
		CharSequence notificationSummary = this.getString(
				R.string.msgNotificationMessage, timelineUpdateCount);

		this.notification.setLatestEventInfo(this, notificationTitle,
				notificationSummary, pendingIntent);
		this.notificationManager.notify(0, this.notification);
		Log.d(TAG, "sendTimelineNotificationed");
	}
}
