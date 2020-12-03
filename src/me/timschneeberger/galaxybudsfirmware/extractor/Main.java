package me.timschneeberger.galaxybudsfirmware.extractor;

import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println();

        if(args.length < 1){
            System.out.println("Missing argument. Please specify a mode (--extract or --compose)");
            return;
        }

        String baseName = args[1].split("[.]")[0];

        if(args[0].equals("--extract")){
            if(args.length < 2){
                System.out.println("Missing argument. Please specify a firmware binary file.");
                return;
            }

            System.out.println("Extracting binary " + args[0]);
            System.out.println();

            File inputFile = new File(args[1]);
            FOTABinary bin = new FOTABinary(inputFile);

            //String outputPath = inputFile.getParentFile().getPath() + "/" + baseName + "_segments";
            String outputPath = baseName + "_segments";
            File dir = new File(outputPath);
            if (!dir.exists()){
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }

            if(!bin.readFirmware())
                exit(1);

            if(!bin.readAudioSegments())
                exit(1);

            System.out.println();
            System.out.print("Extracting binary segments into raw firmware images... ");

            File outputRawBinFile = new File(outputPath + "/" + baseName+ ".raw.bin");
            if(!bin.writeRawArchive(outputRawBinFile))
                exit(1);

            // Write all binary segments to separate files
            if(!bin.extractBinarySegments(outputPath))
                exit(1);

            System.out.println("Done");

            System.out.print("Extracting audio segments as MP3 files... ");
            if(!bin.writeAudioSegments(outputPath))
                exit(1);

            System.out.println("Done");

            System.out.println();
            System.out.println("Segment files have been written to '" + outputPath + "'");
        }
        else if(args[0].equals("--compose")){
            if(args.length < 2){
                System.out.println("Missing argument. Please specify the firmware binary file name or the basename.");
                return;
            }

            File inputFile = new File(args[1]);
            FOTABinary bin = new FOTABinary(inputFile);

            //String outputPath = inputFile.getParentFile().getPath() + "/" + baseName + "_segments";
            String outputPath = baseName + "_segments";
            File dir = new File(outputPath);
            if (!dir.exists()){
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }

            System.out.println("Importing binary segments...");

            if(!bin.importBinarySegments(outputPath))
                exit(1);

            System.out.println();
            System.out.print("Composing firmware file... ");

            File outputFile = new File(baseName + "_composed.bin");
            if(!bin.exportFirmware(outputFile))
                exit(1);

            System.out.println("Done");

            System.out.println();
            System.out.println("Patched firmware file has been successfully written to '" + baseName + "_composed.bin'");
        }
        else{
            System.out.println("Invalid argument. Please specify a mode (--extract or --compose)");
            return;
        }
    }
}
