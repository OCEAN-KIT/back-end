package com.ocean.piuda.admin.report.enums;

public enum ReportDraftType {
    INTERNAL_DRAFT("internalDraft"),
    EXTERNAL_NEWSLETTER("externalNewsletter"),
    EXTERNAL_INSTAGRAM("externalInstagram"),
    EXTERNAL_PUBLICATION("externalPublication");

    private final String jsonKey;

    ReportDraftType(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String jsonKey() {
        return jsonKey;
    }
}
