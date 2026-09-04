package fr.lg30.waynesworld;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/** Applies the compact WWD1 sparse delta; neither complete ROM is bundled. */
public final class FrenchTranslationPatch {
    private static final int MAGIC = 0x57574431; // WWD1

    public byte[] apply(byte[] source) throws IOException {
        try (var resource = getClass().getResourceAsStream("/patch/french-lg30.dlt.gz")) {
            if (resource == null) throw new IOException("Patch français introuvable.");
            try (var in = new DataInputStream(new GZIPInputStream(resource))) {
                if (in.readInt() != MAGIC) throw new IOException("Format du patch français invalide.");
                int outputLength = in.readInt();
                int count = in.readInt();
                if (outputLength < source.length || count < 0) throw new IOException("En-tête du patch invalide.");
                byte[] output = Arrays.copyOf(source, outputLength);
                for (int i = 0; i < count; i++) {
                    int offset = in.readInt(), length = in.readInt();
                    if (offset < 0 || length < 0 || offset > output.length - length) throw new IOException("Bloc du patch invalide.");
                    in.readFully(output, offset, length);
                }
                if (in.read() != -1) throw new IOException("Données superflues dans le patch.");
                return output;
            }
        }
    }
}
