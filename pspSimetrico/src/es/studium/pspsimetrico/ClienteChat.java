package es.studium.pspsimetrico;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
public class ClienteChat extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	Socket socket;
	DatagramSocket fentrada;
	DatagramSocket fsalida;
	String nombre;
	static JTextField mensaje = new JTextField();
	private JScrollPane scrollpane;
	static JTextArea textarea;
	JButton boton = new JButton("Enviar");
	JButton desconectar = new JButton("Salir");
	boolean repetir = true;
	int port=44444;
	public ClienteChat(Socket socket, String nombre)
	{
		// Prepara la pantalla. Se recibe el socket creado y el nombre del cliente
		super(" Conexi�n del cliente chat: " + nombre);
		setLayout(null);
		mensaje.setBounds(10, 10, 400, 30);
		add(mensaje);
		textarea = new JTextArea();
		scrollpane = new JScrollPane(textarea);
		scrollpane.setBounds(10, 50, 400, 300);
		add(scrollpane);
		boton.setBounds(420, 10, 100, 30);
		add(boton);
		desconectar.setBounds(420, 50, 100, 30);
		add(desconectar);
		textarea.setEditable(false);
		boton.addActionListener(this);
		this.getRootPane().setDefaultButton(boton);
		desconectar.addActionListener(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.socket = socket;
		this.nombre = nombre;
		// Se crean los flujos de entrada y salida.
		// En el flujo de salida se escribe un mensaje
		// indicando que el cliente se ha unido al Chat.
		// El HiloServidor recibe este mensaje y
		// lo reenv�a a todos los clientes conectados
		try
		{
			int puerto =2021;
			fentrada = new DatagramSocket(puerto);
			fsalida = new DatagramSocket();
			String texto = "SERVIDOR> Entra en el chat... " + nombre;
//			fsalida.send(encriptar(texto));
			encriptado(texto);
		}
		catch (Exception ex)
		{
			System.out.println("Error de E/S");
			ex.printStackTrace();
			System.exit(0);
		}
	}
	// El m�todo main es el que lanza el cliente,
	// para ello en primer lugar se solicita el nombre o nick del
	// cliente, una vez especificado el nombre
	// se crea la conexi�n al servidor y se crear la pantalla del Chat(ClientChat)
	// lanzando su ejecuci�n (ejecutar()).
	public static void main(String[] args) throws Exception
	{
		int puerto = 44444;
		String nombre = JOptionPane.showInputDialog("Introduce tu nombre o nick:");
		Socket socket = null;
		try
		{
			socket = new Socket("127.0.0.1", puerto);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" + ex.getMessage(), "<<Mensaje de Error:1>>", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		if(!nombre.trim().equals(""))
		{
			ClienteChat cliente = new ClienteChat(socket, nombre);
			cliente.setBounds(0,0,540,400);
			cliente.setVisible(true);
			cliente.ejecutar();
		}
		else
		{
			System.out.println("El nombre est� vac�o...");
		}
	}
	// Cuando se pulsa el bot�n Enviar,
	// el mensaje introducido se env�a al servidor por el flujo de salida
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==boton)
		{
			String texto = nombre + "> " + mensaje.getText();
			try
			{
				mensaje.setText("");
//				fsalida.send(encriptar(texto));
				encriptado(texto);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		// Si se pulsa el bot�n Salir,
		// se env�a un mensaje indicando que el cliente abandona el chat
		// y tambi�n se env�a un * para indicar
		// al servidor que el cliente se ha cerrado
		else if(e.getSource()==desconectar)
		{
			String texto = "SERVIDOR> Abandona el chat... " + nombre;
			try
			{
//				fsalida.send(encriptar(texto));
				encriptado(texto);
//				fsalida.send(encriptar("*"));
				encriptado("*");
				repetir = false;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	// Dentro del m�todo ejecutar(), el cliente lee lo que el
	// hilo le manda (mensajes del Chat) y lo muestra en el textarea.
	// Esto se ejecuta en un bucle del que solo se sale
	// en el momento que el cliente pulse el bot�n Salir
	// y se modifique la variable repetir
	public void ejecutar()
	{
		while(repetir)
		{
			try 
			{
				byte[] data = new byte[1024];
				DatagramPacket packet = new DatagramPacket(data, 1024);
				fentrada.receive(packet);
				String cadena = desencriptado(packet);
				textarea.setText(cadena);
			}
			catch (IOException ex)
			{
				JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" + ex.getMessage(), "<<Mensaje de Error:2>>", JOptionPane.ERROR_MESSAGE);
				repetir = false;
			}
		}
		try
		{
			socket.close();
			System.exit(0);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public void encriptado(String texto){
		DatagramPacket packet = null;
		try {
			int port=2020;
			byte[] ipAddr = new byte[] { 127,0,0,1 };
			InetAddress address = InetAddress.getByAddress(ipAddr);
			byte[] plainBytes = texto.getBytes();
			byte[] keySymme = {0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79}; // ClaveSecreta
			SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
			// Crear objeto Cipher e inicializar modo encriptaci�n
			Cipher cipher = Cipher.getInstance("AES"); // Transformaci�n
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] EncryptedData = cipher.doFinal(plainBytes);	
			packet = new DatagramPacket(EncryptedData, EncryptedData.length, address, port);
			fsalida.send(packet);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("Enviando mensaje al Servidor: " + packet);
		System.out.println("Datos encriptado Cliente: " + new String(packet.getData()));
		System.out.println("Mensaje desencriptado Cliente: " +texto);
		//return packet;
	}
	private String desencriptado(DatagramPacket packet){
		
		String res = "";
		
		byte[] keySymme = {0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79}; // ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
		try
		{
			Cipher cipher = Cipher.getInstance("AES");
			// Reiniciar Cipher al modo desencriptado
			cipher.init(Cipher.DECRYPT_MODE, secretKey, cipher.getParameters());
			byte[] plainBytesDecrypted = cipher.doFinal(packet.getData(),packet.getOffset(), packet.getLength());
			res = new String(plainBytesDecrypted);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("CLIENTE=> Mensaje recibido del Servidor: " + packet);
		System.out.println("CLIENTE=> Datos encriptado del Servidor: " + new String(packet.getData()));
		System.out.println("CLIENTE=> Mensaje desencriptado del Servidor: " +res);
		return res;
	}
}