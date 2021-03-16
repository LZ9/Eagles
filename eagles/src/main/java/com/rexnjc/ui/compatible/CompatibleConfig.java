package com.rexnjc.ui.compatible;

import android.os.Build;

import java.util.HashSet;
import java.util.Set;

/**
 * author : leilei.yll
 * date : 3/8/17.
 * email : leilei.yll@alibaba-inc.com
 * 旺旺 : 病已
 */
public class CompatibleConfig {
    private Set<String> torchBlackSet;
    private Set<String> zoomBlackSet;

    public CompatibleConfig() {
        torchBlackSet = new HashSet<>();
        torchBlackSet.add("samsung/SCH-I739");
        torchBlackSet.add("LENOVO/Lenovo A820t");
        zoomBlackSet = new HashSet<>();
    }

    public boolean checkSupportTorch(String manufacture, String model) {
        String key = manufacture+"/"+model;
        if(torchBlackSet.contains(key)) {
            return false;
        }
        return true;
    }

    public boolean checkSupportAutoZoom() {
        String device = Build.MANUFACTURER + "/" + Build.MODEL;
        if(zoomBlackSet.contains(device)) {
            return false;
        }
        return true;
    }

}
