package com.example.mytagahanap.globals;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    private final static String NON_THIN = "[^iIl1\\.,']";

    private static int textWidth(String str) {
        return str.length() - str.replaceAll(NON_THIN, "").length() / 2;
    }

    public static String ellipsize(String text, int max) {
        if (textWidth(text) <= max)
            return text;

        // Start by chopping off at the word before max
        // This is an over-approximation due to thin-characters...
        int end = text.lastIndexOf(' ', max - 3);

        // Just one long word. Chop it off.
        if (end == -1)
            return text.substring(0, max - 3) + "...";

        // Step forward as long as textWidth allows.
        int newEnd = end;
        do {
            end = newEnd;
            newEnd = text.indexOf(' ', end + 1);

            // No more spaces.
            if (newEnd == -1)
                newEnd = text.length();

        } while (textWidth(text.substring(0, newEnd) + "...") < max);

        return text.substring(0, end) + "...";
    }

    public static Map<String, Integer> countFreq(JSONArray arr, int n) {
        Map<String, Integer> mp = new HashMap<>();
        ArrayList<String> listLocations = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            try {
                listLocations.add(arr.get(i).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int j = 0; j < listLocations.size(); j++) {
            String currentLoc = listLocations.get(j);
            if (currentLoc.contains("124"))
                continue;
            if (mp.containsKey(currentLoc)) {
                mp.put(currentLoc, mp.get(currentLoc) + 1);
            } else {
                mp.put(currentLoc, 1);
            }
        }

        return mp;
    }
}
