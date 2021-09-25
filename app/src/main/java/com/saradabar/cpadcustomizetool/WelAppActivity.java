package com.saradabar.cpadcustomizetool;

import android.annotation.SuppressLint;

import androidx.fragment.app.Fragment;

import com.saradabar.cpadcustomizetool.common.Common;
import com.saradabar.cpadcustomizetool.flagment.WelScrollFragment;
import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.FragmentWelcomePage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

public class WelAppActivity extends WelcomeActivity {
    @SuppressLint("ResourceAsColor")
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .bottomLayout(WelcomeConfiguration.BottomLayout.INDICATOR_ONLY)
                .defaultBackgroundColor(R.color.white)
                .page(new TitlePage(R.drawable.cpadmaterial, "CPad Customize Toolへようこそ！").titleColor(R.color.black))
                .page(new BasicPage(R.drawable.navigationbar, "ナビゲーションバー常時表示", "学習中、それ以外の時でもナビゲーションバーを表示します").descriptionColor(R.color.black).headerColor(R.color.black))
                .page(new BasicPage(R.drawable.ex, "緊急モード", "瞬時に学習環境に変更します").descriptionColor(R.color.black).headerColor(R.color.black))
                .page(new FragmentWelcomePage() {
                    @Override
                    protected Fragment fragment() {
                        return new WelScrollFragment();
                    }
                })
                .swipeToDismiss(false)
                .build();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!(Common.Variable.START_FLAG == 1 || Common.Variable.START_FLAG == 2)) {
            Common.Variable.START_FLAG = 3;
        }
    }
}