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
                System.out.print("Enter a command (L)ist, (D)elete, (R)ename, (U)pload, (O)ownload, or (E)xit: ");
                String command = console.readLine();
                if (command.equals("E")) {
                    output.writeByte('E'); // Exit command
                    break;
                }
                output.writeByte(command.charAt(0));

                if (command.equals("L")) {
                    String fileName;
                    while (!(fileName = input.readUTF()).equals("END")) {
                        System.out.println(fileName);
                    }
                } else if (command.equals("D")) {
                    String fileName = console.readLine();
                    output.writeUTF(fileName);

                    if (input.readByte() == 'S') {
                        System.out.println("File deleted successfully.");
                    } else {
                        System.out.println("File deletion failed.");
                    }
                } else if (command.equals("R")) {
                    String oldFileName = console.readLine();
                    String newFileName = console.readLine();
                    output.writeUTF(oldFileName);
                    output.writeUTF(newFileName);

                    if (input.readByte() == 'S') {
                        System.out.println("File renamed successfully.");
                    } else {
                        System.out.println("File renaming failed.");
                    }
                } else if (command.equals("U")) {
                    String fileName = console.readLine();
                    output.writeUTF(fileName);
                    File file = new File(fileName);

                    if (!file.exists()) {
                        System.out.println("File not found.");
                    } else {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }
                            // Send success confirmation to server
                            output.writeByte('S');
                            if (input.readByte() == 'S') {
                                System.out.println("File uploaded successfully.");
                            } else {
                                System.out.println("File upload failed.");
                            }
                        } catch (IOException e) {
                            System.err.println("Error: " + e.getMessage());
                        }
                    }
                } else if (command.equals("O")) {
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
