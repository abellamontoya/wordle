import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class WordleServer {
    private static List<Socket> clientSockets = new ArrayList<>();
    private static List<BufferedReader> ins = new ArrayList<>();
    private static List<PrintWriter> outs = new ArrayList<>();
    private static int currentPlayerIndex = 0;
    private static List<String> wordList = new ArrayList<>();
    private static boolean gameFinished = false;

    public static void main(String[] args) {

        loadWordsFromFile("words.txt");

        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server started. Waiting for clients...");
            while (clientSockets.size() < 2) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);
                clientSockets.add(clientSocket);
                ins.add(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
                outs.add(new PrintWriter(clientSocket.getOutputStream(), true));

                int playerNumber = clientSockets.size();
                outs.get(playerNumber - 1).println("Welcome! You are Player " + playerNumber);
                if (playerNumber == 1) {
                    outs.get(playerNumber - 1).println("Waiting for opponent to connect...");
                } else {
                    outs.get(playerNumber - 1).println("Opponent connected. You can start guessing.");
                }
            }
            String secretWord = getRandomWord();
            int wordLength = secretWord.length();
            for (PrintWriter out : outs) {
                out.println(wordLength);
            }
            handleClients(secretWord);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadWordsFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                wordList.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRandomWord() {
        Random random = new Random();
        int index = random.nextInt(wordList.size());
        return wordList.get(index);
    }

    private static void handleClients(String secretWord) {
        try {
            while (!gameFinished) {
                synchronized (clientSockets.get(currentPlayerIndex)) {
                    int otherPlayerIndex = (currentPlayerIndex + 1) % 2;
                    outs.get(currentPlayerIndex).println("Your turn. Enter your guess:");
                    String guess = ins.get(currentPlayerIndex).readLine();
                    if (guess == null) {
                        break;
                    }

                    if (!isValidGuess(guess)) {
                        outs.get(currentPlayerIndex).println("Invalid guess. Please enter only letters from 'a' to 'z'.");
                        continue;
                    }

                    outs.get(otherPlayerIndex).println("Opponent's guess: " + guess);
                    String feedback = generateFeedback(secretWord, guess);
                    outs.get(currentPlayerIndex).println("Feedback: " + feedback);

                    if (feedback.equals(guess)) {
                        outs.get(currentPlayerIndex).println("Congratulations! You've guessed the word: " + guess);
                        gameFinished = true;
                        break;
                    }

                    currentPlayerIndex = otherPlayerIndex;
                }
            }for (Socket clientSocket : clientSockets) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValidGuess(String guess) {
        return Pattern.matches("[a-zA-Z]+", guess);
    }

    private static String generateFeedback(String secretWord, String guess) {
        StringBuilder feedback = new StringBuilder();
        for (int i = 0; i < secretWord.length(); i++) {
            char secretChar = secretWord.charAt(i);
            char guessChar = guess.charAt(i);
            if (secretChar == guessChar) {
                feedback.append(guessChar);
            } else if (secretWord.contains(String.valueOf(guessChar))) {
                feedback.append("+");
            } else {
                feedback.append("-");
            }
        }
        return feedback.toString();
    }
}
