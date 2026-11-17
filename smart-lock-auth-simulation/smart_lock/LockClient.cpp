#include <iostream>
#include <winsock2.h>
#include <string>

#pragma comment(lib, "ws2_32.lib")

using namespace std;

int main() {
    WSADATA wsaData;
    WSAStartup(MAKEWORD(2, 2), &wsaData);

    SOCKET serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    sockaddr_in serverAddr;
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(8081);

    bind(serverSocket, (struct sockaddr*)&serverAddr, sizeof(serverAddr));
    listen(serverSocket, 1);
    
    cout << "Smart Lock actively monitoring classroom door on port 8081..." << endl;

    SOCKET clientSocket = accept(serverSocket, NULL, NULL);
    char buffer[1024] = {0};
    recv(clientSocket, buffer, sizeof(buffer), 0);
    
    string command(buffer);
    if (command == "UNLOCK") {
        cout << "PHYSICAL MECHANISM TRIGGERED: Door Unlocked." << endl;
    }

    closesocket(clientSocket);
    closesocket(serverSocket);
    WSACleanup();
    return 0;
}