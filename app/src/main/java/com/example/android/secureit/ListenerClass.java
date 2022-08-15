package com.example.android.secureit;

import com.google.firebase.database.ValueEventListener;

public class ListenerClass {
    private static ValueEventListener valueEventListener;

    public static ValueEventListener getValueEventListener() {
        return valueEventListener;
    }

    public static void setValueEventListener(ValueEventListener valueEventListener) {
        ListenerClass.valueEventListener = valueEventListener;
    }
}
