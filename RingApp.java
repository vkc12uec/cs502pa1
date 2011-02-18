/*
   Your code should throw a RingException when any of the operations in
   RingSubstrate fails - set the cause field appropriately
 */

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.math.*;

/*
TODO:
	- race conditions
	- corner cases, like self leaveRing
	- Exception
*/

class FindLeader extends Thread {			// extend it as RingSubs., so that datamembers of RingSubs are visible here
	public RingSubstrate 		myRingSubs;
	public RingApp 				myRingApp;
	public static boolean	 			am_i_leader;
	//public static volatile boolean	 			am_i_leader;
	public static boolean	 			election_continues;
	String 						msgDelimiter = "#";
	//String 						ordered_list;
	String 						leader_id;				//ringsubs shud be able to change this.
	public int	phase;
	public int	hop;
	String	selfId;
	String	nbrRight;
	String	nbrLeft;
	public static String		sendElection_tag;
	public static String		sendBroadcast_tag;
	public static String		heartBeat_tag;
	public static String		sendUpdateNbr;
	public static Object		lock1;
	public List<String>		ordered_list;
	public List<String>		dead_list;

	public FindLeader (RingApp ra, RingSubstrate rs) {
		//System.out.println ("I am FindLeader::ctor");
		myRingSubs = rs;
		myRingApp = ra;
		//still_leader_in_nbrhood	= true;
		am_i_leader = true;
		//ordered_list = "";
		phase = 0;				// increment in nbrhood -> 1, 2, 4, 8 ...
		hop = (int)Math.pow ((2.00000), phase);
		leader_id = "batman";
		sendElection_tag = "sendElection";
		sendBroadcast_tag = "sendBroadcast";
		heartBeat_tag = "heartBeat";
		sendUpdateNbr 	= "sendUpdateNbr";
		// PKT = TAG # SRC # left/right # new_id 											
		nbrRight = rs.nbrRight;
		nbrLeft = rs.nbrLeft;
		selfId = rs.selfId;
		lock1 = new Object();
		dead_list = new ArrayList <String> ();
		ordered_list = new ArrayList <String> ();
		//System.out.println ("FindLeader::ctor" + selfId + "||"+nbrLeft+"||"+nbrRight);
	}
	
	public void prntDetails () {
		String me = "FindLeader::prntDetails";
		RingSubstrate.debug (me + " selfid = "+selfId + " nbrRight / nbrLeft " + nbrRight + " " + nbrLeft );
	}

	public static void debug (String msg) {
		System.out.println (msg);
	}

	// this thread will run on each host. they carry oepration in sync. rounds. Incase, a node finds that he is not a proble leader in its neighbour hood, it shud stop this thread
	// so, while loop shud be on a boolean, which can be set by substrate .....\
	// if node had stopped its leader_find thread, it shud  just pass by the ELECTION & REPLY msgs.

	public void run() {
		String msg, reply, reply1, reply2;
		String me = "FindLeader::run() ";
		String compareWith = selfId;

		// still_leader_in_nbrhood = true, this will be made false by RingSubs.
		// We fire <ELECTION> just once. Next phase is advanced by RingSubs handler
		// make a election msg. + propagate to left and right
		String []args = {sendElection_tag , selfId , String.valueOf(phase) , String.valueOf(hop - 1) , compareWith };
		msg = RingSubstrate.joinit (args);
			//sendElection_tag + msgDelimiter + selfId + msgDelimiter + phase + msgDelimiter + hop + msgDelimiter + compareWith;		// MSG -elec send
		//debug (me + " : " + msg);
		reply1 = RingSubstrate.sendToHost (msg, nbrRight);		// their's reply will be handled by RingSubs. obj, on this host.
		reply2 = RingSubstrate.sendToHost (msg, nbrLeft);	// alpha1

        election_continues = true;
		synchronized(lock1){
                while (election_continues) {
                        try {
							lock1.wait();			// notify wud reset election_continues and set am_i_leader corectly
							} catch (Exception e) {
								e.printStackTrace();
								}
                }
        }
		//debug(me +"out of synchronised block");
		
		if (am_i_leader == true) {

		try {
				 ordered_list = myRingSubs.getHosts();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

			while (true) {
				try {
					sleep (5000);		// 30 secs.
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					//List <String> ordered_list = myRingSubs.getHosts();
					//ordered_list = myRingSubs.getHosts();
					//List <String> ordered_list = myRingSubs.getHosts();

					// if ordered_list is not > 1 , u r screwed
					//String broadcast = listToString (ordered_list);
					//debug (me + broadcast);

						debug("======before deadlist connection chk=");
					for (int i=0; i < ordered_list.size(); i++) {
						String desti = ordered_list.get(i);
						//check if host is dead ?
						//InetAddress address = InetAddress.getByName(desti);
						String [] args2 = {heartBeat_tag, selfId};
						msg  = RingSubstrate.joinit(args2);
						reply = RingSubstrate.sendToHost(msg, desti);
						if (reply.equals("no")) {
							debug ("\n Node :"+ desti + " has died ");
							dead_list.add(desti);
						}
						debug("======After deadlist connection chk=");

						//String []args1 = {sendBroadcast_tag , selfId, broadcast};
						//msg = RingSubstrate.joinit (args1);
						//reply = RingSubstrate.sendToHost(msg, desti);			// check here if node died	beta1
					}

					if (dead_list.size() != 0) {
					// tell fkers to re-join ; use ordered_list ; find the next living node on either of dead nodes
						for (int i=0;i < dead_list.size(); i++) {
							String deadNode = dead_list.get(i);
							int posi = ordered_list.indexOf (deadNode);
							int N = ordered_list.size();

							if (posi == -1) {
								// this cant be possible ?
							}
							else {
								int j=(posi+1)%N, k;
								k = (posi==0) ? N-1 : posi-1;

								// check for non dead node in ordered_list next to this one.
								while (dead_list.indexOf(ordered_list.get(j)) != -1) { j=(j+1)%N; }
								while (dead_list.indexOf(ordered_list.get(k)) != -1) { 
									if (k==0) { k = N-1; } 
									else { k = k-1; } 
									}

								String new_left = ordered_list.get(j);
								String new_right = ordered_list.get(k);

								debug ("\n\n Dead node = "+ deadNode + " Its Non dead nbrs: " + "Right = " + new_right + "Left = " + new_left );

								String args2[] = {sendUpdateNbr, selfId, "right", new_right };	
								msg = RingSubstrate.joinit (args2);
								reply = RingSubstrate.sendToHost (msg, new_left );
								
								// // PKT = TAG # SRC # left/right # new_id 
								String args3[] = {sendUpdateNbr, selfId, "left", new_left };
								msg = RingSubstrate.joinit (args3);
								reply = RingSubstrate.sendToHost (msg, new_right );
							}	// else


						}	// for

						// repritn for verification of 

					} // if non zero size of dead list
					ordered_list = myRingSubs.getHosts();
					String broadcast = listToString (ordered_list);
					String []args1 = {sendBroadcast_tag , selfId, broadcast};
					msg = RingSubstrate.joinit (args1);
					for (int i=0; i < ordered_list.size(); i++) {
						String desti = ordered_list.get(i);
						reply = RingSubstrate.sendToHost_tmp(msg, desti);			// check here if node died	beta1
					}
					debug ("\n #### After removing the dead nodes, NEW LIST = " + broadcast);

				} catch (Exception e) 
					{ e.printStackTrace(); }
			}	// while-infinite
		}

		else {	// handle shud wait for received an orderer list msg from the leader 
		}

	}	// run()

	public static String listToString (List <String> l) {
		String reply="";
		for (int i =0; i<l.size() ; i++)
			reply += l.get(i);

		return reply;
	}

}	// end Class FindLeader

class RingSubstrate extends Thread {
	/* data mem */
	public String 		nbrLeft;
	public String 		nbrRight;
	public String 		selfId;			// like xinu01.cs.purdue.edu IOException
	ServerSocket 		mysock;
	RingApp 			myrapp;
	Socket 				outsock;
	HashMap 			opids;			// operation ids
	private static int	listenport;
	private int	outport;
	private final Object lock;

	String joinRingSetH0_tag 	= "joinRingSetH0"; // We have only two as we assume that H0 would become the left host of the requesting host
	// PKT = TAG # SRC
	String joinRingSetHR_tag 	= "joinRingSetHR";
	// PKT = TAG # SRC
	String joinRingHost_tag = "joinRingHost";
	// PKT = TAG # SRC1 # SRC2
	String leaveRing_tag 	= "leaveRing";
	// PKT = TAG # SRC # PROSPECTI_NBR
	String getHosts_tag 	= "getHosts";
	// PKT = TAG # h1||h2||h3|| .....
	String sendMsgCW_tag 	= "sendMsgCW";
	// PKT = TAG # <SOURCE>
	String sendMsgACW_tag 	= "sendMsgACW";
	// PKT = TAG # <SOURCE>
	String sendAppMsg_tag 	= "sendAppMsg";
	// PKT = TAG # SRC			: SRC is host for which this msg. is meant
	String sendElection_tag 	= "sendElection";
	// PKT = TAG # SRC # phase # hop-count # comparator			
	String sendElecReply_tag 	= "sendElectionReply";
	// PKT = TAG # SRC # comparator 											
	String sendStopLeaderThread_tag 	= "sendStopLeaderThread";
	// PKT = TAG # SRC # comparator 											
	String sendBroadcast_tag 	= "sendBroadcast";
	// PKT = TAG # SRC # broadcast 											
	String sendUpdateNbr 	= "sendUpdateNbr";
	// PKT = TAG # SRC # left/right # new_id 											
	String		heartBeat_tag = "heartBeat";
	// PKT = TAG # SRC

	String request = "request";
	String response = "response";
	String msgDelimiter = "#";
	String hostsJoiner = "||";
	String hostsJoinedlist;
	int		received;
	boolean hostlistBool;

	int 	phase;
	int 	hop;
	boolean leader_elected;
	String 	leader;
	//private final Object lock1;		// it shudnt be final / ?????  / 
	String broadcastList;
	String selected_leader;

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
		hostsJoinedlist="";
		hostlistBool = false;
		lock = new Object();
		//lock1 = new Object();
		leader_elected = false;
		received = 0;
		broadcastList = "broadcast-list-NULL";
		selected_leader = "null1";
		//still_leader_in_nbrhood = true; phase = 0; hop = 1;
	}

/* algo:
The ELECTION messages sent by candidates contain three fields:
	The id of the candidate.
	The current phase number k .
	A hop counter d, which is initially 0 and is incremented by 1
	whenever the message is forwarded to the next pi .
*/
	public String getLeader() throws RingException {
		debug ("Leader value for my node ("+selfId+") = "+leader);
		return "batman";
	}

	// helper functions
	public String getRightnbr () { return nbrRight; }
	public String getLeftnbr () { return nbrLeft; }
	public String getid () { return selfId; }

    public static String joinit (String[] args) {
        String delim = "#";
        String reply="";
        for (int i=0; i<args.length; i++) {
            reply += args[i];
            if (i == args.length-1)
                break;
            reply += delim;
        }
		return reply;
    }  		// check return OK

	public static void debug (String msg) {
		System.out.println (msg);
	}

	public String relay(String msg, String src) {	// propagate to left or right depending on 'src'
		if (src.equals(nbrRight))
			return sendToHost (msg, nbrLeft);	
		else
			return sendToHost (msg, nbrRight);
	}

	public String antiRelay(String msg, String src) {	// propagate to left or right depending on 'src'
		if (src.equals(nbrRight))
			return sendToHost (msg, nbrRight);
		else
			return sendToHost (msg, nbrLeft);
	}

	public static String sendToHost(String msg, String dest)
	{
		String whoami = "sendToHost";
		try
		{
			Socket clientSocket = new Socket(dest, listenport);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			outToServer.writeBytes(msg + '\n');
			String reply = inFromServer.readLine();

			//debug(reply + " from " + dest);
			clientSocket.close();
			return reply;
			//return "yes";
		}
		catch(Exception e)
		{
			if (e.toString().equals ("java.net.ConnectException: Connection refused"))
			return "no";
			//return "java.net.ConnectException: Connection refused";
			//e.printStackTrace();
		}
		System.out.println (whoami + ": returns null ");
		return null;
	}
	public static String sendToHost_tmp(String msg, String dest)
	{
		String whoami = "sendToHost";
		try
		{
			Socket clientSocket = new Socket(dest, listenport);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			//BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			outToServer.writeBytes(msg + '\n');
			//String reply = inFromServer.readLine();

			//debug(reply + " from " + dest);
			clientSocket.close();
			//return reply;
			return "yes";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println (whoami + ": returns null ");
		return null;
	}
	// END helper functions

// ####################################### Ring substrate thread runs this function ##############################################

	public void run() //throws /*RingException*/ InterruptedException
	{
		try
		{
			mysock = new ServerSocket (listenport);
			String clientMsg;
			String msg, reply, reply1, reply2;		// mite cause PROBLEM 

			while(true)
			{
				//hostsJoinedlist = ("HJL in run thread");
				//debug (hostsJoinedlist);
				Socket connectionSocket = mysock.accept();
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

				clientMsg = inFromClient.readLine();
				debug("\n\nMsg Recevied: \n\t" + clientMsg+ "\n");
				if(clientMsg.indexOf(msgDelimiter) == -1)
				{
					continue;
				}
				//System.out.println(clientMsg);

				// Message sent will have different words/token separated by msgDelimiter defined above
				String[] words = clientMsg.split(msgDelimiter);
				String msg_tag = words[0];
				String src = words[1];				// host who wants to join the ring
				//System.out.println("Tag = " + msg_tag + " src = " + src);
				

//###################################### handler for election msg listen A ###############################################
				if(msg_tag.equals(heartBeat_tag)) {		
					outToClient.writeBytes("yes\n");
//					broadcastList = words[2];
//					selected_leader = src;
//					debug (" Broadcast msg from: " + selected_leader + "\n list = " + broadcastList);

					// TODO : send a ping packetto leader
				}
//###################################### handler for election msg listen A ###############################################

				if(msg_tag.equals(sendUpdateNbr)) {
					//outToClient.writeBytes("yes\n");		// why are we still doing this ?
					String lr = words [2];		// left or right ?
					String newnbr = words[3];
					debug("Got ngbr update msg, nbr = " + newnbr);

					if (lr.equals ("right")) {
						nbrRight = newnbr;
						debug ("\nUpdate my right nbr");
					}
					else {
						nbrLeft = newnbr;
						debug ("\nUpdate my left nbr");
					}

					// TODO :print an ordered list again to see if ring is joined 
				}

//###################################### handler for election msg listen A ###############################################
				if(msg_tag.equals(sendBroadcast_tag)) {		
					//outToClient.writeBytes("yes\n");
					broadcastList = words[2];
					selected_leader = src;
					debug (" Broadcast msg from: " + selected_leader + "\n list = " + broadcastList);

					// TODO : send a ping packetto leader
				}

//###################################### handler for election msg listen B ###############################################

				if(msg_tag.equals(sendStopLeaderThread_tag)) {		// received a stop thread REPLY	alpha3
						// msg = sendStopLeaderThread_tag + msgDelimiter + selfId + msgDelimiter + compareWith
					outToClient.writeBytes("yes\n");
					String midpoint = words[2];
					//debug (selfId + " : msg received sendStopLeaderThread_tag");

					if (selfId.equals(midpoint)) {
						//debug (" Stop self leader election thread ");
						synchronized (FindLeader.lock1) {
							FindLeader.election_continues = false;	
							FindLeader.am_i_leader = false;
							FindLeader.lock1.notifyAll();
							}
						}
					else {		// relay packet
						String [] args = { sendStopLeaderThread_tag, selfId , midpoint };
						msg = joinit (args);		//sendStopLeaderThread_tag + msgDelimiter + selfId + msgDelimiter + compareWith;
						//debug ("id = "+selfId + " sendStopLeaderThread_tag : sending this to :"+midpoint);
						relay (msg, src);
					}
				}

//###################################### handler for election msg listen C ###############################################

				if(msg_tag.equals(sendElecReply_tag)) {		// received a REPLY	alpha2
					//String msg, reply;
					String compareWith = words[2];
					//debug (selfId + " : msg received sendElecReply_tag ");
					outToClient.writeBytes("yes\n");

					//if (src.equals(selfId)) {	// advance to next phase 
					if (selfId.equals(compareWith)) {	// advance to next phase 
						// u shud receive REPLY from 2 sides
						received++;
						if (received == 2) {
							received = 0;
								// send new ELECTION packet
							String [] args = { sendElection_tag , selfId , String.valueOf(++phase) , String.valueOf (Math.round(Math.pow(2, phase) - 1)) , compareWith };	// alpha1
							msg = joinit (args);
								//msg = sendElection_tag + msgDelimiter + selfId + msgDelimiter + (++phase) + msgDelimiter + Math.pow(2, phase) + msgDelimiter + compareWith;	// CHECK phase, hop
								// copy from while leader thread.
							reply1 = sendToHost_tmp (msg, nbrRight);
							//debug("Between right and left in phase = " + phase);
							try {
								//sleep(5000);
								} catch (Exception e) { e.printStackTrace(); }
							reply2 = sendToHost_tmp (msg, nbrLeft);
							//debug("After right and left in phase = " + phase);
						}
						// else nthing todo
					}
					else {	// relay the REPLY packet
						String [] args = { sendElecReply_tag, selfId , compareWith };
						msg = joinit (args);
						//msg = sendElecReply_tag + msgDelimiter + selfId;
						reply = relay (msg, src);	// send to left or right depending on 'src'
					}
				}

//###################################### handler for election msg listen D ###############################################

				if(msg_tag.equals(sendElection_tag)) {		// MSG-elec recv.	alpha1
					/* msg = sendElection_tag + msgDelimiter + selfId + msgDelimiter + phase + msgDelimiter + hop + msgDelimiter + compareWith; */

					int phase 			= Integer.parseInt(words[2]);
					int hop 			= Integer.parseInt(words[3]);
					String compareWith 	= words[4];

					outToClient.writeBytes("yes\n");
					//String new_msg, reply, msg;

					if (selfId.compareTo(compareWith) > 0)	{	// swallow	or tell back the mid point to stop that while kloop
							// u shud tell the leader theread on the mid point to stop 'its leader election'
						//debug (" Msg. swallow packet from " + compareWith );
						String [] args = {sendStopLeaderThread_tag , selfId , compareWith };	//alpha3
						msg = joinit (args);
							//msg = sendStopLeaderThread_tag + msgDelimiter + selfId + msgDelimiter + compareWith;
						reply = antiRelay (msg, src);
						}

					else if ( selfId.compareTo(compareWith) < 0) {	// propagate
						if (hop == 0) { // tell back mid point to go in new round
								String [] args = { sendElecReply_tag, selfId , compareWith };
								msg = joinit (args);
								//msg = sendElecReply_tag + selfId;	// the mid-point will know frm selfId if he has reved 2 replies	alpha2
								antiRelay (msg, src); 	// ack ?
							}
						else {
								// fwd the ELECTION msg along the orig. direction
							String [] args = {sendElection_tag, selfId, String.valueOf(phase) , String.valueOf(hop-1), compareWith };
								//msg = sendElection_tag + msgDelimiter + selfId + msgDelimiter + phase + msgDelimiter + (hop-1) + msgDelimiter + compareWith;
							msg = joinit (args);
							//relay
							//debug("before relaying the msg = " + msg);
							relay (msg, src);
							//debug(msg + " relayed from " + src);
							}
						}

					else {
							// you are the leader

						synchronized (FindLeader.lock1) {
							FindLeader.election_continues = false;
							FindLeader.am_i_leader = true;
							FindLeader.lock1.notifyAll();
							}
						debug (" $$$$$ I am the leader $$$$$ : ");
					}

				}

//###################################### handler for election msg listen  ###############################################

//###################################### case 1 ######################################################################
				if(msg_tag.compareTo(joinRingSetH0_tag) == 0) // Msg-H0 recv
				{
					// By convention node will choose the two hosts as itself and the its right neighbor
					String hostLeft = selfId; // It returns its self Id as the left host for the requesting machine
					String hostRight;

					if(nbrRight == null && nbrLeft == null) // To check whether it is the only member in the ring
					{	// this is just a single node...form a 2 node ring
						nbrRight = src;		// correcting
						nbrLeft = src;		// correcting
						hostRight = selfId;
						// send packet now
					}
					else
					{
						msg = joinRingSetHR_tag + msgDelimiter + src; // Msg-HR sent
						reply = sendToHost(msg, nbrRight);
						if(reply.compareTo("no") == 0)
						{
							System.out.println("Right host is selfish");
						}
						hostRight = nbrRight;
						nbrRight = src;
						//clientSocket.close();
					}

					// ?? nbrRight = src; // It sets the src as its right ngbr
					String joinRingSetH0_res = hostLeft + msgDelimiter + hostRight + '\n'; // how will H0 know, that this is the response type ? i mean its serersocket shud know what msg. id to decipher

					outToClient.writeBytes(joinRingSetH0_res);
					//debug("Left = " + nbrLeft + " Right = " + nbrRight);
					//String reply = sendToHost(joinRingSetH0_res, src);

				}

//###################################### case 2 ######################################################################
				if(msg_tag.compareTo(joinRingSetHR_tag) == 0) // Msg-HR recv
				{
					nbrLeft = src;
					msg = "yes" + '\n';
					outToClient.writeBytes(msg);
					//debug("Left = " + nbrLeft + " Right = " + nbrRight);
				}

//###################################### case 3 ######################################################################
				if(msg_tag.compareTo(joinRingHost_tag) == 0)
				{// ? what is this for ?

				}
//###################################### case 4 ######################################################################

				if(msg_tag.compareTo(leaveRing_tag) == 0)//Msg-leaveRing recv
				{
					String pros_nbr;
					//debug(" ----- " + words[2]);
					pros_nbr = words[2];						// can throw outofbounds
					//debug(" ----- pros_nbr = " + pros_nbr);
					//debug("Left = " + nbrLeft + " Right = " + nbrRight);
					// the host X has send its nbrs msg like = leaveRing_tag # X's id # <prospective-joinee>
					// but this node shud know before hand if X was a right nbr or left nbr ?
					// hence, the msg from X shud also have X's id
					if (nbrLeft.compareTo(src) == 0) {
						//debug("here before nbrLeft = " + nbrLeft);
						nbrLeft = pros_nbr;
						//debug("here after nbrLeft = " + nbrLeft);
					}
					else if (nbrRight.compareTo(src) == 0) {
						nbrRight = pros_nbr;
					}
					//debug("---- yes");
					msg = "yes" + '\n';
					outToClient.writeBytes(msg);
					//debug("Left = " + nbrLeft + " Right = " + nbrRight);
					// reply to leaving host f'off
				}

//###################################### case 5 ######################################################################
				if(msg_tag.compareTo(getHosts_tag) == 0)
				{
					/* grep for selfid in msg received, assume that host pass msg ACW in our algo. 
					   msg received = <tag> + <id-1> + <id-2> ....  */

					outToClient.writeBytes("yes\n");
					if (src.indexOf(selfId) != -1) // u are done
					{
						//debug("Got the host list");
						// all the hostids will be joined by "||"
						synchronized (lock) {
								hostsJoinedlist = src;
								hostlistBool = false;
								lock.notifyAll();

								//debug("Value of this in notify is  " + this);
								//this.doNotify();
								//doNotify();
								//debug("==========After notifying the main thread");
						}
					}
					else {	// make a packet and pass on to ur rite nbr 
						String passon_packet = clientMsg + hostsJoiner + selfId;
						//String passon_packet = getHosts_tag + msgDelimiter + src + hostsJoiner + selfId;
						sendToHost (passon_packet, nbrRight);
						// dont care reply
					}
				}

//###################################### case 6 ######################################################################
				if(msg_tag.compareTo(sendMsgCW_tag) == 0)
				{
					outToClient.writeBytes("yes\n");
						String message = words[2];
					//String dest = words[2];
					if (selfId.compareTo (src) == 0) {
						System.out.println ("Got back the looped msg CW " + src);
						myrapp.deliver(message);
						// we are not sending the sender's UID
					}
					else {
						//String msg = sendAppMsg_tag + msgDelimiter + src;
						//sendToHost (msg, nbrRight);
						sendToHost(clientMsg, nbrLeft);
						//debug("Forwarding msg \"sendMsgCW_tag\" from " + src);
						myrapp.deliver(message);
					}
				}
//###################################### case 7 ######################################################################
				if(msg_tag.compareTo(sendMsgACW_tag) == 0)
				{
					outToClient.writeBytes("yes\n");
						String message = words[2];
					//String dest = words[2];
					if (selfId.compareTo (src) == 0) {
						System.out.println ("got back the looped msg CW " + src);
						myrapp.deliver(message);
						// we are not sending the sender's UID
					}
					else {
						//String msg = sendAppMsg_tag + msgDelimiter + src;
						//sendToHost (msg, nbrRight);
						sendToHost(clientMsg, nbrRight);
						//debug("Forwarding msg from " + src);
						myrapp.deliver(message);
					}
				}
//###################################### case 8 ######################################################################
				if(msg_tag.compareTo(sendAppMsg_tag) == 0)
				{
					// received = sendAppMsg_tag + '#' + 'hostName' used by sendAppMessage()
					// consider ACW movement
					String dest = words[2];
					if (selfId.compareTo (dest) == 0) {
						System.out.println ("\nGot a msg from " + src);
						String message = words[3];
						myrapp.deliver(message);
						// we are not sending the sender's UID
					}
					else {
						//String msg = sendAppMsg_tag + msgDelimiter + src;
						//sendToHost (msg, nbrRight);
						sendToHost(clientMsg, nbrRight);
						//debug("Forwarding msg from " + src);
					}
				}

			}			// end while (1)
		}	// try
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}		// end run()
	
//####################################		API s			############################

	public Set<String> joinRing(String hostName) throws /*RingException,*/ IOException{
		/*
		   One problem with our approach is that, consider h0 asks h1, and h1 has hl and hr ..... 
		   We were asking hl and hr to be h0 prospective neighbours: Assume they form a 4 node ring. 

		   Now h2 comes, and asks h1, if hl, hr agrees , they also form a ring. This will not be a ring structure
		   Hence, i think, we shud return 2 consecutive nodes, who r willing to accept h0.

		   So, best would be to ask ( h1 and either of (hl/hr) if they can include h0 in b/w )
		 */
		String msg = joinRingSetH0_tag + msgDelimiter + selfId; // Msg-H0 sent

		String reply = sendToHost(msg, hostName);
		System.out.println(reply);
		/* how to check if reply is OK ? 
		   1st nrb = hostName
		   2nd nbr = nbr of (hostname)
		 */
		/* Say the 2 hostnames are joined by a # */
		int posi = reply.indexOf("#");
		//debug("POSI = " + posi);
		if (posi == -1)
		{
			return null;
		}
		// -ve 
		else {
			Set<String> s=new LinkedHashSet<String>();
			//debug("oooooooooo" + reply.substring(0, posi) + "|||" + reply.substring(posi+1) );
			s.add(reply.substring(0, posi));
			s.add(reply.substring(posi+1));
			return s;
		}
	}

	public void joinRing(String hostName1, String hostName2) throws /*RingException,*/ IOException{
		/* as we decided, in the function above, we can set the nbrLeft, nbrRight then and there */
		// here we have already changed the pointers of h0 which is hostName1 here ... 
		//String msg_id = joinRingSetHR_tag;
		nbrLeft = hostName1;
		nbrRight = hostName2;
	}

	public void leaveRing()/* throws RingException*/ {
	// TODO - Kill the thread controlling the ring substrate
		String reply_Left = leave_nbr (nbrLeft, nbrRight);
		String reply_Right = leave_nbr (nbrRight, nbrLeft);
		if(reply_Left.equals("yes") && reply_Right.equals("yes"))
		{
			debug("Successfully left the ring");
		}
		else
		{
			debug("Cannot leave the ring");
		}
			
	}

	public String leave_nbr (String whichNbr, String prospectivenbr) {
		String msg = leaveRing_tag + msgDelimiter + selfId + msgDelimiter + prospectivenbr; //Msg-leaveRing sent
		String reply = sendToHost (msg, whichNbr);
		return reply;

		/*		the host 'whichNbr' shud wait till it confirms that host 'prospectivenbr' also received a 
				[LeaveRing] with its hostuuid .
				When it confirms that, then they can modify their datastructures & connect together */
	}

	public List<String> getHosts() throws /*RingException*/ InterruptedException{
		/* circulate clcokwise and append self id . END by searching self id in LIST .
		   java list example : http://www.easywayserver.com/blog/java-list-example/
		 */
		List<String>	ls		=	new ArrayList<String>();
		String			msg 	= 	getHosts_tag + msgDelimiter + selfId;
		sendToHost (msg, nbrRight);		// passon this msg to ur rite
		debug("=======Before Wait");
		hostlistBool = true;
		synchronized(lock){
				while (hostlistBool){
						lock.wait();
				}
		}

		debug("\n\n #### "+ hostsJoinedlist+  " ####");
		StringTokenizer st = new StringTokenizer(hostsJoinedlist, hostsJoiner );
		while(st.hasMoreTokens())
			ls.add(st.nextToken());
		return ls;
	}

	public void sendAppMessageClockwise(String message) //throws RingException
	{
			String msg = sendMsgCW_tag + msgDelimiter + selfId + msgDelimiter + message;
			sendToHost (msg, nbrLeft);	// shud we care about reply ?
	}


	public void sendAppMessageCounterClockwise(String message) //throws RingException
	{
			String msg = sendMsgACW_tag + msgDelimiter + selfId +  msgDelimiter + message;
			sendToHost (msg, nbrRight);	// shud we care about reply ?
	}

	public void sendAppMessage(String message, String hostName) //throws RingException
	{
		// make packet and send ACW
		// we are NOT putting the sender's ID
		if(hostName.equals(selfId))
		{
			myrapp.deliver(message);
		}
		else
		{
			String msg = sendAppMsg_tag + msgDelimiter + selfId + msgDelimiter + hostName + msgDelimiter + message;
			sendToHost (msg, nbrRight);	// shud we care about reply ?
		}
	}

}		// end class Substrate

public class RingApp
{

	private static RingSubstrate ringSubstrate;

	public boolean deliver(String msg)
	{
		System.out.println(msg);
		return true;
	}

	public RingApp(String args[]) throws InterruptedException, IOException
	{
		RingSubstrate ringSubstrate = new RingSubstrate(this);
		//FindLeader fl = new FindLeader(this, ringSubstrate);

		String localHost;
		try 
		{
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
			localHost = localMachine.getHostName();
			ringSubstrate.selfId = localHost;
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
			System.out.println(args[0] + " is my contact node to join the ring");	
			Set<String> ngbrHost = ringSubstrate.joinRing(args[0]); // The two neighbors the node would be inserted in between
			//System.out.println("Set length = " + ngbrHost.size());
			String ngbrHostLeft;
			String ngbrHostRight;

			Iterator it = ngbrHost.iterator();
			if(ngbrHost.size() == 1)
			{
				ngbrHostLeft = it.next().toString();
				ngbrHostRight = ngbrHostLeft;
			}
			else
			{
				ngbrHostLeft = it.next().toString();
				ngbrHostRight = it.next().toString();
			}

			System.out.println("Left = " + ngbrHostLeft + " Right = " + ngbrHostRight);
			ringSubstrate.joinRing(ngbrHostLeft, ngbrHostRight);
		}
		ringSubstrate.start();

		System.out.println("Thread has been started. Now main is controlled by the Application Layer");
		Thread.sleep(5000);


		// testing for leader- election and ordered list	//doom

		while (ringSubstrate.nbrLeft == null && ringSubstrate.nbrRight == null ) {
			}

		System.out.println ("main: now we got a ring" );

		//if(ringSubstrate.selfId.equals("sslab19.cs.purdue.edu")) {

		FindLeader fl = new FindLeader(this, ringSubstrate);
		fl.prntDetails();
		fl.start();
		//}

        if(ringSubstrate.selfId.equals("sslab19.cs.purdue.edu"))
        {
            //ringSubstrate.getHosts();
        }
		
		System.out.println ("main: Ending " );

		// testing for leader- election and ordered list

			/*
//				System.out.println ("In main thread" + ringSubstrate.hostsJoinedlist);
			System.out.println("woken from sleep");
			//ringSubstrate.leaveRing();
			if(ringSubstrate.selfId.equals("cloud01.cs.purdue.edu"))
			{
				//ringSubstrate.getHosts();
				ringSubstrate.sendAppMessageClockwise("Hello World");
				ringSubstrate.sendAppMessageCounterClockwise("Doom  World");
			}
			*/
	}

	public static void main(String args[]) throws InterruptedException, IOException// We need to pass the hostname of one of the nodes in the ring. So that it can request that host to let it join the ring
	{
		RingApp rApp = new RingApp(args);

		//RingSubstrate ringSubstrate = new RingSubstrate(this);
		/*String localHost;
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
			System.out.println(args[0] + " is my contact node to join the ring");	
			Set<String> ngbrHost = ringSubstrate.joinRing(args[0]); // The two neighbors the node would be inserted in between
			//System.out.println("Set length = " + ngbrHost.size());
			String ngbrHostLeft;
			String ngbrHostRight;

			Iterator it = ngbrHost.iterator();
			if(ngbrHost.size() == 1)
			{
				ngbrHostLeft = it.next().toString();
				ngbrHostRight = ngbrHostLeft;
			}
			else
			{
				ngbrHostLeft = it.next().toString();
				ngbrHostRight = it.next().toString();
			}

			System.out.println("Left = " + ngbrHostLeft + " Right = " + ngbrHostRight);
			ringSubstrate.joinRing(ngbrHostLeft, ngbrHostRight);
		}

		// At this point it has joined the ring
		// We can spawn a new thread which takes care of the duties of the ring network. The main thread would continue to do the job of application
//		Thread ringSubThread = new Thread(ringSubstrate);

		//RingSubstrate ringSubstrate = new RingSubstrate(this);
		ringSubstrate.start();

		System.out.println("Thread has been started. Now main is controlled by the Application Layer");
		Thread.sleep(5000);
		System.out.println("woken from sleep");
		//ringSubstrate.leaveRing();
		if(ringSubstrate.selfId.equals("cloud01.cs.purdue.edu"))
		{
			ringSubstrate.getHosts();
		}
		//ringSubstrate.sendAppMessage("Hello World", "cloud02.cs.purdue.edu");*/
	}		// end main
}		// end class RingApp()

