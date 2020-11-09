import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;

import java.awt.Color;

public class Client {

    public static String serverIp="localhost";
    public static Socket sock;
    private Demineur dem;
    public static Thread receiveThread = null;

    public static ConcurrentHashMap<Integer, String> players = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Integer, Integer> scores = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Integer, Commons.playerState> states = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Integer, Color> colors = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, Commons.difficulty> votedDifficulty = new ConcurrentHashMap<>();;

    public static boolean connected;
    public static int id_player = 0;
    public static Commons.connectionState stateClient = Commons.connectionState.NOT_CONNECTED;

    public Client(Demineur dem) {
        this.dem = dem;
        sock = null;
        receiveThread = null;
        players = new ConcurrentHashMap<>();
        scores = new ConcurrentHashMap<>();
        states = new ConcurrentHashMap<>();
        colors = new ConcurrentHashMap<>();
        votedDifficulty = new ConcurrentHashMap<>();
        connected = false;
        id_player = 0;
        stateClient = Commons.connectionState.NOT_CONNECTED;
    }

    public void open(String IP) {
        Thread th = new Thread(() -> {
            try {// ouverture de la socket et des streams
                serverIp = IP;
                sock = new Socket(IP, 7500);
                receiveThread = new Thread(() -> {
                    receive();
                });
                receiveThread.start();
                send("connect," + dem.playerName);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        th.start();
        SaveService.Save(dem);
    }

    public void send(String strToSend) {
        Thread th = new Thread(() -> {
            try {// ouverture de la socket et des streams
                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                out.writeUTF(strToSend);
            } catch (Exception e) {
                dem.disconnect();
            }
        });
        th.start();
        System.out.println("Sent : " + strToSend);
    }

    public void receive() {
        try {// ouverture de la socket et des streams
            DataInputStream in = new DataInputStream(sock.getInputStream());
            while (true) {
                String received = in.readUTF();
                String receivedVal = new String(received);
                switchState(receivedVal);
            }
        } catch (UnknownHostException e) {
            dem.disconnect();
        } catch (IOException e) {
            dem.disconnect();
        }
    }

    public void switchState(String received) throws NumberFormatException {
        System.out.println("Received : " + received);
        String[] data = received.split(",");
        String code = data[0];
        switch (code) {
            case "id_player":
                dem.joinLobby();
                if (stateClient == Commons.connectionState.NOT_CONNECTED) {
                    id_player = Integer.parseInt(data[1]);
                    stateClient = Commons.connectionState.WAITING;
                } else
                    System.out.println("Bad Reception" + code);
                send("consoleMessage," + dem.playerName + " is connected.");
                if (data[2].equals("Spectate")) {
                    stateClient = Commons.connectionState.SPECTATING;
                    dem.joinLobby();
                    dem.gui.updateUI();
                    dem.gui.container.remove(dem.gui.difficultyAsker);
                    dem.gui.gameCases = new Case[Integer.parseInt(data[3])][Integer.parseInt(data[4])];
                    dem.createMultiplayerGame(Integer.parseInt(data[3]), Integer.parseInt(data[4]), data[5],
                            Integer.parseInt(data[6]));// x-size,y-size,difficulty,nbMines
                    dem.getChamp().gameOver = true;
                } else {
                    dem.gui.askDifficulty();
                }
                break;
            case "champ":
                dem.gui.gameCases = new Case[Integer.parseInt(data[1])][Integer.parseInt(data[2])];
                dem.createMultiplayerGame(Integer.parseInt(data[1]), Integer.parseInt(data[2]), data[3],
                        Integer.parseInt(data[4]));// x-size,y-size,difficulty,nbMines
                stateClient = Commons.connectionState.PLAYING;
                break;
            case "playerStatus":
                players.put(Integer.parseInt(data[1]), data[2]);// name
                scores.put(Integer.parseInt(data[1]), Integer.parseInt(data[3]));// score
                colors.put(Integer.parseInt(data[1]),
                        new Color(Integer.parseInt(data[4]), Integer.parseInt(data[5]), Integer.parseInt(data[6])));// color
                states.put(Integer.parseInt(data[1]), Commons.playerState.valueOf(data[7]));// state
                dem.gui.drawStat();
                if (Integer.parseInt(data[1]) == id_player) {
                    dem.gui.scoreLabel.setText("Score : " + data[3]);
                }
                dem.gui.updateUI();
                break;
            case "voted":
                votedDifficulty.put(Integer.parseInt(data[1]), Commons.difficulty.valueOf(data[2]));
                break;
            case "case_reveal":
                int id = Integer.parseInt(data[1]);
                int i = Integer.parseInt(data[2]);
                int j = Integer.parseInt(data[3]);
                int value_case = Integer.parseInt(data[4]);
                dem.gui.gameCases[i][j].reveal(id, value_case);
                if (id == id_player && value_case == -1) {
                    JOptionPane.showMessageDialog(dem.gui, "Game Over");
                    dem.getChamp().gameOver = true;
                }
                break;
            case "nBMines":
                dem.gui.nbMinesLabel.setText("Mines : " + data[1]);
                break;
            case "winner":
                dem.getChamp().gameOver = true;
                if (Integer.parseInt(data[1]) == id_player)
                    dem.gui.winGame();
                else
                    dem.gui.loseGame(players.get(Integer.parseInt(data[1])));
                break;
            case "message":
                dem.gui.writeConsole(String.join(",", Arrays.asList(Arrays.copyOfRange(data, 2, data.length))), Integer.parseInt(data[1]));
                break;
            case "consoleMessage":
            dem.gui.writeConsole(String.join(",", Arrays.asList(Arrays.copyOfRange(data, 1, data.length))));
            break;
            default:
                System.out.println("Bad Reception : " + code);
                break;
        }
    }
}
