package com.jose.feelagaininecuador.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jose.feelagaininecuador.R;

import java.util.List;

/**
 * Created by Jose on 11/30/2015.
 */
public class HashTagAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mHashTags;

    public HashTagAdapter(Context context, List<String> hashTags) {
        mContext = context;
        mHashTags = hashTags;
    }

    @Override
    public int getCount() {
        return mHashTags.size();
    }

    @Override
    public Object getItem(int position) {
        return mHashTags.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0; // we aren't going to use this. Tag items for easy reference
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.hashtag_card, null);
            holder = new ViewHolder();
            holder.hashTag = (TextView) convertView.findViewById(R.id.hashtag_text);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        String hashTagText = String.format("#%s", mHashTags.get(position));

        holder.hashTag.setText(hashTagText);

        return convertView;
    }

    private static class ViewHolder {
        TextView hashTag;
    }
}
