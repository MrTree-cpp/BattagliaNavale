package Server;

import Common.Costanti;
import Common.Protocollo;
import java.io.*;
import java.net.Socket;
import java.util.Random; // Serve per generare le mosse casuali

public class BotHandler implements Runnable {

    // --- VARIABILI GLOBALI ---
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Random random;

    // LE 3 MATRICI DEL BOT
    private char[][] board;
    private char[][] boardSunk;
    private char[][] boardMem;

    // --- COSTRUTTORE ---
    public BotHandler(Socket socket) {
        this.socket = socket;
        this.random = new Random();

    }

    // =========================================================================
    // 1. SETUP (Inizializzazione)
    // =========================================================================

    // Helper per riempire la matrice
    private void riempiMatrice(char[][] matrice) {
        for (int i = 0; i < Costanti.DIMENSIONE_CAMPO; i++) {
            for (int j = 0; j < Costanti.DIMENSIONE_CAMPO; j++) {
                matrice[i][j] = Costanti.MARE;
            }
        }
    }

    // Prepara tutto prima che inizi la partita
    public void Init() {
        // 1. Inizializza le 3 matrici vuote (con Acqua)
        board = new char[Costanti.DIMENSIONE_CAMPO][Costanti.DIMENSIONE_CAMPO];
        boardSunk = new char[Costanti.DIMENSIONE_CAMPO][Costanti.DIMENSIONE_CAMPO];
        boardMem = new char[Costanti.DIMENSIONE_CAMPO][Costanti.DIMENSIONE_CAMPO];

        riempiMatrice(board);
        riempiMatrice(boardSunk);
        riempiMatrice(boardMem);

        // 2. Chiama posizionaNavi (Random) per riempire 'board'
        posizionaNaviRandom();

        // 3. Stampa la situazione iniziale per debug
        System.out.println("Bot Initialized. Maps:");
        stampaMappaServer();
    }

    // Piazza le navi a caso
    private void posizionaNaviRandom() {
        int[] lunghezze = {
            Costanti.LUNGHEZZA_PORTAEREI,
            Costanti.LUNGHEZZA_CORAZZATA,
            Costanti.LUNGHEZZA_INCROCIATORE, // I
            Costanti.LUNGHEZZA_INCROCIATORE, // U
            Costanti.LUNGHEZZA_CACCIATORPEDINIERE,
            Costanti.LUNGHEZZA_SOTTOMARINO
        };
        char[] ids = {
            Costanti.ID_PORTAEREI,
            Costanti.ID_CORAZZATA,
            Costanti.ID_INCROCIATORE_1,
            Costanti.ID_INCROCIATORE_2,
            Costanti.ID_CACCIATORPEDINIERE,
            Costanti.ID_SOTTOMARINO
        };

        for (int i = 0; i < lunghezze.length; i++) {
            boolean piazzata = false;
            while (!piazzata) {
                int x = random.nextInt(Costanti.DIMENSIONE_CAMPO);
                int y = random.nextInt(Costanti.DIMENSIONE_CAMPO);
                boolean orizzontale = random.nextBoolean();

                if (posizioneValida(x, y, lunghezze[i], orizzontale)) {
                    piazzNav(x, y, lunghezze[i], orizzontale, ids[i]);
                    piazzata = true;
                }
            }
        }
    }

    private boolean posizioneValida(int x, int y, int lunghezza, boolean orizzontale) {
        if (orizzontale) {
            if (x + lunghezza > Costanti.DIMENSIONE_CAMPO) return false;
            for (int k = 0; k < lunghezza; k++) {
                if (board[y][x + k] != Costanti.MARE) return false;
            }
        } else {
            if (y + lunghezza > Costanti.DIMENSIONE_CAMPO) return false;
            for (int k = 0; k < lunghezza; k++) {
                if (board[y + k][x] != Costanti.MARE) return false;
            }
        }
        return true;
    }

    private void piazzNav(int x, int y, int lunghezza, boolean orizzontale, char id) {
        for (int k = 0; k < lunghezza; k++) {
            if (orizzontale) {
                board[y][x + k] = Costanti.NAVE;
                boardSunk[y][x + k] = id;
            } else {
                board[y + k][x] = Costanti.NAVE;
                boardSunk[y + k][x] = id;
            }
        }
    }


    // =========================================================================
    // 2. MOTORE DI GIOCO (Loop Principale)
    // =========================================================================

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Init();

            System.out.println("Partita iniziata con il client " + socket.getInetAddress());

            while (true) {
                // --- FASE 1: DIFESA (Tocca al Client sparare) ---
                out.println(Protocollo.TURN_START); //Dico al client che è il mio turno

                //legge le mosse del client
                String request = in.readLine();
                if (request == null) break; // Client disconnesso

                if (request.startsWith(Protocollo.SHOT)) {
                    String[] parts = request.split(Protocollo.SEPARATOR);
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    //guarda se il colpo ha colpito o no
                    String response = gestisciColpoSubito(x, y);
                    //manda la risposta al client
                    out.println(response);

                    //se ha perso manda al client GAMEOVER
                    if (isGameOver()) {
                        out.println(Protocollo.GAMEOVER);
                        System.out.println("Il Bot ha perso!");
                        break;
                    }
                } else if (request.equals(Protocollo.GAMEOVER)) {
                    System.out.println("Il Client dichiara GAME OVER (ha perso o abbandonato).");
                    break;
                }

                // --- FASE 2: ATTACCO (Tocca al Bot sparare) ---
                out.println(Protocollo.WAIT); // Dico al client di aspettare

                // Simulo un po' di "pensiero" del bot
                try { Thread.sleep(500); } catch (InterruptedException e) {}

                //genero la mossa e la mando al client
                String mossa = generaMossa(); // es. "SHOT:3:4"
                out.println(mossa);

                //aspetta la risposta dal client
                String risposta = in.readLine();
                if (risposta == null) break;

                if (risposta.equals(Protocollo.GAMEOVER)) {
                    System.out.println("Il Bot ha VINTO!");
                    break;
                }

                String[] mossaParts = mossa.split(Protocollo.SEPARATOR);
                int mx = Integer.parseInt(mossaParts[1]);
                int my = Integer.parseInt(mossaParts[2]);

                registraEsitoColpo(mx, my, risposta);

                // Stampa di debug dopo ogni turno completo
                stampaMappaServer();
            }

        } catch (IOException e) {
            System.out.println("Errore comunicazione con client: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException e) {}
        }
    }


    // =========================================================================
    // 3. LOGICA DI DIFESA (Quando il Client spara a ME)
    // =========================================================================

    // Analizza il colpo ricevuto e decide la risposta
    private String gestisciColpoSubito(int x, int y) {
        if (x < 0 || x >= 10 || y < 0 || y >= 10) {
            return Protocollo.MISS;
        }

        char cell = board[y][x];

        if (cell == Costanti.MARE) {
            board[y][x] = Costanti.MANCATO;
            return Protocollo.MISS;
        } else if (cell == Costanti.NAVE) {
            board[y][x] = Costanti.NAVE_COLPITA;
            char idNave = boardSunk[y][x];
            if (isAffondata(idNave)) {
                // Opzionale: Segna la nave come affondata visivamente
                segnaAffondata(idNave);
                return Protocollo.SUNK + Protocollo.SEPARATOR + idNave;
            }
            return Protocollo.HIT;
        } else {
            // Già colpita o affondata
            return Protocollo.MISS;
        }
    }

    private void segnaAffondata(char idNave) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (boardSunk[i][j] == idNave && board[i][j] == Costanti.NAVE_COLPITA) {
                    board[i][j] = Costanti.NAVE_AFFONDATA;
                }
            }
        }
    }

    // Controlla se una specifica nave è stata completamente distrutta
    private boolean isAffondata(char idNave) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (boardSunk[i][j] == idNave) {
                    // Se troviamo un pezzo di questa nave che è ancora intatto (Costanti.NAVE)
                    if (board[i][j] == Costanti.NAVE) {
                        return false; // Non è affondata
                    }
                }
            }
        }
        return true;
    }

    // Controlla se ho perso (tutte le navi affondate)
    private boolean isGameOver() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == Costanti.NAVE) {
                    return false;
                }
            }
        }
        return true;
    }


    // =========================================================================
    // 4. LOGICA DI ATTACCO (Quando IO sparo al Client)
    // =========================================================================

    // Decide dove sparare
    private String generaMossa() {
        int x, y;
        int tentativi = 0;
        do {
            x = random.nextInt(10);
            y = random.nextInt(10);
            tentativi++;
            // Se loopa troppo (es. fine gioco), esci
            if (tentativi > 1000) break;
        } while (boardMem[y][x] != Costanti.MARE);
        return Protocollo.SHOT + Protocollo.SEPARATOR + x + Protocollo.SEPARATOR + y;
    }

    // Si segna com'è andata la mossa
    private void registraEsitoColpo(int x, int y, String esito) {
        if (esito.startsWith(Protocollo.HIT)) {
            boardMem[y][x] = Costanti.NAVE_COLPITA;
        } else if (esito.startsWith(Protocollo.SUNK)) {
            boardMem[y][x] = Costanti.NAVE_AFFONDATA;
        } else if (esito.equals(Protocollo.MISS)) {
            boardMem[y][x] = Costanti.MANCATO;
        }
    }


    // =========================================================================
    // 5. UTILITY & STAMPA
    // =========================================================================

    // Stampa le matrici a video (Lato Server) per vedere che succede
    private void stampaMappaServer() {
        System.out.println("   --- SERVER BOARD ---           --- MEMORY BOARD ---");
        System.out.println("    0 1 2 3 4 5 6 7 8 9            0 1 2 3 4 5 6 7 8 9");
        for (int i = 0; i < 10; i++) {
            System.out.print(i + " | ");
            for (int j = 0; j < 10; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.print("         ");
            System.out.print(i + " | ");
            for (int j = 0; j < 10; j++) {
                System.out.print(boardMem[i][j] + " ");
            }
            System.out.println();
        }
    }
}