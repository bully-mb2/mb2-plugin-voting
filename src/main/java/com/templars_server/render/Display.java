package com.templars_server.render;

import java.util.ArrayList;
import java.util.List;

public class Display {

    public static final String PREFIX = "^2Vote » ^7";
    private static final int MAX_MESSAGE_LENGTH = 118;

    public static List<String> renderMaps(List<String> mapList) {
        List<String> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String map : mapList) {
            map = map + ", ";
            if (builder.length() + map.length() > MAX_MESSAGE_LENGTH - PREFIX.length()) {
                result.add(PREFIX + builder.substring(0, builder.length() - 2));
                builder = new StringBuilder();
            }

            builder.append(map);
        }

        if (builder.length() > 0) {
            result.add(PREFIX + builder.substring(0, builder.length() - 2));
        }

        return result;
    }

}
