package org.jcmv.photo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;

@Slf4j
public class PhotoFixer {

  private static final int DATE_TIME_TAG_ID = 306;
  private String inputDirectoryFromCmdLine = null;

  private File askForInputDirectory() throws IOException {
    File dir = null;
    if (inputDirectoryFromCmdLine != null) {
      dir = new File(inputDirectoryFromCmdLine);
      if (dir.isDirectory()) {
        return dir;
      } else {
        System.out.format("Directory %s not found%n", inputDirectoryFromCmdLine);
      }
    }

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
      boolean exit = true;
      do {
        System.out.print("Enter the directory containing your photos: ");
        String inputFolder = reader.readLine();
        dir = new File(inputFolder);
        if (! dir.isDirectory()) {
          System.out.format("Directory %s not found%n", inputFolder);
        } else {
          exit = true;
        }
      } while (!exit);
    }
    return dir;
  }

  public void run() throws Exception {
    File srcDir = askForInputDirectory();

    for (File photoFile : srcDir.listFiles((file) -> {
      return file.getName().toLowerCase().endsWith(".jpg") || file.getName().endsWith(".jpeg");
    })) {
      log.info("Processing file {}", photoFile.getName());
      try {
        Metadata metadata = JpegMetadataReader.readMetadata(photoFile);
        boolean found = false;
        for (Directory dir : metadata.getDirectoriesOfType(ExifIFD0Directory.class)) {
          for (Tag tag : dir.getTags()) {
            if (tag.getTagName().startsWith("Date/Time")) {
              Date realDate = dir.getDate(DATE_TIME_TAG_ID);
              if (realDate != null) {
                found = true;
                log.info("File {} modified time: {}, real time: {}", photoFile.getName(),
                    formatDate(new Date(photoFile.lastModified())), formatDate(realDate));
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
        log.warn("Unable to process {}", photoFile.getName(), e);
      }
    }
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
      log.error("", e);
    }
  }

}
