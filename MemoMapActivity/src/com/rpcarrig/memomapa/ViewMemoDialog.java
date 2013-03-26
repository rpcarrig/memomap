package com.rpcarrig.memomapa;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

public class ViewMemoDialog extends DialogFragment {
	private static final String TAG = "SearchAddressDialog";
	
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Log.d(TAG, "onCreateDialog");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		builder.setView(inflater.inflate(R.layout.dialog_searchaddr, null))
			.setPositiveButton(R.string.search, new DialogInterface
					.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, 
								int which) {
							Log.d(TAG, "onClick");
						}
					})
			.setNegativeButton(R.string.cancel, new DialogInterface
					.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, 
								int which) {
							Log.d(TAG, "onCancel");
							ViewMemoDialog.this.dismiss();
							
						}
					});
		return builder.create();
	}
	
	public interface NoticeDialogListener{
		public void onDialogPositiveClick(DialogFragment dialog);
	}
	
	NoticeDialogListener listener;	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try{
			listener = (NoticeDialogListener)activity;
		} catch (ClassCastException e){
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener.");
		}
	}
}
