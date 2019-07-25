package com.uber.pickbot;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class Disabled_location extends Fragment {


    public Disabled_location() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View disabled = inflater.inflate(R.layout.fragment_disabledloaction, container, false);
        return  disabled;
    }

}
