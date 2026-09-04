package fr.lg30.waynesworld;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.CRC32;

public record Hashes(long size, String crc32, String md5, String sha1) {
    public static Hashes of(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return new Hashes(data.length, "%08x".formatted(crc.getValue()), digest("MD5", data), digest("SHA-1", data));
    }

    private static String digest(String algorithm, byte[] data) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance(algorithm).digest(data)); }
        catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
}
