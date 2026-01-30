// Accetta le connessioni.
package Client;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

// IMPORTIAMO LE NOSTRE FUNZIONI
import Common.Costanti;
import Common.Protocollo;

public class ClientMain {

    private char[][] board = new char[Costanti.DIMENSIONE_CAMPO][Costanti.DIMENSIONE_CAMPO];
    private char[][] boardMem = new char[Costanti.DIMENSIONE_CAMPO][Costanti.DIMENSIONE_CAMPO];
    private char[][] boardSunk = new char[Costanti.DIMENSIONE_CAMPO][Costanti.DIMENSIONE_CAMPO];
    private Random random = new Random();

    // Funzione helper che riempie una matrice con un char
    public void fillMatrix(char c, char[][] A) {
        for (int i = 0; i < Costanti.DIMENSIONE_CAMPO; i++) {
            for (int j = 0; j < Costanti.DIMENSIONE_CAMPO; j++) {
                A[i][j] = c;
            }
        }
    }

    // Funzione che inizializza il campo di gioco
    public void Init() {
        // Riempiamo tutte le board con l'acqua
        fillMatrix(Costanti.MARE, board);
        fillMatrix(Costanti.MARE, boardMem);
        fillMatrix(Costanti.MARE, boardSunk);
    }

    // Funzione helper per vedere se la nave posizionata è fuori dai bordi
    // x e y sono le coordinate
    // lunghezza è quella della nave
    // siorizzontale per vedere se è orizzontale o no
    private boolean PosizioneCorretta(int x, int y, int lunghezza, boolean siorizzontale) {

        if (siorizzontale) {
            //controllo bordi orizzontali
            if (x + lunghezza > Costanti.DIMENSIONE_CAMPO) return false;

            //guardo se ci sono gia navi
            for (int i = 0; i < lunghezza; i++) {
                if (board[y][x + i] != Costanti.MARE) {
                    return false;
                }
            }
        } else {
            // qua è verticale
            if (y + lunghezza > Costanti.DIMENSIONE_CAMPO) return false;

            //guardo se ci sono gia navi
            for (int i = 0; i < lunghezza; i++) {
                if (board[y + i][x] != Costanti.MARE) {
                    return false;
                }
            }
        }
        return true;
    }

    // Funzione per piazzare manulamente le navi
    public void PosizionaManualmente(Scanner scanner) {
        // Le navi standard della battaglia navale
        int[] lunghezze = {Costanti.LUNGHEZZA_PORTAEREI, Costanti.LUNGHEZZA_CORAZZATA, Costanti.LUNGHEZZA_INCROCIATORE, Costanti.LUNGHEZZA_INCROCIATORE, Costanti.LUNGHEZZA_CACCIATORPEDINIERE, Costanti.LUNGHEZZA_SOTTOMARINO};
        String[] nomi = {"Portaerei", "Corazzata", "Incrociatore", "Incrociatore X", "Cacciatorpediniere", "Sottomarino"};

        char[] ids = {Costanti.ID_PORTAEREI, Costanti.ID_CORAZZATA, Costanti.ID_INCROCIATORE_1, Costanti.ID_INCROCIATORE_2, Costanti.ID_CACCIATORPEDINIERE, Costanti.ID_SOTTOMARINO};

        fillMatrix(Costanti.MARE, boardSunk);
        fillMatrix(Costanti.MARE, board);
        printBoards();

        //ciclo per ogni nave
        for (int i = 0; i < lunghezze.length; i++) {
            int lunghezza = lunghezze[i];
            char idNave= ids[i];
            boolean messacorretta = false;
            //aggiungo un while in caso di errore di invalid input da parte dell'utente
            while (!messacorretta) {
                System.out.println("Posizionamento nave: " + nomi[i] + " con lunghezza: " + lunghezza);

                // chiedere le coordinate all'utente
                int coordinate[] = Coordinate(scanner);
                int x = coordinate[0];
                int y = coordinate[1];

                // chiedere la posizione
                System.out.println("Posizione nave: Orizzontale (O) oppure Verticale (V) ");
                char scelta = scanner.next().charAt(0);

                //controllo se la nave ci sta

                // se è true è orizzontale se è false allora è verticale
                boolean posizionamento = (scelta == 'o' || scelta == 'O');

                if (PosizioneCorretta(x, y, lunghezza, posizionamento)) {
                    for (int j = 0; j < lunghezza; j++) {
                        if (posizionamento) {
                            //orizzontale
                            board[y][j + x] = Costanti.NAVE;
                            boardSunk[y][x + j] = idNave;
                        } else {
                            //verticale
                            board[y + j][x] = Costanti.NAVE;
                            boardSunk[y + j][x] = idNave;
                        }
                    }
                    messacorretta = true;
                    printBoards();
                } else {
                    System.out.println("Errore: nave fuori dai bordi, mettila bene!!!!!!!!!!");
                }
            }
        }
    }

    // Posiziona le navi in modo casuale
    public void PosizionaRandom() {
        int[] lunghezze = {Costanti.LUNGHEZZA_PORTAEREI, Costanti.LUNGHEZZA_CORAZZATA, Costanti.LUNGHEZZA_INCROCIATORE, Costanti.LUNGHEZZA_INCROCIATORE, Costanti.LUNGHEZZA_CACCIATORPEDINIERE, Costanti.LUNGHEZZA_SOTTOMARINO};
        char[] ids = {Costanti.ID_PORTAEREI, Costanti.ID_CORAZZATA, Costanti.ID_INCROCIATORE_1, Costanti.ID_INCROCIATORE_2, Costanti.ID_CACCIATORPEDINIERE, Costanti.ID_SOTTOMARINO};

        for (int i = 0; i < lunghezze.length; i++) {
            boolean piazzata = false;
            while (!piazzata) {
                //le coordinate sono generate casualemente
                int x = random.nextInt(Costanti.DIMENSIONE_CAMPO);
                int y = random.nextInt(Costanti.DIMENSIONE_CAMPO);
                boolean orizzontale = random.nextBoolean();

                if (PosizioneCorretta(x, y, lunghezze[i], orizzontale)) {
                    for (int k = 0; k < lunghezze[i]; k++) {
                        if (orizzontale) {
                            board[y][x + k] = Costanti.NAVE;
                            boardSunk[y][x + k] = ids[i];
                        } else {
                            board[y + k][x] = Costanti.NAVE;
                            boardSunk[y + k][x] = ids[i];
                        }
                    }
                    piazzata = true;
                }
            }
        }
        printBoards();
    }


    // Funzione per chiedere all'utente dove vuole sparare
    public int[] Coordinate(Scanner scanner) {
        int[] coordinate = new int[2];
        boolean coordinategiuste = false;

        while (!coordinategiuste) {
            try {
                System.out.println("Inserisci le coordinata X");
                coordinate[0] = scanner.nextInt();

                System.out.println("Inserisci le coordinata Y");
                coordinate[1] = scanner.nextInt();

                if (coordinate[0] >= 0 && coordinate[0] < Costanti.DIMENSIONE_CAMPO && coordinate[1] >= 0 && coordinate[1] < Costanti.DIMENSIONE_CAMPO) {
                    coordinategiuste = true;
                } else {
                    System.out.println("Inserisci le coordinate giuste!! Da 0 a 9");
                }
            }catch (Exception e){
                System.out.println("Errorre di battitura, inserisci un numero intero");
                scanner.nextLine(); // serve per la fault tollerance del progetto, cancella la linea sbagliata scritta precedentemente dall'utente
            }
        }
        return coordinate;
    }

    // Funzione per vedere se una nave è affondata
    private boolean isAffondata(char idnave) {
        for (int i = 0; i < Costanti.DIMENSIONE_CAMPO; i++) {
            for (int j = 0; j < Costanti.DIMENSIONE_CAMPO; j++) {
                if (boardSunk[i][j] == idnave) {
                    // Se troviamo un pezzo di questa nave che è ancora intatto (Costanti.NAVE)
                    if (board[i][j] == Costanti.NAVE) {
                        return false; // Non è affondata
                    }
                }
            }
        }
        return true;
    }

    // Funzione per gestire il colpo (guardare cosa succede, affondato, acqua, colpito ecc.)
    private String gestisciColpoSubito(int x, int y) {
        if (x < 0 || x >= Costanti.DIMENSIONE_CAMPO || y < 0 || y >= Costanti.DIMENSIONE_CAMPO) {
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
                segnaAffondata(idNave);
                return Protocollo.SUNK + Protocollo.SEPARATOR + idNave;
            }
            return Protocollo.HIT;
        } else {
            // Già colpita o affondata
            return Protocollo.MISS;
        }
    }

// se una nave è stata affondata si mette #
    private void segnaAffondata(char idNave) {
        for (int i = 0; i < Costanti.DIMENSIONE_CAMPO; i++) {
            for (int j = 0; j < Costanti.DIMENSIONE_CAMPO; j++) {
                if (boardSunk[i][j] == idNave && board[i][j] == Costanti.NAVE_COLPITA) {
                    board[i][j] = Costanti.NAVE_AFFONDATA;
                }
            }
        }
    }

    // Funzione per vedere se si ha vinto o perso
    private boolean isGameOver() {
        for (int i = 0; i < Costanti.DIMENSIONE_CAMPO; i++) {
            for (int j = 0; j < Costanti.DIMENSIONE_CAMPO; j++) {
                if (board[i][j] == Costanti.NAVE) {
                    return false;
                }
            }
        }
        return true;
    }

    // Aggiorna boardMem con l'esito del mio colpo
    private void registraEsitoColpo(int x, int y, String esito) {
        if (esito.startsWith(Protocollo.HIT)) {
            boardMem[y][x] = Costanti.NAVE_COLPITA;
        } else if (esito.startsWith(Protocollo.SUNK)) {
            boardMem[y][x] = Costanti.NAVE_AFFONDATA;
        } else if (esito.equals(Protocollo.MISS)) {
            boardMem[y][x] = Costanti.MANCATO;
        }
    }

    public void printBoards() {
        System.out.println("\n --- FLOTTA VISIVA ---(Board)   --- MEMORIA COLPI --- (BoardMem)");

        // 1. Intestazione colonne (0 1 2...)
        String header = "    ";
        for (int k = 0; k < Costanti.DIMENSIONE_CAMPO; k++) header += k + " ";

        System.out.println(header + "        " + header);

        // 2. Ciclo Righe
        for (int i = 0; i < Costanti.DIMENSIONE_CAMPO; i++) {

            // --- SINISTRA: board ---
            // %2d allinea i numeri a due cifre, mantenendo tutto dritto
            System.out.printf("%2d | ", i);
            for (int j = 0; j < Costanti.DIMENSIONE_CAMPO; j++) {
                System.out.print(board[i][j] + " ");
            }

            System.out.print("      "); // Spazio di separazione costante

            // --- DESTRA: boardMem ---
            System.out.printf("%2d | ", i);
            for (int j = 0; j < Costanti.DIMENSIONE_CAMPO; j++) {
                System.out.print(boardMem[i][j] + " ");
            }

            System.out.println();
        }
        System.out.println("----------------------------------------------------------------------");
    }

    // game loop del client
    public void startClient(Scanner scanner) {
        try (Socket socket = new Socket("localhost", 6767);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connesso al Server! In attesa...");

            while (true) {
                String cmd = in.readLine();
                if (cmd == null) {
                    System.out.println("Server disconnesso...");
                    break;
                }

                if (cmd.equals(Protocollo.TURN_START)) {
                    // È il mio turno
                    System.out.println("È IL TUO TURNO! Spara!");
                    printBoards();

                    int[] coords = Coordinate(scanner);

                    // Controlla se ho già sparato lì
                    while (boardMem[coords[1]][coords[0]] != Costanti.MARE) {
                        System.out.println("Hai già sparato lì! Riprova.");
                        coords = Coordinate(scanner);
                    }

                    // si manda al server questa stringa
                    out.println(Protocollo.SHOT + Protocollo.SEPARATOR + coords[0] + Protocollo.SEPARATOR + coords[1]);

                    //aspetta la risposta dal server
                    String response = in.readLine();
                    System.out.println("Esito: " + response);

                    //vittoria del client
                    if (response.equals(Protocollo.GAMEOVER)) {
                        System.out.println("HAI VINTO! Il server si è arreso.");
                        break;
                    }

                    //si registra su boardMem
                    registraEsitoColpo(coords[0], coords[1], response);

                } else if (cmd.equals(Protocollo.WAIT)) {
                    System.out.println("In attesa dell'avversario...");

                } else if (cmd.startsWith(Protocollo.SHOT)) {
                    // Il server mi ha sparato
                    String[] parts = cmd.split(Protocollo.SEPARATOR);
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    System.out.println("Il nemico spara a: " + x + ", " + y);
                    // si segna su board
                    String result = gestisciColpoSubito(x, y);
                    // si manda il risultato al server
                    out.println(result);
                    System.out.println("Risultato colpo subito: " + result);

                    // se non abbiamo più navi lo diciamo al server
                    if (isGameOver()) {
                        out.println(Protocollo.GAMEOVER);
                        System.out.println("HAI PERSO! Tutte le tue navi sono affondate.");
                        break;
                    }

                } else if (cmd.equals(Protocollo.GAMEOVER)) {
                    System.out.println("HAI VINTO! Partita terminata.");
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Errore di connessione: " + e.getMessage());
            System.out.println("Assicurati che il server sia avviato.");
        }
    }

    public static void main(String[] args) {
        ClientMain game = new ClientMain();
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- BATTLESHIP CLIENT ---");

        // 1. Inizializzazione (Posizionamento)
        game.Init(); // Inizializza con acqua

        System.out.println("Posizionamento navi: (1) Manuale  (2) Casuale");
        String scelta = scanner.next();
        if (scelta.equals("2")) {
            game.PosizionaRandom();
        } else {
            game.PosizionaManualmente(scanner);
        }

        // 2. Avvia gioco
        game.startClient(scanner);
    }
}
