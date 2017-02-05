package com.monolito.masstransit

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.support.multidex.MultiDex
import android.util.Log
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.j256.ormlite.dao.Dao
import com.lapism.searchview.SearchView
import com.monolito.masstransit.domain.*

import org.joda.time.LocalDate

import java.io.IOException
import java.sql.SQLException
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class CustomExpandableListAdapter(val context: Context,
                                  val expandableListTitle: List<String>,
                                  val expandableListDetail: HashMap<String, List<String>>):
        BaseExpandableListAdapter() {
    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.expandableListDetail[
                this.expandableListTitle[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(listPosition: Int, expandedListPosition: Int,
                             isLastChild: Boolean, convertViewIn: View?, parent: ViewGroup): View? {
        var convertView = convertViewIn

        if (convertView == null) {
            val layoutInflater =
                    this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_item, null)
        }

        val expandedListText = getChild(listPosition, expandedListPosition)
        val expandedListTextView = convertView!!.findViewById(R.id.expandedListItem) as TextView
        expandedListTextView.text = expandedListText as String

        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int =
            this.expandableListDetail[this.expandableListTitle[listPosition]]!!.size

    override fun getGroup(listPosition: Int): Any {
        return this.expandableListTitle[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.expandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean,
                             convertViewIn: View?, parent: ViewGroup) : View? {
        var convertView = convertViewIn
        val listTitle = getGroup(listPosition) as String

        if (convertView == null) {
            val layoutInflater =
                    this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_group, null)
        }

        val listTitleTextView = convertView!!.findViewById(R.id.listTitle) as TextView
        listTitleTextView.setTypeface(null, Typeface.BOLD)
        listTitleTextView.text = listTitle
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}


class MyAdapter(val mDataset: Array<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.my_text_view, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mDataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.mTextView.text = mDataset[position]
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val mTextView: TextView = v.findViewById(R.id.expandedListItem) as TextView
    }
}

class MainActivity : AppCompatActivity(),
        OnMapReadyCallback {
    private var map: GoogleMap? = null
    private var autocomplete: DelayAutoCompleteTextView? = null
    //private var clearAutocomplete: ImageView? = null
    private var progress: ProgressDialog? = null
    private val timer = Timer()
    private val markers = HashMap<LatLng, Stop>()

    lateinit var helper: DatabaseHelper
    private var drawerLayout: DrawerLayout? = null
    //private var navigationView: NavigationView? = null
    //private var navTextHeader: TextView? = null
    private var expandableListView: ExpandableListView? = null

    override fun onDestroy() {
        super.onDestroy()
        OpenHelperManager.releaseHelper()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        helper = OpenHelperManager.getHelper(this, DatabaseHelper::class.java)
        setContentView(R.layout.activity_main)
        //val toolbar = findViewById(R.id.toolbar) as Toolbar?
        drawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout?


        mRecyclerView = findViewById(R.id.my_recycler_view) as RecyclerView?
        mRecyclerView!!.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.setLayoutManager(mLayoutManager)
        mAdapter = MyAdapter(arrayOf("test", "tata", "toto"))
        mRecyclerView!!.setAdapter(mAdapter)


        //setSupportActionBar(toolbar)

        /*val fab = findViewById(R.id.fab) as FloatingActionButton?
        fab!!.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }*/

        //val drawer = findViewById(R.id.drawer_layout) as DrawerLayout?
        //drawer!!.addDrawerListener()

        /*val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer!!.addDrawerListener(toggle)
        toggle.syncState()*/

        /*navigationView = findViewById(R.id.nav_view) as NavigationView?
        navigationView!!.setNavigationItemSelectedListener(this)
        navTextHeader =
                navigationView!!.getHeaderView(0).findViewById(R.id.navTextHeader) as TextView*/

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val mSearchView = findViewById(R.id.searchView) as SearchView
        val searchAdapter = AutoCompleteAdapter(context)
        searchAdapter.addOnItemClickListener(object : AutoCompleteAdapter.OnItemClickListener{
            override fun onItemClick(view: View, position: Int, s: Searchable?) {
                val textView = view.findViewById(R.id.textView_item_text) as TextView
                val result = s as Stop
                moveMap(result.lat, result.lon)
                mSearchView.close(false)
            }
        })

        mSearchView.adapter = searchAdapter
        mSearchView.setOnOpenCloseListener(object: SearchView.OnOpenCloseListener {
            override fun onOpen(): Boolean {
                return true
            }

            override fun onClose(): Boolean {
                return true
            }
        })

        mSearchView.setOnMenuClickListener { drawerLayout!!.openDrawer(GravityCompat.START) }

        /*clearAutocomplete = findViewById(R.id.geo_autocomplete_clear) as ImageView?
        autocomplete = findViewById(R.id.geo_autocomplete) as DelayAutoCompleteTextView?
        autocomplete!!.setAdapter(AutoCompleteAdapter(this))
        autocomplete!!.onItemClickListener =
                AdapterView.OnItemClickListener { adapterView, view, position, id ->
            val result = adapterView.getItemAtPosition(position) as Stop
            autocomplete!!.setText(result.getLabel())
            moveMap(result.lat, result.lon)
            hideSoftKeyboard()
        }

        autocomplete!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty())
                    clearAutocomplete!!.visibility = View.VISIBLE
                else
                    clearAutocomplete!!.visibility = View.GONE
            }
        })

        clearAutocomplete!!.setOnClickListener { autocomplete!!.setText("") }*/


        expandableListView = findViewById(R.id.expandableListView) as ExpandableListView?
        val expandableListDetail = hashMapOf<String, List<String>>()
        val expandableListTitle = expandableListDetail.keys.toList()
        val expandableListAdapter = CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail)
        expandableListView!!.setAdapter(expandableListAdapter)
        expandableListView!!.setOnGroupExpandListener { groupPosition ->
            //Toast.makeText(applicationContext, expandableListTitle.get(groupPosition) + " List Expanded.", Toast.LENGTH_SHORT).show()
        }

        expandableListView!!.setOnGroupCollapseListener { groupPosition ->
            //Toast.makeText(applicationContext, expandableListTitle.get(groupPosition) + " List Collapsed.", Toast.LENGTH_SHORT).show()
        }

        expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            //Toast.makeText(applicationContext, expandableListTitle.get(groupPosition) + " -> " + expandableListDetail.get(expandableListTitle.get(groupPosition))!!.get(childPosition), Toast.LENGTH_SHORT).show()
            false
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout?
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    */

    /*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        /*//noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item)
    }
    */

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onStart() {
        super.onStart()
        progress = ProgressDialog.show(this, "dialog title",
                "dialog message", true)

        StartupDatabase().execute()
    }

    private fun moveMap(lat: Double, lon: Double) {
        val position = LatLng(lat, lon)
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.uiSettings.isMapToolbarEnabled = false
        map!!.uiSettings.isZoomControlsEnabled = false
        map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        map!!.addTileOverlay(TileOverlayOptions().tileProvider(CustomMapTileProvider(this)))
        map!!.setOnMarkerClickListener { marker ->
            /* Intent intent = new Intent(MainActivity.this, StopRoutesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("stop", marker.getTitle());
                intent.putExtras(bundle);

                startActivity(intent);*/
            openDrawer(markers[marker.position]!!)

            false
        }

        map!!.setOnInfoWindowClickListener { drawerLayout!!.openDrawer(GravityCompat.START) }

        map!!.setOnCameraChangeListener {
            timer.purge()

            val maxLat = map!!.projection.visibleRegion.latLngBounds.northeast.latitude
            val minLat = map!!.projection.visibleRegion.latLngBounds.southwest.latitude

            val maxLon = map!!.projection.visibleRegion.latLngBounds.northeast.longitude
            val minLon = map!!.projection.visibleRegion.latLngBounds.southwest.longitude

            timer.schedule(object : TimerTask() {
                override fun run() {
                    GetStopsTask().execute(minLat, maxLat, minLon, maxLon)
                }
            }, 500)
        }

        moveMap(45.495063, -73.699265)
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(autocomplete!!.windowToken, 0)
    }

    private fun openDrawer(stop: Stop) {
        //navTextHeader!!.text = stop.getLabel()
        drawerLayout!!.openDrawer(GravityCompat.START)
        LoadSchedule().execute(stop)
    }

    private inner class RouteSchedule internal constructor(
            internal val route: String, internal val schedule: List<String>)

    private inner class LoadSchedule : AsyncTask<Stop, Void, List<RouteSchedule>>() {

        override fun doInBackground(vararg stops: Stop): List<RouteSchedule> {
            val (id, lat, lon, name, code, routes1) = stops[0]

            val routes = routes1.split(",".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            val routeSchedules = ArrayList<RouteSchedule>()

            try {
                val c = java.util.Calendar.getInstance()
                val d = LocalDate(2016, 7, 3, null)
                val date = String.format(
                        "%04d%02d%02d",
                        d.year,
                        d.monthOfYear,
                        d.dayOfMonth)
                val calendar = helper.calendarDao.queryBuilder()
                        .where().eq("date", date)
                        .queryForFirst()
                var schedules: List<Schedule> = ArrayList()

                if (calendar != null)
                    schedules = helper.scheduleDao.queryBuilder()
                            .where().eq("stop", id)
                            .and().`in`("code", *routes)
                            .and().eq("service", calendar.code)
                            .query()

                if (schedules.size < routes.size && calendar != null) {
                    val retrofit = Retrofit.Builder()
                            .baseUrl("http://10.0.2.2:8888")
                            .addConverterFactory(JacksonConverterFactory.create())
                            .build()

                    val service = retrofit.create(MassTransitService::class.java)

                    for (route in routes) {
                        try {
                            val tmp = service.listSchedules(id, route).execute()
                            schedules = tmp.body()
                            insertEntities(schedules, helper.scheduleDao)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    schedules = helper.scheduleDao.queryBuilder()
                            .where().eq("stop", id)
                            .and().`in`("code", *routes)
                            .and().eq("service", calendar.code)
                            .query()
                }


                val now = String.format("%02d:%02d:00", c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))

                for ((id1, stop, route, service, code1, times1) in schedules) {
                    val times = Arrays.asList(*times1.split(",".toRegex())
                            .dropLastWhile(String::isEmpty)
                            .toTypedArray())
                    val res = ArrayList<String>()
                    times.filter { it > now }
                            .forEach { res.add(it.substringBeforeLast(":")) }
                    Collections.sort(res)
                    routeSchedules.add(RouteSchedule(code1, res.take(10)))
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            return routeSchedules
        }

        override fun onPostExecute(res: List<RouteSchedule>?) {
            if (res == null) return

            val data = hashMapOf<String, List<String>>()
            //val men = navigationView!!.menu
            //men.clear()

            for (rs in res!!) {
                //val sm = men.addSubMenu(rs.route)
                //MenuItem it = sm.add("Favorite");
                //it.setIcon(R.drawable.ic_menu_camera);
                data[rs.route] = rs.schedule
            }

            val expandableListTitle = data.keys.toList()
            val expandableListAdapter = CustomExpandableListAdapter(applicationContext, expandableListTitle, data)
            expandableListView!!.setAdapter(expandableListAdapter)
        }
    }

    private fun openDrawer(route: Route) {}

    private val context: Context
        get() = this

    private inner class StartupDatabase : AsyncTask<Void, Void, Any>() {
        override fun doInBackground(vararg voids: Void): Any? {
            Log.e("Db", "starting db.")
            try {
                val count = helper.stopDao.countOf()
                if (count == 0L) return null

                return true
            } catch (e: Exception) {
                return null
            }

        }

        override fun onPostExecute(res: Any?) {
            if (res == null)
                HttpRequestTask().execute()
            else
                progress!!.dismiss()
        }
    }

    private inner class GetStopsTask : AsyncTask<Double, Void, List<Stop>>() {
        override fun doInBackground(vararg voids: Double?): List<Stop> {
            Log.e("Db", "drawing stops.")
            try {
                return helper.stopDao.queryBuilder()
                        .where().ge("lat", voids[0]).and()
                        .le("lat", voids[1]).and()
                        .ge("lon", voids[2]).and()
                        .ge("lon", voids[3]).query()
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            return ArrayList()
        }

        override fun onPostExecute(res: List<Stop>) {
            for (stop in res) {
                val pos = LatLng(stop.lat, stop.lon)

                if (!markers.containsKey(pos)) {
                    map!!.addMarker(MarkerOptions().position(pos).title(stop.name))
                    markers.put(pos, stop)
                }
            }
        }
    }

    interface MassTransitService {
        @GET("stops")
        fun listStops(): Call<List<Stop>>

        @GET("routes")
        fun listRoutes(): Call<List<Route>>

        @GET("calendars")
        fun listCalendars(): Call<List<Calendar>>

        @GET("schedules")
        fun listSchedules(
                @Query("stop") stop: String, @Query("code") code: String): Call<List<Schedule>>

        @GET("shapes")
        fun listShapes(): Call<List<Shape>>
    }

    private fun <T> insertEntities(entities: List<T>, dao: Dao<T, Int>) {
        try {
            dao.callBatchTasks {
                for (entity in entities) {
                    try {
                        dao.createOrUpdate(entity)
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    }

                }

                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private inner class HttpRequestTask : AsyncTask<Void, Void, Any>() {

        override fun doInBackground(vararg params: Void): List<Stop>? {
            Log.i("Db", "requesting data.")

            try {
                val retrofit = Retrofit.Builder()
                        .baseUrl("http://10.0.2.2:8888")
                        .addConverterFactory(JacksonConverterFactory.create())
                        .build()

                val service = retrofit.create(MassTransitService::class.java)
                val stops = service.listStops().execute().body()
                val dao = helper.stopDao

                insertEntities(stops, dao)

                val calendars = service.listCalendars().execute().body()
                val cdao = helper.calendarDao
                insertEntities(calendars, cdao)
            } catch (e: Exception) {
                Log.e("MainActivity", e.message, e)
            }

            return null
        }

        override fun onPostExecute(res: Any?) {
            //if (stops == null) return;

            //new InsertTask().execute(stops);
            progress!!.dismiss()
        }
    }

    /*private class InsertTask extends AsyncTask<List<Stop>, Void, Object> {

        @Override
        protected Object doInBackground(final List<Stop>... stops) {
            try {
                final Dao<Stop, Integer> dao = getHelper().getStopDao();
                dao.callBatchTasks(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for(Stop stop: stops[0]) {
                            try {
                                dao.createOrUpdate(stop);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                        return null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            progress.dismiss();

            return null;
        }
    }*/
}
