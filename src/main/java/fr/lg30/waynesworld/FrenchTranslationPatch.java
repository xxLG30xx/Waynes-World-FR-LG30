package fr.lg30.waynesworld;
import java.io.*; import java.nio.*; import java.util.*; import java.util.zip.GZIPInputStream;
public final class FrenchTranslationPatch {
 public byte[] apply(byte[] source)throws IOException {try(var in=getClass().getResourceAsStream("/patch/french-lg30.dlt.b64")){if(in==null)throw new IOException("Delta absent");byte[] gz=Base64.getMimeDecoder().decode(in.readAllBytes());try(var d=new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(gz)))){if(d.readInt()!=0x57574431)throw new IOException("Delta invalide");byte[] out=Arrays.copyOf(source,d.readInt());int blocks=d.readInt();for(int i=0;i<blocks;i++){int o=d.readInt(),n=d.readInt();d.readFully(out,o,n);}return out;}}}
}
