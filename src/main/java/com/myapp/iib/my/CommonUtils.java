package com.myapp.iib.my;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class CommonUtils {

	// Charset and decoder for ISO-8859-15
	private static Charset charset = Charset.forName("ISO-8859-15");
	private static CharsetDecoder decoder = charset.newDecoder();
	public static final Date currentDate = new Date();

	// Pattern used to parse lines
	private static Pattern linePattern = Pattern.compile(".*\r?\n");
	private static final int MAPSIZE = 10 * 1024 * 1024; // 4K - make this *
															// 1024 to 4MB in a
															// real system.

	public static Date parseDate(String dt) throws ParseException {
		return new Date(dt);
	}
	
	 public static DefaultMutableTreeNode jTreeBuild(Node node)
	  {
	    if (node == null) {
	      return null;
	    }
	    switch (node.getNodeType())
	    {
	    case 1: 
	      DefaultMutableTreeNode treeNode = 
	        new DefaultMutableTreeNode(node.getNodeName());
	      NamedNodeMap attrs = node.getAttributes();
	      for (int i = 0; i < attrs.getLength(); i++) {
	        treeNode.add(
	          new DefaultMutableTreeNode(
	          '@' + attrs.item(i).getNodeName() + 
	          '[' + attrs.item(i).getNodeValue() + ']'));
	      }
	      Node child = node.getFirstChild();
	      while (child != null)
	      {
	        DefaultMutableTreeNode childTree = jTreeBuild(child);
	        if (childTree != null) {
	          treeNode.add(childTree);
	        }
	        child = child.getNextSibling();
	      }
	      return treeNode;
	    case 3: 
	      String text = node.getNodeValue().trim();
	      return text.length() == 0 ? null : 
	        new DefaultMutableTreeNode(text);
	    }
	    return null;
	  }
	
	 /*public static String prettyPrintJSON(String text)
	  {
	    Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    JsonParser jp = new JsonParser();
	    JsonElement je = jp.parse(text);
	    return gson.toJson(je);
	  }*/
	
	public static boolean isRemotePortInUse(String hostName, int portNumber)
	  {
	    try
	    {
	      new Socket(hostName, portNumber).close();
	      
	      return true;
	    }
	    catch (Exception e) {}
	    return false;
	  }

	public static int grep(Pattern pattern, FileChannel fc) throws IOException {

		CharBuffer cbuf = null;
//		FileChannel fc = null;
		// Get a FileChannel from the given file.
//		fc = new FileInputStream(fileName).getChannel();
		// Map the file's content
		// MappedByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, 0,
		// fc.size());
		// Decode ByteBuffer into CharBuffer
		cbuf = Charset.forName("ISO-8859-1").newDecoder()
				.decode(fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()));
		int count = 0;
		Matcher m = pattern.matcher(cbuf);
		while (m.find()) {
			++count;
		}

		cbuf.clear();
		// buf.clear();
		if (fc != null) {
			fc.close();
		}

		return count;
	}

	public static Date hoursOld(int n) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -n);
		return cal.getTime();
	}

	public static boolean isValidFile(Path path) {
		return Files.exists(path) && !Files.isDirectory(path)
				&& Files.isReadable(path);
	}

	public static List<String> readLines(BufferedReader reader)
			throws IOException {

		String line;
		List<String> lines = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}

		return lines;

	}

	public static boolean between(int i, int minValueInclusive,
			int maxValueInclusive) {
		return (i >= minValueInclusive && i <= maxValueInclusive);
	}

	public static List<String> findMatchingLines(String searchText, File f)
			throws IOException {

		// Open the file and then get a channel from the stream
		FileInputStream fis = new FileInputStream(f);
		FileChannel fc = fis.getChannel();
		// boolean found = false;

		// details.append(KUtils.htmlHeader("Search Results for "+f.getName()));
		// Get the file's size and then map it into memory
		int sz = (int) fc.size();
		// System.out.println(sz);
		MappedByteBuffer bb;
		List<String> details;
		if (sz <= MAPSIZE) {

			bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
			details = findMatchingLines(searchText, bb);

			// Close the channel and the stream
			fc.close();
			fis.close();
		} else {
			throw new IllegalArgumentException("File size is more than "
					+ MAPSIZE + " bytes");
		}
		
		return details;
	}

	public static List<String> findMatchingLines(String searchText,
			ByteBuffer bb) throws CharacterCodingException {

		List<String> details = new ArrayList<>();
		Pattern pattern = Pattern.compile(searchText);

		// Decode the file into a char buffer
		CharBuffer cb = decoder.decode(bb);
		// System.out.println(cb);

		// Perform the search
		Matcher lm = linePattern.matcher(cb); // Line matcher
		// System.out.println(lm);
		Matcher pm = null; // Pattern matcher
		CharSequence cs = null;
		while (lm.find()) {
			cs = lm.group(); // The current line
			if (pm == null)
				pm = pattern.matcher(cs);
			else
				pm.reset(cs);
			if (pm.find()) {
				// System.out.println(cs);
				details.add(cs.toString());
			}
			if (lm.end() == cb.limit())
				break;
		}
		bb.clear();

		return details;

	}

	public static String formatDate(Date paramDate) {
		DateFormat localDateFormat = DateFormat.getDateTimeInstance();
		String str;
		if (paramDate != null) {
			str = localDateFormat.format(paramDate);
		} else {
			str = "unknown (null)";
		}
		return str;
	}

	public static DefaultTableModel addColumns(String... cols) {
		DefaultTableModel tableModel = new DefaultTableModel();
		for (String col : cols) {
			tableModel.addColumn(col);
		}

		return tableModel;
	}

	public static void exit() {
		System.exit(0);
	}

	public static List<String> listFilesNoExt(String path, String globPattern)
			throws IOException {

		Path dir = FileSystems.getDefault().getPath(path);

		List<String> files = new ArrayList<>();
		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(
				"glob:*" + globPattern);

		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path entry) throws IOException {
				return matcher.matches(entry.getFileName());
			}
		};

		if (Files.isDirectory(dir)) {

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
					filter)) {
				for (Path p : stream) {
					files.add(p.getFileName().toString()
							.replaceFirst("[.][^.]+$", ""));
				}
			}
		}
		return files;
	}

	public static String sb(String... msg) {
		StringBuilder sb = new StringBuilder();

		for (String str : msg) {
			sb.append(str).append("\n");
		}

		return sb.length() == 0 ? null : sb.toString();
	}

	public static String runnsingStatus(boolean status) {
		return "Flow " + (status ? "Running" : "Stopped");
	}

	public static void sleep(long time) throws InterruptedException {
		Thread.sleep(time);
	}

	public static String getFirstLine(String paramString) {
		String str = null;
		if (paramString != null) {
			int i = paramString.indexOf('\n');
			if (i != -1) {
				str = paramString.substring(0, i);
			} else {
				str = paramString;
			}
		}
		return str;
	}

	public static String formatMillis(long paramLong) {
		String str;
		if (paramLong < 2000L) {
			str = paramLong + "ms";
		} else {
			long l;
			if (paramLong < 120000L) {
				l = paramLong / 1000L;
				str = l + "s";
			} else {
				l = paramLong / 60000L;
				str = l + "mins";
			}
		}
		return str;
	}

	public static Properties loadPropFile(Path filename) throws IOException {

		InputStream input = null;
		Properties properties = null;
		if (Files.exists(filename)) {
			input = new FileInputStream(filename.toFile());
			// load a properties file
			// prop.load(input);
			properties = new Properties();
			properties.load(input);
			if (input != null) {
				input.close();
			}
		}

		return properties;

	}

	public static String fileNameNoExt(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	public static String fileChooser(String ext, String preSelectedfile) {

		String file = null;

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File(preSelectedfile));
		if (isValidFile(Paths.get(preSelectedfile))) {
			file = fileChooser.getSelectedFile().getAbsolutePath();
			// fileChooser.showOpenDialog(null);
		} else {
			Path parentDir = Paths.get(preSelectedfile).getParent();
			if (Files.exists(parentDir)) {
				fileChooser.setCurrentDirectory(new File(parentDir.toString()));
			} else {
				fileChooser.setCurrentDirectory(new File(System
						.getProperty("user.home")));
			}
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*."
					+ ext, ext));

		}
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION
				&& fileChooser.getSelectedFile().getAbsolutePath()
						.endsWith(ext)) {
			file = fileChooser.getSelectedFile().getAbsolutePath();
		} else {
			file = null;
		}

		return file;

	}

	public static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}

	public static List<Path> listFiles(String path, String globPattern)
			throws IOException {

		Path dir = FileSystems.getDefault().getPath(path);

		List<Path> files = new ArrayList<>();
		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(
				"glob:*" + globPattern);

		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path entry) throws IOException {
				return matcher.matches(entry.getFileName());
			}
		};

		if (Files.isDirectory(dir)) {

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
					filter)) {
				for (Path p : stream) {
					if (!Files.isDirectory(p)) {
						files.add(p);
					}
				}
			}
		}
		return files;
	}

	public static List<Path> listDir(Path dir, boolean sort) throws IOException {

		List<Path> files = new ArrayList<>();

		if (Files.isDirectory(dir)) {

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
				for (Path p : stream) {
					if (Files.isDirectory(p)) {

						files.add(p);
					}
				}
			}
			if (!files.isEmpty() && sort) {
				sortFiles(files);
			}
		}
		return files;
	}

	public static List<String> listDir(String dir, boolean sort)
			throws IOException {

		List<Path> files = listDir(Paths.get(dir), sort);
		List<String> dirs = new ArrayList<>();

		for (Path path : files) {
			dirs.add(path.getFileName().toString());
		}
		return dirs.isEmpty() ? null : dirs;
	}

	public static List<Path> sortFiles(List<Path> files) {

		Collections.sort(files, new Comparator<Path>() {
			public int compare(Path o1, Path o2) {
				try {
					return Files.getLastModifiedTime(o2).compareTo(
							Files.getLastModifiedTime(o1));
				} catch (IOException e) {
					// handle exception
				}
				throw new IllegalArgumentException(
						"IOException has occurred while comparing files last modified date");
			}
		});
		return files;
	}
	
	public static Object handleNullValue(Object obj) {
		return obj != null ? obj : "null";
	}

	public static String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {

			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);

			temp.append(decimal);
		}
		// System.out.println("Decimal : " + temp.toString());

		return sb.toString();
	}

	public static String prettyFormat(String input, int indent)
			throws TransformerException {
		Source xmlInput = new StreamSource(new StringReader(input));
		StringWriter stringWriter = new StringWriter();
		StreamResult xmlOutput = new StreamResult(stringWriter);
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount",
				String.valueOf(indent));
		transformer.transform(xmlInput, xmlOutput);
		return xmlOutput.getWriter().toString();
		// return input;
	}

	public static byte[] decrypt(SecretKey myDesKey, Path path)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, IOException {

		// KeyGenerator keygenerator = KeyGenerator.getInstance("DES");
		// SecretKey myDesKey = keygenerator.generateKey();
		Cipher desCipher;

		// Create the cipher
		desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

		// Initialize the cipher for encryption
		desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);

		// Initialize the same cipher for decryption
		desCipher.init(Cipher.DECRYPT_MODE, myDesKey);

		// Decrypt the text
		byte[] textDecrypted = desCipher.doFinal(Files.readAllBytes(path));

		return textDecrypted;

	}

	public static String getMacAddress() {
		InetAddress ip;

		try {

			ip = InetAddress.getLocalHost();
			// System.out.println("Current IP address : " +
			// ip.getHostAddress());

			NetworkInterface network = NetworkInterface.getByInetAddress(ip);

			byte[] mac = network.getHardwareAddress();

			// System.out.print("Current MAC address : ");

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i],
						(i < mac.length - 1) ? "-" : ""));
			}
			// System.out.println(sb.toString());
			return sb.toString();

		} catch (UnknownHostException e) {

			e.printStackTrace();

		} catch (SocketException e) {

			e.printStackTrace();

		}

		return null;
	}

	public static String getHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public static final String HEX_CHARS = "0123456789ABCDEF";

	public static String bytesToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++)
			buf.append(byteToHex(data[i]));

		return buf.toString();
	}

	public static String byteToHex(byte data) {
		int hi = (data & 0xF0) >> 4;
		int lo = (data & 0x0F);
		return "" + HEX_CHARS.charAt(hi) + HEX_CHARS.charAt(lo);
	}

	// try to get the current system encoding
	public static String getDefaultCharSet() {
		OutputStreamWriter writer = new OutputStreamWriter(
				new ByteArrayOutputStream());
		String enc = writer.getEncoding();
		return enc;
	}

	public static byte[] hexStringToByteArray(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

	public static String getCurrentTime() {
		return new Date().toString();
	}

	public static String prettyPrint(Document document)
			throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		DOMSource source = new DOMSource(document);
		StringWriter strWriter = new StringWriter();
		StreamResult result = new StreamResult(strWriter);

		transformer.transform(source, result);

		return strWriter.getBuffer().toString();

	}

	public static String prettyPrint(String xml) throws TransformerException,
			UnsupportedEncodingException, SAXException, IOException,
			ParserConfigurationException, XPathExpressionException {

		Document document = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(new InputSource(new ByteArrayInputStream(xml
						.getBytes("utf-8"))));

		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.evaluate(
				"//text()[normalize-space()='']", document,
				XPathConstants.NODESET);

		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			node.getParentNode().removeChild(node);
		}

		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");

		StringWriter stringWriter = new StringWriter();
		StreamResult streamResult = new StreamResult(stringWriter);

		transformer.transform(new DOMSource(document), streamResult);

		return stringWriter.toString();

	}

	public static Document toXmlDocument(String str)
			throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document document = docBuilder.parse(new InputSource(new StringReader(
				str)));

		return document;
	}
	
	public static String xmlPrettyFormat(String input) throws TransformerException {

		StringWriter stringWriter = new StringWriter();
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.transform(new StreamSource(new StringReader(input)), new StreamResult(stringWriter));

		return stringWriter.toString().trim();

	}

	public static String getExtensionOfFile(File file) {
		String fileExtension = "";
		// Get file Name first
		String fileName = file.getName();

		// If fileName do not contain "." or starts with "." then it is not a
		// valid file
		if (fileName.contains(".") && fileName.lastIndexOf(".") != 0) {
			fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
		}

		return fileExtension;
	}

	public static String getFileNameWithoutExtension(File file) {
		String fileName = null;

		if (file != null && file.exists()) {
			String name = file.getName();
			fileName = name.replaceFirst("[.][^.]+$", "");
		}

		return fileName;

	}

	public static List<String> readFileLineByLine(String fileName)
			throws IOException {

		File f = new File(fileName);
		List<String> fileContent = new ArrayList<>();

		if (f.exists() && f.isFile()) {
			BufferedReader b = new BufferedReader(new FileReader(f));

			String readLine = "";

			// System.out.println("Reading file using Buffered Reader");

			while ((readLine = b.readLine()) != null) {
				fileContent.add(readLine);
			}

			b.close();
		}

		return fileContent;

	}

	public static String prettyPrintXMLAsString(String xmlString) {
		/* Remove new lines */
		final String LINE_BREAK = "\n";
		xmlString = xmlString.replaceAll(LINE_BREAK, "");
		StringBuffer prettyPrintXml = new StringBuffer();
		/* Group the xml tags */
		Pattern pattern = Pattern
				.compile("(<[^/][^>]+>)?([^<]*)(</[^>]+>)?(<[^/][^>]+/>)?");
		Matcher matcher = pattern.matcher(xmlString);
		int tabCount = 0;
		while (matcher.find()) {
			String str1 = (null == matcher.group(1) || "null".equals(matcher
					.group())) ? "" : matcher.group(1);
			String str2 = (null == matcher.group(2) || "null".equals(matcher
					.group())) ? "" : matcher.group(2);
			String str3 = (null == matcher.group(3) || "null".equals(matcher
					.group())) ? "" : matcher.group(3);
			String str4 = (null == matcher.group(4) || "null".equals(matcher
					.group())) ? "" : matcher.group(4);

			if (matcher.group() != null && !matcher.group().trim().equals("")) {
				printTabs(tabCount, prettyPrintXml);
				if (!str1.equals("") && str3.equals("")) {
					++tabCount;
				}
				if (str1.equals("") && !str3.equals("")) {
					--tabCount;
					prettyPrintXml.deleteCharAt(prettyPrintXml.length() - 1);
				}

				prettyPrintXml.append(str1);
				prettyPrintXml.append(str2);
				prettyPrintXml.append(str3);
				if (!str4.equals("")) {
					prettyPrintXml.append(LINE_BREAK);
					printTabs(tabCount, prettyPrintXml);
					prettyPrintXml.append(str4);
				}
				prettyPrintXml.append(LINE_BREAK);
			}
		}
		return prettyPrintXml.toString();
	}

	private static void printTabs(int count, StringBuffer stringBuffer) {
		for (int i = 0; i < count; i++) {
			stringBuffer.append("\t");
		}
	}

	public static File lastFileModified(String dir, final String ext) {
		File fl = new File(dir);
		File[] files = fl.listFiles(new FileFilter() {
			public boolean accept(File file) {
				// System.out.println(file.getName());
				return file.isFile() && file.getName().endsWith(ext);
			}
		});
		long lastMod = Long.MIN_VALUE;
		File choice = null;
		for (File file : files) {
			if (file.lastModified() > lastMod) {

				choice = file;
				lastMod = file.lastModified();
			}
		}
		return choice;
	}

	public static List<Path> listFiles(Path dir, String globPattern,
			final Date startDate, final Date endDate, boolean sort)
			throws IOException {

		List<Path> files = null;
		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(
				"glob:*" + globPattern);

		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path entry) throws IOException {
				Date d = new Date(Files.getLastModifiedTime(entry).toMillis());
				// System.out.println(entry+"\t"+startDate+"\t"+matcher.matches(entry));
				return d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0
						&& matcher.matches(entry.getFileName());
			}
		};

		if (!Files.isDirectory(dir)) {
			return null;
		} else {
			files = new ArrayList<>();
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
					filter)) {
				for (Path p : stream) {
					files.add(p);
				}
			}

			if (!files.isEmpty() && sort) {
				sortFiles(files);
			}
		}

		return files;
	}

	public static String getStackTraceString(Exception e) {
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(ba));
		return ba.toString();
	}

	public String getExceptionAsString(Exception e) {
		return new StringBuilder(e.getClass().getCanonicalName()
				+ " exception occurred. Message = '").append(e.getMessage())
				.append("'; Stacktrace is '").append(getStackTraceString(e))
				.append("'").toString();
	}

	public static List<String> mapToList(HashMap<?, ?> map) {

		List<String> list = new ArrayList<>();

		for (Entry<?, ?> entry : map.entrySet()) {
			list.add(String.valueOf(entry.getKey()));
			list.add(String.valueOf(entry.getValue()));
		}

		return list;

	}

	public static String inputStreamToString(InputStream inputStream)
			throws IOException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return new String(buffer.toByteArray());
	}

	public static String humanReadableByteCount(long v) {
		if (v < 1024)
			return v + " B";
		int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
		return String.format("%.1f %sB", (double) v / (1L << (z * 10)),
				" KMGTPE".charAt(z));
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public String formatRemoveNewlines(String paramString) {
		if (paramString != null) {
			int i = paramString.indexOf("\n");
			while (i > -1) {
				paramString = paramString.substring(0, i)
						+ paramString.substring(i + 1);
				i = paramString.indexOf("\n");
			}
		}
		return paramString;
	}

	public Properties delimitedStringToProperties(String paramString1,
			String paramString2, String paramString3) {
		Properties localProperties = new Properties();
		if ((paramString1 != null) && (paramString1.length() > 0)) {
			String[] arrayOfString1 = paramString1.split(paramString2);
			for (int i = 0; i < arrayOfString1.length; i++) {
				String[] arrayOfString2 = arrayOfString1[i].split(paramString3,
						2);
				localProperties.setProperty(arrayOfString2[0],
						arrayOfString2[1]);
			}
		}
		return localProperties;
	}

	public static String formatStringArray(String[] paramArrayOfString) {
		return formatObjectArray(paramArrayOfString);
	}

	public static String formatObjectArray(Object[] paramArrayOfObject) {
		String str;
		if (paramArrayOfObject != null) {
			StringBuffer localStringBuffer = new StringBuffer("[");
			for (int i = 0; i < paramArrayOfObject.length; i++) {
				if (i != 0) {
					localStringBuffer.append(", ");
				}
				localStringBuffer.append(paramArrayOfObject[i]);
			}
			localStringBuffer.append("]");
			str = localStringBuffer.toString();
		} else {
			str = "null";
		}
		return str;
	}

	public static String formatObjectArray(List<?> paramList) {
		String str;
		if (paramList != null) {
			StringBuffer localStringBuffer = new StringBuffer("[");
			int i = 0;
			for (Object localObject : paramList) {
				if (i != 0) {
					localStringBuffer.append(", ");
				}
				localStringBuffer.append(localObject);
				i++;
			}
			localStringBuffer.append("]");
			str = localStringBuffer.toString();
		} else {
			str = "null";
		}
		return str;
	}

	public static String formatObjectMap(
			Map<? extends Object, ? extends Object> paramMap) {
		String str;
		if (paramMap != null) {
			StringBuffer localStringBuffer = new StringBuffer("[");
			int i = 1;
			for (Map.Entry localEntry : paramMap.entrySet()) {
				if (i == 0) {
					localStringBuffer.append(",\n");
				} else {
					localStringBuffer.append("\n");
				}
				i = 0;
				localStringBuffer.append("" + localEntry.getKey() + "="
						+ localEntry.getValue());
			}
			if (i == 0) {
				localStringBuffer.append("\n");
			}
			localStringBuffer.append("]");
			str = localStringBuffer.toString();
		} else {
			str = "null";
		}
		return str;
	}

	public static String formatObjectEnumeration(Enumeration paramEnumeration) {
		String str;
		if (paramEnumeration != null) {
			StringBuffer localStringBuffer = new StringBuffer("[");
			int i = 0;
			while (paramEnumeration.hasMoreElements()) {
				if (i++ != 0) {
					localStringBuffer.append(", ");
				}
				localStringBuffer.append(paramEnumeration.nextElement());
			}
			localStringBuffer.append("]");
			str = localStringBuffer.toString();
		} else {
			str = "null";
		}
		return str;
	}

	public static String formatInputStream(InputStream paramInputStream) {
		String str;
		if (paramInputStream != null) {
			try {
				paramInputStream.reset();
				int i = paramInputStream.available();
				byte[] arrayOfByte = new byte[i];
				paramInputStream.read(arrayOfByte);
				try {
					str = new String(arrayOfByte, "UTF8");
				} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
					str = "IntegrationAPIExerciser.formatInputStream(): UTF8 not supported";
				}
			} catch (IOException localIOException) {
				str = "IntegrationAPIExerciser.formatInputStream(): "
						+ localIOException + " when reading InputStream";
			}
		} else {
			str = "null";
		}
		return str;
	}

	public static String userDir() {
		return System.getProperty("user.home");
	}

	public static String getContentType(String text) {

		String contentType = "text/plain";
		if (text.contains("<") && text.contains(">")) {
			contentType = "text/xml";
		} else if (text.contains("{") && text.contains("}")) {
			contentType = "text/json";
		}

		return contentType;
	}

	public static void watch(final JTextArea textArea,
			final InputStream inputStream) {
		new Thread() {
			public void run() {

				try (BufferedReader input = new BufferedReader(
						new InputStreamReader(inputStream))) {
					String s = null;
					while ((s = input.readLine()) != null
							&& s.trim().length() > 0) {
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	static String separatorsToSystem(String res) {
		if (res == null)
			return null;
		if (File.separatorChar == '\\') {
			// From Windows to Linux/Mac
			return res.replace('/', File.separatorChar);
		} else {
			// From Linux/Mac to Windows
			return res.replace('\\', File.separatorChar);
		}
	}
	
	public static String putLine(int n)
	  {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < n; i++) {
	      sb.append("-");
	    }
	    return sb.toString();
	  }
	
	public static String doPost(String url, String msg)
			throws UnsupportedOperationException, IOException {
		HttpPost httpPost = new HttpPost(url);

		httpPost.setEntity(new StringEntity(msg));
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(httpPost);
		try {
			return IOUtils.toString(response.getEntity().getContent(),
					"UTF-8");
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	public static String testSSL(String host, int portNo)
			throws UnknownHostException, IOException {
		StringBuilder sb = new StringBuilder();
		SocketFactory sslsocketfactory = SSLSocketFactory.getDefault();
		SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(host,
				portNo);
		InputStream in = sslsocket.getInputStream();
		OutputStream out = sslsocket.getOutputStream();

		out.write(1);
		while (in.available() > 0) {
			sb.append(in.read());
		}
		return sb.length() != 0 ? sb.toString() : null;
	}
	
	
	public static boolean verifyLicense(String keyPath, String pattern)
		    throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
	{
		Path path = Paths.get(keyPath, new String[0]);
		boolean decrypted = false;
		
		List<Path> keyFiles = listFiles(path.toString(), pattern);

		if (!keyFiles.isEmpty()) {
			String desKey = "0FB80AA80DD80FF8";
			byte[] keyBytes = DatatypeConverter.parseHexBinary(desKey);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = factory.generateSecret(new DESKeySpec(keyBytes));
			
			String add = getMacAddress();

			String textDecrypted = null;
			for (Path keyFile : keyFiles) {
				if (Files.exists(keyFile, new LinkOption[0])) {
					textDecrypted = new String(decrypt(secretKey, keyFile));
					if (textDecrypted.equals(add)) {
						decrypted = true;
						break;
					}
				}
			}
		}else {
//			SwingUtils.errorMsg("License file not found!");
		}
		
		return decrypted;
	}
		  
	public static boolean verifyMac(String url, String macAddress)
			throws MalformedURLException, IOException {
		String response = IOUtils.toString(new URL(url),
				"UTF-8");
		System.out.println(response);
		return true;
	}
	
	public static String watch(InputStream inputStream) throws IOException {
		
		String s = null;
		try (BufferedReader input = new BufferedReader(new InputStreamReader(inputStream))){
            
            StringBuffer sb = new StringBuffer();
        	while ((s = input.readLine() ) != null  ) {
        		
        		sb.append(s).append("\n");
        	}
//        	System.out.println(sb.toString());
        	
//        	tfCommandOutput.setText(sb.toString());
		}
		
		return s;
	}
	
	/***
	 * Decodes a Base64 encoded string 
	 * @param base64String
	 * @return decoded string
	 */
	public static String getStringFromBase64(String base64String) {
		byte[] str = DatatypeConverter.parseBase64Binary(base64String);
		return new String(str);
	}
	
	/*public static boolean isEncoded(String text){

	    Charset charset = Charset.forName("US-ASCII");
	    String checked=new String(text.getBytes(charset),charset);
	    return !checked.equals(text);

	}*/
	
	
	public static String convertUTCtoDefault(String ts, String pattern) throws ParseException {
		
		DateFormat utcFormat = new SimpleDateFormat(pattern);
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		Date date = utcFormat.parse(ts);

		DateFormat pstFormat = new SimpleDateFormat(pattern);
		pstFormat.setTimeZone(TimeZone.getDefault());

//		System.out.println(pstFormat.format(date));
		return pstFormat.format(date);
//				DateUtils.toCalendar(DateUtils.parseDate(ts, pattern), Calendar.getInstance().getTimeZone());
	}
	
	/*public static String[] loadBrokerFiles() throws IOException {
		
		List<String> brokerFiles = CommonUtils.listFilesNoExt(
				SwingUtils.userHome, "broker");
		
		return
				// (String[]) brokerFiles.toArray();
				(String[]) brokerFiles.toArray(new String[brokerFiles.size()]);


	}*/
	
	public static <K, V> Stream<K> getKeyByValueJ8(Map<K, V> map, V value) {
	    return map
	      .entrySet()
	      .stream()
	      .filter(entry -> value.equals(entry.getValue()))
	      .map(Map.Entry::getKey);
	}
}