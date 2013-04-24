/**
 *  @author  Ryan P. Carrigan
 *  @version 1.00 01 April 2013
 */

package com.rpcarrig.memomapa;

import org.acra.*;
import org.acra.annotation.*;
import android.app.Application;

/** Supplies arguments for the error-reporting plug-in. */
@ReportsCrashes(
		formKey                = "dHh1c3UxNERxcVdSVnNKOUxHdHQ3TUE6MQ",
		mode                   = ReportingInteractionMode.NOTIFICATION,
		resToastText           = R.string.crash_toast_text, 
		resNotifTickerText     = R.string.crash_notif_ticker_text,
		resNotifTitle          = R.string.crash_notif_title,
		resNotifText           = R.string.crash_notif_text,
		resNotifIcon           = android.R.drawable.stat_notify_error,
		resDialogText          = R.string.crash_dialog_text,
		resDialogIcon          = android.R.drawable.ic_dialog_info,
		resDialogTitle         = R.string.crash_dialog_title,
		resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
		resDialogOkToast       = R.string.crash_dialog_ok_toast
	)

public class MemoMap extends Application {
	private static MemoMap instance;
	
	public MemoMap(){
		instance = this;
	}
	
	/* Initializes the error-reporting plug-in. */
	public void onCreate(){
		super.onCreate();
		ACRA.init(this);
	}
	
	/* Allows any class to get the application context. */
	public static MemoMap getInstance(){
		return instance;
	}
}
