package janeelsmur.justonelock.listeners;

import java.util.ArrayList;

public class DataChangedListenerControl {
    private static ArrayList<DataChangedListener> listeners = new ArrayList<>();

    public static void addListener(DataChangedListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(DataChangedListener listener) {
        listeners.remove(listener);
    }

    public static void callListeners() {
        for (DataChangedListener listener : listeners) {
            listener.onDataChanged();
        }
    }
}
