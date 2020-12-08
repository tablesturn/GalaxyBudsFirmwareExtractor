package me.timschneeberger.galaxybudsfirmware.extractor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileSystemNotFoundException;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class FirmwareFile {
    private static final long FOTA_BIN_MAGIC = 0xCAFECAFE; //3405695742L;
    private static final String TAG = "[FirmwareFile] ";

    private FirmwareSegment[] firmwareSegments = null;
    private long magic;
    private String baseName;

    public FirmwareFile(){

    }

    public boolean loadFromFile(File file) throws IOException{
        FileInputStream fileInputStream = new FileInputStream(file);
        this.firmwareSegments = null;
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            // Read & verify magic number
            java.lang.Integer magicNumber = Utils.nextFourBytesToIntLittleEndian(bufferedInputStream);
            if (magicNumber == null)
                return false;
            this.magic = magicNumber;
            if (this.magic != FOTA_BIN_MAGIC) {
                fileInputStream.close();
                System.out.println(TAG + "ERROR: Invalid magic number. This is not a valid firmware file.");
                return false;
            }

            // Read firmware size
            java.lang.Integer totalSize = Utils.nextFourBytesToIntLittleEndian(bufferedInputStream);
            if (totalSize == null)
                return false;
            if (totalSize == 0) {
                fileInputStream.close();
                System.out.println(TAG + "ERROR: Total firmware size is zero");
                return false;
            }

            // Read segment count
            java.lang.Integer segmentCount = Utils.nextFourBytesToIntLittleEndian(bufferedInputStream);
            if (segmentCount == null)
                return false;
            this.firmwareSegments = new FirmwareSegment[segmentCount];
            if (segmentCount == 0) {
                fileInputStream.close();
                System.out.println(TAG + "ERROR: Firmware has no binary segments");
                return false;
            }

            System.out.println("Firmware archive \"" + file.getName() + "\" Magic=" + String.format("%x", (int)this.magic) + " TotalSize=" + totalSize);
            System.out.println("│");
            System.out.println("├─┐  [Binary segments] SegmentCount=" + this.getSegmentCount());

            // Read segment information
            FirmwareSegmentInformation segmentInformation[] = new FirmwareSegmentInformation[this.getSegmentCount()];
            for(int i=0; i<segmentCount; i++){
                segmentInformation[i] = new FirmwareSegmentInformation(bufferedInputStream);
                if(!segmentInformation[i].isValid())
                    return false;
            }

            System.out.println();

            // Todo: Check CRC32 of original file and segments (and give warning if wrong)

            // Read basename
            this.baseName = file.getName().split("[.]")[0];

            // Read binary segments
            for (int i = 0; ((long) i) < segmentCount; i++) {
                this.firmwareSegments[i] = new FirmwareSegment(bufferedInputStream, segmentInformation[i].getSize(), segmentInformation[i].getId());
            }

            // Read file crc32
            java.lang.Integer fileCrc32 = Utils.nextFourBytesToIntLittleEndian(bufferedInputStream);
            if(fileCrc32 == null)
                return false;

            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileInputStream.close();
            return false;
        }

        return true;
    }

    public boolean loadSegmentsFromFiles(String directory) throws IOException{
        File directoryFile = new File(directory);
        return loadSegmentsFromFiles(directoryFile);
    }

    public boolean loadSegmentsFromFiles(File directory) throws IOException{
        FilenameFilter textFilefilter = new FilenameFilter(){
            public boolean accept(File dir, String name) {
                if (name.toLowerCase().matches(".+-\\d+\\.bin")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        String[] filesList = directory.list(textFilefilter);
        Arrays.sort(filesList, new FilenameSegmentIdComparator());
        this.firmwareSegments = new FirmwareSegment[filesList.length];
        // Import all binary segments
        for(int i=0; i < filesList.length; i++) {
            this.firmwareSegments[i] = new FirmwareSegment();
            File segmentFile = new File(directory + "/" + filesList[i]);
            if(!this.firmwareSegments[i].loadFromFile(segmentFile, false))
                return false;
        }
        System.out.println();
        System.out.println("Successfully imported " + this.getSegmentCount() + " segments.");
        return true;
    }

    public boolean saveToFirmwareFile(File outputFile) throws IOException{
        byte[] firmwareData = this.getData();
        if(firmwareData == null)
            return false;

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println();
            System.out.println(TAG + "ERROR: Cannot write output file. There exists already a directory with the same name.");
            return false;
        }

        DataOutputStream os = new DataOutputStream(fileOutputStream);
        try {
            os.write(firmwareData);
        } catch (Exception e) {
            e.printStackTrace();
            os.close();
            return false;
        }
        os.close();
        return true;
    }

    public boolean saveSegmentsToFiles(String destinationDirectory) throws IOException{
        for (FirmwareSegment segment : this.firmwareSegments) {
            File outputSegmentFile = new File(destinationDirectory + "/" + this.getBaseName() + "-" + segment.getId() + ".bin");
            if(!segment.saveToFile(outputSegmentFile))
                return false;
            if(!segment.saveAudioSegmentsToFiles(destinationDirectory, this.getBaseName() + "-" + segment.getId()))
                return false;
        }
        return true;
    }

    private byte[] getData() throws IOException{
        // Compose firmware data
        ByteArrayOutputStream firmwareStream = new ByteArrayOutputStream();
        try {
            firmwareStream.write(this.getHeader());
            for(FirmwareSegment segment: this.firmwareSegments){
                firmwareStream.write(segment.getData());
            }
            // Append CRC32 of complete file
            firmwareStream.write(Utils.getCRC32ByteArrayLittleEndian(firmwareStream.toByteArray()));

        } catch (IOException e) {
            e.printStackTrace();
            firmwareStream.close();
            return null;
        }

        return firmwareStream.toByteArray();
    }

    private byte[] getHeader() throws IOException{
        if(!isValid())
            return null;
        ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
        try {
            headerStream.write(Utils.intToByteArrayLittleEndian((int)FOTA_BIN_MAGIC));       // Magic word
            headerStream.write(Utils.intToByteArrayLittleEndian(this.getTotalSize()));      // Firmware file length
            headerStream.write(Utils.intToByteArrayLittleEndian(this.getSegmentCount()));   // Segment count
            int segmentStartPosition = this.getHeaderSize();
            for(FirmwareSegment segment: this.firmwareSegments){
                headerStream.write(Utils.intToByteArrayLittleEndian(segment.getId())); // Segment index
                headerStream.write(Utils.intToByteArrayLittleEndian((int)segment.getCrc32())); // Segment checksum
                headerStream.write(Utils.intToByteArrayLittleEndian(segmentStartPosition)); // Segment start adress
                headerStream.write(Utils.intToByteArrayLittleEndian(segment.getSize())); // Segment length
                segmentStartPosition += segment.getSize();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            headerStream.close();
            return null;
        }
        return headerStream.toByteArray();
    }

    public boolean isValid(){
        if(this.firmwareSegments == null)
            return false;
        if(this.getSegmentCount() < 1)
            return false;
        for(FirmwareSegment segment: this.firmwareSegments) {
            if(!segment.isValid())
                return false;
        }
        return true;
    }

    private int getHeaderSize(){
        if(!this.isValid())
            return -1;
        // 0xFECAFECA + size + number of segments + 4x int32 per segment (id, crc32, start, length)
        int headerSize = 12 + 16*this.getSegmentCount();
        return headerSize;
    }

    public int getTotalSize(){
        if(!this.isValid())
            return -1;

        int totalSize = getHeaderSize();
        // Size of each segment
        for(FirmwareSegment segment: this.firmwareSegments) {
            totalSize += segment.getSize();
        }
        // CRC32 of whole file at the end
        totalSize += 4;
        return totalSize;
    }

    public String getBaseName(){
        return this.baseName;
    }

    public int getSegmentCount(){
        return this.firmwareSegments.length;
    }
}
