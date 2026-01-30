# BattleShip (Battaglia Navale) — Java Client-Server
Implementazione Client-Server del gioco Battaglia Navale in Java, realizzata da Pavel Cavallini e Mirko Pratesi.

Questo repository contiene:
- Un server che espone un "bot" che gioca contro un client,
- Un client CLI che permette di piazzare navi, sparare e ricevere risposte dal server,
- Una semplice protocollo testuale per la comunicazione (definito in `Common.Protocollo`).

# Requisiti
- Java 11 (JDK 11)
- Maven (qualsiasi versione moderna, es. 3.6+)

# Build & Esecuzione (locale)

Compila:
```bash
mvn compile

```

Avvia il server (terminale 1):

```bash
mvn exec:java -Dexec.mainClass="Server.ServerMain"

```

Il server apre la porta **6767** e aspetta il client.
*Nota: Maven scaricherà ed eseguirà il plugin exec se non è già presente.*

Avvia il client (terminale 2):

```bash
mvn exec:java -Dexec.mainClass="Client.ClientMain"

```

Il client si connetterà al server sulla porta 6767 (impostazione hardcoded) e inizierà il loop di gioco.

Se preferisci eseguire direttamente le classi compilate (senza plugin exec), dopo `mvn package` puoi avviare con:

```bash
java -cp target/classes Server.ServerMain
java -cp target/classes Client.ClientMain

```

# Protocollo di rete (Common/Protocollo.java)

Il protocollo è testuale, con messaggi separati dal carattere ":".

### Messaggi di stato

* `TURN_START` — "Tocca a te" (il sender comunica che il destinatario deve effettuare un'azione di sparo)
* `WAIT` — il destinatario deve attendere
* `GAMEOVER` — partita terminata

### Messaggi di azione

* `SHOT` — comando per sparare: `"SHOT:x:y"` (x e y sono coordinate intere 0..9)

### Risposte al colpo

* `HIT` — colpito (ma non affondato)
* `MISS` — acqua
* `SUNK` — affondato (formato: `"SUNK:<ID>"`, es. `"SUNK:P"`) dove `<ID>` è un identificatore di nave (P, C, I, U, D, S)

### Separatore

* `:` (Protocollo.SEPARATOR)

### Esempio di scambio (semplificato)

```text
Server -> Client: TURN_START
Client -> Server: SHOT:3:5
Server -> Client: HIT
Server -> Client: WAIT
Server -> Client: SHOT:2:7 (ora è il bot a sparare)
Client -> Server: MISS
... continua fino a GAMEOVER

```

# Mappa delle classi e responsabilità

**pom.xml**
Maven project file; Java 11 target.

**src/main/java/Server/ServerMain.java**
Entry point del server. Apre `ServerSocket` su porta 6767 e, per ogni connessione, crea un `BotHandler` in un nuovo Thread.

**src/main/java/Server/BotHandler.java**
Gestisce una singola partita con un client (Runnable).
Mantiene tre matrici 10x10:

* `board`: la mappa del bot (navi posizionate e stato delle celle)
* `boardSunk`: map per identificare quale pezzo appartiene a quale nave (ID) per determinare se è affondata
* `boardMem`: memoria del bot delle proprie mosse sul client (MANCATO, NAVE_COLPITA, NAVE_AFFONDATA)

Logica del bot:

* posiziona le navi casualmente (rispettando lunghezze e sovrapposizioni)
* quando riceve `SHOT:x:y` valuta MISS/HIT/SUNK e risponde
* quando attacca genera mosse casuali evitando celle già controllate (`boardMem`)
* rileva GAMEOVER quando non rimangono pezzi di nave

**src/main/java/Client/ClientMain.java**
Applicazione cliente (CLI). Gestisce:

* tre matrici analoghe (`board`, `boardMem`, `boardSunk`)
* piazzamento navi (manuale o casuale), visualizzazione delle mappe
* invio di `SHOT:x:y` e gestione delle risposte dal server
* ciclo di gioco con alternanza di `TURN_START` / `WAIT` come richiesto dal protocollo

**src/main/java/Common/Costanti.java**
Costanti usate dal gioco:

* `DIMENSIONE_CAMPO = 10`
* simboli grafici per console (`~`, `O`, `X`, `@`, `#`)
* identificatori navi: P, C, I, U, D, S
* lunghezze delle navi: portaerei 5, corazzata 4, incrociatori 3, cacciatorpediniere 3, sottomarino 2

**src/main/java/Common/Protocollo.java**
Stringhe usate per il protocollo (messaggi e separatore)

# Dettagli implementativi importanti

### Coordinate e matrici

Tutte le coordinate nel codice seguono l'ordine **x (colonna), y (riga)**. Le matrici sono indicizzate come `board[y][x]`.
Direttiva `DIMENSIONE_CAMPO=10` è usata in tutto il codice; cambiare questa costante richiede verificare piazzamento e visualizzazione.

### Rilevamento nave affondata (server)

`BotHandler` usa `boardSunk` per memorizzare l'ID della nave in ciascuna cella.
Dopo un colpo, se non esistono più celle intatte della stessa ID, la nave è considerata affondata; BotHandler risponde `"SUNK:<ID>"` e marca quei pezzi come affondati nella board.

### Alternanza dei turni

Il server guida il flusso: ogni ciclo il server indica al client se è il suo turno (`TURN_START`) oppure se deve aspettare (`WAIT`).
Questo progetto ha una logica client-server asimmetrica: il server gestisce sia la ricezione dei colpi sia l'invio dei suoi colpi.

# Come giocare localmente

1. Apri due terminali (server e client).
2. Avvia `Server.ServerMain` (vedi comandi sopra).
3. Avvia `Client.ClientMain` — segui le istruzioni a video per piazzare le navi (manuale o random) e spara quando ricevi `TURN_START`.
4. Ripeti fino a `GAMEOVER` per uno dei giocatori.

---

Autori nel codice: Pavel Cavallini, Mirko Pratesi
Buon divertimento!
