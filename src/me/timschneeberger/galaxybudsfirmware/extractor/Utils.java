package me.timschneeberger.galaxybudsfirmware.extractor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
    static java.lang.Integer nextFourBytesToIntLittleEndian(BufferedInputStream inputStream) throws IOException {
        byte[] byteBuffer = new byte[4];
        try{
            if (inputStream.read(byteBuffer) == -1)
                return null;
            return ((((int) byteBuffer[2]) & 255) << 16) | ((((int) byteBuffer[3]) & 255) << 24) | ((((int) byteBuffer[1]) & 255) << 8) | (((int) byteBuffer[0]) & 255);
        } catch (Exception e) {
            e.printStackTrace();
            inputStream.close();
            return null;
        }
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
    static byte[] intToByteArrayBigEndian(int number){
        return ByteBuffer.allocate(4).putInt(number).array();
    }
    static byte[] intToByteArrayLittleEndian(int number){
        return Utils.reverseByteArray(ByteBuffer.allocate(4).putInt(number).array());
    }
    public static long getCrc32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }
    public static byte[] getCRC32ByteArrayLittleEndian(byte[] data) {
        return Utils.reverseByteArray(ByteBuffer.allocate(4).putInt((int)Utils.getCrc32Checksum(data)).array());
    }
    public static int getSegmentIdFromFilename(String fileName){
        Pattern idPattern = Pattern.compile("(?<=-)\\d+(?=[.])");
        Matcher idMatcher = idPattern.matcher(fileName);
        if(idMatcher.find()){
            int segmentId = Integer.parseInt(idMatcher.group(idMatcher.groupCount()));
            return segmentId;
        }
        else return -1;
    }
}