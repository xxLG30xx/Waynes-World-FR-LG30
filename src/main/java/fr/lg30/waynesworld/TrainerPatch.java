package fr.lg30.waynesworld;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public final class TrainerPatch {
    public static final int HOOK_OFFSET = 0x332;
    public static final int TRAINER_OFFSET = 0xF98BA;
    public static final byte[] ORIGINAL_CALL = {0x4E, (byte) 0xB9, 0x00, 0x03, 0x65, 0x7A};
    public static final byte[] TRAINER_CALL = {0x4E, (byte) 0xB9, 0x00, 0x0F, (byte) 0x98, (byte) 0xBA};

    public byte[] apply(byte[] input, Set<Cheat> selected) throws IOException {
        byte[] rom = input.clone();
        if (selected.isEmpty()) return rom;
        if (!Arrays.equals(Arrays.copyOfRange(rom, HOOK_OFFSET, HOOK_OFFSET + 6), ORIGINAL_CALL))
            throw new IOException("Le point d’injection de la ROM est inattendu.");

        ByteArrayOutputStream code = new ByteArrayOutputStream();
        code.writeBytes(ORIGINAL_CALL);
        for (Cheat cheat : Cheat.values()) if (selected.contains(cheat)) {
            for (Cheat.Write write : cheat.writes()) code.writeBytes(new byte[] {
                    0x13, (byte) 0xFC, 0x00, (byte) write.value(), 0x00, (byte) 0xFF,
                    (byte) (write.address() >>> 8), (byte) write.address() });
        }
        code.writeBytes(new byte[] {0x4E, 0x75});
        byte[] routine = code.toByteArray();
        if (TRAINER_OFFSET + routine.length > rom.length) throw new IOException("ROM trop petite pour le trainer.");
        for (int i = 0; i < routine.length; i++) if ((rom[TRAINER_OFFSET + i] & 0xFF) != 0xFF)
            throw new IOException("La zone réservée au trainer n’est pas vierge.");

        System.arraycopy(TRAINER_CALL, 0, rom, HOOK_OFFSET, TRAINER_CALL.length);
        System.arraycopy(routine, 0, rom, TRAINER_OFFSET, routine.length);
        int checksum = calculateChecksum(rom);
        rom[0x18E] = (byte) (checksum >>> 8);
        rom[0x18F] = (byte) checksum;
        return rom;
    }

    public static int calculateChecksum(byte[] rom) {
        int sum = 0;
        for (int i = 0x200; i < rom.length; i += 2) {
            int word = (rom[i] & 0xFF) << 8;
            if (i + 1 < rom.length) word |= rom[i + 1] & 0xFF;
            sum = (sum + word) & 0xFFFF;
        }
        return sum;
    }
}
