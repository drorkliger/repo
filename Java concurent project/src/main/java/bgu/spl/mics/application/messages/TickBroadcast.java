package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    private int currentTick;
    private int lastTick;

    public TickBroadcast(int currentTick, int lastTick)
    {

        this.currentTick=currentTick;
        this.lastTick=lastTick;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public int getLastTick() {
        return lastTick;
    }
}

