package es.studium.pspsimetrico;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
public class HiloServidor extends Thread
{
	DatagramSocket fentrada;
	DatagramSocket fsalida;
	Socket socket;
	boolean fin = false;
	int port=44444;
	public HiloServidor(Socket socket)
	{
		this.socket = socket;
		try
		{
			int puerto =2020;
			fentrada = new DatagramSocket(puerto);
			fsalida = new DatagramSocket();
		}
		catch (IOException e)
		{
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}
	// En el método run() lo primero que hacemos
	// es enviar todos los mensajes actuales al cliente que se
	// acaba de incorporar
	public void run()
	{
		ServidorChat.mensaje.setText("Número de conexiones actuales: " +
				ServidorChat.ACTUALES);
		String texto = ServidorChat.textarea.getText();
		EnviarMensajes(texto);
		// Seguidamente, se crea un bucle en el que se recibe lo que el cliente escribe en el chat.
		// Cuando un cliente finaliza con el botón Salir, se envía un * al servidor del Chat,
		// entonces se sale del bucle while, ya que termina el proceso del cliente,
		// de esta manera se controlan las conexiones actuales
		while(!fin)
		{
			try
			{
				
				byte[] data = new byte[1024];
				DatagramPacket packet = new DatagramPacket(data, 1024);
				fentrada.receive(packet);
				String desencriptar = desencriptado(packet);
				//String txtCadena = new String(packet.getData());
				//ServidorChat.textarea.append("ENCRIPTADO> " + txtCadena.replace("\n", "").trim() + "\n");
				if(desencriptar.trim().equals("*"))
				{
					ServidorChat.ACTUALES--;
					ServidorChat.mensaje.setText("Número de conexiones actuales: "
							+ ServidorChat.ACTUALES);
					fin=true;
				}
				// El texto que el cliente escribe en el chat,
				// se añade al textarea del servidor y se reenvía a todos los clientes
				else
				{
					ServidorChat.textarea.append(desencriptar + "\n");
					texto = ServidorChat.textarea.getText();
					
					EnviarMensajes(texto);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				fin=true;
			}
		}
	}
	// El método EnviarMensajes() envía el texto del textarea a
	// todos los sockets que están en la tabla de sockets,
	// de esta forma todos ven la conversación.
	// El programa abre un stream de salida para escribir el texto en el socket
	@SuppressWarnings({ "unused", "resource" })
	private void EnviarMensajes(String texto)
	{
		for(int i=0; i<ServidorChat.CONEXIONES; i++)
		{
			Socket socket = ServidorChat.tabla[i];
			try
			{
				encriptado(texto);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	public void encriptado(String texto){
		DatagramPacket packet = null;
		try {
			int port=2021;
			byte[] ipAddr = new byte[] { 127,0,0,1 };
			InetAddress address = InetAddress.getByAddress(ipAddr);
			byte[] plainBytes = texto.getBytes();
			byte[] keySymme = {0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79}; // ClaveSecreta
			SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
			// Crear objeto Cipher e inicializar modo encriptación
			Cipher cipher = Cipher.getInstance("AES"); // Transformación
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] EncryptedData = cipher.doFinal(plainBytes);	
			packet = new DatagramPacket(EncryptedData, EncryptedData.length, address, port);
			fsalida.send(packet);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("CLIENTE=> Enviando mensaje al Servidor: " + packet);
		System.out.println("CLIENTE=> Datos encriptado Cliente: " + new String(packet.getData()));
		System.out.println("CLIENTE=> Mensaje desencriptado Cliente: " +texto);
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
		System.out.println("SERVIDOR=> Mensaje recibido del Cliente: " + packet);
		System.out.println("SERVIDOR=> Datos encriptado del Cliente: " + new String(packet.getData()));
		System.out.println("SERVIDOR=> Mensaje desencriptado del Cliente: " +res);
		return res;
	}
}