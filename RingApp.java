/*
	 Your code should throw a RingException when any of the operations in
	 RingSubstrate fails - set the cause field appropriately
 */

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;


class RingSubstrate extends Thread{
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
				String hostsJoiner = "||";
				String hostsJoinedlist;

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
				}

				public String sendToHost(String msg, String dest)
				{
								String whoami = "sendToHost";
								try
								{
												Socket clientSocket = new Socket(dest, listenport);
												DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
												BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

												outToServer.writeBytes(msg + '\n');
												String reply = inFromServer.readLine();

												clientSocket.close();
												return reply;
								}
								catch(Exception e)
								{
												e.printStackTrace();
								}
								System.out.println (whoami + ": returns null ");
								return null;
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
																String src = words[1];				// host who wants to join the ring

																System.out.println("Tag = " + msg_tag + " src = " + src);

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
																								String msg = joinRingSetHR_tag + msgDelimiter + src; // Msg-HR sent
																								String reply = sendToHost(msg, nbrRight);
																								if(reply.compareTo("no") == 0)
																								{
																												System.out.println("Right host is selfish");
																								}
																								hostRight = nbrRight;
																								//clientSocket.close();
																				}

																				// ?? nbrRight = src; // It sets the src as its right ngbr
																				String joinRingSetH0_res = hostLeft + msgDelimiter + hostRight + '\n'; // how will H0 know, that this is the response type ? i mean its serersocket shud know what msg. id to decipher

																				outToClient.writeBytes(joinRingSetH0_res);
																				//String reply = sendToHost(joinRingSetH0_res, src);

																}	// case 1

																if(msg_tag.compareTo(joinRingSetHR_tag) == 0) // Msg-HR recv
																{
																				nbrLeft = src;
																				String msg = "yes" + '\n';
																				outToClient.writeBytes(msg);
																}

																if(msg_tag.compareTo(joinRingHost_tag) == 0)
																{

																}

																if(msg_tag.compareTo(leaveRing_tag) == 0)
																{
																				String pros_nbr;
																				pros_nbr = words[2];						// can throw outofbounds
																				// the host X has send its nbrs msg like = leaveRing_tag # X's id # <prospective-joinee>
																				// but this node shud know before hand if X was a right nbr or left nbr ?
																				// hence, the msg from X shud also have X's id
																				if (nbrLeft.compareTo(src) == 0) {
																								nbrLeft = pros_nbrLeft;
																				}
																				else if (nbrRight.compareTo(src) == 0) {
																								nbrRight = pros_nbr;
																				}

																				// reply to leaving host f'off
																}

																// case 4
																if(msg_tag.compareTo(getHosts_tag) == 0)
																{
																				/* grep for selfid in msg received, assume that host pass msg ACW in our algo. 
																					 msg received = <tag> + <id-1> + <id-2> ....  */

																				if (src.indexOf(selfId) != -1) // u are done
																				{
																								// all the hostids will be joined by "||"
																								hostJoinedlist = src;
																								//signal (blocked_API_gethosts()_on_this_host);		// sem.relase();
																				}
																				else {	// make a packet and pass on to ur rite nbr 
																								String passon_packet = getHosts_tag + delimiter + src + hostsJoiner + selfid;
																								sendToHost (nbrright, passon_packet);
																								// dont care reply
																				}
																}

																// case 5
																if(msg_tag.compareTo(sendMsgCW_tag) == 0)
																{

																}

																if(msg_tag.compareTo(sendMsgACW_tag) == 0)
																{

																}

																if(msg_tag.compareTo(sendAppMsg_tag) == 0)
																{
																				// received = sendAppMsg_tag + '#' + 'hostName' used by sendAppMessage()
																				// consider ACW movement
																				// if selfId == hostName ,,, ,sTOP
																				if (!selfId.compareTo (src)) {
																					System.out.println ("got a msg from which node ? ");		// we are not sending the sender's UID
																				}
																				else {
																					String msg = sendAppMsg_tag + delimeter + src;
																					sendTohost (msg, nbrRight);
																				}
																}

												}			// end while (1)
								}
								catch (Exception e)
												e.printStackTrace();
				}		// end run()

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
								// here we have already changed the pointers of h0 which is hostName1 here ... 
								String msg_id = joinRingSetHR_tag;
				}

				public void leaveRing() throws RingException {
				leave_nbr (nbrLeft, nbrRight);
				leave_nbr (nbrRight, nbrLeft);
				}
				
			public static void leave_nbr (String whichNbr, String prospectivenbr) {
				String msg = leaveRing_tag + delimeter + selfId + delimeter + prospectivenbr;
				sendTohost (msg, whichNbr);
				
				//		DOUBT /* todo in host-listening-part:

				/*		the host 'whichNbr' shud wait till it confirms that host 'prospectivenbr' also received a [LeaveRing] with
						its hostuuid .
						When it confirms that, then they can modify their datastructures & connect together */
					}

				public List<String> getHosts() throws RingException {
								/* circulate clcokwise and append self id . END by searching self id in LIST .
									 java list example : http://www.easywayserver.com/blog/java-list-example/
								 */
								List<String>	ls		=	new ArrayList<String>();
								String			msg 	= 	getHosts_tag + delimeter + selfId;
								sendToHost (msg, nbrRight);		// passon this msg to ur rite

								//wait (for signal from its leftnbr);		// sem.acquire();
								// now we are sure hostsJoinedlist

								StringTokenizer st = new StringTokenizer(hostsJoinedlist, hostsJoiner );
								while(st.hasMoreTokens())
												ls.add(st.nextToken());

								return ls;
				}

				public void sendAppMessageClockwise(String message) //throws RingException
				{}

				public void sendAppMessageCounterClockwise(String message) //throws RingException
				{}
				public void sendAppMessage(String message, String hostName) //throws RingException
				{}

}		// end class Substrate

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
												System.out.println(args[0] + " is my contact node to join the ring");	
												Set<String> ngbrHost = ringSubstrate.joinRing(args[0]); // The two neighbors the node would be inserted in between
												Iterator it = ngbrHost.iterator();
												String ngbrHostLeft = it.next().toString();
												String ngbrHostRight = it.next().toString();

												System.out.println("Left = " + ngbrHostLeft + " Right = " + ngbrHostRight);
												//ringSubstrate.joinRing(ngbrHostLeft, ngbrHostRight);
								}

								// At this point it has joined the ring
								// We can spawn a new thread which takes care of the duties of the ring network. The main thread would continue to do the job of application
								Thread ringSubThread = new Thread(ringSubstrate);
								ringSubThread.start();

								System.out.println("Thread has been started. Now main is controlled by the Application Layer");

				}		// end main
}		// end class RingApp()
