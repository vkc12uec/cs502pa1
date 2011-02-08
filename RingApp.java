/*
   Your code should throw a RingException when any of the operations in
   RingSubstrate fails - set the cause field appropriately
   */

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;


class RingSubstrate extends Thread{
//class RingSubstrate implements Runnable {
	/* data mem */
	private String 		nbrLeft;
	private String 		nbrRight;
	public String 		selfId;			// like xinu01.cs.purdue.edu IOException
	ServerSocket 		mysock;
	RingApp 			myrapp;
	Socket 				outsock;
	HashMap 			opids;			// operation ids
	private final int	listenport;
	private final int	outport;

	/* which part of code shud handle the incoming requests ? There shud be a handler func. here which uses 
		ServerSocket ? */

	String joinRingSetH0_tag 	= "joinRingSetH0"; // We have only two as we assume that H0 would become the left host of the requesting host
	// PKT = TAG # SRC
	String joinRingSetHR_tag 	= "joinRingSetHR";
	// PKT = TAG # SRC
	String joinRingHost_tag = "joinRingHost";
	String leaveRing_tag 	= "leaveRing";
	String getHosts_tag 	= "getHosts";
	String sendMsgCW_tag 	= "sendMsgCW";
	String sendMsgACW_tag 	= "sendMsgACW";
	String sendAppMsg_tag 	= "sendAppMsg";

	String request = "request";
	String response = "response";
	String msgDelimiter = "#";


//	private final int msg_tag_start 	= 0;
//	//private final int msg_tag_end	 	= 0;
//	private final int src_start	 	= 32;
//	//private final int src_end	 	= 0;
//	private final int dest_start	 	= 64;
//	//private final int dest_end	 	= 0;
//	private final int msg_start	 	= 0;

	/* API s */
	public RingSubstrate(RingApp rApp) {
		myrapp = rApp;
		listenport = 6789;
		outport = 6790;
		//mysock = new ServerSocket (listenport);
		nbrLeft=null; nbrRight=null;
		outsock = null;					//new Socket (6790);
		opids = null; 	/* populate it somehow */
		//selfId = "me";
	}

	public void run()
	{
		try
		{
			mysock = new ServerSocket (listenport);
			String clientMsg;

			while(true)
			{
				Socket connectionSocket = mysock.accept();

	        	    	BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		        	DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
	
			        clientMsg = inFromClient.readLine();
				//System.out.println(clientMsg);

				// Message sent will have different words/token separated by msgDelimiter defined above
				String[] words = clientMsg.split(msgDelimiter);
				String msg_tag = words[0];
				//String req_respo = words[1];
				String src = words[1];
				
				System.out.println("Tag = " + msg_tag + " src = " + src);

				if(msg_tag.compareTo(joinRingSetH0_tag) == 0)
				{
					// By convention node will choose the two hosts as itself and the its right neighbor
					String hostLeft = selfId; // It returns its self Id as the left host for the requesting machine
					String hostRight;
					if(nbrRight == null) // To check whether it is the only member in the ring
					{
						nbrLeft = src;
						nbrRight = src;
						hostRight = selfId;
					}
					else
					{
						String msg(joinRingSetHR_tag + msgDelimiter + src);
						String reply;
						Socket clientSocket = new Socket(nbrRight, listenport);
						DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
						BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						
						outToServer.writeBytes(msg + '\n');
						reply = inFromServer.readLine();
						if(reply == null)
						{
							System.out.println("Right host is selfish");
						}
						hostRight = nbrRight;
						clientSocket.close();
					}


				}
				if(msg_tag.compareTo(joinRingSetHR_tag) == 0)
				{

				}
				if(msg_tag.compareTo(joinRingHost_tag) == 0)
				{
				
				}
				if(msg_tag.compareTo(leaveRing_tag) == 0)
				{
				
				}
				if(msg_tag.compareTo(getHosts_tag) == 0)
				{
				
				}
				if(msg_tag.compareTo(sendMsgCW_tag) == 0)
				{
				
				}
				if(msg_tag.compareTo(sendMsgACW_tag) == 0)
				{
				
				}
				if(msg_tag.compareTo(sendAppMsg_tag) == 0)
				{
				
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public Set<String> joinRing(String hostName) throws /*RingException,*/ IOException{
		   /*
		   One problem with our approach is that, consider h0 asks h1, and h1 has hl and hr ..... 
		   We were asking hl and hr to be h0 prospective neighbours: Assume they form a 4 node ring. 

		   Now h2 comes, and asks h1, if hl, hr agrees , they also form a ring. This will not be a ring structure
		   Hence, i think, we shud return 2 consecutive nodes, who r willing to accept h0.

		   So, best would be to ask ( h1 and either of (hl/hr) if they can include h0 in b/w )
		   */
		String reply;
		String msgid = "Set_joinRing";
		//String msgid = opids.get("Set_joinRing");
		String query = msgid+hostName;
		outsock = new Socket (hostName, listenport);
		DataOutputStream outToServer = new DataOutputStream(outsock.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(outsock.getInputStream()));

		outToServer.writeBytes(query + '\n');
		reply = inFromServer.readLine();

		/* how to check if reply is OK ? 
		   1st nrb = hostName
		   2nd nbr = nbr of (hostname)
		   */
		/* Say the 2 hostnames are joined by a # */
		int posi;
		if ( (posi=reply.indexOf('#')) == -1)
		{
			return null;
		}
			// -ve 
		else {
			Set<String> s=new TreeSet<String>();
			s.add(reply.substring(0, posi-1));
			s.add(reply.substring(posi+1));
			return s;
		}
	}

	public void joinRing(String hostName1, String hostName2) throws /*RingException,*/ IOException{
		/* as we decided, in the function above, we can set the nbrLeft, nbrRight then and there */

		// smth else ?
	}

//	public void leaveRing() throws RingException {
//		leave_nbr (nbrLeft, nbrRight);
//		leave_nbr (nbrRight, nbrLeft);
//	}
//
//	public static void leave_nbr (String whichNbr, String prospectivenbr) {
//		int opid = opids.get("leaveRing");
//		String msg = String (opid)+ '#' + prospectivenbr;
//		outsock = new Socket (whichNbr, 6790);
//		DataOutputStream outTonbrs;
//
//		outTonbrs = new DataOutputStream (outsock.getOutputStream());
//		outTonbrs.writeBytes (msg);
//		outsock.close();			// can we do a new Socket after closign it ?
//
//		/* todo in host-listening-part:
//		the host 'whichNbr' shud wait till it confirms that host 'prospectivenbr' also received a [LeaveRing] with
//		its hostuuid .
//		When it confirms that, then they can modify their datastructures & connect together */
//	}
//
//
//	public List<String> getHosts() throws RingException {
//		/* circulate clcokwise and append self id . END by searching self id in LIST .
//		java list example : http://www.easywayserver.com/blog/java-list-example/
//		*/
//		List<String>	ls		=	new ArrayList<String>();
//		int 			opid	= 	opids.get("getHosts");
//		String			msg 	= 	String(opid) + '#' + selfid;
//		outsock = new Socket (nbrLeft, 6789);
//
//		/* but this algo. wud end when this substrate will receive a same query from its right nbr.... that wud mean
//		interaction of listening part of this substrate with this part of the code.....
//		mutex. ??
//		*/
//		wait (need_signal_from_listeningpart);
//
//		// say we got a String populated (it will be a datamember of substrate .... which was received in reply.
//		StringTokenizer st = new StringTokenizer(in, '#'); 
//		while(st.hasMoreTokens())
//			ls.add(st.nextToken());
//
//		return ls;
//	}

	public void sendAppMessageClockwise(String message) //throws RingException
	{}

	public void sendAppMessageCounterClockwise(String message) //throws RingException
	{}
	public void sendAppMessage(String message, String hostName) //throws RingException
	{}

}

public class RingApp
{
	public static void main(String args[]) throws InterruptedException, IOException// We need to pass the hostname of one of the nodes in the ring. So that it can request that host to let it join the ring
	{
		//RingApp rApp = new RingApp();
		RingSubstrate ringSubstrate = new RingSubstrate(new RingApp());
		String localHost;
		try 
		{
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
			//System.out.println("Hostname of local machine: " + localMachine.getHostName());
			localHost = localMachine.getHostName();
			ringSubstrate.selfId = localHost;
			//System.out.println(localHost);
		}
		catch (java.net.UnknownHostException uhe) 
		{
			System.out.println("Problem in getting local host name");
		}


		if(args.length <= 0)
		{
			System.out.println("It is the first member of the ring");
		}
		else
		{
			System.out.println(args[0] + "is my contact node to join the ring");	
			Set<String> ngbrHost = ringSubstrate.joinRing(args[0]); // The two neighbors the node would be inserted in between
			Iterator it = ngbrHost.iterator();
			String ngbrHostLeft = it.next().toString();
			String ngbrHostRight = it.next().toString();

			ringSubstrate.joinRing(ngbrHostLeft, ngbrHostRight);
		}

		// At this point it has joined the ring
		// We can spawn a new thread which takes care of the duties of the ring network. The main thread would continue to do the job of application
		Thread ringSubThread = new Thread(ringSubstrate);
		ringSubThread.start();
		
		System.out.println("Thread has been started. Now main is controlled by the Application Layer");
	}
}

