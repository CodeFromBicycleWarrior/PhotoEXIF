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
public class TestExifReader {

	public static void main(String args[]) {
		readEXIF();
		readExif();
	}

	/**
	 * 使用JpegMetadataReader.readMetadata获取Metadata信息之后，再获取Directory
	 * http://blog.csdn.net/jsnjlc/article/details/2844010
	 * @throws JpegProcessingException
	 */
	public static void readEXIF(){
		try {
			File jpegFile = new File("C://Users//DELL//Desktop//4.jpg");
			Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);
			Directory exif = metadata.getDirectory(ExifDirectory.class);// 这里要稍微注意下
			Iterator tags = exif.getTagIterator();
			while (tags.hasNext()) {
				Tag tag = (Tag) tags.next();
				System.out.println(tag);
			}
		}
		catch (JpegProcessingException e){
			e.printStackTrace();
		}
	}
	/**
	 * 使用ExifReader.extract()获取Metadata信息之后，再获取Directory
	 * http://bbs.csdn.net/topics/40177731#post-25060371
	 */
	private static void readExif() {
		File f = new File("C://Users//DELL//Desktop//4.jpg");
		try {
			ExifReader er = new ExifReader(f);
			Metadata exif = er.extract();
			Iterator itr = exif.getDirectoryIterator();
			while (itr.hasNext()) {
				Directory directory = (Directory) itr.next();
				/*
				 * System.out.println("EXIF版本 " +
				 * directory.getString(ExifDirectory.TAG_EXIF_VERSION));
				 * System.out.println("相机品牌 " +
				 * directory.getString(ExifDirectory.TAG_MAKE));
				 * System.out.println("相机型号 " +
				 * directory.getString(ExifDirectory.TAG_MODEL));
				 * System.out.println("光圈 " +
				 * directory.getString(ExifDirectory.TAG_FNUMBER));
				 * System.out.println("快門 " +
				 * directory.getString(ExifDirectory.TAG_EXPOSURE_TIME));
				 * System.out.println("感光度 " +
				 * directory.getString(ExifDirectory.TAG_ISO_EQUIVALENT));
				 * break;
				 */

				/**
				 *  更多的字段含义可以参考：http://www.cnblogs.com/wangtianxj/archive/2011/06/01/2067433.html
				 *  上面链接中的内容未确认过，仅提供参考
				 */
				System.out.println("EXIF版本 " + directory.getString(ExifDirectory.TAG_EXIF_VERSION));
				System.out.println("相机品牌 " + directory.getString(ExifDirectory.TAG_MAKE));
				System.out.println("相机型号 " + directory.getString(ExifDirectory.TAG_MODEL));
				System.out.println("光圈 " + directory.getString(ExifDirectory.TAG_FNUMBER));
				System.out.println("快門 " + directory.getString(ExifDirectory.TAG_EXPOSURE_TIME));
				System.out .println("感光度 " + directory.getString(ExifDirectory.TAG_ISO_EQUIVALENT));
				
				
				// 获得全部metadata
				Iterator tags = directory.getTagIterator();
				while (tags.hasNext()) {
					Tag tag = (Tag) tags.next();
					System.out.println(tag);
				}
				if (directory.hasErrors()) {
					Iterator errors = directory.getErrors();
					while (errors.hasNext()) {
						System.out.println("ERROR: " + errors.next());
					}
				}
			}
		} catch (JpegProcessingException e) {
			System.err.println("not jpeg file");
//		} catch (FileNotFoundException ex) {
//			System.err.println("file not found");
		}
	}

}