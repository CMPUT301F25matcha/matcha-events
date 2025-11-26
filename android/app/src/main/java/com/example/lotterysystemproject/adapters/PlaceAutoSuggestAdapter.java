package com.example.lotterysystemproject.adapters;

import android.content.Context;
import android.util.Log;
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
    private final List<AutocompletePrediction> predictionList;

    public PlaceAutoSuggestAdapter(Context context, int resId) {
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

    public String getPlaceId(int pos) {
        return predictionList.get(pos).getPlaceId();
    }

    // This function updates the list and forces the dropdown to show
    public void setData(List<AutocompletePrediction> predictions) {
        this.results.clear();
        this.predictionList.clear();
        for (AutocompletePrediction prediction : predictions) {
            this.results.add(prediction.getFullText(null).toString());
            this.predictionList.add(prediction);
        }
        Log.d("PlacesDebug", "Adapter: Data updated with " + results.size() + " items.");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // We are managing data externally via the Fragment's API call,
                // so we just return the current results to keep the dropdown open.
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.values = results;
                    filterResults.count = results.size();
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
}