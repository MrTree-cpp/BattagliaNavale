// Accetta le connessioni.
package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String[] args) {
        int port = 6767;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server avviato, aspetta il client");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // creazione thread
                BotHandler bot = new BotHandler(clientSocket);
                Thread thread = new Thread(bot);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("errore");
        }

        // 1. Apre la porta del server (es. 6767)
        // 2. Entra in un ciclo infinito (while true)
        // 3. Aspetta che un client si connetta (serverSocket.accept)
        // 4. Appena uno si connette, crea un NUOVO BotHandler (il thread)
        // 5. Avvia il thread e torna ad aspettare il prossimo client

    }
}
