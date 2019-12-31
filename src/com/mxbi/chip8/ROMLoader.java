package com.mxbi.chip8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ROMLoader {
    public static short[] loadRomFromFile(String filename) throws IOException {
        Path path = Paths.get(filename);
        byte[] bytes = Files.readAllBytes(path);
        short[] program = new short[bytes.length];

        for (int i=0; i < bytes.length; i++) {
            // Prevent signed integer extension when casting
            program[i] = (short)(0x000000FF & (int)bytes[i]);
        }

        return program;
    }
}
