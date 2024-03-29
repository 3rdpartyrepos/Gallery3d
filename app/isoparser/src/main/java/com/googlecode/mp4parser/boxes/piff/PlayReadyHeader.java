package com.googlecode.mp4parser.boxes.piff;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Specifications > Microsoft PlayReady Format Specification > 2. PlayReady Media Format > 2.7. ASF GUIDs
 * <p/>
 * <p/>
 * ASF_Protection_System_Identifier_Object
 * 9A04F079-9840-4286-AB92E65BE0885F95
 * <p/>
 * ASF_Content_Protection_System_Microsoft_PlayReady
 * F4637010-03C3-42CD-B932B48ADF3A6A54
 * <p/>
 * ASF_StreamType_PlayReady_Encrypted_Command_Media
 * 8683973A-6639-463A-ABD764F1CE3EEAE0
 * <p/>
 * <p/>
 * Specifications > Microsoft PlayReady Format Specification > 2. PlayReady Media Format > 2.5. Data Objects > 2.5.1. Payload Extension for AES in Counter Mode
 * <p/>
 * The sample Id is used as the IV in CTR mode. Block offset, starting at 0 and incremented by 1 after every 16 bytes, from the beginning of the sample is used as the Counter.
 * <p/>
 * The sample ID for each sample (media object) is stored as an ASF payload extension system with the ID of ASF_Payload_Extension_Encryption_SampleID = {6698B84E-0AFA-4330-AEB2-1C0A98D7A44D}. The payload extension can be stored as a fixed size extension of 8 bytes.
 * <p/>
 * The sample ID is always stored in big-endian byte order.
 */
public class PlayReadyHeader extends ProtectionSpecificHeader {
    private long length;
    private List<PlayReadyRecord> records;

    public PlayReadyHeader() {

    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        /*
   Length DWORD 32

   PlayReady Record Count WORD 16

   PlayReady Records See Text Varies

        */

        length = IsoTypeReader.readUInt32BE(byteBuffer);
        int recordCount = IsoTypeReader.readUInt16BE(byteBuffer);

        records = PlayReadyRecord.createFor(byteBuffer, recordCount);
    }

    @Override
    public ByteBuffer getData() {

        int size = 4 + 2;
        for (PlayReadyRecord record : records) {
            size += 2 + 2;
            size += record.getValue().rewind().limit();
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);

        IsoTypeWriter.writeUInt32BE(byteBuffer, size);
        IsoTypeWriter.writeUInt16BE(byteBuffer, records.size());
        for (PlayReadyRecord record : records) {
            IsoTypeWriter.writeUInt16BE(byteBuffer, record.type);
            IsoTypeWriter.writeUInt16BE(byteBuffer, record.getValue().limit());
            ByteBuffer tmp4debug = record.getValue();
            byteBuffer.put(tmp4debug);
        }

        return byteBuffer;
    }

    public void setRecords(List<PlayReadyRecord> records) {
        this.records = records;
    }

    public List<PlayReadyRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    @Override
    public String toString() {
        return "PlayReadyHeader" +
                "{length=" + length +
                ", recordCount=" + records.size() +
                ", records=" + records +
                '}';
    }

    public static abstract class PlayReadyRecord {
        int type;


        public PlayReadyRecord(int type) {
            this.type = type;
        }

        public static List<PlayReadyRecord> createFor(ByteBuffer byteBuffer, int recordCount) {
            List<PlayReadyRecord> records = new ArrayList<>(recordCount);

            for (int i = 0; i < recordCount; i++) {
                PlayReadyRecord record;
                int type = IsoTypeReader.readUInt16BE(byteBuffer);
                int length = IsoTypeReader.readUInt16BE(byteBuffer);
                switch (type) {
                    case 0x1:
                        record = new RMHeader();
                        break;
                    case 0x2:
                        record = new DefaulPlayReadyRecord(0x02);
                        break;
                    case 0x3:
                        record = new EmeddedLicenseStore();
                        break;
                    default:
                        record = new DefaulPlayReadyRecord(type);
                }
                record.parse((ByteBuffer) byteBuffer.slice().limit(length));
                byteBuffer.position(byteBuffer.position() + length);
                records.add(record);
            }

            return records;
        }

        public abstract void parse(ByteBuffer bytes);

        @Override
        public String toString() {
            return "PlayReadyRecord" +
                    "{type=" + type +
                    ", length=" + getValue().limit() +
//            sb.append(", value=").append(Hex.encodeHex(getValue())).append('\'');
                    '}';
        }

        public abstract ByteBuffer getValue();

        public static class RMHeader extends PlayReadyRecord {
            String header;

            public RMHeader() {
                super(0x01);
            }

            @Override
            public void parse(ByteBuffer bytes) {
                byte[] str = new byte[bytes.slice().limit()];
                bytes.get(str);
                header = new String(str, StandardCharsets.UTF_16LE);
            }

            @Override
            public ByteBuffer getValue() {
                byte[] headerBytes;
                headerBytes = header.getBytes(StandardCharsets.UTF_16LE);
                return ByteBuffer.wrap(headerBytes);
            }

            public void setHeader(String header) {
                this.header = header;
            }

            public String getHeader() {
                return header;
            }

            @Override
            public String toString() {
                return "RMHeader" +
                        "{length=" + getValue().limit() +
                        ", header='" + header + '\'' +
                        '}';
            }
        }

        public static class EmeddedLicenseStore extends PlayReadyRecord {
            ByteBuffer value;

            public EmeddedLicenseStore() {
                super(0x03);
            }

            @Override
            public void parse(ByteBuffer bytes) {
                this.value = bytes.duplicate();
            }

            @Override
            public ByteBuffer getValue() {
                return value;
            }

            @Override
            public String toString() {
                return "EmeddedLicenseStore" +
                        "{length=" + getValue().limit() +
                        //sb.append(", value='").append(Hex.encodeHex(getValue())).append('\'');
                        '}';
            }
        }

        public static class DefaulPlayReadyRecord extends PlayReadyRecord {
            ByteBuffer value;

            public DefaulPlayReadyRecord(int type) {
                super(type);
            }

            @Override
            public void parse(ByteBuffer bytes) {
                this.value = bytes.duplicate();
            }

            @Override
            public ByteBuffer getValue() {
                return value;
            }

        }

    }

}
