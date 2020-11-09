import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import java.io.*;
import java.awt.Color;

public class DemineurServer {

	/**
	 *
	 */

	public enum playerState {
		CONNECTED, READY, PLAYING, SPECTATING, DISCONNECTED
	}

	public int[][] revealedCases;

	public ArrayList<Integer> id_given = new ArrayList<>(Arrays.asList(0));

	public ConcurrentHashMap<Integer, String> players = new ConcurrentHashMap<>();

	public ConcurrentHashMap<Integer, Integer> scores = new ConcurrentHashMap<>();

	public ConcurrentHashMap<Integer, playerState> states = new ConcurrentHashMap<>();

	public ConcurrentHashMap<Socket, Integer> sockets = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Socket, Thread> threads = new ConcurrentHashMap<>();

	public ConcurrentHashMap<Integer, Color> colors = new ConcurrentHashMap<>();

	public ConcurrentHashMap<Integer, Commons.difficulty> votedDifficulty = new ConcurrentHashMap<>();

	public serverStates serverState = serverStates.AWAITING_PLAYERS;

	void reset() {
		id_given = new ArrayList<>(Arrays.asList(0));
		players = new ConcurrentHashMap<>();
		scores = new ConcurrentHashMap<>();
		states = new ConcurrentHashMap<>();
		sockets = new ConcurrentHashMap<>();
		threads = new ConcurrentHashMap<>();
		colors = new ConcurrentHashMap<>();
		votedDifficulty = new ConcurrentHashMap<>();
		serverState = serverStates.AWAITING_PLAYERS;
	}

	public enum serverStates {
		AWAITING_PLAYERS, READY_TO_LAUNCH, IN_GAME, ENDED
	}

	ServerSocket gestSock;

	public Champ champ;

	public DemineurServer() {
		System.out.println("Starting up");
		try {
			gestSock = new ServerSocket(7500);
		} catch (IOException e) {
			return;
		}
		champ = null;
		serverState = serverStates.AWAITING_PLAYERS;
		open();
	}

	public void open() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Socket client = gestSock.accept();
						System.out.println("Connexion cliente reçue");
						sockets.put(client, id_given.get(id_given.size() - 1));
						id_given.add(id_given.get(id_given.size() - 1) + 1);
						receive(client);
					} catch (IOException e) {
						return;
					}
				}
			}
		});
		t.start();
	}

	public synchronized void send(String strToSend, Socket sock) {
		Thread sendThread = new Thread(() -> {
			if (sock.isClosed()) {
				try {
					int id = sockets.get(sock);
					states.put(id, playerState.DISCONNECTED);
				} catch (Exception e) {
					return;
				}
			} else {
				DataOutputStream sortie;
				try {
					sortie = new DataOutputStream(sock.getOutputStream());
					sortie.writeUTF(strToSend);
				} catch (IOException e) {
					try {
						int id = sockets.get(sock);
						states.put(id, playerState.DISCONNECTED);
						sock.close();
					} catch (Exception ex) {
						return;
					}
					if ((serverState == serverStates.IN_GAME || numberOfConnectedPlayers() == 0
							|| numberOfPlayingPlayers() == 1) && serverState != serverStates.ENDED) {
						serverState = serverStates.ENDED;
						checkEnd();
					}
				}
			}
		});
		sendThread.start();
		System.out.println("Sent : " + strToSend);
	}

	public void sendAll(String strToSend) {
		for (Socket sock : Collections.list(sockets.keys())) {
			send(strToSend, sock);
		}
	}

	public synchronized void receive(Socket sock) {
		Thread t = new Thread(() -> {
			try {
				DataInputStream in = new DataInputStream(sock.getInputStream());
				while (true) {
					if (sock.isClosed()) {
						int id = sockets.get(sock);
						states.put(id, playerState.DISCONNECTED);
						threads.get(sock).interrupt();
						Thread th = threads.get(sock);
						th = null;
						threads.remove(sock);
					}
					String received = in.readUTF(); // reception d’un nombre
					String receivedVal = new String(received);
					switchState(receivedVal, sock);
				}
			} catch (UnknownHostException e) {
				System.out.println(players.get(sockets.get(sock)) + " left");
				states.put(sockets.get(sock), playerState.DISCONNECTED);
				sendPlayerStatus(sockets.get(sock));
				sendAll("consoleMessage," + players.get(sockets.get(sock)) + " left");
				if ((serverState == serverStates.IN_GAME || numberOfConnectedPlayers() == 0
						|| numberOfPlayingPlayers() == 1) && serverState != serverStates.ENDED) {
					serverState = serverStates.ENDED;
					checkEnd();
				}
				return;
			} catch (IOException e) {
				try {
					if (states.get(sockets.get(sock)) != playerState.DISCONNECTED) {
						states.put(sockets.get(sock), playerState.DISCONNECTED);
						System.out.println(players.get(sockets.get(sock)) + " left");
						sendPlayerStatus(sockets.get(sock));
						sendAll("consoleMessage," + players.get(sockets.get(sock)) + " left");
						if (numberOfConnectedPlayers() >= 2 && checkReady()
								&& serverState == serverStates.AWAITING_PLAYERS) {
							sendAll("consoleMessage,Game will start in 10 seconds.");
							serverState = serverState.READY_TO_LAUNCH;
							startIn10s();
						}
					} else {
						threads.get(sock).interrupt();
						threads.put(sock, null);
					}
				} catch (Exception ex) {
				}
				if ((serverState == serverStates.IN_GAME || numberOfConnectedPlayers() == 0
						|| numberOfPlayingPlayers() == 1) && serverState != serverStates.ENDED) {
					serverState = serverStates.ENDED;
					checkEnd();
				}
				return;
			}
		});
		threads.put(sock, t);
		t.start();
	}

	public void sendPlayerStatus(int id) {
		sendAll("playerStatus," + id + ',' + players.get(id) + "," + scores.get(id) + "," + colors.get(id).getRed()
				+ ',' + colors.get(id).getGreen() + ',' + colors.get(id).getBlue() + "," + states.get(id).toString());
	}

	public boolean checkReady() {
		for (playerState state : states.values()) {
			if (state != playerState.READY && state != playerState.DISCONNECTED)
				return false;
		}
		return true;
	}

	public int numberOfConnectedPlayers() {
		int nb = states.size();
		for (playerState state : states.values()) {
			if (state == playerState.DISCONNECTED || state == playerState.SPECTATING)
				nb--;
		}
		return nb;
	}

	public int numberOfPlayingPlayers() {
		int nb = states.size();
		for (playerState state : states.values()) {
			if (state != playerState.PLAYING)
				nb--;
		}
		return nb;
	}

	void initializeCases() {
		revealedCases = new int[champ.getSizeX()][champ.getSizeY()];
		for (int i = 0; i < champ.getSizeX(); i++) {
			for (int j = 0; j < champ.getSizeY(); j++) {
				revealedCases[i][j] = -1;
			}
		}
	}

	public void startIn10s() {
		Thread start = new Thread(() -> {
			try {
				Random rdm = new Random();
				votedDifficulty.forEachKey(1, key -> {
					if (votedDifficulty.get(key) == null)
						votedDifficulty.remove(key);
				});
				Commons.difficulty diff = votedDifficulty.get(rdm.nextInt(votedDifficulty.size()));
				if (diff == null)
					diff = Commons.difficulty.MEDIUM;
				sendAll("consoleMessage,Voting closed, difficulty : " + diff);
				for (int i = 0; i < 10; i++) {
					TimeUnit.SECONDS.sleep(1);
					sendAll("consoleMessage," + (10 - i - 1));
					if (serverState != serverStates.READY_TO_LAUNCH || numberOfConnectedPlayers() < 2) {
						sendAll("consoleMessage,Launch Interrupted");
						return;
					}
				}
				if (serverState == serverStates.READY_TO_LAUNCH && numberOfConnectedPlayers() >= 2) {
					champ = new Champ(diff);
					sendAll("champ," + champ.getSizeX() + "," + champ.getSizeY() + "," + champ.difficulte + ","
							+ champ.getNbMines());
					serverState = serverStates.IN_GAME;
					states.forEachKey(1, key -> {
						if (states.get(key) != playerState.DISCONNECTED)
							states.put(key, playerState.PLAYING);
						sendPlayerStatus(key);
					});
					initializeCases();
					sendAll("consoleMessage,GAME STARTED !");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		start.start();
	}

	synchronized void checkEnd() {
		if (champ != null && champ.gameOver)
			return;
		boolean end = false;
		if (numberOfConnectedPlayers() == 0) {
			end = true;
		} else if (numberOfConnectedPlayers() == 1) {
			states.forEachKey(1, key -> {
				if (states.get(key) != playerState.DISCONNECTED && states.get(key) != playerState.SPECTATING) {
					if (key != null) {
						sendAll("winner," + key);
						sendAll("consoleMessage," + players.get(key) + " won !");
					}
				}
			});
			end = true;
		} else if (numberOfPlayingPlayers() == 1) {
			states.forEachKey(1, key -> {
				if (key != null) {
					if (states.get(key) == playerState.PLAYING)
						sendAll("winner," + key);
					sendAll("consoleMessage," + players.get(key) + " won !");
				}
			});
			end = true;
		} else if (champ.score == champ.getSizeX() * champ.getSizeY() - champ.getNbMines()) {
			int id = 0;
			id = scores.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
					.get().getKey();
			sendAll("winner," + id);
			sendAll("consoleMessage," + players.get(id) + " won !");
			end = true;
		}
		if (end) {
			if (champ != null)
				champ.gameOver = true;
			System.out.println("Server closed");
			reset();
			try {
				gestSock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			new DemineurServer();
		} else {
			serverState = serverStates.IN_GAME;
		}
	}

	public void switchState(String received, Socket sock) {
		System.out.println("Received : " + received);
		String[] data = received.split(",");
		String code = data[0];
		int id = 0;
		try {
			id = sockets.get(sock);
		} catch (Exception e) {
			players.forEachKey(1, key -> {
				sendPlayerStatus(key);
			});
			return;
		}
		switch (code) {
			case "connect":
				players.put(id, data[1]);
				scores.put(id, 0);
				Random rdm = new Random();
				colors.put(id, new Color(rdm.nextInt(256), rdm.nextInt(256), rdm.nextInt(256)));
				if (serverState == serverStates.IN_GAME || serverState == serverStates.ENDED) {
					send("id_player," + sockets.get(sock) + ",Spectate," + champ.getSizeX() + "," + champ.getSizeY()
							+ "," + champ.difficulte + "," + champ.getNbMinesRestantes(), sock);
					states.put(id, playerState.SPECTATING);
					players.forEachKey(1, key -> {
						sendPlayerStatus(key);
					});
					for (int i = 0; i < champ.getSizeX(); i++) {
						for (int j = 0; j < champ.getSizeY(); j++) {
							if (revealedCases[i][j] != -1)
								send("case_reveal," + revealedCases[i][j] + ',' + i + ',' + j + ',' + champ.champ[i][j],
										sock);
						}
					}
				} else {
					states.put(id, playerState.CONNECTED);
					serverState = serverStates.AWAITING_PLAYERS;
					send("id_player," + sockets.get(sock) + ",Play", sock);
				}
				players.forEachKey(1, key -> {
					sendPlayerStatus(key);
				});
				break;
			case "difficulty":
				states.put(id, playerState.READY);
				votedDifficulty.put(id, Commons.difficulty.valueOf(data[1]));
				sendAll("voted," + id + ',' + data[1]);
				if (numberOfConnectedPlayers() >= 2 && checkReady()) {
					sendAll("consoleMessage,Game will start in 10 seconds.");
					serverState = serverState.READY_TO_LAUNCH;
					startIn10s();
				}
				sendPlayerStatus(id);
				break;
			case "case":
				if (states.get(id) != playerState.SPECTATING) {
					int x = Integer.parseInt(data[1]);
					int y = Integer.parseInt(data[2]);
					sendAll("case_reveal," + id + ',' + x + ',' + y + ',' + champ.champ[x][y]);
					if (champ.champ[x][y] == -1) {
						states.put(id, playerState.SPECTATING);
						sendAll("nBMines," + champ.getNbMinesRestantes());
						sendAll("consoleMessage," + players.get(id) + " lost");
					} else {
						scores.put(id, scores.get(id) + 1);
						champ.score++;
					}
					sendPlayerStatus(id);
					revealedCases[x][y] = id;
					checkEnd();
				}
				break;
			case "message":
				sendAll(received);
				break;
			case "consoleMessage":
				sendAll(received);
				break;
			case "disconnect":
				try {
					sock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sendAll("consoleMessage," + players.get(id) + " left");
				states.put(id, playerState.DISCONNECTED);
				sendPlayerStatus(id);
				break;
			default:
				System.out.println("Bad Reception" + code);
				break;
		}
	}

	public static void main(String[] args) {
		new DemineurServer();
	}

}
