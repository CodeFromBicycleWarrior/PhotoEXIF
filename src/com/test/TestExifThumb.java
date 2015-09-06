package com.test;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import mediautil.gen.directio.SplitInputStream;
import mediautil.image.ImageResources;
import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.LLJTran;

/**
 * 替换缩略图，测试之后没效果，没有继续深究
 * 原文地址：http://www.java3z.com/cwbwebhome/article/article2/2879.html?id=1521
 * @author liufeng
 * 2015.09.07
 * @beijing
 */
public class TestExifThumb {
	/**
	 * Utility Method to get a Thumbnail Image in a byte array from an
	 * InputStream to a full size image. The full size image is read and scaled
	 * to a Thumbnail size using Java API.
	 */
	private static byte[] getThumbnailImage(InputStream ip) throws IOException {
		return getThumbnailImage(ip, 0, 0);
	}

	public static byte[] getThumbnailImage(InputStream ip, int widthRate,
			int heightRate) throws IOException {
		ImageReader reader;
		ImageInputStream iis = ImageIO.createImageInputStream(ip);
		reader = (ImageReader) ImageIO.getImageReaders(iis).next();
		reader.setInput(iis);
		BufferedImage image = reader.read(0);
		iis.close();

		int t, longer, shorter;
		if (widthRate > 0 && heightRate > 0) {
			longer = widthRate;
			shorter = heightRate;
		} else {
			longer = image.getWidth();
			shorter = image.getHeight();
		}

		// 按传入参数的长宽比例放缩
		double factor = 160 / (double) image.getWidth();
		double factor2 = (160 * (double) shorter)
				/ ((double) longer * image.getHeight());
		AffineTransform tx = new AffineTransform();
		tx.scale(factor, factor2);
		AffineTransformOp affineOp = new AffineTransformOp(tx,
				AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		image = affineOp.filter(image, null);

		// Write Out the Scaled Image to a ByteArrayOutputStream and return the
		// bytes
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(2048);
		String format = "JPG";
		ImageIO.write(image, format, byteStream);
		System.out.println(byteStream.toByteArray());
		return byteStream.toByteArray();
	}

	public byte[] extractThumbnail(String fileStr) {
		return extractThumbnail(fileStr, 0, 0);
	}

	// 根据文件名字符串，按长宽比例放缩抽取该文件的ThumbnailImage，返回byte数组
	private byte[] extractThumbnail(String fileStr, int widthRate,
			int heightRate) {
		byte newThumbnail[] = null;
		try {
			InputStream fip = new FileInputStream(fileStr); // No need to buffer
			SplitInputStream sip = new SplitInputStream(fip);
			// Create a substream for LLJTran to use
			InputStream subIp = sip.createSubStream();
			LLJTran llj = new LLJTran(subIp);
			llj.initRead(LLJTran.READ_HEADER, true, true);
			sip.attachSubReader(llj, subIp);
			newThumbnail = getThumbnailImage(sip, widthRate, heightRate);
			sip.wrapup();
			fip.close();
			llj.freeMemory();
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
		} catch (Exception e) {
			System.out.println("extractThumbnail" + e);
		}
		return newThumbnail;
	}

	// 向另一张图片写入Thumbnail的方法，用到mediautil库：
	public void writeThumbnail(byte newThumbnail[], String fileStr) {
		try {
			InputStream fip = new FileInputStream(fileStr);
			LLJTran llj = new LLJTran(fip);
			llj.read(LLJTran.READ_ALL, true);

			AbstractImageInfo imageInfo = llj.getImageInfo();
			// important!!!! If the Image does not have an Exif Header create a
			// dummy Exif
			// Header
			if (!(imageInfo instanceof Exif)) {
				System.out.println("Adding a Dummy Exif Header");
				llj.addAppx(LLJTran.dummyExifHeader, 0,
						LLJTran.dummyExifHeader.length, true);
			}

			// Set the new Thumbnail
			if (llj.setThumbnail(newThumbnail, 0, newThumbnail.length,
					ImageResources.EXT_JPG))
				System.out.println("Successfully Set New Thumbnail");
			fip = new BufferedInputStream(new FileInputStream(fileStr));
			OutputStream out = new BufferedOutputStream(new FileOutputStream(
					"3.jpg"));
			//
			llj.xferInfo(fip, out, LLJTran.REPLACE, LLJTran.REPLACE);
			fip.close();
			out.close();
			// Cleanup
			llj.freeMemory();
		} catch (Exception e) {
			System.out.println("writeThumbnail" + e);
		}
	}

	public BufferedImage readImage(InputStream in, String type)
			throws IOException {
		Iterator readers = ImageIO.getImageReadersByFormatName(type);
		ImageReader reader = (ImageReader) readers.next();
		ImageInputStream iis = ImageIO.createImageInputStream(in);
		reader.setInput(iis, true);
		BufferedImage img = reader.read(0);
		return img;
	}

	public BufferedImage readImage(String fileName) throws IOException {
		String type = fileName.substring(fileName.lastIndexOf(".") + 1);
		return readImage(new FileInputStream(fileName), type);
	}

	public void test(String thumbnailFile, String destfile) {
		BufferedImage buf = null;
		int wRate = 0;
		int hRate = 0;
		try {
			buf = readImage(destfile);
			wRate = buf.getWidth();
			hRate = buf.getHeight();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			byte[] bt = extractThumbnail(thumbnailFile, wRate, hRate);
			writeThumbnail(bt, destfile);
		}
	}

	public static void main(String arg[]) {
		TestExifThumb t = new TestExifThumb();
		t.test("C://Users//DELL//Desktop//old.jpg", "C://Users//DELL//Desktop//new1.jpg"); // 用11.jpg的数据替换22.jpg的缩略图
	}
}