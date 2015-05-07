package com.wattzap.model.social;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author David George
 * @date 1 May 2015
 * 
 */
public class SelfLoopsAPI {
	private final static String url = "https://www.selfloops.com/restapi/public/activities/upload.json";

	/*
	 * @param email -> the user email used to login on selfloops
	 * 
	 * @param pw -> the password
	 * 
	 * @param tcxfile -> the tcx file compressed with gzip or zip
	 * 
	 * @param note -> a text note for the activity
	 * 
	 * Code: 200
	 * 
	 * Code: 200
	 * 
	 * activity_id: [string],
	 * 
	 * message: [string]
	 * 
	 * Description:
	 * 
	 * activity_id --> the new ride id or -1 if error occurs
	 * 
	 * message--> a system message (text)
	 * 
	 * error_code --> the error code (integer).
	 * 
	 * Error Response Code: 403 (not authorized, authentication failure)
	 */

	public static int uploadActivity(String email, String passWord,
			String fileName, String note) throws IOException {
		JSONObject jsonObj = null;

		FileInputStream in = null;
		GZIPOutputStream out = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("enctype", "multipart/mixed");

			in = new FileInputStream(fileName);
			// Create stream to compress data and write it to the to file.
			ByteArrayOutputStream obj = new ByteArrayOutputStream();
			out = new GZIPOutputStream(obj);

			// Copy bytes from one stream to the other
			byte[] buffer = new byte[4096];
			int bytes_read;
			while ((bytes_read = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytes_read);
			}
			out.close();
			in.close();

			ByteArrayBody bin = new ByteArrayBody(obj.toByteArray(),
					ContentType.create("application/x-gzip"), fileName);
			HttpEntity reqEntity = MultipartEntityBuilder
					.create()
					.addPart("email",
							new StringBody(email, ContentType.TEXT_PLAIN))
					.addPart("pw",
							new StringBody(passWord, ContentType.TEXT_PLAIN))
					.addPart("tcxfile", bin)
					.addPart("note",
							new StringBody(note, ContentType.TEXT_PLAIN))
					.build();

			httpPost.setEntity(reqEntity);

			CloseableHttpResponse response = null;
			try {
				response = httpClient.execute(httpPost);
				int code = response.getStatusLine().getStatusCode();
				switch (code) {
				case 200:

					HttpEntity respEntity = response.getEntity();

					if (respEntity != null) {
						// EntityUtils to get the response content
						String content = EntityUtils.toString(respEntity);
						System.out.println(content);
						JSONParser jsonParser = new JSONParser();
						jsonObj = (JSONObject) jsonParser.parse(content);
					}

					break;
				case 403:
					throw new RuntimeException("Authentification failure "
							+ email + " " + response.getStatusLine());
				default:
					throw new RuntimeException("Error " + code + " "
							+ response.getStatusLine());
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (response != null) {
					response.close();
				}
			}

			String activityId = (String) jsonObj.get("activity_id");

			// parse error code
			String error = (String) jsonObj.get("error_code");
			if (error != null) {
				switch (Integer.parseInt(error)) {
				case 102:
					throw new RuntimeException("Empty TCX file " + fileName);
				case 103:
					throw new RuntimeException("Invalide TCX Format "
							+ fileName);
				case 104:
					throw new RuntimeException("TCX Already Present "
							+ fileName);
				case 105:
					throw new RuntimeException("Invalid XML " + fileName);
				case 106:
					throw new RuntimeException("invalid compression algorithm");
				case 107:
					throw new RuntimeException("Invalid file mime types");
				default:
					throw new RuntimeException("Unknown error " + error);
				}
			}
			return Integer.parseInt(activityId);
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			httpClient.close();
		}
	}
}
