package com.syscom.fep.batch.policy;

import java.util.Calendar;

public class BatchJobPolicyConclusion {
    private boolean skip;
    private boolean misfired;
    private Calendar latestExecutedDateTime;
    private Calendar nextExecutedDateTime;

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isMisfired() {
        return misfired;
    }

    public void setMisfired(boolean misfired) {
        this.misfired = misfired;
    }

    public Calendar getLatestExecutedDateTime() {
        return latestExecutedDateTime;
    }

    public void setLatestExecutedDateTime(Calendar latestExecutedDateTime) {
        this.latestExecutedDateTime = latestExecutedDateTime;
    }

    public Calendar getNextExecutedDateTime() {
        return nextExecutedDateTime;
    }

    public void setNextExecutedDateTime(Calendar nextExecutedDateTime) {
        this.nextExecutedDateTime = nextExecutedDateTime;
    }
}
