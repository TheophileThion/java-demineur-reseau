import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputListener;

/**
 * @author Quentin Chevalier
 */

public class Case extends JPanel implements MouseInputListener {
    int case_value;

    public int getCase_value() {
        return this.case_value;
    }

    public boolean revealed, exploded;
    private boolean flagged;
    int abscisse;
    int ordonne;
    String txt = "";
    Color color = Color.darkGray;
    GUI gui;

    Color playerColor;

    Case(GUI gui, int val, int i, int j) {
        this.gui = gui;
        case_value = val;
        abscisse = i;
        ordonne = j;
        addMouseListener(this);
        setBackground(color);
        if (Client.connected) {
            if (revealed)
                setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED, playerColor, playerColor));
            else
                setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED));
        } else {
            if (revealed)
                setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED));
            else
                setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED));
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics gc) {
        super.paintComponent(gc);
        gc.setFont(new Font("open sans", Font.BOLD, 16));
        switch (case_value) {
            case 1:
                gc.setColor(Color.blue);
                break;
            case 2:
                gc.setColor(Color.getHSBColor(0.33f, 1, 0.5f));
                break;
            case 3:
                gc.setColor(Color.red);
                break;
            case 4:
                gc.setColor(Color.MAGENTA);
                break;
            case 5:
                gc.setColor(Color.orange);
                break;
            case 6:
                gc.setColor(Color.PINK);
                break;
            case 7:
                gc.setColor(Color.yellow);
                break;
            case 8:
                gc.setColor(Color.black);
                break;
            default:
                gc.setColor(Color.black);
                break;
        }
        gc.drawString(txt, 8, 17);

        if (exploded) {
            Image img = new ImageIcon(getClass().getResource("img/mine.png")).getImage();
            img = Commons.createResizedCopy(img, 24, 24, false);
            Icon icon = new ImageIcon(img);
            icon.paintIcon(this, gc, 0, 0);
        }
    }

    void drawLabel() {
        if (revealed) {
            switch (case_value) {
                case 0:
                    txt = "";
                    Commons.playSound("audio/plop.wav");
                    break;
                case -1:
                    txt = "";
                    exploded = true;
                    Commons.playSound("audio/bomb.wav");
                    setBorder(BorderFactory.createLineBorder(Color.red));
                    break;
                default:
                    txt = String.valueOf(case_value);
                    Commons.playSound("audio/plop.wav");
                    break;

            }
            color = Color.lightGray;
        } else
            color = flagged ? Color.red : Color.darkGray;
        setBackground(color);
        if (Client.connected) {
            if (revealed)
                setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED, playerColor, playerColor));
            else
                setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED));
        } else {
            if (revealed)
                setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED));
            else
                setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED));
        }
        if (exploded)
            setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED, Color.red, Color.red));
        repaint();
    }

    public void reveal() {
        if (revealed)
            return;
        revealed = true;
        if (!Client.connected) {
            gui.dem.getChamp().score++;
            gui.scoreLabel.setText("Score : " + gui.dem.getChamp().score);
        }
        drawLabel();
    }

    public void reveal(int id, int value) {
        if (revealed)
            return;
        revealed = true;
        case_value = value;
        playerColor = Client.colors.get(id);
        drawLabel();
    }

    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {

        if (!Client.connected && gui.dem.timer.timerVal == 0f)
            gui.dem.timer.runTimer();
        if (gui.dem.getChamp().gameOver) {
            if (Client.connected) {
                JOptionPane.showMessageDialog(this, "You're spectating, you cannot play.");
            } else {
                int response = JOptionPane.showConfirmDialog(this, "You're spectating, restart ?", "Game is ended",
                        JOptionPane.YES_NO_OPTION);
                if (response == 0)
                    gui.dem.startGame();
            }
            return;
        }
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (revealed)
                return;
            if (!Client.connected) {
                revealed = true;
                gui.extendDeminage(abscisse, ordonne);
                gui.dem.getChamp().score++;
                gui.scoreLabel.setText("Score : " + gui.dem.getChamp().score);
                if (case_value == -1) {
                    gui.dem.getChamp().setNbMinesRestantes(gui.dem.getChamp().getNbMinesRestantes() - 1);
                    System.out.println(gui.dem.getChamp().getNbMinesRestantes());
                    gui.nbMinesLabel.setText("Mines : " + gui.dem.getChamp().getNbMinesRestantes());
                    gui.dem.getChamp().gameOver = true;
                }
            } else {
                gui.client.send("case," + abscisse + ',' + ordonne);
            }
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            if (revealed)
                return;
            flagged = !flagged;
            System.out.println(flagged);
            drawLabel();
        }
        if (!Client.connected) {
            drawLabel();
            if (gui.dem.getChamp().gameOver) {
                gui.dem.timer.stopTimer();
                int response = JOptionPane.showConfirmDialog(gui, "Game Over, restart ?", "Game Over",
                        JOptionPane.YES_NO_OPTION);
                if (response == 0)
                    gui.dem.startGame();
            }
            boolean finish = gui.dem.checkSoloFinish();
            if (finish) {
                gui.dem.getChamp().gameOver = true;
                gui.winGame();
            }
        }

    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseDragged(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseMoved(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
