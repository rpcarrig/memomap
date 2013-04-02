package com.rpcarrig.memomapa;

import org.acra.*;
import org.acra.annotation.*;
import android.app.Application;

@ReportsCrashes(formKey = "dHh1c3UxNERxcVdSVnNKOUxHdHQ3TUE6MQ",
	mode = ReportingInteractionMode.NOTIFICATION,
	resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
	resNotifTickerText = R.string.crash_notif_ticker_text,
	resNotifTitle = R.string.crash_notif_title,
	resNotifText = R.string.crash_notif_text,
	resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
	resDialogText = R.string.crash_dialog_text,
	resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
	resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
	resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
	resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
	)
public class MemoMap extends Application {
	private static MemoMap instance;
	
	public MemoMap(){
		instance = this;
	}
	
	public void onCreate(){
		super.onCreate();
		
		ACRA.init(this);
	}
	
	public static MemoMap getInstance(){
		return instance;
	}
}
