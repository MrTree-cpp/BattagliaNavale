package Common;

public class Costanti {

    // Dimensioni Griglia
    public static final int DIMENSIONE_CAMPO = 10;

    // Simboli per la visualizzazione e la logica
    public static final char MARE = '~';
    public static final char NAVE = 'O';            // Simbolo generico nave
    public static final char MANCATO = 'X';         // Colpo mancato a segno
    public static final char NAVE_AFFONDATA = '#';  // Nave completamente affondata
    public static final char NAVE_COLPITA = '@';    // Colpo andato a segno

    // Identificativi delle Navi per boardSunk
    public static final char ID_PORTAEREI = 'P';
    public static final char ID_CORAZZATA = 'C';
    public static final char ID_INCROCIATORE_1 = 'I';
    public static final char ID_INCROCIATORE_2 = 'U';
    public static final char ID_CACCIATORPEDINIERE = 'D';
    public static final char ID_SOTTOMARINO = 'S';

    // Lunghezze delle Navi
    public static final int LUNGHEZZA_PORTAEREI = 5;
    public static final int LUNGHEZZA_CORAZZATA = 4;
    public static final int LUNGHEZZA_INCROCIATORE = 3;
    public static final int LUNGHEZZA_CACCIATORPEDINIERE = 3;
    public static final int LUNGHEZZA_SOTTOMARINO = 2;
}