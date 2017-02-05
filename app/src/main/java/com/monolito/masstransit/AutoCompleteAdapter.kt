package com.monolito.masstransit

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.lapism.searchview.SearchItem
import com.lapism.searchview.SearchView

import com.monolito.masstransit.domain.Searchable
import java.util.*


class AutoCompleteAdapter(private val context: Context) :
        RecyclerView.Adapter<AutoCompleteAdapter.ResultViewHolder>(), Filterable {
    var key = ""
    var mItemClickListeners: MutableList<OnItemClickListener>? = null

    override fun onBindViewHolder(viewHolder: ResultViewHolder?, position: Int) {
        val item = resultList.get(position)
        //viewHolder!!.icon_left.setImageResource(item.get_icon())
        viewHolder!!.icon_left.setColorFilter(SearchView.getIconColor(), PorterDuff.Mode.SRC_IN)
        viewHolder.text.typeface = Typeface.create(
                SearchView.getTextFont(), SearchView.getTextStyle())
        viewHolder.text.setTextColor(SearchView.getTextColor())
        viewHolder.s = item

        val itemText = item.getLabel() //item.get_text().toString()
        val itemTextLower = itemText!!.toLowerCase(Locale.getDefault())

        if (itemTextLower.contains(key) && !key.isEmpty()) {
            val s = SpannableString(itemText)
            s.setSpan(ForegroundColorSpan(SearchView.getTextHighlightColor()),
                    itemTextLower.indexOf(key),
                    itemTextLower.indexOf(key) + key.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            viewHolder.text.setText(s, TextView.BufferType.SPANNABLE)
        } else {
            viewHolder.text.text = item.getLabel() //item.get_text()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int):
            AutoCompleteAdapter.ResultViewHolder {
        val inflater = LayoutInflater.from(parent!!.context)
        val view = inflater.inflate(com.lapism.searchview.R.layout.search_item, parent, false)
        return ResultViewHolder(view)
    }

    override fun getItemCount(): Int {
        return resultList.size
    }

    private var resultList: List<Searchable> = ArrayList()

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getFilter(): Filter {
        val filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val filterResults = Filter.FilterResults()

                if (!constraint.isNullOrBlank()) {
                    val results = findResults(context, constraint.toString())
                    filterResults.values = results
                    filterResults.count = results.size
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
                val data: MutableList<Searchable> = ArrayList()

                if (results!!.count > 0) {
                    val result = results.values as ArrayList<*>
                    data += result.filterIsInstance<Searchable>()
                }

                if (resultList.isEmpty()) {
                    resultList = data
                    notifyDataSetChanged()
                } else {
                    val previousSize = resultList.size
                    val nextSize = data.size
                    resultList = data
                    if (previousSize == nextSize && nextSize != 0)
                        notifyItemRangeChanged(0, previousSize)
                    else if (previousSize > nextSize) {
                        if (nextSize == 0)
                            notifyItemRangeRemoved(0, previousSize)
                        else {
                            notifyItemRangeChanged(0, nextSize)
                            notifyItemRangeRemoved(nextSize - 1, previousSize)
                        }
                    } else {
                        notifyItemRangeChanged(0, previousSize)
                        notifyItemRangeInserted(previousSize, nextSize - previousSize)
                    }
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

            searchResults += stops
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return searchResults //.map { SearchItem(it.getLabel()) }
    }

    fun addOnItemClickListener(listener: OnItemClickListener) {
        addOnItemClickListener(listener, null)
    }

    fun addOnItemClickListener(listener: OnItemClickListener, position: Int?) {
        if (mItemClickListeners == null)
            mItemClickListeners = ArrayList<OnItemClickListener>()
        if (position == null)
            mItemClickListeners!!.add(listener)
        else
            mItemClickListeners!!.add(position, listener)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int, s: Searchable?)
    }


    inner class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val icon_left: ImageView = view.findViewById(
                com.lapism.searchview.R.id.imageView_item_icon_left) as ImageView
        val text: TextView = view.findViewById(
                com.lapism.searchview.R.id.textView_item_text) as TextView

        var s: Searchable? = null

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (mItemClickListeners != null) {
                for (listener in mItemClickListeners!!)
                    listener.onItemClick(v, layoutPosition, s)
            }
        }
    }
}
