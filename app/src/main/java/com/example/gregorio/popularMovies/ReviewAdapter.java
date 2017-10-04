package com.example.gregorio.popularMovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gregorio on 03/10/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {


    private static final String TAG = ReviewAdapter.class.getSimpleName();

    private int mNumberItems;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    private List<Review> mReviewData = new ArrayList<>();

    /**
     * Constructor for ReviewAdapter that accepts a number of items to display and the specification
     * for the ListItemClickListener.
     *
     * @param numberOfItems Number of items to display in list
     */
    public ReviewAdapter(int numberOfItems) {
        mNumberItems = numberOfItems;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new NumberViewHolder that holds the View for each list item
     */
    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item_review;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        ReviewViewHolder viewHolder = new ReviewViewHolder(view);

        return viewHolder;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        Review currentReview = mReviewData.get(position);
        String reviewAuthor = currentReview.getmAuthor();
        String review = currentReview.getmReview();

        holder.listItemReview.setText(reviewAuthor);
        holder.listItemAuthor.setText(review);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param reviews
     */
    public void addAll(List<Review> reviews) {
        if (mReviewData != null)
            mReviewData.clear();
        mReviewData.addAll(reviews);
        notifyDataSetChanged();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        if (mReviewData != null)
            mReviewData.clear();
        notifyDataSetChanged();
    }


    /**
     * A class called ReviewViewHolder that extends RecyclerView.ViewHolder
     * Cache of the children views for a list item.
     */
    class ReviewViewHolder extends RecyclerView.ViewHolder {

        // Within ReviewViewHolder, create 2 x TextViews variable called listItemAuthor
        // Will display the name of the reviewer in the list, and one called listItemReview to display the actual review
        TextView listItemAuthor;
        TextView listItemReview;

        //  Create a constructor for NumberViewHolder that accepts a View called itemView as a parameter

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         *
         * @param itemView The View that you inflated in
         *                 {@link ReviewAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public ReviewViewHolder(View itemView) {
            // Within the constructor, call super(itemView) and then find listItemNumberView by ID
            super(itemView);

            listItemAuthor = (TextView) itemView.findViewById(R.id.author);
            listItemReview = (TextView) itemView.findViewById(R.id.review);
        }

        // COMPLETED (16) Within the ReviewViewHolder class, create a void method called bind that accepts an int parameter called listIndex

        /**
         * A method we wrote for convenience. This method will take an integer as input and
         * use that integer to display the appropriate text within a list item.
         *
         * @param listIndex Position of the item in the list
         */
        void bind(int listIndex) {
            // COMPLETED (17) Within bind, set the text of listItemNumberView to the listIndex
            // COMPLETED (18) Be careful to get the String representation of listIndex, as using setText with an int does something different
            listItemAuthor.setText(String.valueOf(listIndex));
            listItemReview.setText(String.valueOf(listIndex));
        }
    }
}
