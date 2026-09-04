package fr.lg30.waynesworld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

public final class RomCreationService {
    public static final String OUTPUT_NAME = "Waynes_World_FR.bin";
    private final RomValidator validator = new RomValidator();

    public byte[] createBytes(byte[] source, boolean translation, Set<Cheat> cheats) throws IOException {
        if (!validator.isCompatible(source)) throw new IOException("ROM incompatible : sélectionnez la ROM USA officielle.");
        byte[] result = translation ? new FrenchTranslationPatch().apply(source) : source.clone();
        return new TrainerPatch().apply(result, cheats);
    }

    public Path create(Path source, Path destination, boolean translation, Set<Cheat> cheats) throws IOException {
        byte[] original = Files.readAllBytes(source);
        byte[] result = createBytes(original, translation, cheats);
        Files.write(destination, result, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        if (!Hashes.of(original).equals(Hashes.of(Files.readAllBytes(source))))
            throw new IOException("La ROM source a été modifiée de façon inattendue.");
        return destination;
    }
}
