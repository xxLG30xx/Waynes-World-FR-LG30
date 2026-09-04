package fr.lg30.waynesworld;

/** A selectable trainer operation. Addresses are offsets in 0xFF0000 RAM. */
public enum Cheat {
    INFINITE_TIME("Temps infini = 999", new Write(0x01BC, 0x09), new Write(0x01BD, 0x09), new Write(0x01BE, 0x09)),
    INVINCIBILITY("Invincibilité", new Write(0x00D5, 0x05)),
    UNLIMITED_SCHWING("Schwing illimité", new Write(0x011F, 0x01)),
    RAPID_FIRE("Tir rapide / PCB illimité", new Write(0x00D1, 0x03));

    public record Write(int address, int value) {}
    private final String label;
    private final Write[] writes;

    Cheat(String label, Write... writes) { this.label = label; this.writes = writes; }
    public String label() { return label; }
    public Write[] writes() { return writes.clone(); }
}
