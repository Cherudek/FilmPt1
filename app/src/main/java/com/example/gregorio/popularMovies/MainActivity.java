package com.example.gregorio.popularMovies;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements FilmAdapter.FilmAdapterOnClickHandler, LoaderManager.LoaderCallbacks<List<Film>> {

    final static String API_KEY_PARAM = "api_key";

    final static String EXTRA_TEXT_TITLE = "com.example.gregorio.filmpt1.EXTRA_TEXT_TITLE";

    final static String EXTRA_TEXT_RELEASE_DATE = "com.example.gregorio.filmpt1.EXTRA_TEXT_RELEASE_DATE";

    final static String EXTRA_TEXT_USER_RATING = "com.example.gregorio.filmpt1.EXTRA_TEXT_USER_RATING";

    final static String EXTRA_TEXT_PLOT = "com.example.gregorio.filmpt1.EXTRA_TEXT_PLOT";

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int FILM_LOADER_ID = 1;

    private static final String API_KEY = BuildConfig.API_KEY;

    private static final String MOVIE_DB_API_REQUEST_URL = "http://api.themoviedb.org/3/movie/";

    private RecyclerView recyclerView;

    private GridLayoutManager layoutManager;

    private FilmAdapter mFilmAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private int numberOFMovies;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mFilmAdapter = new FilmAdapter(this, numberOFMovies);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        //Checks the phone orientation
        int orientation = this.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            //code for portrait mode
            layoutManager = new GridLayoutManager(MainActivity.this, 2);
        } else {
            //code for landscape mode
            layoutManager = new GridLayoutManager(MainActivity.this, 4);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mFilmAdapter);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_spinner);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(FILM_LOADER_ID, null, this);

        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mLoadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mErrorMessageDisplay.setText(R.string.no_internet);
        }
    }


    //restart the loader to check whether we have a new sort-by value coming from the settings menu
    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(FILM_LOADER_ID, null, this);

    }


    @Override
    public Loader<List<Film>> onCreateLoader(int i, Bundle bundle) {

        //onCreateLoader() method to read the user’s latest preferences for the sort criteria,
        //construct a proper URI with their preference, and then create a new Loader for that URI.
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Get the selected preference parameter from the SettingsActivity and pass it on to the uri builder
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        //Uri builder to pass on the JSON query request
        Uri baseUri = Uri.parse(MOVIE_DB_API_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        Uri.Builder baseAndOrderBy = uriBuilder.appendEncodedPath(orderBy);
        Uri.Builder baseAndKey = baseAndOrderBy.appendQueryParameter(API_KEY_PARAM, API_KEY);

        //Switch statement to update the Activity title depending on the Order By criteria:
        //Sort by popular movies or by Top Rated
        switch (orderBy) {
            case "popular":
                getSupportActionBar().setTitle("Popular Movies");
                break;
            case "top_rated":
                getSupportActionBar().setTitle("Top Rated Movies");
                break;
        }

        //returns a url string for the QueryUtils background task
        Log.i(LOG_TAG, "URI is: " + baseAndKey);
        return new FilmLoader(this, baseAndKey.toString());


    }


    @Override
    public void onLoadFinished(Loader<List<Film>> loader, List<Film> movies) {
        Log.v(LOG_TAG, "TEST: Loader Cleared");

        // Hide loading indicator because the data has been loaded
        mLoadingIndicator.setVisibility(View.GONE);

        // If there is a valid list of {@link movie}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (movies != null && !movies.isEmpty()) {
            numberOFMovies = movies.size();
            mFilmAdapter.addAll(movies);
            showMovieDataView();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Film>> loader) {
        // Loader reset, so we can clear out our existing data.
        mFilmAdapter.clear();
    }


    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        recyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


    //putExtra information fields to pass on to the detail activity
    @Override
    public void onClick(String filmPoster, String FilmTitle, String releaseDate, String userRating, String filmPlot) {
        Context context = this;
        Class destinationClass = DetailActivity.class;

        Film dataToSend = new Film();

        Intent intentToStartDetailActivity = new Intent(context, destinationClass);

        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, dataToSend);

        Log.i(LOG_TAG, "Data to send " + dataToSend);

        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, filmPoster);
        intentToStartDetailActivity.putExtra(EXTRA_TEXT_TITLE, FilmTitle);
        intentToStartDetailActivity.putExtra(EXTRA_TEXT_RELEASE_DATE, releaseDate);
        intentToStartDetailActivity.putExtra(EXTRA_TEXT_USER_RATING, userRating);
        intentToStartDetailActivity.putExtra(EXTRA_TEXT_PLOT, filmPlot);

        startActivity(intentToStartDetailActivity);
    }


    //Settings menu set Up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void setsharedPreference (FilmAdapter mFilmAdapter, SharedPreferences sharedPreferences, String key){
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        recyclerView.setAdapter(mFilmAdapter);
//        key = sharedPreferences.getString(toString(R.string.settings_order_by_key));
//    }
//
//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if(key.equals(getString(R.string.settings_order_by_key))){
//            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
//            recyclerView
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
//    }
}



