package com.cribcaged.getoffthecouch.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.util.Log;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.groups.GroupsInterface;
import com.aetrion.flickr.groups.pools.PoolsInterface;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.uploader.UploadMetaData;
import com.aetrion.flickr.uploader.Uploader;
import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.entities.Photo;

/**
 * Class that handles the communication with Flickr API
 * @author Yunus Evren
 */
public class FlickrConnector {
	private static final String API_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
	private static final String SECRET = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
	
	private String token = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

	private Flickr flickr;
	private RequestContext requestContext;
	private Uploader uploader;
	private GroupsInterface groupsInterface;

	/**
	 * Constructor
	 * @param token - token that will be used to make API calls
	 */
	public FlickrConnector(String token){
		try {
			this.token = token;
			
			flickr = new Flickr(API_KEY, 
					SECRET,
					new REST());
			Flickr.debugStream = false;

			requestContext = RequestContext.getRequestContext();

			AuthInterface authInterface = flickr.getAuthInterface();
			Auth auth = authInterface.checkToken(token);
			System.out.println(auth.getPermission());
			System.out.println(auth.getUser().getUsername());
			requestContext.setAuth(auth);
			requestContext.setSharedSecret(SECRET);
		} catch (ParserConfigurationException e) {
			Log.e(MainActivity.LOG, "FlickrConnector: ParserConfigurationException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(MainActivity.LOG, "FlickrConnector: IOException " + e);
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e(MainActivity.LOG, "FlickrConnector: SAXException " + e);
			e.printStackTrace();
		} catch (FlickrException e) {
			Log.e(MainActivity.LOG, "FlickrConnector: FlickrException " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Uploads a photo into the Flickr group
	 * @param filePath - photo's file path in user's Android device
	 * @param locationName - name of the event location
	 * @param dateAndTime - date and time of the event
	 * @param userName - name of the user who uploads the photo
	 * @param participants - participants of the event
	 * @param groupId - Flickr group id that will host the photo
	 * @return Flickr id of the uploaded photo
	 */
	public String uploadPhoto(String filePath, String locationName, String dateAndTime, String userName, String participants, String groupId){
		try {
			InputStream fileIn = new FileInputStream(filePath);
			ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
			
			int i;
			byte[] buffer = new byte[1024];
			while ((i = fileIn.read(buffer)) != -1) {
				fileOut.write(buffer, 0, i);
			}
			fileIn.close();
			byte data[] = fileOut.toByteArray();

			uploader = flickr.getUploader();

			UploadMetaData uploadMetaData = new UploadMetaData();
			uploadMetaData.setTitle(locationName + " - " + dateAndTime);
			String photoDesc = "Uploaded by: " + userName + "\n\n" + "Event participants: \n" + participants;
			uploadMetaData.setDescription(photoDesc);
			uploadMetaData.setPublicFlag(true);
			String photoId = uploader.upload(data,uploadMetaData);
			
			Log.i(MainActivity.LOG, "photoId: " + photoId + ", groupId: " + groupId);
			
			PoolsInterface poolsInterface = flickr.getPoolsInterface();
			poolsInterface.add(photoId, groupId);
			return photoId;
		} catch (FileNotFoundException e) {
			Log.e(MainActivity.LOG, "FlickrConnector: uploadPhoto() FileNotFoundException " + e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(MainActivity.LOG, "FlickrConnector: uploadPhoto() IOException " + e);
			e.printStackTrace();
		} catch (FlickrException e) {
			Log.e(MainActivity.LOG, "FlickrConnector: uploadPhoto() FlickrException " + e);
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e(MainActivity.LOG, "FlickrConnector: uploadPhoto() SAXException " + e);
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * Updates a list of photos with up to date information.
	 * Calls the Flickr API and gets the photo details. Updates the photo objects.
	 * @param photoList input photo list that will be populated
	 * @return list of photos that are populated with the missing info
	 */
	public ArrayList<Photo> getPhotoInfo(ArrayList<Photo> photoList){
		PhotosInterface photosInterface = flickr.getPhotosInterface();
		for(Photo p : photoList){
			try {
				String id = p.getPhotoId();
				com.aetrion.flickr.photos.Photo flickrPhoto = photosInterface.getInfo(id, SECRET);
				Date d = flickrPhoto.getDatePosted();
				SimpleDateFormat outputFormatter = new SimpleDateFormat("d MMM yyyy, HH:mm");
				String dateAndTime = outputFormatter.format(d);
				
				p.setDateAndTime(dateAndTime);
				p.setLargeURL(flickrPhoto.getLargeUrl());
				p.setSmallURL(flickrPhoto.getSmallSquareUrl());
			} catch (IOException e) {
				Log.e(MainActivity.LOG, "FlickrConnector: getPhotoInfo() IOException " + e);
				e.printStackTrace();
				return null;
			} catch (SAXException e) {
				Log.e(MainActivity.LOG, "FlickrConnector: getPhotoInfo() SAXException " + e);
				e.printStackTrace();
				return null;
			} catch (FlickrException e) {
				Log.e(MainActivity.LOG, "FlickrConnector: getPhotoInfo() FlickrException " + e);
				e.printStackTrace();
				return null;
			}
		}
		return photoList;
	}
}
