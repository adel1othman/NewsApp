package com.example.android.newsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Adel on 5/29/2017.
 */

public class NewsAdapter extends ArrayAdapter<News> {

    public NewsAdapter(Context context, List<News> newses) {
        super(context, 0, newses);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        News currentNews = getItem(position);

        TextView sectionView = (TextView) listItemView.findViewById(R.id.txtSection);
        sectionView.setText(currentNews.getmSectionName());

        TextView titleView = (TextView) listItemView.findViewById(R.id.txtTitle);
        titleView.setText(currentNews.getmTitle());

        TextView publishView = (TextView) listItemView.findViewById(R.id.txtPublish);
        publishView.setText(currentNews.getmPublicationDate());

        TextView authorView = (TextView)listItemView.findViewById(R.id.txtAuthor);
        authorView.setText(currentNews.getmAuthor());

        return listItemView;
    }
}