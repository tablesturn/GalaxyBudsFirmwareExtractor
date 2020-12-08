package me.timschneeberger.galaxybudsfirmware.extractor;

import java.io.*;
import java.util.ArrayList;

public class Mp3Segment {
    private final String TAG = "[Mp3Segment] ";

    private final byte[] data;
    private final long offset;
    private final long index;
    private final int samplerate;
    private final int bitrate;

    public Mp3Segment(byte[] data, long offset, long index, int samplerate, int bitrate){
        this.data = data;
        this.offset = offset;
        this.index = index;
        this.samplerate = samplerate;
        this.bitrate = bitrate;
    }

    public boolean saveToFile(File file) throws IOException{
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        DataOutputStream os = new DataOutputStream(fileOutputStream);
        try {
            os.write(this.data);
        } catch (Exception e) {
            e.printStackTrace();
            os.close();
            return false;
        }
        os.close();
        return true;
    }

    public byte[] getData() {
        return data;
    }

    public long getIndex() {
        return index;
    }

    public long getOffset() {
        return offset;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getSamplerate() {
        return samplerate;
    }

    public static Mp3Segment[] toPrimitiveMp3Segment(ArrayList<Mp3Segment> array){
        Mp3Segment[] bytes = new Mp3Segment[array.size()];
        for(int i = 0; i < array.size(); i++){
            bytes[i] = array.get(i);
        }
        return bytes;
    }
}
