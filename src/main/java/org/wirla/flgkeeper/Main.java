package org.wirla.flgkeeper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

public class Main {

    private Map<Byte, Color> colorHash = new HashMap<>();

    static String pathraw;
    static String output = null;
    static String toSLB = null;

    private List<Byte> unk = new ArrayList<>();

    public Main() {

    }

    public static void main(String[] args) {
        if (args.length >= 1) {
            for (int a = 0; a < args.length; a++) {
                String arg = args[a].toLowerCase();
                switch (arg) {
                    default:
                        if (pathraw != null) output = args[a];
                        else pathraw = args[a];
                        break;
                    case "--to-slb":
                        toSLB = args[a + 1];
                        a++;
                        break;
                }
            }
        }



        if (output == null) {
            int cutStart = 0;
            if (pathraw.contains("/")) cutStart = pathraw.lastIndexOf('/') + 1;
            else if (pathraw.contains("\\")) cutStart = pathraw.lastIndexOf('\\') + 1;
            output = pathraw.substring(cutStart, pathraw.length() - 3) + ".png";
        }

        if (pathraw == null) {
            System.out.println("flgkeeper <FILE> [--show-window] [--output <FILE>]");
            return;
        }

        try {
            new Main().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void start() throws IOException {
        Path path = Paths.get(pathraw);
        byte[] data = Files.readAllBytes(path);
        Image x;
        switch(pathraw.substring(pathraw.length() - 4).toLowerCase()) {
            default:
                throw new IOException();
            case ".flg":
                colorHash = ColorMapFile.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("flg.map")));
                x = parse_FGT(data);
                if (toSLB != null) {
                    FLG_to_SLB(data);
                }
                break;
            case ".slb":
                colorHash = ColorMapFile.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("slb.map")));
                x = parse_SLB(data);
                break;
            case ".cei":
                colorHash = ColorMapFile.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("cei.map")));
                x = parse_CEI(data);
                break;
        }

        if (output != null) {
            BufferedImage bi = new BufferedImage
                    (x.getWidth(null),x.getHeight(null),BufferedImage.TYPE_INT_RGB);
            Graphics bg = bi.getGraphics();
            bg.drawImage(x, 0, 0, null);
            bg.dispose();
            ImageIO.write(
                    bi,
                    output.substring(output.length()-3),
                    new File(output));
        }
    }

    private byte[] toSingleArray(byte[][] d) {
        byte[] newbyte = new byte[14450];
        int bCount = 0;
        for (int y = 0; y < 85; y++) {
            for (int x = 0; x < 85; x++) {
                newbyte[bCount] = d[y][x];
                newbyte[bCount + 1] = 0x00;
                bCount += 2;
            }
        }
        return newbyte;
    }

    private void FLG_to_SLB(byte[] b) throws IOException {
        Map<Byte, Byte> mc = Transformer.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("flg-slb.map")));
        byte[][] flgBytes = new byte[256][256];
        byte[][] slbBytes = new byte[85][85];

        // Read FLG
        int f = 0;
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                flgBytes[y][x] = mc.get(b[f]);
                f+=2;
            }
        }

        // Write SLB
        int sy = 0;
        for (int y = 1; y < 256; y+=3) {
            int sx = 0;
            for (int x = 1; x < 256; x+=3) {
                slbBytes[sy][sx] = flgBytes[y][x];
                sx++;
            }
            sy++;
        }

        for (Byte b1 : unk) {
            System.out.printf("0x%02X\n", b1);
        }

        try (FileOutputStream fos = new FileOutputStream(toSLB)) {
            fos.write(toSingleArray(slbBytes));
            fos.close();
        }
    }

    private Image parse_FGT(byte[] data) {
        int width = 256;
        int height = 256;
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        int c = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Byte b1 = data[c];
                Color bColor;
                if (colorHash.containsKey(b1))
                    bColor = colorHash.get(b1);
                else {
                    bColor = new Color(255,255,255);
                    if (!unk.contains(b1)) {
                        unk.add(b1);
                    }
                }

                if (data[c+1] != 0) System.out.printf("Byte 0x%02X seems to have second byte of 0x%02X\n", data[c], data[c+1]);

                image.setRGB(x, y, bColor.getRGB());
                c+=2;
            }
        }

        for (Byte b : unk) {
            System.out.printf("0x%02X\n", b);
        }
        return image;
    }

    private Image parse_SLB(byte[] data) {
        int width = 85;
        int height = 85;
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        int c = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Byte b1 = data[c];
                Color bColor;
                if (colorHash.containsKey(b1))
                    bColor = colorHash.get(b1);
                else {
                    bColor = new Color(255,255,255);
                    if (!unk.contains(b1)) {
                        unk.add(b1);
                    }
                }

                image.setRGB(x, y, bColor.getRGB());
                c+=2;
                System.out.printf("0x%02X", b1);
            }
        }

        for (Byte b : unk) {
            System.out.printf("0x%02X", b);
            System.out.print("\n");
        }
        return image;
    }

    private Image parse_CEI(byte[] data) {
        int width = 256;
        int height = 256;
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        int c = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Byte b1 = data[c];
                Color bColor;
                if (colorHash.containsKey(b1))
                    bColor = colorHash.get(b1);
                else {
                    bColor = new Color(255,255,255);
                    if (!unk.contains(b1)) {
                        unk.add(b1);
                    }
                }

                image.setRGB(x, y, bColor.getRGB());
                c++;
            }
        }

        for (Byte b : unk) {
            System.out.printf("0x%02X", b);
            System.out.print("\n");
        }
        return image;
    }
}
