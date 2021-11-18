package org.wirla.flgkeeper;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/* Color Map File Render

Simple file format with it's only intended use being for this project.
Super easy way, didn't feel like reading a text file for bytes.

Files are in resources and work like this:
Each entry is 4 bytes:
- First byte is byte for map data (usually only defined by byte)
- Next three bytes is RGB for byte.
 */

public class ColorMapFile {

    public static Map<Byte, Color> read(String map) throws IOException {
        return read(new File(map));
    }

    public static Map<Byte, Color> read(File map) throws IOException {
        return read(new FileInputStream(map));
    }

    public static Map<Byte, Color> read(InputStream map) throws IOException {
        Map<Byte,Color> mc = new HashMap<>();
        byte[] data = map.readAllBytes();
        for (int x = 0; x < data.length; x+=4) {
            mc.put(data[x], new Color(data[x + 1] & 0xff, data[x + 2] & 0xff, data[x + 3] & 0xff));
        }
        return mc;
    }

}
