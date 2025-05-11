# PhotoDateFixer

Recursively set JPEG file timestamp to JPEG's EXIF date/time.

When you start the application, it will show a dialog window to ask for a folder to start the process.

It will recursevely find all files that end on .jpg and modify their last modified time to the one on the EXIF data structure.

This uses artifact com.drewnoakes:metadata-extractor:2.13.0 from Maven central repository to read the EXIF data structure of the pictures.
