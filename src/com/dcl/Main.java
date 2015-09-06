package com.dcl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.ImageIcon;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class Main {
	public static void main(String[] a) {
		createMark("C://Users//DELL//Desktop//1.jpg", "C://Users//DELL//Desktop//4.jpg", "014312406036", Color.BLACK, 1.0F, "Times New Roman", 30);
	}

	public static boolean createMark(String filePath, String filePath1,
			String markContent, Color markContentColor, float qualNum,
			String fontType, int fontSize) {
		ImageIcon imgIcon = new ImageIcon(filePath);
		Image theImg = imgIcon.getImage();
		// Image可以获得 输入图片的信息
		int width = theImg.getWidth(null);
		int height = theImg.getHeight(null);
		Object pro = theImg.getProperty("", null);
		System.out.println(pro);
		// 800 800 为画出图片的大小
		BufferedImage bimage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		// 2d 画笔
		Graphics2D g = bimage.createGraphics();
		g.setColor(markContentColor);
		g.setBackground(Color.white);

		// 画出图片-----------------------------------
		g.drawImage(theImg, 0, 0, null);
		// 画出图片-----------------------------------

		// --------对要显示的文字进行处理--------------
		AttributedString ats = new AttributedString(markContent);
		Font f = new Font(fontType, Font.HANGING_BASELINE, fontSize);
		ats.addAttribute(TextAttribute.FONT, f, 0, markContent.length());
		AttributedCharacterIterator iter = ats.getIterator();
		// ----------------------
		g.drawString(iter, 80, height - 20);
		// 添加水印的文字和设置水印文字出现的内容 ----位置
		g.dispose();// 画笔结束
		try {
			// 输出 文件 到指定的路径
			FileOutputStream out = new FileOutputStream(filePath1);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);

			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bimage);
			param.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
			param.setXDensity(150);
			param.setYDensity(150);
			param.setQuality(qualNum, true);
			encoder.encode(bimage, param);
			out.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}