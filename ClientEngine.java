
public class ClientEngine extends Datenverwaltung{
	public LinkedList<Monster> monsterList;
	public PrisonBreak prisonBreak;
	public Player Player;
	public CommClient comm;
	public String addresse = "localhost";

	public int currentLevel;
	public boolean playing;
	public boolean lost;
	public boolean levelloaded;
	public long startTime;

	public LinkedList<HighScoreElement> highScore;
	public Gameelement[][] level;
	public LinkedList<String> systemMessage;
	public LinkedList<String> chatMessage;

	public final int MAXLEVEL = 10;
	
    private Map map;
	private Opponent[][] opponent;
	private Potion[][] potion;
	private MainWindow gui;
	private Login login;
	private ClientComm clientComm;
	public ClientEngine() {
		this.comm = new CommClient(this, this.addresse, 9000);
		this.highScore = new LinkedList<HighScoreElement>();
		this.systemMessage = new LinkedList<String>();
		this.chatMessage = new LinkedList<String>();
		super.monsterList = new LinkedList<Monster>();
		this.playing = true;
		this.lost = false;
		this.levelloaded = false;
		this.currentLevel = 0;
		super.player = new Player("img//spieler.png", this);
		this.prisonBreak = new PrisonBreak(this);
		this.prisonBreak.gameSymbol();
	}

		public Map getMap() {
			return map;
		}

		private Character character;

		public Character getCharacter() {
			return character;
		}

		public Opponent[][] getOpponent() {
			return opponent;
		}

		public Potion[][] getPotion() {
			return potion;
		}

		public ClientEngine() {
			character = new Character();
			login = new Login(this);
			CommClient = new CommClient("127.0.0.1", 1150, this);
			CommClient.connection();
		}

		private void startGame(Position gamestartingpoint) {
			character.column = gamestartingpoint.column;
			character.row = gamestartpoint.row;
			character.setMaxHealth(100);
			gui = new MainWindow(this);
			gui.gameWindow.symbol(map, character);
			
			login.setVisible(false);
		}

		public void receive(Message message) {
			if (message instanceof ConnectionSuccessed) {
				ConnectionSuccessed connection = (ConnectionSuccesse) message;
				map = connection.map;
				opponent = map.getOpponent();
				potion = map.getPotion();
				startgame(map.startpoint);
				;
			} else if (messeage instanceof ConnectionFailed) {
				ConnectionFailed errorMessage = (ConnectionFailed) message;

				character.setUsername("");

				if (errorMessage.errorNr == 0) {
					// Passwort falsch
					login.passwordError();
				} else if (errorMessage.errorNr == 1) {
					// Benutzername existiert nicht (bei Anmeldung; nicht
					// Registrierung)
					login.userFalsch();
				} else if (errorMessage.errorNr == 2) {
					// Benutzername bereits vergeben
					login.usernameUsed();
				}
			} else if (message instanceof MonsterMovement) {
				/*
				 * Macht keinen Sinn Gegner-Matrix als Eigenschaft in der Engine zu
				 * haben. Lieber Matrix in der Karte bearbeiten.
				 */
				MonsterMovement mMovement = (MonsterMovement) message;
				int oldColumn = mMovement.posX;
				int oldRow = mMovement.posY;
				if (mMovement.direction == 1) {
					oldColumn = mMovement.posX - 1;
				} else if (mMovement.direction == 2) {
					oldColumn = mMovement.posX + 1;
				} else if (mMovement.direction == 3) {
					oldRow = mMovement.posY - 1;
				} else if (mMovement.richtung == 4) {
					oldRow = mMovement.posY + 1;
				}

				Opponent buffer = opponent[oldRow][oldColumn];
				Opponent[oldRow][oldColumn] = null;
				opponent[mMovement.posY][mMovement.posX] = buffer;
				map.setOpponent(opponent);

				gui.gameWindow.symbol(map, character);
			} else if (message instanceof MonsterDamaged) {
				MonsterDamaged monsterDamaged = (MonsterDamaged) message;
				opponent[monsterDamaged.posY][monsterDamaged.posX].setLife(monsterDamaged.newlifepoint);
				// gui.spielFenster();
			} else if (message instanceof MonsterDead) {
				MonsterDead monsterDead = (MonsterDead) meassage;
				opponent[monsterDead.posY][monsterDead.posX] = null;
				map.setOpponent(opponent);
				potion[monsterDead.posY][monsterDead.posX] = new Potion(); // Neuen
																			// Trank
																			// an
																			// Monstertodesstelle
																			// legen
				// gui.loescheMonster();
			} else if (message instanceof PlayerMovement) {
				PlayeMovement movement = (PlayerMovement) message;
				character.column = movement.posX;
				character.row = movement.posY;
				System.out.println("################## playermovement Client #############");
				gui.gamewindow.symbol(map, character);
				gui.gamewindow.repaint();
			} else if (message instanceof PlayerDamaged) {
				PlayerDamaged playerdamaged  = (PlayerDamaged) message;
				character.setLifepoint(playerDamaged.newLifepoint);
			} else if (message instanceof PlayerDead) {
				character.setLifepoint(0);
				HighscoreInquiry highscoreInquiry = new HighscoreInquiry();
				CommClient.sendMsg(highscoreInquiry);
			} else if (message instanceof PickupPotion) {
				PickupPotion potion = (PickupPotion) message;
			    potion[potion.posY][potion.posX] = null;
				map.setPotion(potion);
				character.setPotion(character.getPotion() + 1);
				// gui.pickupPotion(potion.posX, potion.posY);
			} else if (message instanceof usePotion) {
				UsePotion potion = (usePotion) message;
				character.setPotion(character.getPotion() - 1);
				character.setLifepoint(drink.newlifepoint);
				// gui.usePotion(potion.newHealth);
			} else if (message instanceof pickupKey) {
				character.setgotKey(true);
			} else if (message instanceof NewLevel) {
				NewLevel newLevel = (NewLevel) message;
				map = newLevel.newLevel;
				oppponent = map.getOpponent();
				potion = map.getPotion();
				startgame(map.startpoint);
			} else if (message instanceof Highscore) {
				Highscore highscore = (Highscore) message;
				gui.Showhighscore(highscore.highscoreList);
			} else if (message instanceof Time) {
				Time time = (Time) message;
				// gui.timeUpdate(time.secondTime);
			}

		}

		public void charactergoto(int posX, int posY) {
			Chractergoto click = new Charactergoto(posX, posY);
			
			CommClient.sendMsg(click);
		}

		public void usePotion() {
			usePotionInquiry inquiry = new usePotionInquiry();
			CommClient.sendMsg(inquiry);
		}

		public void pickupPotion(int posX, int posY) {
			pickupPotionInquiry inquiry = new pickupPotionInquiry(posX, posY);
			CommClient.sendMsg(inquiry);
		}

		public void pickupKey(int posX, int posY) {
			pickupKeyInquiry keyinquiry = new pickupKeyInquiry();
			// siehe kommantar bei empfang der antwort
			CommClient.sendMsg(keyinquiry);
		}

		public void savegame() {
			SaveExplanation save = new SaveExplanation();
			CommClient.sendMsg(save);
		}

		// von Gui aufrufbar
		public void playerLogin(String username, String password) {
			Connectioninquiry inquiry = new Connectioninquiry(useername, password);
			character.setUsername(username);
			CommClient.sendMsg(inquiry);
		}

		// von Gui aufrufbar
		public void playerLogout() {
			Connectioncancel cancel = new Connectioncancel();
			CommClient.sendMsg(cancel);
		}

		public void pickupItem() {
			int posX = character.column;
			int posY = character.row;

			if (potion[posX][posY] != null) {
				pickupPotion(posX, posY);
			} else if (map.key.column == posX && map.key.row == posY) {
				pickupKey(character.column, character.row);
			} else if (map
					.getGameStoneOnPosition(new Position(character.column, character.row)) == Gamestone.Exitdoor) {
				LevelChange change = new LevelChange();
				CommClient.sendMsg(change);
			}
		}

		public void highscoreInquiry() {
			HighscoreInquiry inquiry = new HighscoreInquiry();
			CommClient.sendMsg(inquiry);
		}

	}
    public void gameReset() {
	 this.playing = true;
	 this.levelloaded = false;
	 super.monsterList = new LinkedList<Monster>();

	 this.currentLevel = 0;
	 this.lost = false;
	 this.startTime = System.currentTimeMillis();
	  String playername = super.player.getName();
	 super.player = new Player("img//spieler.png", this);
	 super.player.setName(playername);
	 this.highScore = new LinkedList<HighScoreElement>();

	// NeuesSpielNachricht erstellen und an den Server senden
	 NewgameMessage message = new NewgameMessage();
	 this.CommClient.send(message);
}
public void loadLevel(int[][] leveldata) {
	this.levelloaded = false;
	super.monsterListe = new LinkedList<Monster>();
	for (int i = 0; i < leveldata.length; i++) {
		String row = "";
		for (int j = 0; j < leveldata[i].length; j++) {
			row += String.valueOf(leveldata[i][j]);
		}
		System.out.println(row);
	}
	for (int i = 0; i < leveldata.length; i++) {
		for (int j = 0; j < leveldata[i].length; j++) {
			switch (leveldata[i][j]) {
			case 0:
				newlevel[i][j] = new Wall();
				break;
			case 1:
				newlevel[i][j] = new Ground();
				break;
			case 2:
				newlevel[i][j] = new Ground();
				super.monsterList.add(new Monster(i, j, this, 0,true, 0));
				break;
			case 3:
				newlevel[i][j] = new Door(false);
				break;
			case 4:
				newlevel[i][j] = new Door(true);
				super.player.setPosition(i, j);
				break;
			case 5:
				newlevel[i][j] = new Ground();
				super.monsterList.add(new Monster(i, j, this, 0,false, 0));
				System.out.println("Monster 0 added.");
				break;
			// Monster, welche erst nach dem Aufheben des Schluessels
			// erscheinen
			case 6:
				newlevel[i][j] = new Ground();
				super.monsterList.add(new Monster(i, j, this, 1,false, 0));
				System.out.println("Monster 1 added.");
				break;
			}
		}
	}
	 super.level = newlevel;
	 this.levelloaded = true;
	 
public void sendChatMessage(String text) {
	System.out.println("PlayerName: " + super.spieler.getName());
	Chatmessage message = new Chatmessage(-1,
			super.player.getName(), text);
	CommClient.sendMsg(message);
}
public void attack() {
	if (this.playing || !this.levelloaded) {
		System.out.println("ERROR: We are not in game.");
		return;
	}
	Monster attackedMonster = super.spieler.attackedMonster();
	System.out.println(super.monsterList.size());
	if (attackedMonster == null) {
	    System.out.println("Warning: There is no monster around you.");
		return;
	}
	// Bestime ID des Monsters (Nach Absprache die Position in der
	// Monsterliste)
	int monsterID = super.monsterListe.indexOf(attackedMonster);
	// Nur fuer den Fall, dass das Monster nicht gefunden wird
	if (monsterID == -1) {
		System.out
				.println("Fehler: Monster not on the list.");
		return;
	}
	Attackmessage message = new Attackmessage(-1, monsterID);
	CommClient.sendMsg(message);
}
}