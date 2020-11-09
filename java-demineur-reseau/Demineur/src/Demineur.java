import java.awt.Font;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * @author Quentin Chevalier MAIN
 */
@SuppressWarnings("serial")
public class Demineur extends JFrame {
	public GameTimer timer;
	private Champ champ;
	public String playerName;
	public Commons.difficulty gameDifficulty = Commons.difficulty.MEDIUM;
	public GUI gui;
	private Client client;
	
	public Demineur(Champ champ) {
		this.champ = champ;
	}

	public Champ getChamp() {
		return this.champ;
	}

	/**
	 * Demineur constructor, starts a game.
	 */
	public Demineur() {
		setTitle("Démineur par Quentin & Thionman");
		playerName = "Player" + new Random().nextInt(100);
		loadGame();
		setIconImage(new ImageIcon(getClass().getResource("img/mine.png")).getImage());
		setFont(new Font("open sans", Font.PLAIN, 12));
		Commons.playSound("audio/music.wav", true);
		setSize(900, 900);
		setLocation(WIDTH, HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client = new Client(this);
		startGame();
	}

	/**
	 * Starts the game.
	 */
	public void startGame() {
		champ = new Champ(gameDifficulty);
		saveGame();
		System.out.println(champ);
		gui = new GUI(this,client);
		timer = new GameTimer(gui);
		gui.Draw_main();
		setContentPane(gui);
		setVisible(true);
		gui.updateUI();
		timer.resetTimer();
	}


	public void joinLobby(){
		gui = new GUI(this,client);
		gui.Draw_main();
		setContentPane(gui);
		setVisible(true);
		gui.updateUI();
	}

	public void createMultiplayerGame(int x, int y, String difficulty, int nbMines) {
		champ = new Champ(x, y);
		champ.difficulte = difficulty;
		champ.setNbMinesRestantes(nbMines);
		gui.drawMultiplayerGame();
	}

	public void restartMultiplayer(){
		String serverIP = Client.serverIp;
		client = new Client(this);
		Client.connected = true;
		client.open(serverIP);
		joinLobby();
	}

	public void disconnect(){
		Client.receiveThread=null;
		client = new Client(this);
		champ = new Champ(gameDifficulty);
		saveGame();
		System.out.println(champ);
		remove(gui);
		gui = new GUI(this,client);
		timer = new GameTimer(gui);
		Client.connected=false;
		gui.Draw_main();
		setContentPane(gui);
		setVisible(true);
		gui.updateUI();
		timer.resetTimer();
	}
	/**
	 * For solo play, triggers end of game.
	 * 
	 * @return true if the game is finished.
	 */
	public Boolean checkSoloFinish() {
		if (champ.score == champ.getSizeX() * champ.getSizeY() - champ.getNbMines() && !champ.gameOver) {
			timer.stopTimer();
			return true;
		}
		return false;
	}

	/**
	 * Load saved data.
	 */
	public void loadGame() {
		SaveService.Load(this);
	}

	/**
	 * Save game data.
	 */
	public void saveGame() {
		SaveService.Save(this);
	}

	public static void main(String[] args) {
		new Demineur();
	}
}
