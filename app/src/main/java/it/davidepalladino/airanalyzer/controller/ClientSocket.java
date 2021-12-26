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

import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_BROADCAST;

public class ClientSocket {
    public static final String REQUEST_CODE_SOCKET = "REQUEST_CODE_SOCKET";
    public static final String ERROR_SOCKET = "ERROR_SOCKET";
    public static final String MESSAGE_SOCKET = "MESSAGE_SOCKET";

    private Context context;

    private String serverIP;
    private int serverPort;

    private boolean connect;
    private boolean errorWrite;
    private boolean errorRead;

    public String messageRead;

    private Thread workingThread;

    public ClientSocket(Context context, String serverIP, int serverPort) {
        this.context = context;

        this.serverIP = serverIP;
        this.serverPort = serverPort;

        this.connect = false;
        this.errorWrite = false;
        this.errorRead = false;

        this.messageRead = null;

        this.workingThread = null;
    }

    public Socket connect() throws IOException {
        Socket socket = null;

        if (!connect) {
            socket = new Socket(InetAddress.getByName(serverIP), serverPort);
            if (socket != null) {
                connect = true;
            }
        }

        return socket;
    }

    public void write(int requestCodeSocket, String messageSocket, String requestCodeBroadcast) {
        workingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = connect();
                    if (socket != null) {
                        errorWrite = false;

                        OutputStream out = socket.getOutputStream();
                        InputStream in = socket.getInputStream();

                        /* Sending the request code. */
                        out.write(requestCodeSocket);

                        /* Attempting the confirmation, with the same 'requestCode'. */
                        while (in.available() == 0);

                        if (in.read() == requestCodeSocket) {

                            /* Sending the message. */
                            out.write(messageSocket.getBytes());
                        }

                        /* Closing the socket and deleting the reference of object 'workingThread'. */
                        socket.close();
                        workingThread = null;
                        connect = false;
                    } else {
                        errorWrite = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    errorWrite = true;
                }

                /* Sending the information into broadcast. */
                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SOCKET, requestCodeBroadcast);
                intentBroadcast.putExtra(ERROR_SOCKET, errorWrite);
                context.sendBroadcast(intentBroadcast);
            }
        });
        workingThread.start();
    }

    public void read(int requestCodeSocket, String requestCodeBroadcast) {
        workingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = connect();
                    if (socket != null) {
                        errorWrite = false;

                        OutputStream out = socket.getOutputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                        inputStreamReader.getEncoding();
                        BufferedReader in = new BufferedReader(inputStreamReader);

                        /* Sending the request code and getting message. */
                        out.write(requestCodeSocket);

                        messageRead = in.readLine();

                        /* Closing the socket and deleting the reference of object 'workingThread'. */
                        socket.close();
                        workingThread = null;
                        connect = false;
                    } else {
                        errorWrite = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    errorWrite = true;
                }

                /* Sending the information into broadcast. */
                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SOCKET, requestCodeBroadcast);
                intentBroadcast.putExtra(ERROR_SOCKET, errorRead);
                intentBroadcast.putExtra(MESSAGE_SOCKET, messageRead);
                context.sendBroadcast(intentBroadcast);

                messageRead = null;
            }
        });
        workingThread.start();
    }
}