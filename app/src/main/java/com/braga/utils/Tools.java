package com.braga.utils;

public class Tools {
     static String join(String glue, Object[] items) {
        String rval = "";
        if (items == null) {
            return "";
        }
        if (items.length <= 0) {
            return "";
        }
        rval = items[0].toString();
        for (int i = 1; i < items.length; i++) {
            rval = new StringBuilder(String.valueOf(rval)).append(glue).append(items[i].toString()).toString();
        }
        return rval;
    }
}
