package common;

import java.io.*;
import java.net.*;

public class Link implements AutoCloseable {
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public Link(String ip, int port) throws IOException {
		this.socket = new Socket(ip, port);
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.out.flush(); // flush to ensure the stream header is sent
		this.in = new ObjectInputStream(socket.getInputStream());
	}

	public Link(Socket socket) throws IOException {
		this.socket = socket;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.out.flush(); // flush to ensure the stream header is sent
		this.in = new ObjectInputStream(socket.getInputStream());
	}

	public void send(Trame message) throws IOException {
		out.writeObject(message);
		out.flush();
	}

	public Trame receive() throws IOException{
		try {
			Object obj = in.readObject();

			if (obj instanceof Trame) {
				return (Trame) obj;
			} else {
				throw new IOException("Received object is not a Trame");
			}
		} catch (Exception e) {
		}
		return null;
	}

	public void close() throws IOException {
		in.close();
		out.close();
		socket.close();
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public ObjectOutputStream getOut() {
		return out;
	}

	public void setOut(ObjectOutputStream out) {
		this.out = out;
	}

	public ObjectInputStream getIn() {
		return in;
	}

	public void setIn(ObjectInputStream in) {
		this.in = in;
	}

}
