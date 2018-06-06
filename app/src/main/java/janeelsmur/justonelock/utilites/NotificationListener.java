package janeelsmur.justonelock.utilites;

/**
 * Этот интерфейс реализует вызов функций активностей из фрагментов, диалогов и т.д.
 */
public interface NotificationListener {
    int DATA_CHANGED = 0x0001;
    int CLOSE_FAB = 0x0002;
    int FINISH = 0x0003;
    void onNotificationTaken(int notification);
}


