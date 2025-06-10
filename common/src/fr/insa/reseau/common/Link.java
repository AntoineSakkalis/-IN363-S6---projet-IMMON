package fr.insa.reseau.common;

import java.io.*;
import java.net.*;

public class Link {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Link(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush(); // flush to ensure the stream header is sent
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(String message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    public String receive() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (obj instanceof String) {
            return (String) obj;
        } else {
            throw new IOException("Received object is not a String");
        }
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
