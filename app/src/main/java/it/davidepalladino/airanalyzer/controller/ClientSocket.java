/*
 * This control class provides to connect to a Server socket for sending or receiving message, that will
 *  be processed by another component.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 24th October, 2021
 *
 * This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version
 *
 * This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 */

package it.davidepalladino.airanalyzer.controller;

import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;

public class ClientSocket {
    public static final String ERROR_SOCKET = "ERROR_SOCKET";
    public static final String MESSAGE_SOCKET = "MESSAGE_SOCKET";

    private Context context;

    private String serverIP;
    private int serverPort;

    private boolean isConnected;
    private boolean isErrorWrite;
    private boolean isEerrorRead;

    public String messageRead;

    private Thread workingThread;

    /**
     * @brief This constructor provides to set the object setting only the Context where has been called, the server IP and port.
     * @param context Context where the constructor has been called.
     * @param serverIP Server IP necessary for the connection.
     * @param serverPort Server port necessary for the connection.
     */
    public ClientSocket(Context context, String serverIP, int serverPort) {
        this.context = context;

        this.serverIP = serverIP;
        this.serverPort = serverPort;

        this.isConnected = false;
        this.isErrorWrite = false;
        this.isEerrorRead = false;

        this.messageRead = null;

        this.workingThread = null;
    }

    /**
     * @brief This method provides to connect to the server, necessary to the purposes of the public methods.
     * @return Null pointer if the client is already connected; else, a socket object.
     */
    private Socket connect() throws IOException {
        Socket socket = null;

        /* Checking if there is an previously connection. */
        if (!isConnected) {
            socket = new Socket(InetAddress.getByName(serverIP), serverPort);
            if (socket != null) {
                isConnected = true;
            }
        }

        return socket;
    }

    /**
     * @brief This method provides to send a String message to the server. Will be launched a message Broadcast with the name of the applicant Activity.
     * @param messageSocket Message to send.
     * @param applicantActivity Name of the applicant activity for the broadcast message.
     */
    public void write(String messageSocket, String applicantActivity) {
        workingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = connect();
                    if (socket != null) {
                        isErrorWrite = false;

                        InputStream in = socket.getInputStream();
                        OutputStream out = socket.getOutputStream();

                        /* Sending the message. */
                        out.write(messageSocket.getBytes());

                        /* Closing the socket and deleting the reference of object 'workingThread'. */
                        socket.close();
                        workingThread = null;
                        isConnected = false;
                    } else {
                        isErrorWrite = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isErrorWrite = true;
                }

                /* Sending the result with the broadcast. */
                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY, applicantActivity);
                intentBroadcast.putExtra(ERROR_SOCKET, isErrorWrite);
                context.sendBroadcast(intentBroadcast);
            }
        });
        workingThread.start();
    }

    /**
     * @brief This method provides to read a String message from the server. Will be launched a message Broadcast with the name of the applicant Activity
     *  and the message received.
     * @param applicantActivity Name of the applicant activity for the broadcast message.
     */
    public void read(String applicantActivity) {
        workingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = connect();
                    if (socket != null) {
                        isEerrorRead = false;

                        OutputStream out = socket.getOutputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                        inputStreamReader.getEncoding();
                        BufferedReader in = new BufferedReader(inputStreamReader);

                        /* Reading the message. */
                        messageRead = in.readLine();

                        /* Closing the socket and deleting the reference of object 'workingThread'. */
                        socket.close();
                        workingThread = null;
                        isConnected = false;
                    } else {
                        isEerrorRead = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isEerrorRead = true;
                }

                /* Sending the result with the broadcast. */
                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY, applicantActivity);
                intentBroadcast.putExtra(ERROR_SOCKET, isEerrorRead);
                intentBroadcast.putExtra(MESSAGE_SOCKET, messageRead);
                context.sendBroadcast(intentBroadcast);

                messageRead = null;
            }
        });
        workingThread.start();
    }
}