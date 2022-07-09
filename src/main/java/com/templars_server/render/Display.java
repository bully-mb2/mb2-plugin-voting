package com.templars_server.render;

import java.util.ArrayList;
import java.util.List;

public class Display {

    private static final int MAX_MESSAGE_LENGTH = 118;

    public static List<String> renderMaps(String prefix, List<String> mapList) {
        List<String> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String map : mapList) {
            map = map + ", ";
            if (builder.length() + map.length() > MAX_MESSAGE_LENGTH - prefix.length()) {
                result.add(prefix + builder.substring(0, builder.length() - 2));
                builder = new StringBuilder();
            }

            builder.append(map);
        }

        if (builder.length() > 0) {
            result.add(prefix + builder.substring(0, builder.length() - 2));
        }

        return result;
    }

}
