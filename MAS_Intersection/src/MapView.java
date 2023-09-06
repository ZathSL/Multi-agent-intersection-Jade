import agent.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

public class MapView extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int MAP_SIZE = 10;
    private static final int AGENT_SIZE = 30;
    private static final int GRID_SIZE = 5;
    private static final double ZOOM_FACTOR = 1.5; // Imposta il fattore di zoom

    private List<Position> agentPositions;
    private boolean gridEnabled = true;
    private double zoom = 1.0; // Imposta lo zoom iniziale
    private int offsetX = 0;
    private int offsetY = 0;

    public MapView(List<Position> agentPositions) {
        this.agentPositions = agentPositions;
        setPreferredSize(new java.awt.Dimension(MAP_SIZE * AGENT_SIZE, MAP_SIZE * AGENT_SIZE));

        // Aggiungi un KeyListener per gestire il zoom in e fuori
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_ADD) {
                    zoomIn();
                } else if (e.getKeyCode() == KeyEvent.VK_MINUS || e.getKeyCode() == KeyEvent.VK_SUBTRACT) {
                    zoomOut();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        setFocusable(true);
        requestFocus();
    }

    public void updateAgentPositions(List<Position> agentPositions) {
        this.agentPositions = agentPositions;
        repaint();
    }

    public void toggleGrid(boolean enableGrid) {
        this.gridEnabled = enableGrid;
        repaint();
    }

    public void zoomIn() {
        zoom *= ZOOM_FACTOR;
        repaint();
    }

    public void zoomOut() {
        zoom /= ZOOM_FACTOR;
        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int mapWidth = getWidth();
        int mapHeight = getHeight();
        int squareSize = (int) (Math.min(mapWidth, mapHeight) / (MAP_SIZE * zoom));

        if (gridEnabled) {
            for (int i = 0; i < MAP_SIZE; i++) {
                for (int j = 0; j < MAP_SIZE; j++) {
                    int x = i * squareSize + offsetX;
                    int y = j * squareSize + offsetY;
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x, y, squareSize, squareSize);
                }
            }
        }

        for (Position agentPosition : agentPositions) {
            int x = (int) (agentPosition.getCurrentlyPositionX() * squareSize * zoom) + offsetX;
            int y = (int) (agentPosition.getCurrentlyPositionY() * squareSize * zoom) + offsetY;

            g.setColor(Color.RED);
            g.fillOval(x, y, (int) (squareSize * zoom), (int) (squareSize * zoom));

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String agentName = agentPosition.getAid().getName().substring(0, 9);
            int nameX = x + (int) (squareSize * zoom / 2) - g.getFontMetrics().stringWidth(agentName) / 2;
            int nameY = y + (int) (squareSize * zoom / 2) + g.getFontMetrics().getHeight() / 3;
            g.drawString(agentName, nameX, nameY);
        }
    }
}
