package com.test;

import com.drew.metadata.*;
import com.drew.metadata.exif.*;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import java.io.*;
import java.util.Iterator;

/**
 * 两种不同的方法读取EXIF信息
 * @author liufeng
 * 2015.09.07
 * @beijing
 */
public class TestGpsReader {

	public static void main(String args[]) {
		readGps();
	}

	/**
	 * 使用JpegMetadataReader.readMetadata获取Metadata信息之后，再获取Directory
	 * http://blog.csdn.net/jsnjlc/article/details/2844010
	 * @throws JpegProcessingException
	 */
	public static void readGps(){
		try {
			File jpegFile = new File("C://Users//DELL//Desktop//4.jpg");
			Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);
//			Directory exif = metadata.getDirectory(ExifDirectory.class);// 这里要稍微注意下
	        Directory gps = metadata.getDirectory(GpsDirectory.class);  
			Iterator tags = gps.getTagIterator();
			while (tags.hasNext()) {
				Tag tag = (Tag) tags.next();
				System.out.println(tag);
			}
		}
		catch (JpegProcessingException e){
			e.printStackTrace();
		}
	}
}