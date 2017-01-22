package com.monolito.masstransit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

import com.monolito.masstransit.domain.Searchable
import java.util.ArrayList


class AutoCompleteAdapter(private val context: Context) : BaseAdapter(), Filterable {
    private var resultList: List<Searchable> = ArrayList()

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(index: Int): Searchable {
        return resultList[index]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.geo_search_result, parent, false)
        }

        (convertView!!.findViewById(R.id.geo_search_result_text) as TextView).text =
                getItem(position).getLabel()

        return convertView
    }

    override fun getFilter(): Filter {
        val filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val filterResults = Filter.FilterResults()

                if (constraint != null) {
                    val results = findResults(context, constraint.toString())
                    filterResults.values = results
                    filterResults.count = results.size
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: Filter.FilterResults?) {
                if (results != null && results.count > 0) {
                    resultList = results.values as List<Searchable>
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }

        return filter
    }

    private fun findResults(context: Context, queryText: String): List<Searchable> {
        val searchResults = ArrayList<Searchable>()
        val db = (context as MainActivity).helper

        try {
            val query = db.stopDao.queryBuilder()//
                    .where().like("name", String.format("%s%%", queryText))//
                    .prepare()
            val stops = db.stopDao.query(query)

            for (stop in stops) {
                searchResults.add(stop)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return searchResults
    }
}
