package edu.bsu;

import java.io.*;
import java.net.*;

public class FileClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java FileClient <server IP address> <server port number>");
            System.exit(1);
        }

        String serverIP = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
                Socket socket = new Socket(serverIP, portNumber);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to file server at " + serverIP + ":" + portNumber);

            while (true) {
                System.out.print("Enter a command (L)ist, (D)elete, (R)ename, (U)pload, or (O)ownload: ");
                String command = console.readLine();
                output.writeByte(command.charAt(0));

                if (command.equals("L")) {
                    String fileName;
                    while (!(fileName = input.readUTF()).equals("END")) {
                        System.out.println(fileName);
                    }
                    if (input.readByte() == 'S') {
                        System.out.println("operation successful");
                    }
                    else {
                        System.out.println("operation failed");
                    }
                } else if (command.equals("D")) {
                    System.out.println("Enter File Name: ");
                    String fileName = console.readLine();
                    output.writeUTF(fileName);

                    if (input.readByte() == 'S') {
                        System.out.println("operation successful");
                    } else {
                        System.out.println("operation failed");
                    }
                } else if (command.equals("R")) {
                    System.out.println("Enter File Name: ");
                    String oldFileName = console.readLine();
                    System.out.println("Enter New File Name: ");
                    String newFileName = console.readLine();
                    output.writeUTF(oldFileName);
                    output.writeUTF(newFileName);

                    if (input.readByte() == 'S') {
                        System.out.println("operation successful");
                    } else {
                        System.out.println("operation failed");
                    }
                } else if (command.equals("U")) {
                    System.out.println("Enter File Name: ");
                    String fileName = console.readLine();
                    output.writeUTF(fileName);
                    File file = new File(fileName);

                    if (!file.exists()) {
                        System.out.println("operation failed");
                    } else {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            int fileSize = (int) file.length();
                            byte[] buffer = new byte[fileSize];
                            int bytesRead = fis.read(buffer);
                            if (bytesRead != fileSize) {
                                output.writeByte('F');
                            } else {
                                output.write(buffer, 0, fileSize);
                                System.out.println("operation successful");
                            }
                        } catch (IOException e) {
                            System.err.println("operation failed");
                        }
                    }
                } else if (command.equals("O")) {
                    System.out.println("Enter File Name: ");
                    String fileName = console.readLine();
                    output.writeUTF(fileName);

                    if (input.readByte() == 'F') {
                        System.out.println("File not found.");
                    } else {
                        try (FileOutputStream fos = new FileOutputStream(fileName)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = input.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                            System.out.println("File downloaded successfully.");
                        } catch (IOException e) {
                            System.err.println("Error: " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("Invalid command.");
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Error: Unknown host " + serverIP);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
