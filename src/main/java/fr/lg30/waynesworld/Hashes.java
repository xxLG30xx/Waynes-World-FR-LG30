package fr.lg30.waynesworld;
import java.security.*; import java.util.HexFormat; import java.util.zip.CRC32;
public record Hashes(long size,String crc32,String md5,String sha1){
 public static Hashes of(byte[] b){try{CRC32 c=new CRC32();c.update(b);return new Hashes(b.length,"%08x".formatted(c.getValue()),dig("MD5",b),dig("SHA-1",b));}catch(Exception e){throw new IllegalStateException(e);}}
 private static String dig(String n,byte[] b)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance(n).digest(b));}
}
