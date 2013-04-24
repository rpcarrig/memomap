/**
 * Handles all the interactions between the app and the database server that is in the cloud.
 * 
 * @author  Ryan P. Carrigan, Drew Markle
 * @version 1.20 18 April 2013
 */

package com.rpcarrig.memomapa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ServerHandler {
	
	/* Upload shared memos to the server database. */
	public static void upload(Memo memo) {
		String urlUpload = "http://memomapalpha-memomap.rhcloud.com/mysql_upload.php";
		// BufferedReader in = null;

		// build the Array List to be passed to the server
		ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
		postParam.add(new BasicNameValuePair("androidId", memo.getAndroidId()));
		postParam.add(new BasicNameValuePair("locationName", memo.getLocationName()));
		postParam.add(new BasicNameValuePair("memoBody", memo.getMemoBody()));
		postParam.add(new BasicNameValuePair("memoDate", memo.getMemoDate()));
		postParam.add(new BasicNameValuePair("latitude", Double.toString(memo.getLatitude())));
		postParam.add(new BasicNameValuePair("longitude", Double.toString(memo.getLongitude())));
		postParam.add(new BasicNameValuePair("radius", Integer.toString(memo.getRadius())));

		try {
			// Make a post request
			HttpPost request = new HttpPost(urlUpload);

			// give the post request parameters
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParam);
			request.setEntity(entity);

			// execute the post request
			HttpClient client = new DefaultHttpClient();
			client.execute(request);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/* Downloads memo from the database server to the local database. */
	public static void download() {
		String urlDownload = "http://memomapalpha-memomap.rhcloud.com/mysql_download.php";

		// setup needed variables
		StringBuffer buffer = new StringBuffer("");
		BufferedReader in = null;

		try {
			// Make a get request
			HttpGet request = new HttpGet();

			// set the URI for the request, execute and get the response
			request.setURI(new URI(urlDownload));

			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(request);

			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));

			String line = "";
			String NL = System.getProperty("line.separator");

			// make data into one string object for the creation of a JSONArray
			while ((line = in.readLine()) != null) {
				buffer.append(line + NL);
			}
			in.close();
			String result = buffer.toString();

			try {

				// make JSONArray based off server response
				JSONArray array = new JSONArray(result);
				String androidId, locationName, memoBody, memoDate;
				double latitude, longitude;
				int radius, key;

				// Use data from server response to make a memo object and add
				// it to the client-side database
				for (int i = 0; i < array.length(); i++) {
					JSONObject data = array.getJSONObject(i);

					key          = data.getInt("KEY");
					androidId    = data.getString("androidId");
					locationName = data.getString("locationName");
					memoBody     = data.getString("memoBody");
					memoDate     = data.getString("memoDate");
					latitude     = data.getDouble("latitude");
					longitude    = data.getDouble("longitude");
					radius       = data.getInt("radius");

					Memo m = new Memo(locationName, memoBody, latitude, longitude, radius, 
							 	 	  memoDate, key, androidId);
					
					DataHandler.getInstance(MemoMap.getInstance()).addPublicMemo(m);
					Log.d("TAG", "Memo added");
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
