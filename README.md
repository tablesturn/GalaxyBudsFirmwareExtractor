# Galaxy Buds Firmware Extractor

Utility to extract and compose firmware update packages for Galaxy Buds devices.  
An official firmware image is required. You can pull one directly from Samsung's FOTA servers.

## Operating modes

### Extract (`--extract firmware.bin`)

Extracts the firmware binary segments and mp3 audio resources from the given binary file.  

### Compose (`--compose firmware.bin`)

Composes the firmware binary segments from `firmware_segments/` into a valid firmware file `firmware_composed.bin`.  
Until now, the mp3 files do not get re-imported.

## Sample output
### Extraction mode
```
Importing firmware binary FOTA_R175XXU0ATH7.bin

Firmware archive "FOTA_R175XXU0ATH7.bin" Magic=cafecafe TotalSize=1444388
│
├─┐  [Binary segments] SegmentCount=7
│ └─ ID=1	Offset=0x007c	Size=180122	CRC32=0xf1ad0d6f
│ └─ ID=6	Offset=0x2c016	Size=368740	CRC32=0xc98f567a
│ └─ ID=7	Offset=0x8607a	Size=104516	CRC32=0x72b7396e
│ └─ ID=10	Offset=0x9f8be	Size=154260	CRC32=0xf4f10cc2
│ └─ ID=11	Offset=0xc5352	Size=58124	CRC32=0x391695ef
│ └─ ID=12	Offset=0xd365e	Size=147232	CRC32=0xeffbc7ed
│ └─ ID=20	Offset=0xf757e	Size=431266	CRC32=0x2144df1c

Reading segment with id 1 (no MP3 segments found)
Reading segment with id 6 (no MP3 segments found)
Reading segment with id 7 (no MP3 segments found)
Reading segment with id 10 (no MP3 segments found)
Reading segment with id 11 (no MP3 segments found)
Reading segment with id 12 (no MP3 segments found)
Reading segment with id 20
├─┐
│ └─ ID=0	Offset=0x9436	Size=16896	Bitrate=128000	Samplerate=48000
│ └─ ID=1	Offset=0xe044	Size=16896	Bitrate=128000	Samplerate=48000
│ └─ ID=2	Offset=0x122e4	Size=12672	Bitrate=128000	Samplerate=48000
│ └─ ID=3	Offset=0x15484	Size=3264	Bitrate=32000	Samplerate=48000
│ └─ ID=4	Offset=0x16164	Size=4320	Bitrate=32000	Samplerate=48000
│ └─ ID=5	Offset=0x17264	Size=10056	Bitrate=32000	Samplerate=48000
│ └─ ID=6	Offset=0x199cc	Size=1584	Bitrate=32000	Samplerate=48000
│ └─ ID=7	Offset=0x1a01c	Size=5880	Bitrate=128000	Samplerate=48000
│ └─ ID=8	Offset=0x1b734	Size=4680	Bitrate=32000	Samplerate=48000
│ └─ ID=9	Offset=0x1c99c	Size=3888	Bitrate=32000	Samplerate=48000
│ └─ ID=10	Offset=0x1d8ec	Size=2496	Bitrate=32000	Samplerate=48000
│ └─ ID=11	Offset=0x1e2cc	Size=17496	Bitrate=32000	Samplerate=48000
│ └─ ID=12	Offset=0x22744	Size=7920	Bitrate=32000	Samplerate=48000
│ └─ ID=13	Offset=0x24654	Size=6552	Bitrate=32000	Samplerate=48000
│ └─ ID=14	Offset=0x2600c	Size=7872	Bitrate=32000	Samplerate=48000
│ └─ ID=15	Offset=0x27eec	Size=14208	Bitrate=32000	Samplerate=48000
│ └─ ID=16	Offset=0x2b68c	Size=3240	Bitrate=32000	Samplerate=48000
│ └─ ID=17	Offset=0x2c354	Size=16704	Bitrate=192000	Samplerate=48000
│ └─ ID=18	Offset=0x304b4	Size=19008	Bitrate=192000	Samplerate=48000
│ └─ ID=19	Offset=0x34f14	Size=23040	Bitrate=192000	Samplerate=48000
│ └─ ID=20	Offset=0x3a934	Size=8424	Bitrate=32000	Samplerate=48000
│ └─ ID=21	Offset=0x3d50e	Size=20480	Bitrate=128000	Samplerate=44100
│ └─ ID=22	Offset=0x425ae	Size=3216	Bitrate=32000	Samplerate=48000
│ └─ ID=23	Offset=0x4325e	Size=3048	Bitrate=32000	Samplerate=48000
│ └─ ID=24	Offset=0x43e66	Size=3120	Bitrate=32000	Samplerate=48000
│ └─ ID=25	Offset=0x44ab6	Size=3024	Bitrate=32000	Samplerate=48000
│ └─ ID=26	Offset=0x456a6	Size=5064	Bitrate=32000	Samplerate=48000
  └─ [EOF] SegmentCount=27

Extracting binary and audio segments to 'FOTA_R175XXU0ATH7_segments'... Done
```

### Composition mode

```
Importing binary segments from FOTA_R175XXU0ATH7_segments...
Reading segment with id 1
Reading segment with id 6
Reading segment with id 7
Reading segment with id 10
Reading segment with id 11
Reading segment with id 12
Reading segment with id 20

Successfully imported 7 segments.

Composing firmware file to FOTA_R175XXU0ATH7_composed.bin... Done
```

## Todo

- Check for unhandled exceptions
- Composition of mp3 files
