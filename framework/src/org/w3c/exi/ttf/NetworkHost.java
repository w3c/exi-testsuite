/*
 * EXI Testing Task Force Measurement Suite: http://www.w3.org/XML/EXI/
 *
 * Copyright © [2006] World Wide Web Consortium, (Massachusetts Institute of
 * Technology, European Research Consortium for Informatics and Mathematics,
 * Keio University). All Rights Reserved. This work is distributed under the
 * W3C® Software License [1] in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.exi.ttf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Network application used with EXIDriverBase to facilitate network read and write tests.
 * This class automatically switches between one of two modes of operation: relay and consume.
 * In 'relay' mode, this application provides a continuous feed of bytes for consumption during 
 * EXI read tests. Once a connection is initated, the network host reads
 * from the connection until the uplink half of the connection is closed.
 * Then the server writes the bytes collected from those reads back to the
 * client, repeating the data until the connection is closed. 
 * In 'consume' mode, this application continuously consumes bytes written during
 * an EXI write tests until the connection is closed. 
 * 
 * <p>
 * Command-line parameters: [-verbose|-v] <port>
 * 
 * @author AgileDelta
 * @author Sun
 * 
 */
public class NetworkHost 
{
    /**
     * Turn on logging to System.err
     */
    public static boolean _verbose = false;
    /**
     * Turn on more verbose logging to System.err
     */
    public static boolean _veryverbose = false;

    private Thread _serverThread;
    private volatile boolean _stop = false;
    private ServerSocket _socket = null;
    private int _useCount = 1;
    
    private static int _activePort;
    private static NetworkHost _activeHost;

    protected NetworkHost()
    {
    }

    protected NetworkHost(int port) throws IOException
    {
        _socket = new ServerSocket(port);
    }

    private static synchronized void clearActiveHost()
    {
        _activeHost = null;
        _activePort = 0;
    }

    /**
     * Close any open connections, stop accepting new connections,
     * and close down worker threads.
     */
    public synchronized void stop()
    {
        // there may be other threads still using this server
        if (0 != --_useCount)
            return;
        
        _stop = true;
        ServerSocket ss = _socket;
        if (ss != null)
        {
            try
            {
                ss.close();
            }
            catch (IOException e)
            {
            }
        }
        Thread serverThread = this._serverThread;
        if (serverThread != null)
        {
            try
            {
                serverThread.join();
            }
            catch (InterruptedException e)
            {
            }
        }
        clearActiveHost();
    }

    /**
     * Write out header indicating relay connection to output stream
     */
    public static void writeRelayHeader(OutputStream output) throws IOException
    {
        output.write(0xDD);
        output.write(0xB2);
        output.write('r');
    }

    /**
     * Write out header indicating consume connection to output stream
     */
    public static void writeConsumeHeader(OutputStream output) throws IOException
    {
        output.write(0xDD);
        output.write(0xB2);
        output.write('w');
    }

    /**
     * Create a local instance of NetworkHost mirror server that listens to 
     * connections on the local host.
     *
     * @param port the port to listen on.
     */
    public static synchronized NetworkHost createLocalMirrorServer(final int port) throws IOException {
        // reuse existing host, if there is one
        if (_activeHost != null)
        {
            if (_activePort == port)
            {
                synchronized (_activeHost)
                {
                    _activeHost._useCount++;
                }
                return _activeHost; // reuse existing host
            }

            _activeHost.stop();
        }

        final NetworkHost mirrorServer = new NetworkHost(port);
        _activePort = port;
        _activeHost = mirrorServer;

        Thread serverThread = new Thread() {
            public void run() {
                mirrorServer.listen(port);
            }
        };
        serverThread.start();

        return mirrorServer;
    }
    
    /**
     * Open a server socket on the specified port, listening for connections
     * then sniffing the connection to determine if it is a relay or consume
     * connection and spinning up an appropriate worker thread to handle the
     * connection.  This method will loop until the stop() method is called.
     */
    public void listen(int port)
    {
        _serverThread = Thread.currentThread();
        Socket client = null;
        try
        {
            // create socket to listen for connections
            if (_socket == null)
                _socket = new ServerSocket(port);

            // loop forever
            while(!_stop)
            {
                client = null;
    
                try
                {
                    if (_verbose)
                        System.out.print("waiting for connection on "+port+" ... ");
    
                    // Wait for a connection on the local port
                    client = _socket.accept();
                    if (_verbose)
                        System.out.println();

                    InputStream input = new BufferedInputStream(client.getInputStream());
                    if (input.read() != 0xDD
                        || input.read() != 0xB2)
                    {
                        System.err.println("Invalid connection header");
                        client.close();
                    }
                    else 
                    {
                        Thread worker;
                        switch (input.read())
                        {
                            case 'r':
                                worker = new RelayWorker(client, input);
                                break;
                            case 'w':
                                worker = new ConsumeWorker(client, input);
                                break;
                            default:
                                worker = null;
                                System.err.println("Invalid connection type header");
                                client.close();
                                break;
                        }
                        if (worker != null)
                            worker.start();
                    }
                    
                    Thread.yield();
                }
                catch (IOException e)
                {
                    if (!_stop)
                    {
                        System.err.println(e);
                        if (_verbose)
                            System.out.println("Resuming after exception");
                    }
                }
            }
        }
        catch (IOException e)
        {
            System.err.println("Unexpected IO Exception: " + e);
            e.printStackTrace();
        }
        finally
        {
            // Close the sockets no matter what happens.
            try {
                if (client != null)
                    client.close();
            } catch(IOException e) {}

            try {
                if (_socket != null)
                    _socket.close();
            } catch(IOException e) {}
        }
    }

    /**
     * Worker thread to handle 'relay' connection with client.
     * For a relay connection, the worker records incoming data 
     * from the client, until the client closes the inbound stream.
     * Then the worker sends those bytes back to the client, looping
     * until the client closes the network connection.
     */
    private static class RelayWorker extends Thread
    {
        final InputStream inStream;
        final Socket client;
        
        public RelayWorker(Socket socket, InputStream input)
        {
            this.client = socket;
            this.inStream = input;
        }

        public void run()
        {
            OutputStream outStream = null;         
            try
            {
                if (_verbose)
                    System.out.print("receiving... ");

                // read bytes to repeat from client
                int cbData = 0, cbRead = 0;
                byte[] data = new byte[1024*8];
                while ((cbRead = inStream.read(data, cbData, data.length - cbData)) != -1) {
                    cbData += cbRead;
                    if (data.length == cbData)
                    {
                        byte[] bigger = new byte[cbData * 2];
                        System.arraycopy(data, 0, bigger, 0, cbData);
                        data = bigger;
                    }
                }
                client.shutdownInput();
                
                if (_verbose)
                    System.out.println("received " + cbData + " bytes from client");
                
                // now just repetitively push data[] back to the client 
                outStream = new BufferedOutputStream(client.getOutputStream());

                // exit loop via IOException (because client closes connection)
                try
                {
                    while (true)
                    {
                        outStream.write(data,0,cbData);
                        if (_veryverbose)
                        {
                            System.out.print(".");
                            System.out.flush();
                        }
                    }
                }
                catch (IOException e)
                {
                    // we actually expect a connection abort... 
                    // turn on very-verbose if there are problem to watch for other errors
                    if (_verbose)
                    {
                        if (_veryverbose)
                            System.out.println("\nconnection-closed: "+e);
                        else
                            System.out.println("\nconnection-closed.");
                        System.out.flush();
                    }
                }
            }
            catch (IOException e)
            {
                try
                {
                    client.close();
                }
                catch (IOException eClose)
                {                    
                    eClose.printStackTrace();
                }
            }
            finally {
              try {
                if (outStream != null)
                  outStream.close();
              }
              catch (IOException e) {
                System.err.println("Failed to close relayed outStream");
                System.err.println(e);
              }
            }
        }
    }

    /**
     * Implements the worker thread to handle 'consume' connections
     * The worker just reads off the inbound stream, doing nothing
     * with the incoming bytes.
     */
    private static class ConsumeWorker extends Thread
    {
        final InputStream inStream;
        final Socket client;
        
        public ConsumeWorker(Socket socket, InputStream input)
        {
            this.client = socket;
            this.inStream = input;
        }

        public void run()
        {
            if (_verbose)
                System.out.println("consuming... ");

            byte[] buffer = new byte[1024*8];
            try
            {
            	while (-1 != inStream.read(buffer))
                {
                    if (_veryverbose)
                        System.out.print(".");
                }
            }
            catch (IOException e)
            {
                System.err.println("Unexpected IOException");
                System.err.println(e);
            }     
            finally {
              try {
                inStream.close();
              }
              catch (IOException e) {
                System.err.println("Failed to close consumed inStream");
                System.err.println(e);
              }
            }
        }
    }
        
    protected static void usage()
    {
        System.err.println("usage: NetworkHost [-verbose] <port>");
    }
    
    public static void main(String[] args)
    {
        int port = -1;

        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (arg.startsWith("-"))
            {
                if (arg.equalsIgnoreCase("-?") || arg.equalsIgnoreCase("-help"))
                {
                    usage();
                    return;
                }
                else if (arg.equalsIgnoreCase("-v") || arg.equalsIgnoreCase("-verbose"))
                {
                    if (_verbose)
                        _veryverbose = true;
                    else
                        _verbose = true;
                }
                else if (arg.equalsIgnoreCase("-vv"))
                {
                    _verbose = true;
                    _veryverbose = true;
                }
                else
                {
                    System.err.println("!ignoring unknown argument: "+arg);
                }
            }
            else
            {
                port = Integer.parseInt(arg);
            }
        }

        if (port > 0)
        {
//        	// don't wait for input, so we can background the task without suspending for input
//        	while (true) ;
//            Thread quit = new Thread() {
//                public void run()
//                {
//                    try
//                    {
//                        System.in.read();
//                    }
//                    catch (IOException e)
//                    {
//                        e.printStackTrace();
//                    }
//                    System.out.println("[exiting]");
//                    System.exit(0);
//                }
//            };
//            System.out.println("[press enter to quit]");
//            quit.start();

            NetworkHost mirror = new NetworkHost();
            mirror.listen(port);
        }

        usage();
        return;
    }
}
