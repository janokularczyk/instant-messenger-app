package com.jano;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class Server extends JFrame {

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;

    // constructor GUI
    public Server() {
        super("serverWindow by Jano");
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        sendMessage(event.getActionCommand());
                        userText.setText("");
                    }
                }
        );
        add(userText, BorderLayout.SOUTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(600, 600);
        setVisible(true);
    }

    // set up and run the server
    public void startRunning() {
        try {
            server = new ServerSocket(6913, 100);
            while(true) {
                try {
                    waitForConnection();  // waiting for connection with someone
                    setupStreams();  // creating a stream between two computers
                    whileChatting();  // chatting while two computers are connected
                } catch(EOFException eofException) {
                    showMessage("\n Server ended the connection! ");  // connection lost
                } finally {
                    closeCrap();  // server session ended
                }
            }
        } catch(IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // wait for connection, than display connection information
    private void waitForConnection() throws IOException {
        showMessage(" Waiting for someone to connect... \n");
        connection = server.accept();
        showMessage(" Now connected to: " + connection.getInetAddress().getHostName());
    }

    // get stream to send and receive data
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\n Streams are now setup! \n");
    }

    // during the chat conversation
    private void whileChatting() throws IOException {
        String message = "You are now connected! ";
        sendMessage(message);
        ableToType(true);
        do {
            try {
                message = (String) input.readObject();
                showMessage("\n" + message);
            } catch(ClassNotFoundException classNotFoundException) {
                showMessage("\n ERROR: IDK WTF THAT USER SEND! ");
            }
        } while(!message.equals("CLIENT - END"));
    }

    // close streams and sockets after you are done chatting
    public void closeCrap() {
        showMessage("\n Closing connections... \n");
        ableToType(false);
        try {
            output.close();
            input.close();
            connection.close();
        } catch(IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // send message to client
    private void sendMessage(String message) {
        try {
            output.writeObject("SERVER - " + message);
            output.flush();
            showMessage("\nSERVER - " + message);
        } catch(IOException ioException) {
            chatWindow.append("\n ERROR: DUDE I CAN'T SEND THAT MESSAGE");
        }
    }

    // updates chatWindow
    private void showMessage(final String text) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        chatWindow.append(text);
                    }
                }
        );
    }

    // let the user type stuff into their box
    private void ableToType(final boolean tof) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        userText.setEditable(tof);
                    }
                }
        );
    }

}
