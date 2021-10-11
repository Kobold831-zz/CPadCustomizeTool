package com.saradabar.cpadcustomizetool.flagment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.R;
import com.stephentuso.welcome.WelcomePage;

public class WelScrollFragment extends Fragment implements WelcomePage.OnChangeListener {

    Button wel_no, wel_yes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.welcome_scroll, container, false);
        TextView titleView = view.findViewById(R.id.wel_scroll);
        wel_no = view.findViewById(R.id.wel_no);
        wel_yes = view.findViewById(R.id.wel_yes);

        titleView.setText(R.string.wel_terms_of_service);

        wel_no.setOnClickListener(v -> {
            getActivity().finishAffinity();
        });

        wel_yes.setOnClickListener(v -> {
            Common.Variable.START_FLAG = 1;
            getActivity().finish();
        });

        return view;
    }

    @Override
    public void onWelcomeScreenPageScrolled(int pageIndex, float offset, int offsetPixels) {
    }

    @Override
    public void onWelcomeScreenPageSelected(int pageIndex, int selectedPageIndex) {
    }

    @Override
    public void onWelcomeScreenPageScrollStateChanged(int pageIndex, int state) {
    }
}