package com.saradabar.cpadcustomizetool.data.check;

import android.app.Activity;
import android.os.AsyncTask;

import com.saradabar.cpadcustomizetool.data.event.UpdateEventListener;
import com.saradabar.cpadcustomizetool.data.event.UpdateEventListenerList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class AsyncFileDownload extends AsyncTask<String, Void, Boolean> {

	private final UpdateEventListenerList updateListeners;
	private final String url;
	private final File outputFile;
	private FileOutputStream fileOutputStream;
	private BufferedInputStream bufferedInputStream;
	private int totalByte = 0, currentByte = 0;

	public AsyncFileDownload(Activity mActivity, String string, File oFile) {
		updateListeners = new UpdateEventListenerList();
		updateListeners.addEventListener((UpdateEventListener) mActivity);
		url = string;
		outputFile = oFile;
	}

	@Override
	protected Boolean doInBackground(String... mString) {
		final byte[] buffer = new byte[1024];

		try {
			HttpURLConnection mHttpURLConnection;
			mHttpURLConnection = (HttpURLConnection) new URL(url).openConnection();
			mHttpURLConnection.setReadTimeout(5000);
			mHttpURLConnection.setConnectTimeout(5000);
			InputStream mInputStream = mHttpURLConnection.getInputStream();
			bufferedInputStream = new BufferedInputStream(mInputStream, 1024);
			fileOutputStream = new FileOutputStream(outputFile);
			totalByte = mHttpURLConnection.getContentLength();
		} catch (SocketTimeoutException | MalformedURLException ignored) {
			return false;
		} catch (IOException ignored) {
			return null;
		}

		if (isCancelled()) {
			return false;
		}

		try {
			int len;
			while ((len = bufferedInputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, len);
				currentByte += len;
				if (isCancelled()) break;
			}
		} catch (IOException ignored) {
			return false;
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
			if (result) updateListeners.downloadCompleteNotify();
			else updateListeners.connectionErrorNotify();
		} else updateListeners.downloadErrorNotify();
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
		if (totalByte <= 0) return 0;
		return (int) Math.floor(100 * currentByte / totalByte);
	}
}