package org.jcmv.photo;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.extern.java.Log;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.logging.Level.*;

@Log
public class PhotoFixer {

    private static final int DATE_TIME_TAG_ID = 306;
    private String inputDirectoryFromCmdLine = null;

    private File askForInputDirectory() throws IOException {
        File dir = processCmdLine();
        if (dir == null) {
            // Request directory from user
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                boolean exit;
                do {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileChooser.showOpenDialog(null);

//          System.out.print("Enter the directory containing your photos: ");
                    dir = fileChooser.getSelectedFile();
                    if (dir == null) {
                        log.info("User decided to exit");
                        System.out.println("Bye");
                        exit = true;
                    } else if (!dir.isDirectory()) {
                        System.out.format("Directory %s not found%n", dir);
                        exit = false;
                    } else {
                        exit = true;
                    }
                } while (!exit);
            }
        }
        return dir;
    }

    private File processCmdLine() {
        File dir = null;
        if (inputDirectoryFromCmdLine != null) {
            dir = new File(inputDirectoryFromCmdLine);
            if (dir.isDirectory()) {
                return dir;
            } else {
                System.out.format("Directory %s not found%n", inputDirectoryFromCmdLine);
                return null;
            }
        }
        return dir;
    }

    public void run() throws Exception {
        File srcDir = askForInputDirectory();
        if (srcDir == null) {
            return;
        }

        for (File photoFile : srcDir.listFiles((file) -> isJpegExtension(file))) {
            log.log(INFO, "Processing file {0}", photoFile.getName());
            try {
                Metadata metadata = JpegMetadataReader.readMetadata(photoFile);
                boolean found = false;
                for (Directory dir : metadata.getDirectoriesOfType(ExifIFD0Directory.class)) {
                    for (Tag tag : dir.getTags()) {
                        if (tag.getTagName().startsWith("Date/Time")) {
                            Date realDate = dir.getDate(DATE_TIME_TAG_ID);
                            if (realDate != null) {
                                found = true;
                                log.log(INFO, "File {0} modified time: {1}, real time: {2}", new Object[]{
                                        photoFile.getName(), formatDate(new Date(photoFile.lastModified())), formatDate(realDate)});
                                photoFile.setLastModified(realDate.getTime());
                                break;
                            }
                        }
                    }
                }
                if (!found) {
                    System.out.format("Didn't find date for file %s!!!%n", photoFile.getName());
                }
            } catch (JpegProcessingException | IOException e) {
                log.log(WARNING, "Unable to process " + photoFile.getName(), e);
            }
        }
    }

    private boolean isJpegExtension(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    private String formatDate(Date time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(time);
    }

    public static void main(String[] args) {
        try {
            PhotoFixer photoFixer = new PhotoFixer();
            if (args.length > 0) {
                photoFixer.inputDirectoryFromCmdLine = args[0];
            }
            photoFixer.run();
        } catch (Exception e) {
            log.log(SEVERE, "", e);
        }
    }

}
