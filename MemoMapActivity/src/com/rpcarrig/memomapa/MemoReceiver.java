package com.rpcarrig.memomapa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MemoReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent memoServiceIntent = new Intent(context, MemoMapService.class);
		context.startService(memoServiceIntent);
	}

}
