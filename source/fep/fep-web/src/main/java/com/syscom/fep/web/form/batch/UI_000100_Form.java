package com.syscom.fep.web.form.batch;

import com.syscom.fep.web.form.BaseForm;

public class UI_000100_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private String batchName;

    private String batchId;

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
