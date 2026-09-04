package fr.lg30.waynesworld;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public final class AllTests {
    private static int passed;
    private static void check(boolean condition, String name) { if (!condition) throw new AssertionError(name); passed++; System.out.println("PASS " + name); }
    private static Path frenchRom() throws IOException { try (var files = Files.list(Path.of("."))) { return files.filter(p -> p.getFileName().toString().startsWith("Waynes_World_FR_complete_183_bulles") && p.toString().endsWith(".bin")).findFirst().orElseThrow(); } }

    public static void main(String[] args) throws Exception {
        byte[] usa = Files.readAllBytes(Path.of("Wayne's World (USA).md"));
        byte[] officialFrench = Files.readAllBytes(frenchRom());
        Hashes frenchHashes = new Hashes(1_085_408, "6762266d", "3f9b17922988941d9c652803cf787f2a", "c24eeb619f7b263953a837b6c23a405e8b4af783");
        check(Hashes.of(usa).equals(RomValidator.OFFICIAL_USA), "hashes ROM USA");
        check(Hashes.of(officialFrench).equals(frenchHashes), "hashes ROM FR officielle");
        check(new RomValidator().isCompatible(usa), "validation ROM USA");
        byte[] corrupt = usa.clone(); corrupt[42] ^= 1;
        check(!new RomValidator().isCompatible(corrupt), "rejet mauvaise ROM");
        byte[] translated = new FrenchTranslationPatch().apply(usa);
        check(Arrays.equals(translated, officialFrench), "traduction byte-for-byte");
        check(Hashes.of(translated).equals(frenchHashes), "hashes traduction exacts");
        check(Arrays.equals(new TrainerPatch().apply(officialFrench, Set.of()), officialFrench), "traduction seule sans checksum modifié");

        for (Cheat cheat : Cheat.values()) verifySingleCheat(officialFrench, cheat);
        byte[] all = new TrainerPatch().apply(officialFrench, EnumSet.allOf(Cheat.class));
        check(Arrays.equals(Arrays.copyOfRange(all, TrainerPatch.HOOK_OFFSET, TrainerPatch.HOOK_OFFSET + 6), TrainerPatch.TRAINER_CALL), "hook 0x332");
        check(Arrays.equals(Arrays.copyOfRange(all, TrainerPatch.TRAINER_OFFSET, TrainerPatch.TRAINER_OFFSET + 6), TrainerPatch.ORIGINAL_CALL), "appel original conservé");
        check((all[TrainerPatch.TRAINER_OFFSET + 54] & 0xFF) == 0x4E && (all[TrainerPatch.TRAINER_OFFSET + 55] & 0xFF) == 0x75, "RTS après les quatre cheats");
        check(headerChecksum(all) == TrainerPatch.calculateChecksum(all), "checksum Mega Drive");
        check(all[TrainerPatch.TRAINER_OFFSET + 9] == 9 && all[TrainerPatch.TRAINER_OFFSET + 17] == 9 && all[TrainerPatch.TRAINER_OFFSET + 25] == 9, "trois écritures temps infini");
        byte[] occupied = officialFrench.clone(); occupied[TrainerPatch.TRAINER_OFFSET] = 0;
        boolean refused = false; try { new TrainerPatch().apply(occupied, Set.of(Cheat.INVINCIBILITY)); } catch (IOException expected) { refused = true; }
        check(refused, "rejet zone trainer occupée");

        Path temp = Files.createTempDirectory("wayne-test-"); Path input = temp.resolve("source.md"); Path output = temp.resolve(RomCreationService.OUTPUT_NAME);
        Files.write(input, usa); new RomCreationService().create(input, output, true, Set.of());
        check(Files.isRegularFile(output) && output.getFileName().toString().equals(RomCreationService.OUTPUT_NAME), "sortie Waynes_World_FR.bin");
        check(Arrays.equals(Files.readAllBytes(input), usa), "ROM originale non modifiée");
        check(Arrays.equals(Files.readAllBytes(output), officialFrench), "service de création byte-for-byte");
        check(Files.mismatch(Path.of("Image.png"), Path.of("src/main/resources/ui/reference.png")) == -1, "référence visuelle importée sans retouche");
        var image = ImageIO.read(Path.of("src/main/resources/ui/reference.png").toFile());
        check(image.getWidth() == 1536 && image.getHeight() == 1024, "référence 1536 x 1024");
        check(Cheat.values().length == 4, "exactement quatre cheats");
        System.out.println("TOTAL: " + passed + " tests réussis");
    }

    private static void verifySingleCheat(byte[] rom, Cheat cheat) throws Exception {
        byte[] result = new TrainerPatch().apply(rom, Set.of(cheat));
        check(Arrays.equals(Arrays.copyOfRange(result, TrainerPatch.HOOK_OFFSET, TrainerPatch.HOOK_OFFSET + 6), TrainerPatch.TRAINER_CALL), "hook " + cheat.label());
        int cursor = TrainerPatch.TRAINER_OFFSET + 6;
        for (Cheat.Write write : cheat.writes()) {
            check((result[cursor] & 0xFF) == 0x13 && (result[cursor + 1] & 0xFF) == 0xFC &&
                    (result[cursor + 3] & 0xFF) == write.value() && (result[cursor + 6] & 0xFF) == write.address() / 256 &&
                    (result[cursor + 7] & 0xFF) == write.address() % 256, "écriture " + cheat.label() + " @ FF%04X".formatted(write.address()));
            cursor += 8;
        }
        check((result[cursor] & 0xFF) == 0x4E && (result[cursor + 1] & 0xFF) == 0x75, "RTS " + cheat.label());
        check(headerChecksum(result) == TrainerPatch.calculateChecksum(result), "checksum " + cheat.label());
    }
    private static int headerChecksum(byte[] rom) { return ((rom[0x18E] & 0xFF) << 8) | (rom[0x18F] & 0xFF); }
}
