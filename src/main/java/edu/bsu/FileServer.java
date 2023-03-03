package edu.bsu;

import java.io.*;
import java.net.*;

public class FileServer {
    public static void main(String[] args) throws IOException {
        int portNumber = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(portNumber);
        System.out.println("Server is running on port " + portNumber);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket);

            Thread t = new Thread(new ServerThread(clientSocket));
            t.start();
        }
    }
}

class ServerThread implements Runnable {
    private Socket clientSocket;

    public ServerThread(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received command: " + inputLine);
                String[] tokens = inputLine.split(" ");
                String command = tokens[0];

                switch (command) {
                    case "list":
                        File folder = new File(".");
                        File[] listOfFiles = folder.listFiles();
                        StringBuilder files = new StringBuilder();

                        for (File file : listOfFiles) {
                            if (file.isFile()) {
                                files.append(file.getName()).append("\n");
                            }
                        }

                        out.println(files.toString());
                        break;
                    case "delete":
                        String fileName = tokens[1];
                        File fileToDelete = new File(fileName);
                        boolean deleted = fileToDelete.delete();

                        if (deleted) {
                            out.println(fileName + " has been deleted.");
                        } else {
                            out.println("Unable to delete " + fileName);
                        }

                        break;
                    case "rename":
                        String oldName = tokens[1];
                        String newName = tokens[2];
                        File fileToRename = new File(oldName);
                        File renamedFile = new File(newName);
                        boolean renamed = fileToRename.renameTo(renamedFile);

                        if (renamed) {
                            out.println(oldName + " has been renamed to " + newName);
                        } else {
                            out.println("Unable to rename " + oldName);
                        }

                        break;
                    case "download":
                        String fileToSend = tokens[1];
                        File requestedFile = new File(fileToSend);

                        if (requestedFile.isFile()) {
                            out.println("READY");
                            FileInputStream fileStream = new FileInputStream(requestedFile);
                            BufferedInputStream buffer = new BufferedInputStream(fileStream);

                            byte[] bufferArray = new byte[(int) requestedFile.length()];
                            buffer.read(bufferArray, 0, bufferArray.length);

                            OutputStream outputStream = clientSocket.getOutputStream();

                            outputStream.write(bufferArray, 0, bufferArray.length);
                            outputStream.flush();
                            buffer.close();
                            System.out.println(fileToSend + " sent to client.");
                        } else {
                            out.println("FILE_NOT_FOUND");
                        }

                        break;
                    case "upload":
                        String fileNameToSave = tokens[1];
                        FileOutputStream fileOutput = new FileOutputStream(fileNameToSave);
                        InputStream inputStream = clientSocket.getInputStream();

                        byte[] bufferArray = new byte[1024 * 1024];
                        int bytesRead;

                        while ((bytesRead = inputStream.read(bufferArray)) != -1) {
                            fileOutput.write(bufferArray, 0, bytesRead);
                        }

                        fileOutput.close();
                        System.out.println(fileNameToSave + " received from client.");
                        out.println("FILE_RECEIVED");
                        break;
                    default:
                        out.println("Invalid command");
                        break;
                }
            }

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        }
    }
}