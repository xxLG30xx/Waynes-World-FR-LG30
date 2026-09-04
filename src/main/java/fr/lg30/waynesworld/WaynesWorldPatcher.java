package fr.lg30.waynesworld;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

public final class WaynesWorldPatcher {
    private WaynesWorldPatcher() {}
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && "--render-preview".equals(args[0])) { renderPreview(); return; }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Wayne’s World ROM Patcher LG30");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new PatcherPanel());
            frame.pack(); frame.setMinimumSize(new Dimension(1100, 733)); frame.setLocationRelativeTo(null); frame.setVisible(true);
        });
    }

    private static void renderPreview() throws Exception {
        final Exception[] failure = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                PatcherPanel panel = new PatcherPanel(); panel.setPreviewState(true);
                panel.setSize(PatcherPanel.DESIGN_WIDTH, PatcherPanel.DESIGN_HEIGHT);
                BufferedImage image = new BufferedImage(PatcherPanel.DESIGN_WIDTH, PatcherPanel.DESIGN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics(); panel.printAll(graphics); graphics.dispose();
                ImageIO.write(image, "PNG", new File("preview.png"));
            } catch (Exception e) { failure[0] = e; }
        });
        if (failure[0] != null) throw failure[0];
    }
}
