package edu.bsu;

import java.io.*;
import java.net.*;

public class FileClient {
    public static void main(String[] args) {
        String serverAddress = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
                Socket socket = new Socket(serverAddress, portNumber);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to server: " + socket);
            String userInput;

            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                String serverResponse = in.readLine();
                System.out.println("Server response: " + serverResponse);
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            System.exit(1);
        }
    }
}
