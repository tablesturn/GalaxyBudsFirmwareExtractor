package me.timschneeberger.galaxybudsfirmware.extractor;

import java.util.Comparator;

// Helper class for sorting a list of segment filenames
public class FilenameSegmentIdComparator implements Comparator<String> {
    public int compare(String strNumber1, String strNumber2) {
        // Extract ids from string first
        int number1 = Utils.getSegmentIdFromFilename(strNumber1);
        int number2 = Utils.getSegmentIdFromFilename(strNumber2);

        // Compare numbers
        if( number1 > number2 ){
            return 1;
        }else if( number1 < number2 ){
            return -1;
        }else{
            return 0;
        }
    }
}