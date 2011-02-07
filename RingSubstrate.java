/*
   Your code should throw a RingException when any of the operations in
   RingSubstrate fails - set the cause field appropriately
   */

import java.io.*;
import java.util.*;
import java.net.*;

public class RingSubstrate {
	/* data mem */
	private String 		nbrleft;
	private String 		nbrright;
	private String 		selfId;			// like xinu01.cs.purdue.edu
	ServerSocket 		mysock;
	RingApp 			myrapp;
	Socket 				outsock;
	HashMap 			opids;			// operation ids
	private final int	listenport;
	private final int	outport;

	/* which part of code shud handle the incoming requests ? There shud be a handler func. here which uses 
		ServerSocket ? */

	/* API s */
	public RingSubstrate(RingApp rApp) {
		myrapp = rApp;
		listenport = 6789;
		outport = 6790;
		mysock = new ServerSocket (listenport);
		nbrleft="", nbrright="";
		outsock = NULL;					//new Socket (6790);
		opids = NULL; 	/* populate it somehow */
	}

	public Set<String> joinRing(String hostName) throws RingException {
		   /*
		   One problem with our approach is that, consider h0 asks h1, and h1 has hl and hr ..... 
		   We were asking hl and hr to be h0 prospective neighbours: Assume they form a 4 node ring. 

		   Now h2 comes, and asks h1, if hl, hr agrees , they also form a ring. This will not be a ring structure
		   Hence, i think, we shud return 2 consecutive nodes, who r willing to accept h0.

		   So, best would be to ask ( h1 and either of (hl/hr) if they can include h0 in b/w )
		   */
		String reply;
		String msgid = opids.get("Set_joinRing");
		String query = msgid+hostName;
		outsock = new Socket (hostname, 6790);
		DataOutputStream outToServer = new DataOutputStream(outsock.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		outToServer.writeBytes(query + '\n');
		reply = inFromServer.readLine();

		/* how to check if reply is OK ? 
		   1st nrb = hostName
		   2nd nbr = nbr of (hostname)
		   */
		/* Say the 2 hostnames are joined by a # */
		int posi;
		if ( (posi=reply.indexOf('#')) == -1)
			// -ve 
		else {
			Set<String> s=new TreeSet<String>();
			s.add(reply.substring(0, posi-1));
			s.add(reply.substring(posi+1));
			return s;
		}
	}

	public void joinRing(String hostName1, String hostName2) throws RingException {
		/* as we decided, in the function above, we can set the nbrleft, nbrright then and there */

		// smth else ?
	}

	public void leaveRing() throws RingException {
		leave_nbr (nbrleft, nbrright);
		leave_nbr (nbrright, nbrleft);
	}

	public static void leave_nbr (String whichNbr, String prospectivenbr) {
		int opid = opids.get("leaveRing");
		String msg = String (opid)+ '#' + prospectivenbr;
		outsock = new Socket (whichNbr, 6790);
		DataOutputStream outTonbrs;

		outTonbrs = new DataOutputStream (outsock.getOutputStream());
		outTonbrs.writeBytes (msg);
		outsock.close();			// can we do a new Socket after closign it ?

		/* todo in host-listening-part:
		the host 'whichNbr' shud wait till it confirms that host 'prospectivenbr' also received a [LeaveRing] with
		its hostuuid .
		When it confirms that, then they can modify their datastructures & connect together */
	}


	public List<String> getHosts() throws RingException {
		/* circulate clcokwise and append self id . END by searching self id in LIST .
		java list example : http://www.easywayserver.com/blog/java-list-example/
		*/
		List<String>	ls		=	new ArrayList<String>();
		int 			opid	= 	opids.get("getHosts");
		String			msg 	= 	String(opid) + '#' + selfid;
		outsock = new Socket (nbrleft, 6789);

		/* but this algo. wud end when this substrate will receive a same query from its right nbr.... that wud mean
		interaction of listening part of this substrate with this part of the code.....
		mutex. ??
		*/
		wait (need_signal_from_listeningpart);

		// say we got a String populated (it will be a datamember of substrate .... which was received in reply.
		StringTokenizer st = new StringTokenizer(in, '#'); 
		while(st.hasMoreTokens())
			ls.add(st.nextToken());

		return ls;
	}

	public void sendAppMessageClockwise(String message) throws RingException;

	public void sendAppMessageCounterClockwise(String message) throws RingException;

	public void sendAppMessage(String message, String hostName) throws RingException;

}
