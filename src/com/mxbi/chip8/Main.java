package com.mxbi.chip8;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {
    	JFrame frame = new JFrame("CHIPSTER: Select a CHIP-8 ROM");
//	    JFileChooser fc = new JFileChooser("./roms");

	    FileDialog fd = new FileDialog(frame, "CHIPSTER: Select a CHIP-8 ROM", FileDialog.LOAD);
	    fd.setDirectory("roms");
	    fd.setFilenameFilter((dir, name) -> name.endsWith(".ch8") || name.endsWith(".rom"));
	    fd.setVisible(true);
	    String filename = fd.getFile();

//	    int returnVal = fc.showDialog(frame, "Load ROM");
//	    frame.dispose();
//	    if (returnVal == JFileChooser.APPROVE_OPTION) {
//		    short[] rom = ROMLoader.loadRomFromFile(fc.getSelectedFile().getAbsolutePath());
		if (filename != null) {
	        short[] rom = ROMLoader.loadRomFromFile("roms/" + filename);

		    SwingDisplay display = new SwingDisplay();

		    CPU cpu = new CPU(rom, display, display);
		    cpu.run();
	    }
    }
}
