package org.wirla.flgkeeper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Transformer {

    public static Map<Byte, Byte> read(InputStream map) throws IOException {
        Map<Byte, Byte> nbyte = new HashMap<>();
        byte[] data = map.readAllBytes();
        for (int x = 0; x < data.length - 1; x+=1) {
            nbyte.put(data[x], data[x+1]);
        }
        return nbyte;
    }

}
