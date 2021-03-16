package com.rexnjc.ui.widget;

import android.text.TextUtils;

public enum ScanType {
    SCAN_MA("MA");

    private String value;

    ScanType(String value) {
        this.value = value;
    }

    public static ScanType getType(String value) {
        for(ScanType scanType : ScanType.values()){
            if(TextUtils.equals(scanType.value, value)){
                return scanType;
            }
        }
        return SCAN_MA;
    }

    public String toBqcScanType(){
        switch (this){
            case SCAN_MA:
                return "MA";
            default:
                return "MA";
        }
    }
}
