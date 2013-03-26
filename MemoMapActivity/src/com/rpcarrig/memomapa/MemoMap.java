package com.rpcarrig.memomapa;

import android.app.Application;

public class MemoMap extends Application {
	private static MemoMap instance;
	
	public MemoMap(){
		instance = this;
	}
	
	public static MemoMap getInstance(){
		return instance;
	}
}
