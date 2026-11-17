package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.InputStream;

public class AuthServer {
    private static final int PORT = 8080;
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Authentication Server listening on port " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                CompletableFuture.runAsync(() -> handleClient(clientSocket), pool);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (InputStream input = socket.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            
            if (bytesRead > 0) {
                String encryptedPayload = new String(buffer, 0, bytesRead);
                String decryptedData = CryptoUtils.decryptAES(encryptedPayload);
                
                if (decryptedData != null) {
                    System.out.println("Decrypted Data: " + decryptedData);
                    
                    String[] parts = decryptedData.split("\\|");
                    long clientTimestamp = 0;
                    
                    for (String part : parts) {
                        if (part.startsWith("TS:")) {
                            clientTimestamp = Long.parseLong(part.substring(3));
                        }
                    }
                    
                    long currentUnixTime = System.currentTimeMillis() / 1000;
                    if (currentUnixTime - clientTimestamp > 5) {
                        System.out.println("SECURITY ALERT: Access Denied. Replay Attack Detected (Expired Timestamp).");
                    } else {
                        System.out.println("Access Granted: Valid Request. Forwarding UNLOCK command to Smart Lock...");
                        try (Socket lockSocket = new Socket("127.0.0.1", 8081)) {
                            lockSocket.getOutputStream().write("UNLOCK".getBytes());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}