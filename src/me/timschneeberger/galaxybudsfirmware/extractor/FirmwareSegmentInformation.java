package me.timschneeberger.galaxybudsfirmware.extractor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FirmwareSegmentInformation {
    private boolean valid = true;
    private java.lang.Integer id;
    private java.lang.Integer crc32;
    private java.lang.Integer offset;
    private java.lang.Integer size;

    // Reads next 16 bytes and extracts the information
    public FirmwareSegmentInformation(BufferedInputStream inputStream) throws IOException {
        try {
            this.id = Utils.nextFourBytesToIntLittleEndian(inputStream);
            this.crc32 = Utils.nextFourBytesToIntLittleEndian(inputStream);
            this.offset = Utils.nextFourBytesToIntLittleEndian(inputStream);
            this.size = Utils.nextFourBytesToIntLittleEndian(inputStream);

            System.out.println("│ └─ ID=" + this.id +
                    "\tOffset=0x" + String.format("%04x", this.offset) +
                    "\tSize=" + this.size +
                    "\tCRC32=0x" + String.format("%04x", this.crc32));
        } catch (Exception e) {
            e.printStackTrace();
            inputStream.close();
        }
    }

    public boolean isValid() {
        if((this.id == null) | (this.crc32 == null) | (this.offset == null) | (this.size == null))
            return false;
        return true;
    }

    public int getCrc32() {
        return this.crc32;
    }

    public int getOffset() { return this.offset; }

    public int getSize() {
        return this.size;
    }

    public int getId() {
        return this.id;
    }

}
