package me.timschneeberger.galaxybudsfirmware.extractor;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Utils {
    static void addBytes(ArrayList<Byte> array, byte[] bytes){
        for(byte b : bytes){
            array.add(b);
        }
    }
    static byte[] toPrimitiveBytes(ArrayList<Byte> array){
        byte[] bytes = new byte[array.size()];
        for(int i = 0; i < array.size(); i++){
            bytes[i] = array.get(i);
        }
        return bytes;
    }
    static Mp3Segment[] toPrimitiveMp3Segment(ArrayList<Mp3Segment> array){
        Mp3Segment[] bytes = new Mp3Segment[array.size()];
        for(int i = 0; i < array.size(); i++){
            bytes[i] = array.get(i);
        }
        return bytes;
    }
    // Returns the segment id from a filename like "XYZ-segmentId.xyz"
    // Returns -1 in case there was no id found
    static int getSegmentIdFromFilename(String filename){
        Pattern idPattern = Pattern.compile("(?<=-)\\d+(?=[.])");
        Matcher idMatcher = idPattern.matcher(filename);
        if(idMatcher.find()){
            int segmentId = Integer.parseInt(idMatcher.group(idMatcher.groupCount()));
            return segmentId;
        }
        else return -1;
    }
    static byte[] reverseByteArray(byte[] byteArray){
        byte temp;
        for(int i=0; i<byteArray.length/2; i++){
            temp = byteArray[i];
            byteArray[i] = byteArray[byteArray.length -i -1];
            byteArray[byteArray.length -i -1] = temp;
        }
        return byteArray;
    }
    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }
}