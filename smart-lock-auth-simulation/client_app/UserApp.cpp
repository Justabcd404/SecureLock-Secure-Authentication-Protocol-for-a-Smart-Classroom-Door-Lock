#include <iostream>
#include <winsock2.h>
#include <string>

// Link with Ws2_32.lib for Windows sockets
#pragma comment(lib, "ws2_32.lib")

using namespace std;

int main() {
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        cerr << "WSAStartup failed." << endl;
        return 1;
    }

    SOCKET sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock == INVALID_SOCKET) {
        cerr << "Socket creation failed." << endl;
        WSACleanup();
        return 1;
    }

    sockaddr_in serverAddress;
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_port = htons(8080);
    
    // Swapped to inet_addr to bypass MinGW compiler macro issues
    serverAddress.sin_addr.s_addr = inet_addr("127.0.0.1");

    if (connect(sock, (struct sockaddr*)&serverAddress, sizeof(serverAddress)) == SOCKET_ERROR) {
        cerr << "Connection to server failed. Ensure AuthServer is running." << endl;
        closesocket(sock);
        WSACleanup();
        return 1;
    }

    // Pre-encrypted AES payload for testing
    string encryptedPayload = "q/3Z3v9x/0z/6Y5v3z9x/0z/6Y5v3z9x/0z/6Y5v3z8="; 
    
    send(sock, encryptedPayload.c_str(), encryptedPayload.length(), 0);
    cout << "Encrypted access request sent to Authentication Server." << endl;

    closesocket(sock);
    WSACleanup();
    return 0;
}