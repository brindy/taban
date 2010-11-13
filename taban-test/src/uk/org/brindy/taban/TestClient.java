package uk.org.brindy.taban;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestClient {

	private static final String URL = "http://localhost:8080/taban";

	public static void main(String args[]) throws Exception {
		get("/");

		put("/14", "{ \"value\" : \"world\" }");
		get("/");
		get("/14");

		put("/", "{ \"x\" : 1 }");
		get("/");
		get("/1");

		get("/hello");
		put("/hello", "{ \"value\" : \"world\" }");
		get("/hello");
		
		get("/hello/");
		put("/hello/", "{ \"more\" : \"json\" }");
		get("/");
		get("/hello/");

	}

	private static void get(String path) throws Exception {
		URL u = new URL(URL + path);

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					u.openStream()));
			String line = null;

			System.out.println("GET " + path + " : ");
			while (null != (line = reader.readLine())) {
				System.out.println("\t" + line);
			}
		} catch (FileNotFoundException e) {
			// could happen
			System.out.println("GET " + path + " : 404");
		}
	}

	private static void put(String path, String json) throws Exception {

		URL u = new URL(URL + path);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestMethod("PUT");
		conn.setDoOutput(true);

		PrintWriter pw = new PrintWriter(conn.getOutputStream());
		pw.println(json);
		pw.close();

		int code = conn.getResponseCode();
		System.out.println("PUT " + path + " : " + json + " : " + code);

		if (code == 200) {

			String id = conn.getHeaderField("taban_autoid");
			System.out.println("\tid : " + id);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line = null;

			while (null != (line = reader.readLine())) {
				System.out.println("\t" + line);
			}

		}

		conn.disconnect();
	}

}
