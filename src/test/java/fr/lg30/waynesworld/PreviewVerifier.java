package fr.lg30.waynesworld;

import javax.imageio.ImageIO;
import java.io.File;

public final class PreviewVerifier {
    public static void main(String[] args) throws Exception {
        var expected = ImageIO.read(new File("Image.png"));
        var actual = ImageIO.read(new File("preview.png"));
        if (actual.getWidth() != 1536 || actual.getHeight() != 1024) throw new AssertionError("dimensions preview");
        for (int y = 0; y < 1024; y++) for (int x = 0; x < 1536; x++)
            if (actual.getRGB(x, y) != expected.getRGB(x, y)) throw new AssertionError("pixel différent à " + x + "," + y);
        System.out.println("PASS preview 1536 x 1024 rendu par PatcherPanel et pixel-identique");
    }
}
