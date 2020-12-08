package me.timschneeberger.galaxybudsfirmware.extractor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileSystemNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class FirmwareSegment {
    private byte[] data = null;
    private static final String TAG = "[FirmwareSegment] ";
    private int id = -1;

    private Mp3Segment[] mp3Segments;

    public FirmwareSegment(){
    }
    public FirmwareSegment(File file) throws IOException{
        this.loadFromFile(file);
    }
    public FirmwareSegment(BufferedInputStream inputStream, int length, int id) throws IOException{
        this.loadFromStream(inputStream, length, id);
    }

    public boolean loadFromFile(File file) throws IOException{
        return this.loadFromFile(file, true);
    }

    public boolean loadFromFile(File file, boolean readAudioSegments) throws IOException{
        this.id = getSegmentIdFromFilename(file.getName());
        if(this.getId() == -1)
            return false;

        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        boolean returnValue = this.loadFromStream(bufferedInputStream, (int)file.length(), id, readAudioSegments);
        bufferedInputStream.close();
        fileInputStream.close();
        return returnValue;
    }

    public boolean loadFromStream(BufferedInputStream inputStream, int length, int id) throws IOException{
        return this.loadFromStream(inputStream, length, id, true);
    }

    public boolean loadFromStream(BufferedInputStream inputStream, int length, int id, boolean readAudioSegments) throws IOException{
        try {
            this.id = id;
            this.data = new byte[length];
            if(inputStream.read(this.data, 0, length) != -1){
                System.out.print("Reading segment with id " + this.id);
            }
            if(readAudioSegments == true)
                this.readAudioSegments();
            else
                System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            inputStream.close();
            return false;
        }
        return true;
    }

    public int readAudioSegments() throws IOException {
        this.mp3Segments = Mp3Segment.toPrimitiveMp3Segment(Mp3Detection.analyse(this.getData()));
        if(this.getMp3SegmentCount() == 0) System.out.println(" (no MP3 segments found)");
        return this.getMp3SegmentCount();
    }

    public boolean saveAudioSegmentsToFiles(String destinationDirectory, String prefix) throws IOException{
        for (Mp3Segment segment : this.mp3Segments) {
            File outputMp3File = new File(destinationDirectory + "/" + prefix + "-" + segment.getIndex() + ".mp3");
            if(!segment.saveToFile(outputMp3File))
                return false;
        }
        return true;
    }

    public boolean saveToFile(File file) throws IOException{
        if(!this.isValid()) {
            System.out.println();
            System.out.println(TAG + "ERROR: Empty segment. Unable to export segment.");
            return false;
        }

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println();
            System.out.println(TAG + "ERROR: Cannot write output file. There exists already a directory with the same name.");
            return false;
        }

        DataOutputStream outputStream = new DataOutputStream(fileOutputStream);
        try {
            outputStream.write(this.getData());
        } catch (Exception e) {
            e.printStackTrace();
            outputStream.close();
            return false;
        }
        outputStream.close();

        return true;
    }

    public boolean isValid(){
        if(this.data == null)
            return false;
        if(this.data.length < 1)
            return false;
        if(this.id < 0)
            return false;
        return true;
    }

    public int getMp3SegmentCount(){
        return this.mp3Segments.length;
    }

    public int getSize(){
        return this.data.length;
    }

    public int getId(){
        return this.id;
    }

    public byte[] getData(){
        return this.data;
    }

    public long getCrc32(){
        return Utils.getCrc32Checksum(this.getData());
    }

    private int getSegmentIdFromFilename(String fileName){
        Pattern idPattern = Pattern.compile("(?<=-)\\d+(?=[.])");
        Matcher idMatcher = idPattern.matcher(fileName);
        if(idMatcher.find()){
            int segmentId = Integer.parseInt(idMatcher.group(idMatcher.groupCount()));
            return segmentId;
        }
        else return -1;
    }

}
