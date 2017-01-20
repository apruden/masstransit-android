package com.monolito.masstransit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.j256.ormlite.stmt.PreparedQuery;
import com.monolito.masstransit.domain.Searchable;
import com.monolito.masstransit.domain.Stop;

import java.util.ArrayList;
import java.util.List;


public class AutoCompleteAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<Searchable> resultList = new ArrayList<>();

    public AutoCompleteAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Searchable getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.geo_search_result, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.geo_search_result_text))
                .setText(getItem(position).getLabel());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    List results = findResults(context, constraint.toString());
                    filterResults.values = results;
                    filterResults.count = results.size();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }

    private List<Searchable> findResults(Context context, String queryText) {
        List<Searchable> searchResults = new ArrayList<>();
        DatabaseHelper db = ((MainActivity) context).getHelper();

        try {
            PreparedQuery<Stop> query = db.getStopDao().queryBuilder()//
                .where().like("name", String.format("%s%%", queryText))//
                .prepare();
            List<Stop> stops = db.getStopDao().query(query);

            for(Stop stop: stops) {
                searchResults.add(stop);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return searchResults;
    }
}
