package com.uber.pickbot.historyRecyclerView;

public class HistoryObject {
    String rideId;
    String time;
    String distance;
    String rideprize;

    public HistoryObject(String rideId,String time,String distance,String rideprize) {

        this.rideId = rideId;
        this.time = time;
        this.distance = distance;
        this.rideprize = rideprize;
    }

    public String getRideId() {
        return rideId;
    }

    public String getTime() {
        return time;
    }

    public String getDistance() {
        return distance;
    }

    public String getPrize() {
        return rideprize;
    }

}
