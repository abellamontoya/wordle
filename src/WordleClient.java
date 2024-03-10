import java.io.*;
import java.net.*;

public class WordleClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String playerNumberMessage = in.readLine();
            System.out.println(playerNumberMessage);
            boolean isMyTurn = false;
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println(serverMessage);

                if (serverMessage.startsWith("Your turn")) {
                    isMyTurn = true;
                    BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
                    System.out.print("Enter your guess: ");
                    String guess = consoleIn.readLine();
                    out.println(guess);
                } else if (serverMessage.startsWith("Waiting for opponent")) {
                    isMyTurn = false;
                    System.out.println("Wait till your opponent gives a response...");
                } else if (serverMessage.startsWith("Opponent connected")) {
                    isMyTurn = false;
                    System.out.println("Opponent connected. Waiting for opponent to start guessing...");
                } else {
                    int wordLength = Integer.parseInt(serverMessage);
                    System.out.println("The word has " + wordLength + " letters.");
                }
                if (!isMyTurn) {
                    break;
                }
            }

            while ((serverMessage = in.readLine()) != null) {
                System.out.println(serverMessage);
                if (serverMessage.startsWith("Your turn")) {
                    isMyTurn = true;
                    BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
                    System.out.print("Enter your guess: ");
                    String guess = consoleIn.readLine();
                    out.println(guess);
                }
                if (serverMessage.contains("Congratulations!") || serverMessage.contains("Waiting for opponent")) {
                    break;
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
