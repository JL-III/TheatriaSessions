package com.playtheatria.theatriaSessions.managers;

import com.playtheatria.theatriaSessions.database.data.ResetTime;

public class ResetTimeManager {
    private ResetTime resetTime;

    public ResetTimeManager(ResetTime resetTime) {
        this.resetTime = resetTime;
    }

    public ResetTime getResetTime() {
        return resetTime;
    }

    public void setResetTime(ResetTime resetTime) {
        this.resetTime = resetTime;
    }
}
