package Common;

public class Protocollo {
    // Messaggi di stato
    public static final String TURN_START = "TURN_START"; // Tocca a te
    public static final String WAIT = "WAIT";             // Aspetta
    public static final String GAMEOVER = "GAMEOVER";     // Partita finita

    // Messaggi di azione
    public static final String SHOT = "SHOT"; // Comando inviato dal client: SHOT:x:y

    // Messaggi di risposta al colpo
    public static final String HIT = "HIT";           // Colpito!
    public static final String MISS = "MISS";         // Acqua
    public static final String SUNK = "SUNK";         // Affondato! (seguito dal tipo, es: SUNK:P)

    // Separatore per i messaggi composti (es. SHOT:4:5)
    public static final String SEPARATOR = ":";
}