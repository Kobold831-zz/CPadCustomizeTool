package com.saradabar.cpadcustomizetool.flagment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.saradabar.cpadcustomizetool.R;
import com.stephentuso.welcome.WelcomePage;

public class WelScrollFragment extends Fragment implements WelcomePage.OnChangeListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.welcome_scroll, container, false);
        TextView textView = view.findViewById(R.id.wel_scroll);

        textView.setText(R.string.wel_terms_of_service);

        view.findViewById(R.id.wel_no).setOnClickListener(v -> {
            getActivity().finishAffinity();
        });

        view.findViewById(R.id.wel_yes).setOnClickListener(v -> {
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