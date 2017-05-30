package com.example.android.newsapp;

/**
 * Created by Adel on 5/29/2017.
 */

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {
    }

    public static List<News> fetchNewsData(String requestUrl) {
        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        List<News> newses = extractFeatureFromJson(jsonResponse);

        return newses;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<News> extractFeatureFromJson(String newsJSON) {
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        List<News> newses = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            if (!baseJsonResponse.isNull("response")){
                JSONObject responseInfo = baseJsonResponse.getJSONObject("response");
                JSONArray newsArray = responseInfo.getJSONArray("results");

                for (int i = 0; i < newsArray.length(); i++) {

                    JSONObject currentNews = newsArray.getJSONObject(i);

                    if (!currentNews.isNull("sectionName") && !currentNews.isNull("webTitle")){
                        String section = currentNews.getString("sectionName");

                        String publish = currentNews.getString("webPublicationDate");

                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        DateFormat ndf = new SimpleDateFormat("LLL dd, yyyy");
                        DateFormat tf = new SimpleDateFormat("H:mm");

                        String myTime = "";
                        if (publish.contains("T")){
                            int tPos = -1;
                            for (int l = 0; l < publish.length(); l++){
                                if (publish.toCharArray()[l] == 'T'){
                                    tPos = l;
                                    break;
                                }
                            }
                            if (tPos != -1){
                                for (int m = tPos + 1; m < publish.length() - 1; m++){
                                    myTime += publish.toCharArray()[m];
                                }
                            }
                        }

                        Date startDate, startTime;
                        try {
                            startDate = df.parse(publish);
                            String newDateString = ndf.format(startDate);

                            startTime = tf.parse(myTime);
                            String newTimeString = tf.format(startTime);

                            publish = newDateString + " " + newTimeString;
                        } catch (ParseException e) {
                            Log.e("DateTime", "Problem parsing Date or Time", e);
                        }

                        String title = currentNews.getString("webTitle");

                        String webURL = "https://www.theguardian.com/international";
                        if (!currentNews.isNull("webUrl")){
                            webURL = currentNews.getString("webUrl");
                        }

                        String author = "";

                        JSONArray tagsArray = currentNews.getJSONArray("tags");
                        for (int j = 0; j < tagsArray.length(); j++){
                            JSONObject currentTag = tagsArray.getJSONObject(j);

                            if (!currentTag.isNull("firstName")){
                                String firstName = currentTag.getString("firstName");
                                author += firstName + " ";
                            }
                            if (!currentTag.isNull("lastName")){
                                String lastName = currentTag.getString("lastName");
                                author += lastName;
                            }
                        }

                        News news = new News(section, publish, title, webURL, author);

                        newses.add(news);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the news JSON results", e);
        }

        return newses;
    }

}