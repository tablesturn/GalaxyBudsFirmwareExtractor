package me.timschneeberger.galaxybudsfirmware.extractor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileSystemNotFoundException;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class FOTABinary {
    private static final long FOTA_BIN_MAGIC = 3405695742L;
    private static final String TAG = "[FW Binary] ";
    private File source;
    private String baseName;

    private long entry_cnt;
    private FOTAEntry[] fota_entry;
    private long[] segment_indices;
    private long magic;
    private long total_size;

    private Mp3Segment[] audio_segments;
    private byte[][] binary_segments;
    private byte[] firmware_data;

    public FOTABinary(File file){
        if (file.exists()) {
            source = file;
            this.baseName = file.getName().split("[.]")[0];
        }
        else {
            System.out.println(TAG + "ERROR: File does not exist");
            throw new FileSystemNotFoundException();
        }
    }

    public boolean readAudioSegments() throws IOException {

        System.out.println("│");
        System.out.println("├─┐  [Audio segments]");

        audio_segments = Utils.toPrimitiveMp3Segment(Mp3Detection.analyse(source));
        return true;
    }

    public boolean writeAudioSegments(String directory) throws IOException {
        boolean success = true;
        for(Mp3Segment segment : audio_segments){
            boolean result = segment.writeFile(directory, source.getName().split("[.]")[0]);
            if(!result)
                success = false;
        }
        return success;
    }

    public boolean readFirmware() throws IOException {
        byte[] bArr = new byte[4];
        FileInputStream fileInputStream = new FileInputStream(source);
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            if (bufferedInputStream.read(bArr) != -1) {
                this.magic = ((((long) bArr[2]) & 255) << 16) | ((((long) bArr[3]) & 255) << 24) | ((((long) bArr[1]) & 255) << 8) | (((long) bArr[0]) & 255);

                if (this.magic != FOTA_BIN_MAGIC) {
                    fileInputStream.close();
                    System.out.println(TAG + "ERROR: Invalid magic number. This is not a valid firmware file.");
                    return false;
                }
            }
            if (bufferedInputStream.read(bArr) != -1) {
                this.total_size = ((((long) bArr[2]) & 255) << 16) | ((((long) bArr[3]) & 255) << 24) | ((((long) bArr[1]) & 255) << 8) | (((long) bArr[0]) & 255);
                if (this.total_size == 0) {
                    fileInputStream.close();
                    System.out.println(TAG + "ERROR: Total firmware size is zero");
                    return false;
                }
            }
            if (bufferedInputStream.read(bArr) != -1) {
                this.entry_cnt = ((((long) bArr[1]) & 255) << 8) | ((((long) bArr[3]) & 255) << 24) | ((((long) bArr[2]) & 255) << 16) | (((long) bArr[0]) & 255);
                if (this.entry_cnt == 0) {
                    fileInputStream.close();
                    System.out.println(TAG + "ERROR: Firmware has no binary segments");
                    return false;
                }
            }

            System.out.println("Firmware archive \"" + source.getName() + "\" Magic=" + String.format("%x", this.magic) + " TotalSize=" + this.total_size);
            System.out.println("│");
            System.out.println("├─┐  [Binary segments] SegmentCount=" + this.entry_cnt);

            this.fota_entry = new FOTAEntry[((int) this.entry_cnt)];
            for (int i = 0; ((long) i) < this.entry_cnt; i++) {
                this.fota_entry[i] = new FOTAEntry(i, this.entry_cnt, source);
            }

            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileInputStream.close();
            return false;
        }

        return true;
    }

    // Writes all binary segments to one big file
    public boolean writeRawArchive(File file) throws IOException {
        if (this.fota_entry.length < 1) {
            System.out.println();
            System.out.println(TAG + "ERROR: Empty firmware. Unable to extract data.");
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

        DataOutputStream os = new DataOutputStream(fileOutputStream);
        FileInputStream sourceInputStream = new FileInputStream(source);
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(sourceInputStream);
            int currentOffset = 0;
            for (FOTAEntry entry : this.fota_entry) {

                int bytesToBeSkipped = (int)(entry.getPosition() - currentOffset);
                bufferedInputStream.readNBytes(bytesToBeSkipped);
                currentOffset += bytesToBeSkipped;

                os.write(bufferedInputStream.readNBytes((int) entry.getSize()));
                currentOffset += entry.getSize();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sourceInputStream.close();
            os.close();
            return false;
        }

        sourceInputStream.close();
        os.close();

        return true;
    }

    // Extracts the binary segment from entry to file
    public boolean exportBinarySegment(File file, FOTAEntry entry) throws IOException {
        if (this.fota_entry.length < 1) {
            System.out.println();
            System.out.println(TAG + "ERROR: Empty firmware. Unable to extract data.");
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

        DataOutputStream os = new DataOutputStream(fileOutputStream);
        FileInputStream sourceInputStream = new FileInputStream(source);
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(sourceInputStream);

            int bytesToBeSkipped = (int)(entry.getPosition());
            bufferedInputStream.readNBytes(bytesToBeSkipped);

            os.write(bufferedInputStream.readNBytes((int) entry.getSize()));
        } catch (Exception e) {
            e.printStackTrace();
            sourceInputStream.close();
            os.close();
            return false;
        }

        sourceInputStream.close();
        os.close();

        return true;
    }

    // Writes (extracts) all binary segments from the FOTA binary to separate files in directory
    public boolean extractBinarySegments(String destinationDirectory) throws IOException {
        for (FOTAEntry entry : this.fota_entry) {
            File outputBinarySegmentFile = new File(destinationDirectory + "/" + this.baseName + "-" + entry.getId() + ".bin");
            if(!exportBinarySegment(outputBinarySegmentFile,entry))
                return false;
        }
        return true;
    }

    // Imports all binary segments stores in seperate files in directory as XYZ-id.bin
    public boolean importBinarySegments(String directory) throws IOException{
        File directoryPath = new File(directory);

        FilenameFilter textFilefilter = new FilenameFilter(){
            public boolean accept(File dir, String name) {
                if (name.toLowerCase().matches(".+-\\d+\\.bin")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        // Sorted list of all .bin files
        String[] filesList = directoryPath.list(textFilefilter);
        Arrays.sort(filesList, new FilenameSegmentIdComparator());
        // Clear temp data
        this.segment_indices = new long[filesList.length];
        this.binary_segments = new byte[filesList.length][];
        // Import all binary segments
        for(int i=0; i < filesList.length; i++) {
            String binaryFileName = filesList[i];;
            this.segment_indices[i] = Utils.getSegmentIdFromFilename(binaryFileName);
            File binaryFile = new File(directory + "/" + filesList[i]);
            importBinarySegment(binaryFile, i);
            if(this.binary_segments.length == 0)
                return false;
        }
        System.out.println("Successfully imported " + this.binary_segments.length + " segments.");
        return true;
    }

    // Imports the binary segment stored in a seperate file to bin
    public boolean importBinarySegment(File source, int index) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(source);
        try {
            long segmentId = Utils.getSegmentIdFromFilename(source.getName());
            if(segmentId != -1){
                this.binary_segments[index] = new byte[(int) source.length()];
                if (fileInputStream.read(this.binary_segments[index]) != -1) {
                    System.out.println("Reading segment with id " + segmentId + " from '" + source.getName() + "'");
                }
            }

            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileInputStream.close();
            return false;
        }
        fileInputStream.close();

        return true;
    }

    // Counts the number of segment files in directory
    public long getSegmentFileCount(String directory) throws IOException {
        File directoryPath = new File(directory);

        FilenameFilter textFilefilter = new FilenameFilter(){
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".bin")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        // List of all the binary files
        String filesList[] = directoryPath.list(textFilefilter);

        return filesList.length;
    }

    private boolean composeFirmware() throws IOException{
        // Calculate firmware size
        long segment_count = this.binary_segments.length;
        int header_size = 12;  // 0xFECAFECA + size + number of segments
        header_size += 16*segment_count;    // 16 bytes for each segment header
        this.total_size = header_size;
        for(int i=0; i<segment_count; i++){     // Length of each segment
            this.total_size += this.binary_segments[i].length;
        }
        this.total_size += 4;   // CRC32 of whole file at the end

        // Compose firmware data
        ByteArrayOutputStream firmwareStream = new ByteArrayOutputStream();
        try {
            // Compose header
            firmwareStream.write(Utils.reverseByteArray(ByteBuffer.allocate(4).putInt(0xCAFECAFE).array()));   // Magic word
            firmwareStream.write(Utils.reverseByteArray(ByteBuffer.allocate(4).putInt((int) this.total_size).array())); // Firmware file length
            firmwareStream.write(Utils.reverseByteArray(ByteBuffer.allocate(4).putInt((int)segment_count).array())); // Segment count
            int segmentStartPosition = header_size;
            for(int i=0; i<segment_count; i++){
                firmwareStream.write(Utils.reverseByteArray(ByteBuffer.allocate(4).putInt((int)this.segment_indices[i]).array())); // Segment index
                firmwareStream.write(Utils.reverseByteArray(ByteBuffer.allocate(4).putInt((int)Utils.getCRC32Checksum(this.binary_segments[i])).array())); // Segment checksum
                firmwareStream.write(Utils.reverseByteArray(ByteBuffer.allocate(4).putInt(segmentStartPosition).array())); // Segment start adress
                firmwareStream.write(Utils.reverseByteArray(ByteBuffer.allocate(4).putInt(this.binary_segments[i].length).array())); // Segment length
                segmentStartPosition += this.binary_segments[i].length;
            }
            // Compose segments
            for(int i=0; i<segment_count; i++){
                firmwareStream.write(this.binary_segments[i]);
            }
            // Append CRC32 of complete file
            firmwareStream.write(Utils.reverseByteArray(ByteBuffer.allocate(4).putInt((int)Utils.getCRC32Checksum(firmwareStream.toByteArray())).array()));

        } catch (IOException e) {
            e.printStackTrace();
            firmwareStream.close();
            return false;
        }

        this.firmware_data = firmwareStream.toByteArray();

        return true;
    }

    // Composes and exports the FOTABinary content to a binary file
    public boolean exportFirmware(File outputFile) throws IOException {
        if(!composeFirmware())
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
            os.write(this.firmware_data);
        } catch (Exception e) {
            e.printStackTrace();
            os.close();
            return false;
        }
        os.close();
        return true;
    }
}