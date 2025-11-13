package com.kevo.svcmuteaddon.client;

public class ClientMuteState {
    private static volatile boolean muted = false;

    public static boolean isMuted() { return muted; }
    public static void setMuted(boolean value) { muted = value; }
}
