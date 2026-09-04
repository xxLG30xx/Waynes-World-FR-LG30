package fr.lg30.waynesworld;

public final class RomValidator {
    public static final Hashes OFFICIAL_USA = new Hashes(1_048_576L, "d2cf6ebe",
            "f0e8150da96d61f7364a020d6c1df07c", "2b55be87cd53514b261828fd264108fbad1312cd");
    public boolean isCompatible(byte[] rom) { return OFFICIAL_USA.equals(Hashes.of(rom)); }
}
