package com.example.lotterysystemproject.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import java.util.ArrayList;
import java.util.List;

public class PlaceAutoSuggestAdapter extends ArrayAdapter<String> implements Filterable {
    private final List<String> results;
    // We store the actual Prediction objects to retrieve the Place ID later
    private final List<AutocompletePrediction> predictionList;

    public PlaceAutoSuggestAdapter(Context context, int resId, PlacesClient placesClient) {
        super(context, resId);
        this.results = new ArrayList<>();
        this.predictionList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public String getItem(int pos) {
        return results.get(pos);
    }

    // Helper method to get the Place ID for a specific position
    public String getPlaceId(int pos) {
        return predictionList.get(pos).getPlaceId();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Create the request to Google Places
                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                            .setQuery(constraint.toString())
                            .build();

                    // We use a Task API, but Filter expects synchronous return.
                    // For simplicity in UI responsiveness, we usually process this asynchronously.
                    // However, standard Android Filters run on a background thread automatically.

                    // NOTE: Implementation logic normally involves waiting for the Task.
                    // But passing data back via the UI thread is safer.
                    // Below is a simplified handling for the Adapter logic:

                    try {
                        // This logic is handled better by attaching a listener in the Fragment
                        // However, to keep the Adapter self-contained, we return empty here
                        // and notify data change when the API responds (see Step 3 logic).
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    // Method to update data from the Fragment
    public void setData(List<AutocompletePrediction> predictions) {
        this.results.clear();
        this.predictionList.clear();
        for (AutocompletePrediction prediction : predictions) {
            this.results.add(prediction.getFullText(null).toString());
            this.predictionList.add(prediction);
        }
        notifyDataSetChanged();
    }
}
