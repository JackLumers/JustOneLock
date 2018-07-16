package janeelsmur.justonelock;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import janeelsmur.justonelock.adapters.PagerAdapter;
import janeelsmur.justonelock.utilites.SharedPreferencesManager;
import janeelsmur.justonelock.welcomeScreens.WelcomeScreen1Fragment;
import janeelsmur.justonelock.welcomeScreens.WelcomeScreen2Fragment;
import janeelsmur.justonelock.welcomeScreens.WelcomeScreen3Fragment;
import janeelsmur.justonelock.welcomeScreens.WelcomeScreen4Fragment;
import janeelsmur.justonelock.welcomeScreens.WelcomeScreen5Fragment;

public class WelcomeScreenActivity extends AppCompatActivity implements View.OnClickListener{

    //Окна
    private final WelcomeScreen1Fragment screen1 = new WelcomeScreen1Fragment();
    private final WelcomeScreen2Fragment screen2 = new WelcomeScreen2Fragment();
    private final WelcomeScreen3Fragment screen3 = new WelcomeScreen3Fragment();
    private final WelcomeScreen4Fragment screen4 = new WelcomeScreen4Fragment();
    private final WelcomeScreen5Fragment screen5 = new WelcomeScreen5Fragment();
    private final Fragment[] pages = {screen1, screen2, screen3, screen4, screen5};

    private TextView startButton;
    private LinearLayout dotesLayout;
    private SharedPreferencesManager sharedPreferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //*ЭКРАН ПРИВЕТСТВИЯ*//
        sharedPreferencesManager = new SharedPreferencesManager(this);
        if(!sharedPreferencesManager.isFirstAppLaunch()) { //Если это не первый запуск, переходим в MainActivity
            Intent intent = new Intent(WelcomeScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.welcome_screen_view_pager);

        dotesLayout = (LinearLayout) findViewById(R.id.sliding_tabs);
        startButton = (TextView) findViewById(R.id.startButton);

        startButton.setOnClickListener(this);

        //Установка ViewPager'а
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), pages);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(pagerAdapter);
        setDotes(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setDotes(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        startButton.setEnabled(false);
        sharedPreferencesManager.disableWelcomeScreen();
        Intent intent = new Intent(WelcomeScreenActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //Установка точек
    private void setDotes(int position){
        TextView[] dots = new TextView[pages.length];
        dotesLayout.removeAllViews();

        for(int i = 0; i<dots.length; i++){
            dots[i] = new TextView(this);
            dots[i].setText("●");
            dots[i].setTextColor(getResources().getColor(R.color.gray));
            dotesLayout.addView(dots[i]);
        }

        dots[position].setTextColor(getResources().getColor(R.color.black));
    }

}
