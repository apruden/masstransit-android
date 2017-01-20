package com.monolito.masstransit;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.monolito.masstransit.domain.Calendar;
import com.monolito.masstransit.domain.Route;
import com.monolito.masstransit.domain.Schedule;
import com.monolito.masstransit.domain.Shape;
import com.monolito.masstransit.domain.Stop;

import org.joda.time.LocalDate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap map;
    private DelayAutoCompleteTextView autocomplete;
    private ImageView clearAutocomplete;
    private ProgressDialog progress;
    private Timer timer = new Timer();
    private Map<LatLng, Stop> markers = new HashMap<>();

    private DatabaseHelper databaseHelper = null;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView navTextHeader;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    public DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }

        return databaseHelper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navTextHeader = (TextView) navigationView.getHeaderView(0).findViewById(R.id.navTextHeader);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        clearAutocomplete = (ImageView) findViewById(R.id.geo_autocomplete_clear);
        autocomplete = (DelayAutoCompleteTextView) findViewById(R.id.geo_autocomplete);
        autocomplete.setAdapter(new AutoCompleteAdapter(this));
        autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Stop result = (Stop) adapterView.getItemAtPosition(position);
                autocomplete.setText(result.getLabel());
                moveMap(result.getLat(), result.getLon());
                hideSoftKeyboard();
            }
        });

        autocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0)
                    clearAutocomplete.setVisibility(View.VISIBLE);
                else
                    clearAutocomplete.setVisibility(View.GONE);
            }
        });

        clearAutocomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autocomplete.setText("");
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /*//noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        progress = ProgressDialog.show(this, "dialog title",
                "dialog message", true);

        new StartupDatabase().execute();
    }

    private void moveMap(double lat, double lon) {
        LatLng position = new LatLng(lat, lon);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.setMapType(GoogleMap.MAP_TYPE_NONE);
        map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(this)));
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
               /* Intent intent = new Intent(MainActivity.this, StopRoutesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("stop", marker.getTitle());
                intent.putExtras(bundle);

                startActivity(intent);*/
                openDrawer(markers.get(marker.getPosition()));

                return false;
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                timer.purge();

                final double maxLat = map.getProjection().getVisibleRegion().latLngBounds.northeast.latitude;
                final double minLat = map.getProjection().getVisibleRegion().latLngBounds.southwest.latitude;

                final double maxLon = map.getProjection().getVisibleRegion().latLngBounds.northeast.longitude;
                final double minLon = map.getProjection().getVisibleRegion().latLngBounds.southwest.longitude;

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        new GetStopsTask().execute(minLat, maxLat, minLon, maxLon);
                    }
                }, 500);
            }
        });

        moveMap(45.495063, -73.699265);
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autocomplete.getWindowToken(), 0);
    }

    private void openDrawer(Stop stop) {
        //http://stackoverflow.com/questions/32419446/adding-expandablelistview-to-navigationview
        navTextHeader.setText(stop.getLabel());
        drawerLayout.openDrawer(GravityCompat.START);
        new LoadSchedule().execute(stop);
    }

    private class RouteSchedule {
        private final String route;
        private final List<String> schedule;

        RouteSchedule(String route, List<String> schedule) {
            this.route = route;
            this.schedule = schedule;
        }

        String getRoute() {
            return route;
        }

        List<String> getSchedule() {
            return schedule;
        }
    }

    private class LoadSchedule extends AsyncTask<Stop, Void, List<RouteSchedule>> {

        @Override
        protected List<RouteSchedule> doInBackground(Stop... stops) {
            Stop stop = stops[0];
            String[] routes = stop.getRoutes().split(",");
            List<RouteSchedule> routeSchedules = new ArrayList<>();

            try {
                java.util.Calendar c = java.util.Calendar.getInstance();
                LocalDate d = new LocalDate(2016, 7, 3, null);
                String date = String.format(
                        "%04d%02d%02d",
                        d.getYear(),
                        d.getMonthOfYear(),
                        d.getDayOfMonth());
                Calendar calendar = getHelper().getCalendarDao().queryBuilder()
                        .where().eq("date", date)
                        .queryForFirst();
                List<Schedule> schedules = new ArrayList<>();

                if (calendar != null)
                    schedules = getHelper().getScheduleDao().queryBuilder()
                            .where().eq("stop", stop.getId())
                            .and().in("code", routes)
                            .and().eq("service", calendar.getCode())
                            .query();

                if(schedules.size() < routes.length && calendar != null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("http://10.0.2.2:8888")
                            .addConverterFactory(JacksonConverterFactory.create())
                            .build();

                    MassTransitService service = retrofit.create(MassTransitService.class);

                    for(String route: routes) {
                        try {
                            schedules = service.listSchedules(stop.getId(), route).execute().body();
                            insertEntities(schedules, getHelper().getScheduleDao());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    schedules = getHelper().getScheduleDao().queryBuilder()
                            .where().eq("stop", stop.getId())
                            .and().in("code", routes)
                            .and().eq("service", calendar.getCode())
                            .query();
                }


                String now = String.format("%02d:%02d:00", c.get(c.HOUR_OF_DAY), c.get(c.MINUTE));

                for (Schedule sch : schedules) {
                    List<String> times = Arrays.asList(sch.getTimes().split(","));
                    List<String> res = new ArrayList<>();

                    for (String t : times) {
                        if (t.compareTo(now) > 0 && res.size() < 10) res.add(t);
                    }

                    Collections.sort(res);

                    routeSchedules.add(new RouteSchedule(sch.getCode(), res));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return routeSchedules;
        }

        @Override
        protected void onPostExecute(List<RouteSchedule> res) {
            Menu men = navigationView.getMenu();
            men.clear();

            for(RouteSchedule rs : res) {
                SubMenu sm = men.addSubMenu(rs.getRoute());
                //MenuItem it = sm.add("Favorite");
                //it.setIcon(R.drawable.ic_menu_camera);
                for(String time: rs.getSchedule()) {
                    sm.add(time);
                }
            }
        }
    }

    private void openDrawer(Route route) {
    }

    private Context getContext() {
        return this;
    }

    private class StartupDatabase extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... voids) {
            Log.e("Db", "starting db.");
            try {
                long count = getHelper().getStopDao().countOf();
                if(count == 0) return null;

                return true;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object res) {
            if (res == null) new HttpRequestTask().execute();
            else progress.dismiss();
        }
    }

    private class GetStopsTask extends AsyncTask<Double, Void, List<Stop>> {

        @Override
        protected List<Stop> doInBackground(Double... voids) {
            Log.e("Db", "drawing stops.");

            try {
                return getHelper().getStopDao().queryBuilder()
                        .where().ge("lat", voids[0]).and()
                        .le("lat", voids[1]).and()
                        .ge("lon", voids[2]).and()
                        .ge("lon", voids[3]).query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<Stop> res) {
            for(Stop stop: res) {
                LatLng pos = new LatLng(stop.getLat(), stop.getLon());

                if(!markers.containsKey(pos)) {
                    map.addMarker(new MarkerOptions().position(pos).title(stop.getName()));
                    markers.put(pos, stop);
                }
            }
        }
    }

    public interface MassTransitService {
        @GET("stops")
        Call<List<Stop>> listStops();

        @GET("routes")
        Call<List<Route>> listRoutes();

        @GET("calendars")
        Call<List<Calendar>> listCalendars();

        @GET("schedules")
        Call<List<Schedule>> listSchedules(@Query("stop") String stop, @Query("code") String code);

        @GET("shapes")
        Call<List<Shape>> listShapes();
    }

    private <T> void insertEntities(final List<T> entities, final Dao<T, Integer> dao) {
        try {
            dao.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(T entity: entities) {
                        try {
                            dao.createOrUpdate(entity);
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
    }

    private class HttpRequestTask extends AsyncTask<Void, Void, Object> {

        @Override
        protected List<Stop> doInBackground(Void... params) {
            Log.i("Db", "requesting data.");

            try {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://10.0.2.2:8888")
                        .addConverterFactory(JacksonConverterFactory.create())
                        .build();

                MassTransitService service = retrofit.create(MassTransitService.class);
                final List<Stop> stops = service.listStops().execute().body();
                final Dao<Stop, Integer> dao = getHelper().getStopDao();
                insertEntities(stops, dao);

                final List<Calendar> calendars = service.listCalendars().execute().body();
                final Dao<Calendar, Integer> cdao = getHelper().getCalendarDao();
                insertEntities(calendars, cdao);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object res) {
            //if (stops == null) return;

            //new InsertTask().execute(stops);
            progress.dismiss();
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
