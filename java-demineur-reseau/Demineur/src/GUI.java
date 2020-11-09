
import javax.swing.*;
import javax.swing.border.SoftBevelBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.AttributeSet.FontAttribute;

import java.awt.event.ActionListener;
import java.io.Console;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.text.NumberFormat.Style;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.*;

/**
 * @author Quentin Chevalier
 */
public class GUI extends JPanel implements ActionListener {

    public Demineur dem;
    public Case[][] gameCases;
    public JPanel container = new JPanel();
    public JLabel nbMinesLabel = new JLabel();
    public JLabel scoreLabel = new JLabel();
    public JLabel playerNameLabel;
    public JLabel gameTime;
    JLabel difficulte;
    public Client client;
    JPanel topPanel;
    public JPanel difficultyAsker = new JPanel();
    public JPanel listeJoueurPanel = new JPanel();
    JScrollBar scrollBarConsole;
    private JPanel globalPanel = new JPanel();
    private JTextPane console = new JTextPane();
    private JTextField chat = new JTextField();
    private String buffConsole = "";

    /**
     * GUI constructor, does not draw any GUI.
     * 
     * @param demineur Main demineur instance.
     */
    GUI(Demineur demineur, Client client) {
        dem = demineur;
        gameCases = new Case[dem.getChamp().getSizeX()][dem.getChamp().getSizeY()];
        this.client = client;
    }

    /**
     * The game's menu Bar.
     * 
     * @return the game's menu bar as a JMenuBar.
     */
    public JMenuBar menuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Game");
        JMenuItem setName = new JMenuItem("Change Name");
        setName.setActionCommand("setName");
        setName.addActionListener(this);
        JMenuItem connect = new JMenuItem("Connect");
        connect.setActionCommand("Connect");
        connect.addActionListener(this);
        JMenuItem Disconnect = new JMenuItem("Disconnect");
        Disconnect.setActionCommand("Disconnect");
        Disconnect.addActionListener(this);
        JMenuItem Quit = new JMenuItem("Quit");
        Quit.setActionCommand("Quit");
        Quit.addActionListener(this);
        JMenu createGame = new JMenu("Create Solo Game");
        JMenuItem easyGame = new JMenuItem("EASY");
        easyGame.setActionCommand("easyGame");
        easyGame.addActionListener(this);
        createGame.add(easyGame);
        JMenuItem mediumGame = new JMenuItem("MEDIUM");
        mediumGame.setActionCommand("mediumGame");
        mediumGame.addActionListener(this);
        createGame.add(mediumGame);
        JMenuItem hardGame = new JMenuItem("HARD");
        hardGame.setActionCommand("hardGame");
        hardGame.addActionListener(this);
        createGame.add(hardGame);
        JMenuItem customGame = new JMenuItem("CUSTOM");
        customGame.setActionCommand("customGame");
        customGame.addActionListener(this);
        createGame.add(customGame);
        if (!Client.connected) {
            menu.add(setName);
            menu.add(createGame);
            menu.add(connect);
        }
        if (Client.connected)
            menu.add(Disconnect);
        menu.add(Quit);
        bar.add(menu);
        return bar;
    }

    public JPanel drawTopPanel() {
        JPanel topPanel = new JPanel();
        FlowLayout top = new FlowLayout(FlowLayout.CENTER, 100, 20);
        topPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        if (!Client.connected) {
            nbMinesLabel = new JLabel("Mines : " + dem.getChamp().getNbMinesRestantes());
            difficulte = new JLabel("Difficulty : " + dem.getChamp().getDifficulte());
            gameTime = new JLabel("Time : 0");
        }
        scoreLabel = new JLabel("Score : " + dem.getChamp().score);
        playerNameLabel = new JLabel("Player : " + dem.playerName);
        topPanel.setLayout(top);
        topPanel.add(playerNameLabel);
        topPanel.add(scoreLabel);
        if (!Client.connected) {
            topPanel.add(nbMinesLabel);
            topPanel.add(difficulte);
            topPanel.add(gameTime);
        }
        return topPanel;
    }

    JPanel drawChat() {
        JPanel botPane = new JPanel();
        JPanel chatContainer = new JPanel();
        JPanel consolePanel = new JPanel();
        JPanel selectionChatPanel = new JPanel();
        botPane.setLayout(new BoxLayout(botPane, BoxLayout.Y_AXIS));
        console.setEditable(false);
        console.setPreferredSize(new Dimension(200, 400));
        console.setMaximumSize(new Dimension(200, 400));
        JScrollPane scrollConsole = new JScrollPane(console);
        scrollConsole.setPreferredSize(new Dimension(200, 400));
        scrollConsole.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollBarConsole = scrollConsole.getVerticalScrollBar();
        scrollConsole.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        consolePanel.add(scrollConsole);
        botPane.add(consolePanel);
        chat.setPreferredSize(new Dimension(200, 20));
        selectionChatPanel.add(chat);
        botPane.add(selectionChatPanel);
        chat.setActionCommand("Chat");
        chat.addActionListener(this);
        chat.setBackground(Color.lightGray);
        console.setBackground(Color.lightGray);
        consolePanel.setBackground(Color.gray);
        selectionChatPanel.setBackground(Color.gray);
        chatContainer.add(botPane);
        chatContainer.setBackground(Color.gray);
        return chatContainer;
    }

    void drawStat() {
        globalPanel.remove(listeJoueurPanel);
        listeJoueurPanel = new JPanel();
        listeJoueurPanel.setPreferredSize(new Dimension(200, 450));
        listeJoueurPanel.setBackground(Color.lightGray);
        listeJoueurPanel.setLayout(new BoxLayout(listeJoueurPanel, BoxLayout.Y_AXIS));
        ArrayList<Integer> playerByKey = new ArrayList<>(Client.players.keySet());
        Collections.sort(playerByKey);
        playerByKey.forEach(key -> {
            Color col = Client.colors.get(key);
            String name = Client.players.get(key);
            int score = Client.scores.get(key);
            String state = Client.states.get(key).name();
            if (!(state == "DISCONNECTED" && score == 0)) {
                JLabel txt = new JLabel("" + name + " : " + score + "            " + state);
                txt.setForeground(col);
                listeJoueurPanel.add(txt);
            }
        });
        globalPanel.add(listeJoueurPanel, BorderLayout.WEST);
        globalPanel.updateUI();
    }

    /**
     * Add all the game's gui to this object.
     */
    public void Draw_main() {
        dem.setJMenuBar(menuBar());
        topPanel = drawTopPanel();
        BorderLayout bl = new BorderLayout();
        container.setSize(500, 500);
        if (!Client.connected) {
            JPanel midPanel = GUImidPanel();
            container.add(midPanel);
        }
        JPanel botPane = new JPanel();
        if (Client.connected) {
            botPane = drawChat();
        }
        globalPanel.setLayout(new BorderLayout());
        globalPanel.add(container, BorderLayout.CENTER);
        if (Client.connected) {
            globalPanel.add(listeJoueurPanel, BorderLayout.LINE_START);
            globalPanel.add(botPane, BorderLayout.EAST);
        }
        container.setBackground(Color.gray);
        JScrollPane scrollPane = new JScrollPane(globalPanel);
        setLayout(bl);
        add(topPanel, BorderLayout.PAGE_START);
        add(scrollPane, BorderLayout.CENTER);
        updateUI();
    }

    /**
     * Generates the Grid of the demineur.
     * 
     * @return the gui panel at the center as a JPanel.
     */
    JPanel GUImidPanel() {
        JPanel midPanel = new JPanel();
        GridLayout grid = new GridLayout(dem.getChamp().getSizeX(), dem.getChamp().getSizeY());
        grid.setVgap(0);
        grid.setHgap(0);
        midPanel.setLayout(grid);
        System.out.println(gameCases.length);
        for (int i = 0; i < dem.getChamp().getSizeX(); i++) {
            for (int j = 0; j < dem.getChamp().getSizeY(); j++) {
                JPanel caseJPanel = new JPanel();
                Case caseDemineur = new Case(this, dem.getChamp().champ[i][j], i, j);
                gameCases[i][j] = caseDemineur;
                caseDemineur.setPreferredSize(new Dimension(24, 24));
                caseJPanel.setLayout(new BorderLayout());
                caseJPanel.add(caseDemineur, BorderLayout.CENTER);
                midPanel.add(caseJPanel);
            }
        }
        midPanel.setSize(new Dimension(600, 600));
        return midPanel;
    }

    public void winGame() {
        JPanel on_est_pas_des_animaux = new JPanel();
        on_est_pas_des_animaux.setLayout(new BorderLayout());
        ImageIcon win = new ImageIcon(getClass().getResource("img/you_win.png"));
        JLabel jl = new JLabel("Restart ?");
        ImageIcon img = new ImageIcon(Commons.createResizedCopy(win.getImage(), 300, 300, false));
        on_est_pas_des_animaux.add(new JLabel(img), BorderLayout.PAGE_START);
        on_est_pas_des_animaux.add(jl, BorderLayout.PAGE_END);
        jl.setHorizontalAlignment(JLabel.CENTER);
        int response = JOptionPane.showOptionDialog(this, on_est_pas_des_animaux, "YOU WIN", JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (response == 0) {
            if (!Client.connected)
                dem.startGame();
            else
                dem.restartMultiplayer();
        }
    }

    public void loseGame(String player) {
        JPanel on_est_pas_des_animaux = new JPanel();
        on_est_pas_des_animaux.setLayout(new BorderLayout());
        ImageIcon win = new ImageIcon(getClass().getResource("img/you_lose.png"));
        JLabel jl = new JLabel(player + " won !\n Restart ?");
        ImageIcon img = new ImageIcon(Commons.createResizedCopy(win.getImage(), 300, 300, false));
        on_est_pas_des_animaux.add(new JLabel(img), BorderLayout.PAGE_START);
        on_est_pas_des_animaux.add(jl, BorderLayout.PAGE_END);
        jl.setHorizontalAlignment(JLabel.CENTER);
        int response = JOptionPane.showOptionDialog(this, on_est_pas_des_animaux, "YOU LOSE", JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (response == 0) {
            if (!Client.connected)
                dem.startGame();
            else
                dem.restartMultiplayer();
        }
    }

    public void drawMultiplayerGame() {
        nbMinesLabel = new JLabel("Mines : " + dem.getChamp().getNbMinesRestantes());
        difficulte = new JLabel("Difficulty : " + dem.getChamp().getDifficulte());
        topPanel.add(nbMinesLabel);
        topPanel.add(difficulte);
        Client.stateClient = Commons.connectionState.PLAYING;
        JPanel midPanel = GUImidPanel();
        container.add(midPanel);
        updateUI();
    }

    public void askDifficulty() {
        difficultyAsker = new JPanel();
        difficultyAsker.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
        BoxLayout boxLayout = new BoxLayout(difficultyAsker, 1);
        difficultyAsker.setLayout(boxLayout);
        JButton easy = new JButton("EASY");
        easy.addActionListener(this);
        easy.setActionCommand("easyMultiplayer");
        easy.setBackground(Color.lightGray);
        JButton medium = new JButton("MEDIUM");
        medium.addActionListener(this);
        medium.setActionCommand("mediumMultiplayer");
        medium.setBackground(Color.lightGray);
        JButton hard = new JButton("HARD");
        hard.addActionListener(this);
        hard.setActionCommand("hardMultiplayer");
        hard.setBackground(Color.lightGray);
        difficultyAsker.setBackground(Color.darkGray);
        difficultyAsker.setPreferredSize(new Dimension(250, 150));
        JLabel lbl = new JLabel("Vote for desired difficulty");
        lbl.setFont(new FontUIResource("open sans", 0, 16));
        lbl.setForeground(Color.white);
        difficultyAsker.add(Box.createHorizontalStrut(3));
        difficultyAsker.add(lbl);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        difficultyAsker.add(new JLabel(" "));
        difficultyAsker.add(easy);
        difficultyAsker.add(Box.createHorizontalStrut(3));
        easy.setAlignmentX(Component.CENTER_ALIGNMENT);
        difficultyAsker.add(medium);
        difficultyAsker.add(Box.createHorizontalStrut(3));
        medium.setAlignmentX(Component.CENTER_ALIGNMENT);
        difficultyAsker.add(hard);
        hard.setAlignmentX(Component.CENTER_ALIGNMENT);
        difficultyAsker.setAlignmentX(Component.CENTER_ALIGNMENT);
        difficultyAsker.add(Box.createHorizontalStrut(3));
        container.add(difficultyAsker, BorderLayout.CENTER);
        updateUI();
    }

    /**
     * Extends the click to the blank area around it.
     * 
     * @param i the position along the x-axis
     * @param j the position along the y-axis
     */
    void extendDeminage(int i, int j) {
        gameCases[i][j].reveal();
        if (gameCases[i][j].getCase_value() == 0) {
            for (int k = -1; k < 2; k++) {
                for (int l = -1; l < 2; l++) {
                    int posX = i + k;
                    int posY = j + l;
                    if (posX == -1 || posY == -1 || posX == i && posY == j || posX >= dem.getChamp().getSizeX()
                            || posY >= dem.getChamp().getSizeY())
                        continue;
                    if (!gameCases[posX][posY].revealed)
                        extendDeminage(posX, posY);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case "Quit":
                System.exit(0);
                break;
            case "setName":
                String txt = JOptionPane.showInputDialog(null, "New Name");
                txt = txt.replaceAll(",", "");
                dem.playerName = txt;
                playerNameLabel.setText("Player : " + txt);
                dem.saveGame();
                break;
            case "Connect":
                JPanel connectPanel = new JPanel();
                connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.PAGE_AXIS));
                connectPanel.add(new JLabel("IP / Domain name"));
                JTextField domain = new JTextField(Client.serverIp);
                connectPanel.add(domain);
                connectPanel.setPreferredSize(new Dimension(120, 50));
                int response = JOptionPane.showOptionDialog(this, connectPanel, "Connect to server",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (response == 0) {
                    Client.connected = true;
                    client.open(domain.getText().toString());
                }
                break;
            case "Disconnect":
                try {
                    client.send("disconnect");
                    Client.sock.close();
                } catch (IOException e1) {

                }
                dem.disconnect();
                break;
            case "easyMultiplayer":
                client.send("difficulty,EASY");
                client.send("consoleMessage," + dem.playerName + " voted for an easy game.");
                container.remove(difficultyAsker);
                updateUI();
                break;
            case "mediumMultiplayer":
                client.send("difficulty,MEDIUM");
                client.send("consoleMessage," + dem.playerName + " voted for a medium game.");
                container.remove(difficultyAsker);
                updateUI();
                break;
            case "hardMultiplayer":
                client.send("difficulty,HARD");
                client.send("consoleMessage," + dem.playerName + " voted for a hard game.");
                container.remove(difficultyAsker);
                updateUI();
                break;
            case "easyGame":
                dem.gameDifficulty = Commons.difficulty.EASY;
                dem.startGame();
                break;
            case "mediumGame":
                dem.gameDifficulty = Commons.difficulty.MEDIUM;
                dem.startGame();
                break;
            case "hardGame":
                dem.gameDifficulty = Commons.difficulty.HARD;
                dem.startGame();
                break;
            case "customGame":
                JPanel customPanel = new JPanel();
                customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.PAGE_AXIS));
                customPanel.add(new JLabel("Height"));
                JFormattedTextField height = new JFormattedTextField(NumberFormat.getIntegerInstance());
                height.setValue(Commons.customDiff[0]);
                customPanel.add(height);
                customPanel.add(new JLabel("Width"));
                JFormattedTextField width = new JFormattedTextField(NumberFormat.getIntegerInstance());
                width.setValue(Commons.customDiff[1]);
                customPanel.add(width);
                customPanel.add(new JLabel("Nombre de Mines"));
                JFormattedTextField mines = new JFormattedTextField(NumberFormat.getIntegerInstance());
                mines.setValue(Commons.customDiff[2]);
                customPanel.add(mines);
                customPanel.setPreferredSize(new Dimension(120, 120));
                int reponse = JOptionPane.showOptionDialog(this, customPanel, "Custom Game",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (reponse == 0) {
                    dem.gameDifficulty = Commons.difficulty.CUSTOM;
                    Commons.customDiff = new int[] { Integer.parseInt(height.getValue().toString()),
                            Integer.parseInt(width.getValue().toString()),
                            Integer.parseInt(mines.getValue().toString()) };
                    dem.startGame();
                }
                break;
            case "Chat":
                if (chat.getText().equals(""))
                    break;
                client.send("message," + Client.id_player + "," + chat.getText());
                chat.setSelectionColor(new Color(50, 50, 50));
                chat.setSelectedTextColor(new Color(50, 50, 50));
                chat.setText("");
                break;
        }
    }

    public void writeConsole(String message, int id_joueur) {
        Color col = Client.colors.get(id_joueur);

        Font font = new Font("Arial", Font.PLAIN, 13);
        console.setFont(font);
        StyledDocument doc = console.getStyledDocument();
        javax.swing.text.Style style = console.addStyle("", null);
        StyleConstants.setForeground(style, col);
        try {
            doc.insertString(doc.getLength(), Client.players.get(id_joueur) + " : " + message + '\n', style);
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            scrollBarConsole.setValue(scrollBarConsole.getMaximum());
        } catch (Exception e) {
            return;
        }
    }

    public void writeConsole(String message) {
        Font font = new Font("Arial", Font.PLAIN, 13);
        console.setFont(font);
        StyledDocument doc = console.getStyledDocument();
        javax.swing.text.Style style = console.addStyle("", null);
        StyleConstants.setForeground(style, Color.black);
        try {
            doc.insertString(doc.getLength(), message + '\n', style);
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            scrollBarConsole.setValue(scrollBarConsole.getMaximum());
        } catch (Exception e) {
            return;
        }
    }

}
