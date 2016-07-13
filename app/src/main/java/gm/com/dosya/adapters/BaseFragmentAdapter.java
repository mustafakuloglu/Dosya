/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package gm.com.dosya.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import gm.com.dosya.R;
import gm.com.dosya.models.ListItem;

public class BaseFragmentAdapter extends BaseAdapter {
    Context context;
    ArrayList<ListItem> liste;
    private LayoutInflater mInflater;
    public  BaseFragmentAdapter(Context con,ArrayList<ListItem> list)
    {
        context=con;
        liste=list;
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return liste.size();
    }

    @Override
    public Object getItem(int position) {
        return liste.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getViewTypeCount() {
        return 2;
    }

    public int getItemViewType(int pos) {
        return liste.get(pos).getSubtitle().length() > 0 ? 0 : 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View satirView;

        satirView = mInflater.inflate(R.layout.text_detail,null);
        TextView textView =
                (TextView) satirView.findViewById(R.id.textView);
        TextView valueTextView =
                (TextView) satirView.findViewById(R.id.valueTextView);

        ImageView imageView =(ImageView)satirView.findViewById(R.id.imageView);
        ListItem item = liste.get(position);
        imageView.setImageResource(item.getIcon());
        textView.setText(item.getTitle());
        valueTextView.setText(item.getSubtitle());


        return satirView;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }


}
