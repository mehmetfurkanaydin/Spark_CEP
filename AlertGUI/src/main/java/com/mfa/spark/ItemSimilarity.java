package com.mfa.spark;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mfa on 19.01.2017.
 */
public class ItemSimilarity {

    public static String foundSuggestedItems (String firstItems, String secondItems) {

        String suggestedItems = "";
        //split the second string into words
        List<String> wordsOfCurrent = Arrays.asList(firstItems.split(" "));
        int count = 0;
        //split and compare each word of the first string
        for (String word : secondItems.split(" ")) {
            if(!wordsOfCurrent.contains(word))

                if(count == 0) {
                    suggestedItems = suggestedItems + word;
                }else {
                    suggestedItems = suggestedItems + " - " + word;
                }
               count++;
        }

        return suggestedItems;
    }
}
