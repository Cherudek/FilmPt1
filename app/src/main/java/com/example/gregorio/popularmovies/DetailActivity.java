package com.example.gregorio.popularmovies;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gregorio.popularmovies.adapters.ReviewAdapter;
import com.example.gregorio.popularmovies.adapters.TrailerAdapter;
import com.example.gregorio.popularmovies.data.FilmContract;
import com.example.gregorio.popularmovies.loaders.ReviewLoader;
import com.example.gregorio.popularmovies.loaders.TrailerLoader;
import com.example.gregorio.popularmovies.models.Film;
import com.example.gregorio.popularmovies.models.Review;
import com.example.gregorio.popularmovies.models.Trailer;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailActivity extends AppCompatActivity implements TrailerAdapter.TrailerAdapterOnClickHandler {

    final static String API_KEY_PARAM = "api_key";
    final static String API_KEY = BuildConfig.API_KEY;
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final int FILM_REVIEWS_LOADER_ID = 2;
    private static final int FILM_TRAILERS_LOADER_ID = 5;
    private static final String FILM_API_REQUEST_URL = "https://api.themoviedb.org/3/movie";
    private static final String FILM_REVIEWS = "reviews";
    private static final String FILM_TRAILERS = "trailers";

    boolean mIsFavourite = false;

    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.posterDisplay)
    ImageView mImageDisplay;
    @BindView(R.id.plot)
    TextView mPlot;
    @BindView(R.id.user_rating)
    TextView mUserRating;
    @BindView(R.id.release_date)
    TextView mReleaseDate;
    @BindView(R.id.add_to_favourites)
    ToggleButton mAddToFavourites;
    @BindView(R.id.rv_review)
    RecyclerView rvListView;
    @BindView(R.id.rv_trailers)
    RecyclerView rvTrailers;
    @BindView(R.id.detail__error_message_display)
    TextView mErrorMessageDisplay;
    @BindView(R.id.detail_loading_spinner)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.trailer_loading_spinner)
    ProgressBar mTrailerLoadingIndicator;
    SharedPreferences sharedPref;
    @BindView(R.id.scrollViewDetail)
    ScrollView mScrollView;
    /**
     * Cursor for checking if the film in the details view is in the favourite Films database.
     */
    private Cursor mCursor;
    private Uri mFavouriteFilmUri;
    /**
     * Content URI for the existing film (null if it's a new record)
     */
    private Uri mCurrentFilmUri;
    private String mCurrentFilmUriString;
    private String filmID;
    private String filmTitle;
    private String plot;
    private String releaseDate;
    private String poster;
    private String rating;

    private LinearLayoutManager reviewsLayoutManager;
    private LinearLayoutManager trailerLayoutManager;

    private ReviewAdapter mReviewsAdapter;
    private TrailerAdapter mTrailersAdapter;

    private int numberOfReviews;
    private int numberOfTrailers;

    private Context mContext;
    LoaderManager.LoaderCallbacks<List<Trailer>> trailerLoader = new LoaderManager.LoaderCallbacks<List<Trailer>>() {

        @Override
        public Loader<List<Trailer>> onCreateLoader(int id, Bundle args) {
            //Uri builder to pass on the JSON query request
            Uri baseUri = Uri.parse(FILM_API_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            Uri.Builder baseAndfilmId = uriBuilder.appendEncodedPath(filmID);
            Uri.Builder filmIdTrailers = baseAndfilmId.appendEncodedPath(FILM_TRAILERS);
            Uri.Builder baseAndKey = filmIdTrailers.appendQueryParameter(API_KEY_PARAM, API_KEY);

            mContext = getApplicationContext();

            //returns a url string for the QueryMovieUtils background task
            Log.i(LOG_TAG, "URI is: " + baseAndKey);
            return new TrailerLoader(mContext, baseAndKey.toString());
        }

        @Override
        public void onLoadFinished(Loader<List<Trailer>> loader, List<Trailer> data) {
            mTrailerLoadingIndicator.setVisibility(View.GONE);

            // If there is a valid list of {@link movie}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (data != null && !data.isEmpty()) {
                numberOfTrailers = data.size();
                mTrailersAdapter.addAll(data);
                showTrailersDataView();
            } else {
                showTrailerErrorMessage();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Trailer>> loader) {
            mTrailersAdapter.clear();
        }
    };
    private Parcelable mStateParcel;
    private String SAVED_STATE_KEY;
    private String REVIEWS_STATE_KEY;
    private String TRAILERS_STATE_KEY;
    private Film object;
    private LoaderManager.LoaderCallbacks<List<Review>> reviewsLoader = new LoaderManager.LoaderCallbacks<List<Review>>() {

        @Override
        public Loader<List<Review>> onCreateLoader(int id, Bundle args) {

            //Uri builder to pass on the JSON query request
            Uri baseUri = Uri.parse(FILM_API_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            Uri.Builder baseAndfilmId = uriBuilder.appendEncodedPath(filmID);
            Uri.Builder filmIdReviews = baseAndfilmId.appendEncodedPath(FILM_REVIEWS);
            Uri.Builder baseAndKey = filmIdReviews.appendQueryParameter(API_KEY_PARAM, API_KEY);

            mContext = getApplicationContext();

            //returns a url string for the QueryMovieUtils background task
            Log.i(LOG_TAG, "URI is: " + baseAndKey);
            return new ReviewLoader(mContext, baseAndKey.toString());
        }

        @Override
        public void onLoadFinished(Loader<List<Review>> loader, List<Review> data) {

            mLoadingIndicator.setVisibility(View.GONE);

            // If there is a valid list of {@link movie}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (data != null && !data.isEmpty()) {
                numberOfReviews = data.size();
                mReviewsAdapter.addAll(data);
                showMovieDataView();
            } else {
                showErrorMessage();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Review>> loader) {
            // Loader reset, so we can clear out our existing data.
            mReviewsAdapter.clear();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        object = getIntent().getParcelableExtra(Intent.EXTRA_TEXT);

        filmTitle = object.getmTitle();
        filmID = object.getmId();
        plot = object.getmPlot();
        releaseDate = object.getmReleaseDate();
        poster = object.getmThumbnail();
        rating = object.getmUserRating();

        mCurrentFilmUriString = FilmContract.favouriteFilmEntry.CONTENT_URI + "/" + filmID;

        mTitle.setText(filmTitle);
        mPlot.setText(plot);
        mReleaseDate.setText(releaseDate);
        Picasso.get().load("http://image.tmdb.org/t/p/w342/" + poster).into(mImageDisplay);
        mUserRating.setText(rating);

        reviewsLayoutManager = new LinearLayoutManager(this);
        trailerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);

        // Use setLayoutManager on rvListView with the LinearLayoutManager we created above
        rvListView.setLayoutManager(reviewsLayoutManager);
        rvTrailers.setLayoutManager(trailerLayoutManager);

         /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        rvListView.setHasFixedSize(true);
        rvTrailers.setHasFixedSize(true);

        /*
         * The 2 Adapters are responsible for displaying trailers and reviews int their appropriate recyclerViews.
         */
        mReviewsAdapter = new ReviewAdapter(numberOfReviews);
        mTrailersAdapter = new TrailerAdapter(this, numberOfTrailers);

        //Set the ReviewAdapter you created on rvListView
        rvListView.setAdapter(mReviewsAdapter);
        rvTrailers.setAdapter(mTrailersAdapter);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).

            getSupportLoaderManager().initLoader(FILM_REVIEWS_LOADER_ID, null, reviewsLoader);
            getSupportLoaderManager().initLoader(FILM_TRAILERS_LOADER_ID, null, trailerLoader);

        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mLoadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mErrorMessageDisplay.setText(R.string.no_internet);

        }

        checkIfMovieIsInDatabase();

        mAddToFavourites.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isFavourite) {
                if (isFavourite) {
                    //Insert the selected film into the database
                    insertToFavourite();
                } else {
                    //Delete film from database
                    showDeleteConfirmationDialog();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();

        bundle.putParcelable(SAVED_STATE_KEY, object);
        bundle.putParcelable(TRAILERS_STATE_KEY, trailerLayoutManager.onSaveInstanceState());
        bundle.putParcelable(REVIEWS_STATE_KEY, reviewsLayoutManager.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState instanceof Bundle) {
            mStateParcel = ((Bundle) savedInstanceState).getParcelable(SAVED_STATE_KEY);
            mStateParcel = savedInstanceState.getParcelable(TRAILERS_STATE_KEY);
            mStateParcel = savedInstanceState.getParcelable(REVIEWS_STATE_KEY);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    ///method to check whether the film displayed in our DetailView is favourite film database.
    public void checkIfMovieIsInDatabase() {
        String[] projection = {FilmContract.favouriteFilmEntry.COLUMN_FILM_ID};
        String selection = FilmContract.favouriteFilmEntry.COLUMN_FILM_ID + "=? ";
        String[] selectionArgs = {filmID};

        mCursor = getContentResolver().query(FilmContract.favouriteFilmEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (mCursor.getCount() > 0) {
            mAddToFavourites.setChecked(true);
            mIsFavourite = isFavorite();
        }
        mCursor.close();
    }

    public boolean isFavorite() {
        if (mCursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void insertToFavourite() {

        ContentValues values = new ContentValues();
        values.put(FilmContract.favouriteFilmEntry.COLUMN_FILM_ID, filmID);
        values.put(FilmContract.favouriteFilmEntry.COLUMN_TITLE, filmTitle);
        values.put(FilmContract.favouriteFilmEntry.COLUMN_OVERVIEW, plot);
        values.put(FilmContract.favouriteFilmEntry.COLUMN_RELEASE_DATE, releaseDate);
        values.put(FilmContract.favouriteFilmEntry.COLUMN_POSTER_PATH, poster);
        values.put(FilmContract.favouriteFilmEntry.COLUMN_VOTE_AVERAGE, rating);


        // Determine if this is a new or existing record by checking if mCurrentFilmUri is null or not
        if (mCursor != null) {
            // This is a NEW record, so insert a new record into the provider,
            // returning the content URI for the new record.
            Uri newUri = getContentResolver().insert(FilmContract.favouriteFilmEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.insert_film_failed),
                        Toast.LENGTH_SHORT).show();

            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.insert_film_successful),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            // Otherwise this is an EXISTING record, Send a Toast Message saying this film is already
            // in your Favourites
            Toast.makeText(this, getString(R.string.film_already_added),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prompt the user to confirm that they want to delete this film from the database.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the record.
                deleteFilm();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);


        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the record in the database.
     */
    private void deleteFilm() {

        mCurrentFilmUri = Uri.parse(mCurrentFilmUriString);

        // Only perform the delete if this is an existing record.
        if (mCurrentFilmUri != null) {
            // Call the ContentResolver to delete the record at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentRecordUri
            // content URI already identifies the record that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentFilmUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.film_remove_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.film_remove_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        getSupportLoaderManager().restartLoader(FILM_REVIEWS_LOADER_ID, null, reviewsLoader);
        getSupportLoaderManager().restartLoader(FILM_TRAILERS_LOADER_ID, null, trailerLoader);


        checkIfMovieIsInDatabase();

        super.onResume();
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
        rvListView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        rvListView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void showTrailersDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        rvTrailers.setVisibility(View.VISIBLE);
    }

    private void showTrailerErrorMessage() {
        /* First, hide the currently visible data */
        rvTrailers.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(String trailerName, String trailerId) {

        Uri youtubeUrl = Uri.parse("https://www.youtube.com/watch?v=" + trailerId);
        Intent youTubeIntent = new Intent(Intent.ACTION_VIEW, youtubeUrl);
        youTubeIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        startActivity(youTubeIntent);
    }
}

