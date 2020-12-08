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

            System.out.println("Importing firmware binary " + args[1]);
            System.out.println();

            File inputFile = new File(args[1]);

            String outputPath = baseName + "_segments";
            File dir = new File(outputPath);
            if (!dir.exists()){
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }

            FirmwareFile firmwareFile = new FirmwareFile();
            if(!firmwareFile.loadFromFile(inputFile))
                exit(1);

            System.out.println();
            System.out.print("Extracting binary and audio segments to '" + outputPath + "'... ");

            // Write all binary segments to separate files
            if(!firmwareFile.saveSegmentsToFiles(outputPath))
                exit(1);

            System.out.println("Done");
        }
        else if(args[0].equals("--compose")){
            if(args.length < 2){
                System.out.println("Missing argument. Please specify the firmware binary file name or the basename.");
                return;
            }

            String segmentsPath = args[1].split("[.]")[0] + "_segments";
            File dir = new File(segmentsPath);
            if (!dir.exists()){
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }

            System.out.println("Importing binary segments from " + segmentsPath + "...");

            FirmwareFile firmwareFile = new FirmwareFile();
            if(!firmwareFile.loadSegmentsFromFiles(segmentsPath))
                exit(1);

            System.out.println();
            System.out.print("Composing firmware file to " + baseName + "_composed.bin... ");

            File outputFile = new File(baseName + "_composed.bin");
            if(!firmwareFile.saveToFirmwareFile(outputFile))
                exit(1);

            System.out.println("Done");
        }
        else{
            System.out.println("Invalid argument. Please specify a mode (--extract or --compose)");
            return;
        }
    }
}
