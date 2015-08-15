package net.walklight.busio.utils;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by Poh Yee Hui on 3/2/2015.
 */
public class WebTool {
    public static String getHtml(String url, List<NameValuePair> data, String user, String pin) throws ClientProtocolException, IOException{

        String address = url.toString() + "?";
        for(NameValuePair pair : data){
            address += (pair.getName() + "=" + pair.getValue() + "&");
        }

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpClient httpclient = new DefaultHttpClient(httpParams);
        HttpGet httpget = new HttpGet(address);
//        Log.i("Auth", user + ":" + pin);
//        Log.d("BernerInternational", address);
//        httpget.addHeader("Authorization", "Basic " + Base64.encodeToString((user + ":" + pin).getBytes(), Base64.NO_WRAP));

        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        String strResponse = EntityUtils.toString(entity);
        return strResponse;
    }

    public static String postHtml(String uri, List<NameValuePair> data, String user, String pin)
            throws ClientProtocolException, IOException {

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpClient httpclient = new DefaultHttpClient(httpParams);
        HttpPost httppost = new HttpPost(uri);
        httppost.setEntity(new UrlEncodedFormEntity(data));
//        Log.i("Auth", user + ":" + pin);
//        Log.i("Response", httppost.getURI().toASCIIString());
//        httppost.addHeader("Authorization", "Basic " + Base64.encodeToString((user + ":" + pin).getBytes(), Base64.NO_WRAP));
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public static String postJsonHtml(String uri, JSONObject data, String user, String pin)
            throws ClientProtocolException, IOException {

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpClient httpclient = new DefaultHttpClient(httpParams);
        HttpPost httppost = new HttpPost(uri);

//        Log.i("Auth", user + ":" + pin);

        httppost.setEntity(new StringEntity(data.toString(), "UTF8"));
//        Log.i("Response", httppost.getURI().toASCIIString());
//        httppost.addHeader("Authorization", "Basic " + Base64.encodeToString((user + ":" + pin).getBytes(), Base64.NO_WRAP));
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public static void getHtmlInBackground(String url, List<NameValuePair> data, String user, String pin, WebCallback callback){
        WebAsyncTask task = new WebAsyncTask(Method.GET, url, user, pin);
        task.setCallback(callback);
        task.setDataPairs(data);
        task.execute();
    }

    public static void postHtmlInBackground(String url, List<NameValuePair> data, String user, String pin, WebCallback callback){
        WebAsyncTask task = new WebAsyncTask(Method.POST, url, user, pin);
        task.setCallback(callback);
        task.setDataPairs(data);
        task.execute();
    }

    public static void postJsonHtmlInBackground(String url, JSONObject data, String user, String pin, WebCallback callback){
        WebAsyncTask task = new WebAsyncTask(Method.POSTJSON, url, user, pin);
        task.setCallback(callback);
        task.setDataJSON(data);
        task.execute();
    }

    private enum Method {
        POST,
        GET,
        POSTJSON
    }

    public static class WebAsyncTask extends AsyncTask<Void, Void, String>{
        private JSONObject dataJSON;
        private List<NameValuePair> dataPairs;
        private Method method;
        private String url;
        private String pin;
        private String user;
        private WebCallback callback = null;

        public WebAsyncTask(Method method, String url, String user, String pin) {
            this.method = method;
            this.url = url;
            this.pin = pin;
        }

        public JSONObject getDataJSON() {
            return dataJSON;
        }

        public void setDataJSON(JSONObject dataJSON) {
            this.dataJSON = dataJSON;
        }

        public List<NameValuePair> getDataPairs() {
            return dataPairs;
        }

        public void setDataPairs(List<NameValuePair> dataPairs) {
            this.dataPairs = dataPairs;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPin() {
            return pin;
        }

        public void setPin(String pin) {
            this.pin = pin;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public WebCallback getCallback() {
            return callback;
        }

        public void setCallback(WebCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = "";
            try {
                if (method == Method.GET) {
                    response = getHtml(url, dataPairs, user, pin);
                }
                else if(method == Method.POST){
                    response = postHtml(url, dataPairs, user, pin);
                }
                else if(method == Method.POSTJSON){
                    response = postJsonHtml(url, dataJSON, user, pin);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if(callback != null) {
                callback.run(response);
            }
        }
    }
}
