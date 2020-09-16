package edu.nmsu.cs.webserver;

/**
 * Web worker: an object of this class executes in its own new thread to receive and respond to a
 * single HTTP request. After the constructor the object executes on its "run" method, and leaves
 * when it is done.
 *
 * One WebWorker object is only responsible for one client connection. This code uses Java threads
 * to parallelize the handling of clients: each WebWorker runs in its own thread. This means that
 * you can essentially just think about what is happening on one client at a time, ignoring the fact
 * that the entirety of the webserver execution might be handling other clients, too.
 *
 * This WebWorker class (i.e., an object of this class) is where all the client interaction is done.
 * The "run()" method is the beginning -- think of it as the "main()" for a client interaction. It
 * does three things in a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it writes out some HTML
 * content for the response content. HTTP requests and responses are just lines of text (in a very
 * particular format).
 * 
 * @author Jon Cook, Ph.D.
 *
 **/

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException; 

public class WebWorker implements Runnable
{

	private Socket socket;

	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s)
	{
		socket = s;
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP request and then returns, which
	 * destroys the thread. This method assumes that whoever created the worker created it with a
	 * valid open socket object.
	 **/
	public void run()
	{
		System.err.println("Handling connection...");
		try
		{
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			File reqFile = readHTTPRequest(is);
			writeHTTPHeader(os, reqFile);
			writeContent(os, reqFile);
			os.flush();
			socket.close();
		}
		catch (Exception e)
		{
			System.err.println("Output error: " + e);
		}
		System.err.println("Done handling connection.");
		return;
	}

	/**
	 * Read the HTTP request header.
	 **/
	private File readHTTPRequest(InputStream is)
	{
		String line;
		File reqFile = null;
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		while (true)
		{
			try
			{
				while (!r.ready())
					Thread.sleep(1);
				line = r.readLine();
				System.err.println("Request line: (" + line + ")");
				if (line.startsWith("GET"))
				{
					String fileName = line.substring(5, line.length() - 9);
					reqFile = new File(fileName);
				}
				if (line.length() == 0)
					break;
			}
			catch (Exception e)
			{
				System.err.println("Request error: " + e);
				break;
			}
		}
		
		return reqFile;
	}

	/**
	 * Write the HTTP header lines to the client network connection.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 * @param reqFile
	 *          is the File
	 **/
	private void writeHTTPHeader(OutputStream os, File reqFile) throws Exception
	{
		Date d = new Date();
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		Path path = Paths.get(reqFile.getAbsolutePath());
		String contentType = Files.probeContentType(path);
		
		if (contentType == null) {
			os.write("HTTP/1.1 200 OK\n".getBytes());
		} else if (reqFile.exists() && contentType.equals("text/html")) {
			os.write("HTTP/1.1 200 OK\n".getBytes());
		} else {
			os.write("HTTP/1.1 404 NOT FOUND\n".getBytes());
		}
		
		os.write("Date: ".getBytes());
		os.write((df.format(d)).getBytes());
		os.write("\n".getBytes());
		os.write("Server: Lu's server\n".getBytes());
		// os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
		// os.write("Content-Length: 438\n".getBytes());
		os.write("Connection: close\n".getBytes());
		os.write("Content-Type: ".getBytes());
		if (contentType != null) {
			os.write(contentType.getBytes());
		} else {
			os.write("text/html".getBytes());
		}
		os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
		return;
	}

	/**
	 * Write the data content to the client network connection. This MUST be done after the HTTP
	 * header has been written out.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 **/
	private void writeContent(OutputStream os, File reqFile) throws Exception
	{
		Path path = Paths.get(reqFile.getAbsolutePath());
		String contentType = Files.probeContentType(path);
//		if (contentType == null)
//		{
//			os.write("<html><head></head><body>\n".getBytes());
//			os.write("<h3>My web server works!!!!!!!</h3>\n".getBytes());
//			os.write("</body></html>\n".getBytes());
//			return;
//		}
	
		if (reqFile.exists() && contentType.equals("text/html")) {
			System.out.println("write file...");
			writeFile(os, reqFile);
		} else {
			os.write("404 Not Found\n".getBytes());
		}
//		if (reqFile != null)
//		{
//			os.write("<html><head></head><body>\n".getBytes());
//			os.write(new String("<h1>localfile:" + reqFile.getAbsolutePath() + "</h1>").getBytes());
//			os.write("<h3>My web server works!!!!!!!</h3>\n".getBytes());
//			os.write("</body></html>\n".getBytes());
//		}

	}
	
	private void writeFile(OutputStream os, File reqFile)
	{
		try {
			Path path = Paths.get(reqFile.getAbsolutePath());
			String content = new String (Files.readAllBytes(path));
//			Stream <String> lines = Files.lines(path);
			String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
			String name = InetAddress.getLocalHost().getHostName();
			String replaced = content.replaceAll("<cs371date>", date);
			replaced = replaced.replaceAll("<cs371server>", name);
			os.write(replaced.getBytes());
//			List <String> replaced = lines.map(line -> line.replaceAll("<cs371date>", date)).collect(Collectors.toList());
//			replaced = lines.map(line -> line.replaceAll("<cs371server>", name)).collect(Collectors.toList());
//			os.write(replaced.toString().getBytes());
//			lines.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

} // end class
