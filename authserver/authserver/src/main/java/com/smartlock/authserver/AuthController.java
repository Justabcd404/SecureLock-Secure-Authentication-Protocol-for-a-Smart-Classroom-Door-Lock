package com.smartlock.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static void main(String[] args) {
        SpringApplication.run(AuthController.class, args);
    }

    @PostMapping("/verify")
    public CompletableFuture<ResponseEntity<String>> verifyAccess(@RequestBody Map<String, String> payload) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encryptedData = payload.get("data");
                String decryptedData = CryptoUtils.decryptAES(encryptedData);

                if (decryptedData == null) {
                    return ResponseEntity.badRequest().body("Decryption Failed");
                }

                System.out.println("Decrypted Data: " + decryptedData);
                
                String[] parts = decryptedData.split("\\|");
                long clientTimestamp = 0;
                String studentId = "";
                String roomId = "";

                for (String part : parts) {
                    if (part.startsWith("TS:")) clientTimestamp = Long.parseLong(part.substring(3));
                    if (part.startsWith("ID:")) studentId = part.substring(3);
                    if (part.startsWith("ROOM:")) roomId = part.substring(5);
                }

                long currentUnixTime = System.currentTimeMillis() / 1000;
                if (currentUnixTime - clientTimestamp > 5) {
                    System.out.println("SECURITY ALERT: Replay Attack Detected for Student " + studentId);
                    return ResponseEntity.status(403).body("Access Denied: Expired Timestamp");
                }

                System.out.println("Access Granted for Student " + studentId + " to Room " + roomId);
                return ResponseEntity.ok("Access Granted");

            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Server Error");
            }
        });
    }
}