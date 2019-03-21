package janeelsmur.justonelock;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import janeelsmur.justonelock.listeners.FragmentsMassagesListener;
import janeelsmur.justonelock.utilities.SharedPreferencesManager;

public class VaultActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, FragmentsMassagesListener, View.OnTouchListener {

    private long back_pressed; // Таймер нажатия "назад"

    /* СОХРАНЕННЫЕ ДАННЫЕ */
    private byte[] key;
    private String fullFilePath;
    private String fileName;

    private DrawerLayout drawer;
    private FloatingActionButton fab;
    private FloatingActionButton createFolderFab;
    private FloatingActionButton createPasswordFab;
    private FloatingActionButton createNoteFab;
    private TextView createFolderTextView;
    private TextView createPasswordTextView;
    private TextView createNoteTextView;
    private ImageView blackScreen;
    private Animation alphaScale;
    private Animation alphaScaleReversed;

    /* -------CONTENT_FRAGMENTS------- */
    private final VaultViewPagerFragment vaultViewPagerFragment = new VaultViewPagerFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        //Добавляет фрагмент с viewPager'ом
        setViewPagerContent();

        fileName = getIntent().getStringExtra("fileName");
        fullFilePath = getIntent().getStringExtra("fullFilePath");
        key = getIntent().getByteArrayExtra("KEY");

        //-----------------
        alphaScale = AnimationUtils.loadAnimation(this, R.anim.alpha_scale);
        alphaScaleReversed = AnimationUtils.loadAnimation(this, R.anim.alpha_scale_reversed);
        alphaScale.setDuration(200);
        alphaScaleReversed.setDuration(200);

        fab = (FloatingActionButton) findViewById(R.id.openCreatingMenuFab);

        blackScreen = (ImageView) findViewById(R.id.blackScreen);
        createFolderFab = (FloatingActionButton) findViewById(R.id.createFolderFab);
        createNoteFab = (FloatingActionButton) findViewById(R.id.createNoteFab);
        createPasswordFab = (FloatingActionButton) findViewById(R.id.createEntryFab);

        createFolderTextView = (TextView) findViewById(R.id.createFolderTextView);
        createPasswordTextView = (TextView) findViewById(R.id.createEntryTextView);
        createNoteTextView = (TextView) findViewById(R.id.createNoteTextView);

        //-----------------

        //Инициализация и выставление тулбура
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Инициализация и выставление навигационной панели
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(actionBarDrawerToggle);
            drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    hideFabs();
                }
            });
            actionBarDrawerToggle.syncState();
        }


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Установка в тексте навигационной панели названия хранилища
        View navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        TextView navHeaderText = navHeaderView.findViewById(R.id.nav_header_main_text);
        navHeaderText.setText(fileName);

        Log.d("VaultActivity", "onCreate!" + fullFilePath);
    }

    @Override
    protected void onStart() {
        super.onStart();

        fab.setOnClickListener(this);
        blackScreen.setOnTouchListener(this);
        createPasswordFab.setOnClickListener(this);
        createNoteFab.setOnClickListener(this);
        createFolderFab.setOnClickListener(this);
        createNoteTextView.setOnClickListener(this);
        createPasswordTextView.setOnClickListener(this);
        createFolderTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.openCreatingMenuFab:
                if(!createFolderFab.isShown()) {
                    showFabs();
                } else {
                    hideFabs();
                }
                break;

            case R.id.createFolderTextView:
            case R.id.createFolderFab:
                v.setOnClickListener(null);
                Intent intent = new Intent(this, CreateFolderActivity.class);
                intent.putExtra("fullFilePath", fullFilePath);
                startActivity(intent);
                hideFabs();
                break;

            case R.id.createEntryTextView:
            case R.id.createEntryFab:
                v.setOnClickListener(null);
                Intent intent1 = new Intent(this, CreatePasswordActivity.class);
                intent1.putExtra("KEY", key);
                intent1.putExtra("fullFilePath", fullFilePath);
                startActivity(intent1);
                hideFabs();
                break;

            case R.id.createNoteTextView:
            case R.id.createNoteFab:
                v.setOnClickListener(null);
                Intent intent2 = new Intent(this, NoteActivity.class);
                intent2.putExtra("KEY", key);
                intent2.putExtra("fullFilePath", fullFilePath);
                startActivity(intent2);
                hideFabs();
                break;

            case R.id.blackScreen:
                hideFabs();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideFabs();
        return false;
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Если открыта панель, закрываем
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            // Если закрыта, то проверяем, прошло ли 2000 милисекунд
            // Если прошло, закрываем приложение
        } else if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
            // Иначе запускаем таймер и выводим текст
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.ExitToast), Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_change_vault) {
            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
            sharedPreferencesManager.setFilePathToNULL(); //Очистка пути к файлу
            Intent intent = new Intent(VaultActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
//        else if (id == R.id.nav_settings) {
//            // Открыть фрагмент с настройками
//        } else if (id == R.id.nav_export) {
//            //Открыть проводник для выбора пути сохранения хранилища
//        }
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

        }



        return true;
    }

    // FragmentsMassagesListener
    @Override
    public void onNotificationTaken(int notification) {
        switch (notification){
            case FragmentsMassagesListener.CLOSE_FAB:
                hideFabs();
                break;

            case FragmentsMassagesListener.DATA_CHANGED:
                vaultViewPagerFragment.notifyDataHasChanged();
                break;

        }
    }

    private void setViewPagerContent(){
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_pages_container, vaultViewPagerFragment)
                .commit();
    }

    private void hideFabs(){
        if(blackScreen.getVisibility() == View.VISIBLE) { //Нужна проверка, так как метод срабатывает при прогрузке активности (баг с viewPager'ом)
            blackScreen.setVisibility(View.INVISIBLE);
            createFolderFab.hide(); createNoteFab.hide(); createPasswordFab.hide();
            createFolderTextView.setVisibility(View.INVISIBLE); createPasswordTextView.setVisibility(View.INVISIBLE); createNoteTextView.setVisibility(View.INVISIBLE);
            createFolderTextView.startAnimation(alphaScaleReversed); createPasswordTextView.startAnimation(alphaScaleReversed);
            createNoteTextView.startAnimation(alphaScaleReversed);
        }
    }

    private void showFabs(){
        blackScreen.setVisibility(View.VISIBLE);
        createFolderFab.show(); createNoteFab.show(); createPasswordFab.show();
        createFolderTextView.setVisibility(View.VISIBLE); createPasswordTextView.setVisibility(View.VISIBLE); createNoteTextView.setVisibility(View.VISIBLE);
        createFolderTextView.startAnimation(alphaScale); createPasswordTextView.startAnimation(alphaScale); createNoteTextView.startAnimation(alphaScale);
    }
}
