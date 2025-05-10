package org.jcmv.photo;

import lombok.extern.java.Log;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Fix WhatsApp pictures which don't contain EXIF information. Use the file name to infer the creation date.
 */
@Log
public class FixPhotoByName {
    private final File dir = new File(".");
    // private File dir = new File("\\\\ideacentre\\new_albums\\20240621 - From WhatsApp Gabby\\Para Gaby");
    private static final Pattern filenameRegex = Pattern.compile("IMG-(\\d{4})(\\d{2})(\\d{2})-WA\\d+\\.jpg");

    /**
     * Main application loop
     *
     * @throws IOException If unable to read from the console or unable to read the current directory.
     */
    public void run() throws IOException {
        System.out.format("OK to fix photos on %s? ", dir.getAbsolutePath());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            String answer = in.readLine();
            if (!answer.toLowerCase().startsWith("y")) {
                return;
            }
            processDir(dir);
        }
    }

    /**
     * Process the directory and fix the last modified time of the files.
     *
     * @param dir The directory to process.
     */
    private void processDir(File dir) {
        File[] fileList = dir.listFiles(f -> f.getName().endsWith(".jpg"));
        if (fileList == null) {
            log.log(WARNING, "Dir {0} has no subfiles", dir);
            return;
        }
        processFiles(fileList);
    }

    protected void processFiles(File[] fileList) {
        LocalDateTime lastModified;
        for (File file : fileList) {
            Matcher matcher = filenameRegex.matcher(file.getName());
            if (matcher.matches()) {
                lastModified = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault()
                );

                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));

                if (isInvalidDay(file, month, day, year)) continue;

                lastModified = lastModified
                        .withYear(year)
                        .withMonth(month)
                        .withDayOfMonth(day);


                System.out.format("File %s: Setting last modified to %s%n", file.getName(), lastModified);
                boolean success = file.setLastModified(lastModified.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                if (!success) {
                    log.log(SEVERE, "Unable to modify last modified time of {0}", file);
                }
            } else {
                System.out.format("File %s  didn't match%n", file.getName());
            }

        }
    }

    /**
     * Check if the day is valid for the month and year.
     *
     * @param file  The file with the name that has been parsed. Needed only to log the file name.
     * @param month The month parsed from the file name.
     * @param day   The day parsed from the file name.
     * @param year  The year parsed from the file name.
     * @return True if the day is invalid for the month and year.
     */
    private static boolean isInvalidDay(File file, int month, int day, int year) {
        final int JANUARY = 1;
        final int FEBRUARY = 2;
        final int MARCH = 3;
        final int MAY = 5;
        final int JULY = 7;
        final int AUGUST = 8;
        final int OCTOBER = 10;
        final int DECEMBER = 12;

        if (month < JANUARY || month > DECEMBER) {
            return true;
        }

        switch (month) {
            case JANUARY:
            case MARCH:
            case MAY:
            case JULY:
            case AUGUST:
            case OCTOBER:
            case DECEMBER:
                if (day < 1 || day > 31) {
                    log.log(WARNING, "File {0} has invalid day for month {1}. Not modifying.", new Object[]{file, month});
                    return true;
                }
                break;
            case FEBRUARY:
                int lastValidDay = isLeap(year) ? 29 : 28;
                if (day < 1 || day > lastValidDay) {
                    log.log(WARNING, "File {0} has invalid day for month 2. Not modifying", file);
                    return true;
                }
                // April, June, September, November
            default:
                if (day < 1 || day > 30) {
                    log.log(WARNING, "File {0} has invalid day for month {1}. Not modifying", new Object[]{file, month});
                    return true;
                }
        }
        return false;
    }

    /**
     * Leap years are those divisible by 4 but not by 100 unless they're divisible by 400.
     *
     * @param year The year to check.
     * @return True if the year is a leap year, false otherwise.
     */
    private static boolean isLeap(int year) {
        boolean isLeap;
        if (year % 4 != 0) {
            isLeap = false;
        } else if (year % 100 != 0) {
            isLeap = true;
        } else if (year % 400 != 0) {
            isLeap = false;
        } else {
            isLeap = true;
        }
        return isLeap;
    }

    /**
     * Main method to run the program.
     *
     * @param args command line arguments not needed.
     */
    public static void main(String[] args) {
        try {
            new FixPhotoByName().run();
        } catch (Exception e) {
            log.log(SEVERE, "", e);
        }
    }
}
