package org.jcmv.photo;

import lombok.extern.java.Log;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;

@Log
public class FixPhotoByNameTest extends FixPhotoByName {
    @Test
    public void testFixLastModified() {
        // Setup
        String filename = "20240621-IMG-20230621-WA0001.jpg";
        File testDir = new File("src/test/resources");
        File[] fileList = testDir.listFiles(f -> f.getName().endsWith(".jpg"));
        Arrays.asList(fileList).stream().forEach(f -> f.setLastModified(0));

        // Run the method
        super.processFiles(fileList);

        // Verify the results
        for (File file : fileList) {
            LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
            assertEquals(2022, date.getYear());
            assertEquals(Month.AUGUST, date.getMonth());

            if (file.getName().contains("20220810")) {
                assertEquals(10, date.getDayOfMonth());
            } else if (file.getName().contains("20220811")) {
                assertEquals(11, date.getDayOfMonth());
            } else if (file.getName().contains("20220812")) {
                assertEquals(12, date.getDayOfMonth());
            } else if (file.getName().contains("20220813")) {
                assertEquals(13, date.getDayOfMonth());
            } else if (file.getName().contains("20220814")) {
                assertEquals(14, date.getDayOfMonth());
            } else {
                log.log(Level.WARNING, "Unable to handle test file {0}", file);
            }
        }
    }
    // @Test
    // public void testRun() throws IOException {
    //     FixPhotoByName fixPhotoByName = new FixPhotoByName();
    //     fixPhotoByName.run();
    // }
}