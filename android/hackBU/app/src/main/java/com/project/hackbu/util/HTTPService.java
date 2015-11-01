package com.project.hackbu.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by alqiluo on 2015-10-31.
 */
public class HTTPService {

    public static Map<String, String> getMap(String... strings) {
        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < strings.length; i++) {
            map.put(strings[i], strings[++i]);
        }
        return map;
    }

    public static HttpResponse post(String url, Map<String, String> body) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> nameValuePair = new ArrayList<>(2);

        for(Map.Entry<String, String> entry : body.entrySet()) {
            nameValuePair.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

        return httpClient.execute(httpPost);
    }

}
