import java.util.Random;

/**
 * @author Quentin Chevalier
 */
public class Champ {
	public int champ[][];// the playground
	private int sizeX;
	private int sizeY;
	public boolean gameOver;// is the player gameOver

	/**
	 * x-axis size getter.
	 * 
	 * @return x-axis size.
	 */
	public int getSizeX() {
		return this.sizeX;
	}

	/**
	 * y-axis size getter.
	 * 
	 * @return y-axis size.
	 */
	public int getSizeY() {
		return this.sizeY;
	}

	public int score = 0;// score for this game.
	private int nbMines;

	/**
	 * NbMines getter.
	 * 
	 * @return total number of mines.
	 */
	public int getNbMines() {
		return this.nbMines;
	}

	final static int[] easyDiff = { 12, 12, 16 };
	final static int[] mediumDiff = { 18, 18, 50 };
	final static int[] hardDiff = { 27, 27, 110 };
	final static int MINE = -1;
	private int nbMinesRestantes;
	Random rdm = new Random();
	public String difficulte = "EASY";

	/**
	 * Difficulty getter.
	 * 
	 * @return the current difficulty.
	 */
	public String getDifficulte() {
		return this.difficulte;
	}

	/**
	 * NbMinesRestantes getter.
	 * 
	 * @return the number of not exploded mines.
	 */
	public int getNbMinesRestantes() {
		return this.nbMinesRestantes;
	}

	/**
	 * NbMinesRestantes setter.
	 * 
	 * @param x number of mines
	 */
	public void setNbMinesRestantes(int x) {
		this.nbMinesRestantes = x;
	}

	/**
	 * Constructeur par defaut (easy)
	 */
	public Champ() {
		this(Commons.difficulty.EASY);
	}

	/**
	 * field initialization.
	 * 
	 * @param dimX    x-axis size.
	 * @param dimY    y-axis size.
	 * @param nbmines number of mines.
	 */
	private void creerChamp(int dimX, int dimY, int nbmines) {
		if (nbmines > dimX * dimY) {
			System.out.println("Trop de Mines, nb réduit");
			nbmines = dimX * dimY;
		}
		sizeX = dimX;
		sizeY = dimY;
		nbMines = nbmines;
		champ = new int[sizeX][sizeY];
		placeMines();
		nbMinesCases();
	}

	/**
	 * Constructor Champ with difficulty
	 * 
	 * @param difficulty field difficulty among EASY, MEDIUM, HARD and CUSTOM.
	 */
	public Champ(Commons.difficulty difficulty) {
		switch (difficulty) {
			case EASY:
				difficulte = "EASY";
				creerChamp(easyDiff[0], easyDiff[1], easyDiff[2]);
				break;
			case MEDIUM:
				difficulte = "MEDIUM";
				creerChamp(mediumDiff[0], mediumDiff[1], mediumDiff[2]);
				break;
			case HARD:
				difficulte = "HARD";
				creerChamp(hardDiff[0], hardDiff[1], hardDiff[2]);
				break;
			case CUSTOM:
				difficulte = "CUSTOM";
				creerChamp(Commons.customDiff[0], Commons.customDiff[1], Commons.customDiff[2]);
				break;
		}
	}

	public Champ(int x, int y) {
		creerChamp(x, y,0);
	}

	/**
	 * Place mines in the field
	 */
	void placeMines() {
		nbMinesRestantes = nbMines;
		int nbMinesAPlacer = nbMines;
		while (nbMinesAPlacer > 0) {
			int x = rdm.nextInt(sizeX);
			int y = rdm.nextInt(sizeY);
			if (this.champ[x][y] != MINE) {
				champ[x][y] = -1;
				nbMinesAPlacer--;
			}
		}
	}

	/**
	 * Update the field to add near mines information to squares.
	 */
	void nbMinesCases() {
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				if (this.champ[i][j] == MINE)
					continue;
				this.champ[i][j] = nbMinesPos(i, j);
			}
		}
	}

	/**
	 * toString method.
	 */
	public String toString() {
		String returnedString = "";
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				if (this.champ[i][j] == -1)
					returnedString += "X";
				else
					returnedString += this.champ[i][j];
			}
			returnedString += "\n";
		}
		return returnedString;
	}

	/**
	 * Calculate the number of mines around a position
	 * 
	 * @param x x position
	 * @param y y position
	 * @return sum of mines.
	 */
	public int nbMinesPos(int x, int y) {
		int s = 0;
		for (int k = -1; k < 2; k++) {
			for (int l = -1; l < 2; l++) {
				int posX = x + k;
				int posY = y + l;
				if (posX == -1 || posY == -1 || posX == x && posY == y || posX >= this.champ.length
						|| posY >= this.champ[0].length)
					continue;
				if (this.champ[posX][posY] == MINE)
					s++;
			}
		}
		return s;
	}
}
