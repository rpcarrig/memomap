package com.rpcarrig.memomapa;

import android.app.IntentService;
import android.content.Intent;

public class MemoMapIntentService extends IntentService {

	public MemoMapIntentService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String dataString = intent.getDataString();
	}

}
