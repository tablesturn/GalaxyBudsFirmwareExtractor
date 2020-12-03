# Galaxy Buds Firmware Extractor

Utility to extract and compose firmware update packages for Galaxy Buds devices.  
An official firmware image is required. You can pull one directly from Samsung's FOTA servers.

## Extraction mode (`--extract firmware.bin`)

Extracts the firmware binary segments and mp3 audio resources from the given binary file.  
Additionally creates a file which contains all segments at once (`firmware_segments/firmware.raw.bin`).

## Compose mode (`--compose firmware.bin`)

Composes the firmware binary segments from `firmware_segments/` into a valid firmware file `firmware_patched.bin`.  
Until now, the mp3 files do not get re-imported.

## Sample output
### Extraction mode
```
Extracting binary --extract

Firmware archive "FOTA_R175XXU0ATH7.bin" Magic=cafecafe TotalSize=1444388
│
├─┐  [Binary segments] SegmentCount=7
│ ├─ ID=1	Offset=0x007c	Size=180122	CRC32=0xf1ad0d6f
│ ├─ ID=6	Offset=0x2c016	Size=368740	CRC32=0xc98f567a
│ ├─ ID=7	Offset=0x8607a	Size=104516	CRC32=0x72b7396e
│ ├─ ID=10	Offset=0x9f8be	Size=154260	CRC32=0xf4f10cc2
│ ├─ ID=11	Offset=0xc5352	Size=58124	CRC32=0x391695ef
│ ├─ ID=12	Offset=0xd365e	Size=147232	CRC32=0xeffbc7ed
│ └─ ID=20	Offset=0xf757e	Size=431266	CRC32=0x2144df1c
│
├─┐  [Audio segments]
│ ├─ ID=0	Offset=0x1009b4	Size=16896	Bitrate=128000	Samplerate=48000
│ ├─ ID=1	Offset=0x1055c2	Size=16896	Bitrate=128000	Samplerate=48000
│ ├─ ID=2	Offset=0x109862	Size=12672	Bitrate=128000	Samplerate=48000
│ ├─ ID=3	Offset=0x10ca02	Size=3264	Bitrate=32000	Samplerate=48000
│ ├─ ID=4	Offset=0x10d6e2	Size=4320	Bitrate=32000	Samplerate=48000
│ ├─ ID=5	Offset=0x10e7e2	Size=10056	Bitrate=32000	Samplerate=48000
│ ├─ ID=6	Offset=0x110f4a	Size=1584	Bitrate=32000	Samplerate=48000
│ ├─ ID=7	Offset=0x11159a	Size=5880	Bitrate=128000	Samplerate=48000
│ ├─ ID=8	Offset=0x112cb2	Size=4680	Bitrate=32000	Samplerate=48000
│ ├─ ID=9	Offset=0x113f1a	Size=3888	Bitrate=32000	Samplerate=48000
│ ├─ ID=10	Offset=0x114e6a	Size=2496	Bitrate=32000	Samplerate=48000
│ ├─ ID=11	Offset=0x11584a	Size=17496	Bitrate=32000	Samplerate=48000
│ ├─ ID=12	Offset=0x119cc2	Size=7920	Bitrate=32000	Samplerate=48000
│ ├─ ID=13	Offset=0x11bbd2	Size=6552	Bitrate=32000	Samplerate=48000
│ ├─ ID=14	Offset=0x11d58a	Size=7872	Bitrate=32000	Samplerate=48000
│ ├─ ID=15	Offset=0x11f46a	Size=14208	Bitrate=32000	Samplerate=48000
│ ├─ ID=16	Offset=0x122c0a	Size=3240	Bitrate=32000	Samplerate=48000
│ ├─ ID=17	Offset=0x1238d2	Size=16704	Bitrate=192000	Samplerate=48000
│ ├─ ID=18	Offset=0x127a32	Size=19008	Bitrate=192000	Samplerate=48000
│ ├─ ID=19	Offset=0x12c492	Size=23040	Bitrate=192000	Samplerate=48000
│ ├─ ID=20	Offset=0x131eb2	Size=8424	Bitrate=32000	Samplerate=48000
│ ├─ ID=21	Offset=0x134a8c	Size=20480	Bitrate=128000	Samplerate=44100
│ ├─ ID=22	Offset=0x139b2c	Size=3216	Bitrate=32000	Samplerate=48000
│ ├─ ID=23	Offset=0x13a7dc	Size=3048	Bitrate=32000	Samplerate=48000
│ ├─ ID=24	Offset=0x13b3e4	Size=3120	Bitrate=32000	Samplerate=48000
│ ├─ ID=25	Offset=0x13c034	Size=3024	Bitrate=32000	Samplerate=48000
│ ├─ ID=26	Offset=0x13cc24	Size=5064	Bitrate=32000	Samplerate=48000
│ └─ [EOF] SegmentCount=27

Extracting binary segments into raw firmware images... Done
Extracting audio segments as MP3 files... Done

Segment files have been written to 'FOTA_R175XXU0ATH7_segments'
```

### Composition mode

```
Importing binary segments...
Reading segment with id 1 from 'FOTA_R175XXU0ATH7-1.bin'
Reading segment with id 6 from 'FOTA_R175XXU0ATH7-6.bin'
Reading segment with id 7 from 'FOTA_R175XXU0ATH7-7.bin'
Reading segment with id 10 from 'FOTA_R175XXU0ATH7-10.bin'
Reading segment with id 11 from 'FOTA_R175XXU0ATH7-11.bin'
Reading segment with id 12 from 'FOTA_R175XXU0ATH7-12.bin'
Reading segment with id 20 from 'FOTA_R175XXU0ATH7-20.bin'
Successfully imported 7 segments.

Composing firmware file... Done

Patched firmware file has been successfully written to 'FOTA_R175XXU0ATH7_composed.bin'
```

## Todo

- Code cleanup
- Refactoring of new features in seperate classes
- Composition of mp3 files
