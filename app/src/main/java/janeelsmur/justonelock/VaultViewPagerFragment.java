package janeelsmur.justonelock;

import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import janeelsmur.justonelock.adapters.PagerAdapter;
import janeelsmur.justonelock.utilites.NotificationListener;
import janeelsmur.justonelock.vaultScreens.FavoritesPageFragment;
import janeelsmur.justonelock.vaultScreens.NotesPageFragment;
import janeelsmur.justonelock.vaultScreens.PasswordsPageFragment;

public class VaultViewPagerFragment extends Fragment {

    private final FavoritesPageFragment favoritesPageFragment = new FavoritesPageFragment();
    private final PasswordsPageFragment passwordsPageFragment = new PasswordsPageFragment();
    private final NotesPageFragment notesPageFragment = new NotesPageFragment();
    private final Fragment[] pages = {favoritesPageFragment, passwordsPageFragment, notesPageFragment};

    //Интерфейс для посыла сообщения для активности, чтобы та закрыла floating action button
    private NotificationListener notificationListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.vault_view_pager, null);
        setRetainInstance(false);
        //Инициализация интерфейса
        notificationListener = (NotificationListener) getActivity();
        Log.i("VaultViewActivity"," создалась заново");
        //Инициализация PagerAdapter'а
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager(), pages);
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(pagerAdapter);

        //Инициализация табов с ViewPager'ом
        TabLayout tabLayout = view.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        int[] imageResId = {R.drawable.favorites_icon, R.drawable.passwords_icon, R.drawable.notes_icon};
        for(short i = 0; i<imageResId.length; i++){
            tabLayout.getTabAt(i).setIcon(imageResId[i]);
        }

        //Закрываем клавиатуру при перелистывании
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                //Закрытие фаба в активности
                notificationListener.onNotificationTaken(NotificationListener.CLOSE_FAB);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    /** Уведомляет все фрагменты, что данные устарели и их нужно обновить */
    public void notifyDataHasChanged(){
            favoritesPageFragment.notifyDataHasChanged();
            passwordsPageFragment.notifyDataHasChanged();
            notesPageFragment.notifyDataHasChanged();
    }

}
