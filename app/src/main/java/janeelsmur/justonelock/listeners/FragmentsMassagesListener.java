package janeelsmur.justonelock.listeners;

/**
 * Этот интерфейс реализует вызов методов активностей по сообщениям из фрагментов, диалогов и т.д.
 */
public interface FragmentsMassagesListener {
    int DATA_CHANGED = 0x0001; // Данные изменились, нужно обновить списки
    int CLOSE_FAB = 0x0002; // Нужно закрыть FAB'ы
    int FINISH = 0x0003; // Нужно завершить активность

    /** Принимает сообщения от фрагментов, диалогов и т.д. внутри данной активности.
     *
     * @param notification - сообщение
     *                     DATA_CHANGED - уведомить ViewPager, что данные нужно обновить.
     *                     CLOSE_FAB - уведомляет, что нужно закрыть фабы.
     *                     FINISH - Нужно завершить активность
     */
    void onNotificationTaken(int notification);
}


