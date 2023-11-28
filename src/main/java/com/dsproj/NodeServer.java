package com.dsproj;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TThreadPoolServer;
/*
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
*/
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.layered.TFramedTransport;





//We might need to import IdComparator

public class NodeServer {

  public static node myNode= new node();
	
  public static String SuperNodeHost= "127.0.0.1";
  public static int SuperNodePort= SNServer.SNPort;	
  //public static int ThisNodePort; 

  //Handler to start the NodeServer
  public static NodeHandler handler;
  public static NService.Processor processor;


  //Join	
  public static void joinNetwork() {
	
	try {
      TTransport transport;
     
      //To be changes to the IP and Port of the Super Node	
      transport= new TSocket(SuperNodeHost, SuperNodePort);

      TProtocol protocol = new  TBinaryProtocol(new TFramedTransport(transport));
      //protocol=  new  TBinaryProtocol(new TFramedTransport(transport));

      NService.Client client = new NService.Client(protocol);
      //SNClient = new SNService.Client(protocol1);

      transport.open();
     // System.out.println("Going to call the perform");
      perform(client);

      transport.close();
    } catch (TException x) {
      x.printStackTrace();
    }


  }	
/*
  public static List<String> getFiles(){
        //System.out.println("Going to find the version in scratch "+ fileName);
        File myFolder= new File("/export/scratch");
        File[] listOfFiles = myFolder.listFiles();
        LinkedList<String> myList = new LinkedList<String>();
        String myFileName="";
        //System.out.println("List of files on this node are:");
        for(int i=0; i< listOfFiles.length; i++)
        {
                if(listOfFiles[i].isFile() && listOfFiles[i].getName()!= "README.txt")
                {
			myList.add(listOfFiles[i].getName());
                 	System.out.println(listOfFiles[i].getName());
                }
        }

	return myList;

        }
*/

 
  //We are calling function of the supernode for Join
  private static void perform(NService.Client client) throws TException
  {
    //System.out.println("Get new id for new node");
    myNode.id = client.getId(myNode);
    System.out.println("Starting a server with Id "+ myNode.id);

  }


  //Main
  public static void main(String [] args) {

	SuperNodePort= SNServer.SNPort;

   try{
   myNode.ip= InetAddress.getLocalHost().getHostAddress();
   }
   catch (UnknownHostException e){
	e.printStackTrace();
   }
        myNode.port= Integer.parseInt(args[0]);
   
        //Join request to the supernode through perform
	//SuperNodePort= SNServer.SNPort;
	//System.out.println("Port no="+ SuperNodePort);
	joinNetwork();
   




    //Now we start our server
    try {
      handler = new NodeHandler();
      processor = new NService.Processor(handler);

      Runnable simple = new Runnable() {
        public void run() {
          simple(processor);
        }
      };
	//System.out.println("Kuch bhi");
      new Thread(simple).start();
    } catch (Exception x) {
      x.printStackTrace();
    }

	File file = new File("/export/scratch/kajar_mahal");
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("File Server directory named kajar_mahal is created in /export/scratch");
            } else {
                System.out.println("Failed to create directory!");
            }
        }	

  }

  public static void simple(NService.Processor processor) {
    try {
      //System.out.println("Beforeopening socket");
      TServerTransport serverTransport = new TServerSocket(myNode.port);
  	  TTransportFactory factory = new TFramedTransport.Factory();  
      TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);  
      args.processor(processor);  
      args.transportFactory(factory); 
        //System.out.println("After configuring the port");
	TThreadPoolServer server = new TThreadPoolServer(args);
      //System.out.println("Starting the node server...");
      server.serve();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

