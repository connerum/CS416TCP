package edu.bsu;

import java.io.*;
import java.net.*;

public class FileServer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java FileServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("File server is listening on port " + portNumber);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress().getHostAddress());
                new ServerThread(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}

class ServerThread extends Thread {
    private Socket clientSocket;

    public ServerThread(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();
                DataInputStream dataInput = new DataInputStream(input);
                DataOutputStream dataOutput = new DataOutputStream(output);
        ) {
            byte command = dataInput.readByte();

            if (command == 'L') {
                // List files
                File folder = new File(".");
                File[] files = folder.listFiles();

                for (File file : files) {
                    if (file.isFile()) {
                        dataOutput.writeUTF(file.getName());
                    }
                }
                dataOutput.writeUTF("END");
                dataOutput.writeByte('S');
            } else if (command == 'D') {
                // Delete file
                String fileName = dataInput.readUTF();
                File file = new File(fileName);

                if (file.delete()) {
                    dataOutput.writeByte('S'); // Success
                } else {
                    dataOutput.writeByte('F'); // Failure
                }
            } else if (command == 'R') {
                // Rename file
                String oldFileName = dataInput.readUTF();
                String newFileName = dataInput.readUTF();
                File oldFile = new File(oldFileName);
                File newFile = new File(newFileName);

                if (oldFile.renameTo(newFile)) {
                    dataOutput.writeByte('S'); // Success
                } else {
                    dataOutput.writeByte('F'); // Failure
                }
            } else if (command == 'U') {
                // Upload file
                String fileName = dataInput.readUTF();
                File file = new File(fileName);

                if (file.exists()) {
                    dataOutput.writeByte('F'); // Failure
                }
                else {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = dataInput.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        dataOutput.writeByte('S');
                    } catch (IOException e) {
                        dataOutput.writeByte('F'); // Failure
                    }
                }
            } else if (command == 'O') {
                // Download file
                String fileName = dataInput.readUTF();
                File file = new File(fileName);

                if (!file.exists()) {
                    dataOutput.writeByte('F'); // Failure
                } else {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        int fileSize = (int) file.length();
                        byte[] buffer = new byte[fileSize];
                        int bytesRead = fis.read(buffer);
                        if (bytesRead != fileSize) {
                            dataOutput.writeByte('F'); // Failure
                        } else {
                            dataOutput.writeByte('S'); // Success
                            dataOutput.write(buffer, 0, fileSize);
                        }
                    } catch (IOException e) {
                        dataOutput.writeByte('F'); // Failure
                    }
                }
            }
            clientSocket.shutdownOutput();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}