package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private String REQUEST_URL;

    private static final int News_LOADER_ID = 1;

    private NewsAdapter mAdapter;

    private TextView mEmptyStateTextView;
    EditText search;
    ListView newsListView;
    ProgressBar loadingIndicator;
    LoaderManager.LoaderCallbacks<List<News>> myCallbacks;
    private Handler mHandler;
    String query = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        newsListView = (ListView) findViewById(R.id.newsList);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);

        myCallbacks = new LoaderManager.LoaderCallbacks<List<News>>() {
            @Override
            public Loader<List<News>> onCreateLoader(int id, Bundle args) {
                loadingIndicator.setVisibility(View.VISIBLE);
                return new NewsLoader(getBaseContext(), REQUEST_URL);
            }

            @Override
            public void onLoadFinished(Loader<List<News>> loader, List<News> newses) {
                loadingIndicator.setVisibility(View.GONE);

                mAdapter.clear();

                if (newses != null && !newses.isEmpty()) {
                    mEmptyStateTextView.setVisibility(View.GONE);
                    mAdapter.addAll(newses);
                }else {
                    mEmptyStateTextView.setText(R.string.no_news);
                    newsListView.setEmptyView(mEmptyStateTextView);
                }
            }

            @Override
            public void onLoaderReset(Loader<List<News>> loader) {
                loadingIndicator.setVisibility(View.GONE);
                mAdapter.clear();
            }
        };

        loadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        startRepeatingTask();

        search = (EditText)findViewById(R.id.etSearch);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            private Timer timer = new Timer();
            private final long DELAY = 1000;

            @Override
            public void afterTextChanged(final Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        query = wordsCounter(s.toString());
                                        requestManager(query);
                                        /*stopRepeatingTask();
                                        startRepeatingTask();*/
                                    }
                                });
                            }
                        },
                        DELAY
                );
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            requestManager(query);
            int mInterval = 600000;
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    public void requestManager(String srch){
        if (mAdapter != null){
            mAdapter.clear();
        }

        if (srch.isEmpty()){
            REQUEST_URL = "http://content.guardianapis.com/search?api-key=test&show-tags=contributor";
        }else {
            REQUEST_URL = "http://content.guardianapis.com/search?api-key=test&show-tags=contributor&q=" + query;
        }

        mAdapter = new NewsAdapter(getBaseContext(), new ArrayList<News>());

        newsListView.setAdapter(mAdapter);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                News currentNews = mAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNews.getmWebUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(websiteIntent);
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            mEmptyStateTextView.setVisibility(View.GONE);
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.restartLoader(News_LOADER_ID, null, myCallbacks);
        } else {
            loadingIndicator.setVisibility(View.GONE);

            mEmptyStateTextView.setText(R.string.no_internet_connection);
            newsListView.setEmptyView(mEmptyStateTextView);
        }
    }

    public String wordsCounter(String input)
    {
        String word = "";
        ArrayList<String> words = new ArrayList<>();
        int i = 0;

        for (char item : input.toCharArray()) {
            i++;
            if (!Character.isWhitespace(item)) {
                word += item;
                if (i == input.length())
                {
                    words.add(word);
                }
            }
            else {
                if (!word.isEmpty())
                {
                    words.add(word);
                }
                word = "";
            }
        }
        return TextUtils.join("+", words);
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
