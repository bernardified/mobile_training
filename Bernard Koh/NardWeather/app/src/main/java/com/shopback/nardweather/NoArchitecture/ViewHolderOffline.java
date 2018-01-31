package com.shopback.nardweather.NoArchitecture;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.shopback.nardweather.R;

class ViewHolderOffline extends RecyclerView.ViewHolder {
    //view holder of offline items
    private TextView cityField, lastUpdatedField;

    /*
        Constructor of the entire item row and carry out view lookups to find each subview
         */
    ViewHolderOffline(View itemView) {
        super(itemView);
        cityField = itemView.findViewById(R.id.city_field);
        lastUpdatedField = itemView.findViewById(R.id.updated_field);
    }

    TextView getCityField() {
        return cityField;
    }

    TextView getLastUpdatedField() {
        return lastUpdatedField;
    }

}

