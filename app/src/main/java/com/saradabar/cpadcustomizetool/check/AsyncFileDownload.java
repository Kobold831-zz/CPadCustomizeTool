package com.saradabar.cpadcustomizetool.check;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;

import com.saradabar.cpadcustomizetool.check.event.UpdateEventListener;
import com.saradabar.cpadcustomizetool.check.event.UpdateEventListenerList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class AsyncFileDownload extends AsyncTask<String, Void, Boolean> {

	private final UpdateEventListenerList updateListeners;
	@SuppressLint("StaticFieldLeak")
	public Activity activity;
	private final int BUFFER_SIZE = 1024;
	private final String url;
	private final File outputFile;
	private FileOutputStream fileOutputStream;
	private BufferedInputStream bufferedInputStream;
	private int totalByte = 0, currentByte = 0;
	private final byte[] buffer = new byte[BUFFER_SIZE];

	public AsyncFileDownload(Activity mActivity, String mString, File oFile) {
		updateListeners = new UpdateEventListenerList();
		updateListeners.addEventListener((UpdateEventListener) mActivity);
		activity = mActivity;
		url = mString;
		outputFile = oFile;
	}

	@Override
	protected Boolean doInBackground(String... mString) {

		try {
			HttpURLConnection mHttpURLConnection;

			mHttpURLConnection = (HttpURLConnection) new URL(url).openConnection();
			mHttpURLConnection.setReadTimeout(5000);
			mHttpURLConnection.setConnectTimeout(5000);
			InputStream mInputStream = mHttpURLConnection.getInputStream();
			bufferedInputStream = new BufferedInputStream(mInputStream, BUFFER_SIZE);
			fileOutputStream = new FileOutputStream(outputFile);
			totalByte = mHttpURLConnection.getContentLength();
			currentByte = 0;
		} catch (SocketTimeoutException | MalformedURLException ignored) {
			return false;
		} catch (FileNotFoundException ignored) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		if (isCancelled()) {
			return false;
		}

		if (bufferedInputStream != null) {
			try {
				int len;
				while ((len = bufferedInputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, len);
					currentByte += len;
					if (isCancelled()) {
						break;
					}
				}
			} catch (IOException e) {
				return false;
			}
		}

		try {
			close();
		} catch (IOException ignored) {
		}
		return true;
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result != null) {
			if (result) {
				updateListeners.downloadCompleteNotify();
			} else updateListeners.connectionErrorNotify();
		} else {
			updateListeners.downloadErrorNotify();
		}
	}

	@Override
	protected void onProgressUpdate(Void... progress) {
	}

	private void close() throws IOException {
		fileOutputStream.flush();
		fileOutputStream.close();
		bufferedInputStream.close();
	}

	public int getLoadedBytePercent() {
		if (totalByte <= 0) {
			return 0;
		}
		return (int) Math.floor(100 * currentByte / totalByte);
	}
}