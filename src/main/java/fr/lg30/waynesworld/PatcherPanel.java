package fr.lg30.waynesworld;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

/** The real, interactive application surface and the source of preview.png. */
public final class PatcherPanel extends JPanel {
    public static final int DESIGN_WIDTH = 1536, DESIGN_HEIGHT = 1024;
    private final BufferedImage reference;
    private final Set<Cheat> cheats = EnumSet.noneOf(Cheat.class);
    private boolean translation = true;
    private boolean previewState;
    private Path source;
    private String error;

    public PatcherPanel() {
        try (var in = getClass().getResourceAsStream("/ui/reference.png")) {
            if (in == null || (reference = ImageIO.read(in)) == null) throw new IOException("image absente");
        } catch (IOException e) { throw new IllegalStateException("Ressource graphique Image.png invalide", e); }
        setBackground(Color.BLACK);
        setPreferredSize(new java.awt.Dimension(DESIGN_WIDTH, DESIGN_HEIGHT));
        addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { click(toDesignX(e.getX()), toDesignY(e.getY())); } });
    }

    /** Gives the deterministic reference state used by the headless visual regression preview. */
    public void setPreviewState(boolean enabled) { previewState = enabled; repaint(); }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(reference, 0, 0, getWidth(), getHeight(), null);
        if (!previewState && source == null) paintUnselectedState(g);
        if (!previewState) paintOptionState(g);
        g.dispose();
    }

    private void paintUnselectedState(Graphics2D screen) {
        double sx = getWidth() / (double) DESIGN_WIDTH, sy = getHeight() / (double) DESIGN_HEIGHT;
        Graphics2D g = (Graphics2D) screen.create(); g.scale(sx, sy);
        g.setColor(new Color(7, 17, 24)); g.fillRoundRect(481, 365, 685, 130, 10, 10);
        g.setFont(new Font("SansSerif", Font.PLAIN, 20)); g.setColor(Color.WHITE);
        g.drawString("Fichier sélectionné :", 493, 402); g.drawString("Taille :", 493, 440); g.drawString("Statut :", 493, 477);
        g.setColor(new Color(145, 155, 165)); g.drawString("Aucun fichier", 705, 402); g.drawString("—", 705, 440);
        g.drawString("Sélectionnez la ROM USA officielle", 705, 477);
        g.setColor(new Color(47, 68, 59)); g.fillRoundRect(1120, 597, 371, 80, 10, 10);
        g.setColor(new Color(190, 200, 194)); g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.drawString("▶  CRÉER MA ROM", 1180, 648);
        if (error != null) { g.setColor(new Color(245, 75, 75)); g.setFont(new Font("SansSerif", Font.BOLD, 18)); g.drawString(error, 705, 477); }
        g.dispose();
    }

    private void paintOptionState(Graphics2D screen) {
        int[] ys = {606, 676, 751, 840};
        Graphics2D g = (Graphics2D) screen.create();
        g.scale(getWidth() / (double) DESIGN_WIDTH, getHeight() / (double) DESIGN_HEIGHT);
        if (!translation) {
            g.setColor(new Color(9, 20, 28)); g.fillRoundRect(49, 595, 29, 29, 5, 5);
            g.setColor(new Color(120, 140, 155)); g.drawRoundRect(49, 595, 28, 28, 5, 5);
        }
        for (int i = 0; i < Cheat.values().length; i++) if (cheats.contains(Cheat.values()[i])) {
            g.setColor(new Color(50, 145, 255)); g.fillRoundRect(546, ys[i] - 10, 21, 21, 4, 4);
            g.setColor(Color.WHITE); g.setFont(new Font("SansSerif", Font.BOLD, 17)); g.drawString("✓", 548, ys[i] + 7);
        }
        g.dispose();
    }

    private void click(int x, int y) {
        if (x >= 48 && x <= 466 && y >= 385 && y <= 465) chooseRom();
        else if (x >= 48 && x <= 360 && y >= 590 && y <= 630) { translation = !translation; repaint(); }
        else if (x >= 535 && x <= 1055 && y >= 575 && y <= 895) {
            int[] centers = {606, 676, 751, 850}; int best = 0;
            for (int i = 1; i < centers.length; i++) if (Math.abs(y - centers[i]) < Math.abs(y - centers[best])) best = i;
            Cheat cheat = Cheat.values()[best]; if (!cheats.remove(cheat)) cheats.add(cheat); repaint();
        } else if (x >= 1115 && x <= 1495 && y >= 590 && y <= 685 && source != null) createRom();
    }

    private void chooseRom() {
        JFileChooser chooser = new JFileChooser(); chooser.setFileFilter(new FileNameExtensionFilter("ROM Mega Drive (*.md, *.bin)", "md", "bin"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path candidate = chooser.getSelectedFile().toPath();
        try {
            if (!new RomValidator().isCompatible(Files.readAllBytes(candidate))) {
                source = null; error = "✕ ROM non compatible"; JOptionPane.showMessageDialog(this,
                        "Cette ROM n’est pas la version USA officielle attendue.", "ROM incompatible", JOptionPane.ERROR_MESSAGE);
            } else { source = candidate; error = null; }
        } catch (IOException e) { source = null; error = "✕ Lecture impossible"; }
        repaint();
    }

    private void createRom() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(RomCreationService.OUTPUT_NAME));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path destination = chooser.getSelectedFile().toPath();
        try {
            new RomCreationService().create(source, destination, translation, EnumSet.copyOf(cheats));
            JOptionPane.showMessageDialog(this, "ROM créée avec succès :\n" + destination, "PARTY ON!", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) { JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); }
    }

    private int toDesignX(int x) { return (int) Math.round(x * DESIGN_WIDTH / (double) getWidth()); }
    private int toDesignY(int y) { return (int) Math.round(y * DESIGN_HEIGHT / (double) getHeight()); }
}
