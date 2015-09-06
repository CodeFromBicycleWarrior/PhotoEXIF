package com.dcl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import mediautil.gen.directio.SplitInputStream;
import mediautil.image.ImageResources;
import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.image.jpeg.Entry;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.LLJTran;
import mediautil.image.jpeg.LLJTranException;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectory;

public class MainExif {

	/**
	 * 写入图片的exif信息
	 * 
	 * @param args
	 * @see [类、类#方法、类#成员]
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		File oldFile = new File("C://Users//DELL//Desktop//1.jpeg");
		File newFile = new File("C://Users//DELL//Desktop//2.jpeg");
		try {
			// 源图片exif信息读取
			Metadata metadata = JpegMetadataReader.readMetadata(oldFile);
			Directory directory = metadata.getDirectory(ExifDirectory.class);// 这里要稍微注意下
			Iterator<Tag> tags = directory.getTagIterator();
			while (tags.hasNext()) {
				Tag tag = (Tag) tags.next();
				System.out.println("old" + tag);
			}
			// 新图片exif信息读取
			metadata = JpegMetadataReader.readMetadata(newFile);
			directory = metadata.getDirectory(ExifDirectory.class);// 这里要稍微注意下
			tags = directory.getTagIterator();
			while (tags.hasNext()) {
				Tag tag = (Tag) tags.next();
				System.out.println("new" + tag);
			}
			// 从旧文件中获取Thumbnail信息,长度和偏移量
			InputStream inOld = new BufferedInputStream(new FileInputStream(
					"C://Users//DELL//Desktop//1.jpeg"));
			SplitInputStream sipOld = new SplitInputStream(inOld);
			InputStream subIpOld = sipOld.createSubStream();
			LLJTran lljOld = new LLJTran(subIpOld);
			lljOld.initRead(LLJTran.READ_HEADER, true, true);
			sipOld.attachSubReader(lljOld, subIpOld);
			sipOld.wrapup();
			inOld.close();

			AbstractImageInfo imageInfoOld = lljOld.getImageInfo();
			Exif exifOld = (Exif) imageInfoOld;
			byte[] markerDataOld = Exif.getMarkerData();
			int thumbnailLengthOld = imageInfoOld.getThumbnailLength();
			int offsetOld = imageInfoOld.getThumbnailOffset();// 旧文件的偏移量
			String thumbnailExtensionOld = imageInfoOld.getThumbnailExtension();// 旧文件的扩展名
			InputStream thumbnailStreamOld = lljOld.getThumbnailAsStream();
			byte[] thumbOld = new byte[thumbnailLengthOld];
			thumbnailStreamOld.read(thumbOld);
			thumbnailStreamOld.close();
			sipOld.close();

			// 目标文件exif信息修改
			InputStream fip = new BufferedInputStream(new FileInputStream(
					"c:/new.jpg"));
			SplitInputStream sip = new SplitInputStream(fip);
			InputStream subIp = sip.createSubStream();
			LLJTran llj = new LLJTran(subIp);
			llj.initRead(LLJTran.READ_HEADER, true, true);
			sip.attachSubReader(llj, subIp);
			sip.wrapup();
			fip.close();
			// Check llj for errors
			String msg = llj.getErrorMsg();
			if (msg != null) {
				System.out.println("Error in LLJTran While Loading Image: "
						+ msg);
				Exception e = llj.getException();
				if (e != null) {
					System.out.println("Got an Exception, throwing it..");
					throw e;
				}
				System.exit(1);
			}

			AbstractImageInfo imageInfo = llj.getImageInfo();
			String string = imageInfo.getThumbnailExtension();
			System.out.println(string);
			if (imageInfo.getThumbnailLength() > 0) {
				System.out.println("Image already has a Thumbnail. Exitting..");
				System.exit(1);
			}
			if (!(imageInfo instanceof Exif)) {
				System.out.println("Adding a Dummy Exif Header");
				llj.addAppx(markerDataOld, 0, markerDataOld.length, true);
				imageInfo = llj.getImageInfo(); // This would have changed

				Exif exif = (Exif) imageInfo;
				// Changed Date/Time and dimensions in Dummy Exif
				Entry entry = exif.getTagValue(Exif.DATETIME, true);
				if (entry != null)
					entry.setValue(0, "1998:08:18 11:15:00");
				entry = exif.getTagValue(Exif.DATETIMEORIGINAL, true);
				if (entry != null)
					entry.setValue(0, "1998:08:18 11:15:00");
				entry = exif.getTagValue(Exif.DATETIMEDIGITIZED, true);
				if (entry != null)
					entry.setValue(0, "1998:08:18 11:15:00");

				int imageWidth = llj.getWidth();
				int imageHeight = llj.getHeight();
				if (imageWidth > 0 && imageHeight > 0) {
					entry = exif.getTagValue(Exif.EXIFIMAGEWIDTH, true);
					if (entry != null)
						entry.setValue(0, new Integer(imageWidth));
					entry = exif.getTagValue(Exif.EXIFIMAGELENGTH, true);
					if (entry != null)
						entry.setValue(0, new Integer(imageHeight));
				}
			}

			if (llj.setThumbnail(thumbOld, 294, thumbOld.length,
					ImageResources.EXT_JPG))
				System.out.println("Successfully Set New Thumbnail");
			else
				System.out.println("Error Setting New Thumbnail");

			fip = new BufferedInputStream(new FileInputStream("C://Users//DELL//Desktop//1.jpeg"));
			OutputStream out = new BufferedOutputStream(new FileOutputStream(
					"C://Users//DELL//Desktop//2.jpeg"));

			// Replace the new Exif Header in llj while copying the image from
			// fip
			// to out
			llj.xferInfo(fip, out, LLJTran.REPLACE, LLJTran.REPLACE);
			fip.close();
			out.close();
			llj.freeMemory();
			lljOld.freeMemory();
		} catch (JpegProcessingException e) {
			e.printStackTrace();
		} catch (LLJTranException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}