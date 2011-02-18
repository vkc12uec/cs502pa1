import java.io.*;
import java.net.*;

class TCPClient
{
 public static void main(String argv[]) throws Exception
 {
  String sentence;

  String modifiedSentence;

  BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));

	Socket clientSocket;

try {
 clientSocket = new Socket("localhost", 6780);
 
  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

  sentence = inFromUser.readLine();

  outToServer.writeBytes(sentence + '\n');

  modifiedSentence = inFromServer.readLine();
 System.out.println("FROM SERVER: " + modifiedSentence);

  clientSocket.close();

} catch (Exception e) {
	if (e.equals( new Exception("java.net.ConnectException:") ) )
	System.out.println ("[["+e.toString()+"]]");

	//e.printStackTrace(); 
}

/*
  if (clientSocket == null) {
  	System.out.println("u r genuir");
  }
  else {
  	System.out.println("u r dnam");
  }
*/
  }
}

/*
/**
     * To check if a server is alive
     *
     * @param host either host name or host IP address
     * @param port
     * @return true or false
     */
    public static boolean isServerAlive(String host, int port) {
        java.net.Socket soc = null;

        try {
            java.net.InetAddress addr = java.net.InetAddress.getByName(host);
            soc = new java.net.Socket(addr, port);

            return true;
        } catch (java.io.IOException e) {
            return false;
        } finally {
            if (soc != null) {
                try {
                    soc.close();
                } catch (java.io.IOException e) {
                }
            }
        }
    }
*/
