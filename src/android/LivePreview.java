package com.radostyan.cordova.livepreview;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Runnable;
import java.util.Scanner;

import com.radostyan.cordova.livepreview.MJpegInputStream;

public class LivePreview extends CordovaPlugin {
  private HttpURLConnection connection;

	@Override
	public boolean execute(String action, JSONArray args,final CallbackContext callbackContext) throws JSONException {
		if (action.equals("getLivePreview")) {
			final String ip = args.getString(0);

      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            getLivePreview(callbackContext, ip);
          } catch (Exception e) {
            callbackContext.error(e.getMessage());
          }
        }
      });

      PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
      pluginResult.setKeepCallback(true);
      callbackContext.sendPluginResult(pluginResult);

			return true;
		} else if (action.equals("stopLivePreview")) {
      try {
        connection.disconnect();
        callbackContext.success();
      } catch (Exception e) {
        callbackContext.error(e.getMessage());
      }
    }

		return false;
	}

	private void getLivePreview(final CallbackContext callbackContext,final String ip) throws IOException, JSONException, MalformedURLException {
		URL url = new URL(ip + "osc/commands/execute");
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);

		JSONObject input = new JSONObject();
    input.put("name", "camera.getLivePreview");

		OutputStream os = connection.getOutputStream();
		os.write(input.toString().getBytes());

		connection.connect();
		os.flush();
		os.close();

		InputStream is = connection.getInputStream();
		MJpegInputStream mjis = new MJpegInputStream(is);

    boolean keepRunning = true;
    while (keepRunning) {
      try {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, mjis.readMJpegFrame());
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
      } catch (IOException e) {
        keepRunning = false;
        callbackContext.success();
      }
    }
	}
}
