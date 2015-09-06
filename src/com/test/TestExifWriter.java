package com.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import mediautil.gen.Rational;
import mediautil.image.jpeg.Entry;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.LLJTran;
import mediautil.image.jpeg.LLJTranException;

/**
 * 重写照片EXIF信息
 * @author liufeng
 * 2015.09.07
 * @beijing
 *
 */
public class TestExifWriter {
	public static void main(String[] args) throws Exception {
		modifyEXIF();
	}

	
	/**
     * 将照片中的信息进行重写
     * http://blog.csdn.net/jsnjlc/article/details/2844010
     * @param args
     * @throws Exception
     */
    public static void modifyEXIF() throws Exception {
        //原文件
        InputStream fip = new BufferedInputStream(new FileInputStream("C://Users//DELL//Desktop//1.jpg")); // No need to buffer
        LLJTran llj = new LLJTran(fip);
        try {
            llj.read(LLJTran.READ_INFO, true);
        } catch (LLJTranException e) {
            e.printStackTrace();
        }
        Exif exif = (Exif) llj.getImageInfo();      
        
        /* Set some values directly to gps IFD */
                
        Entry e;
        // Set Latitude
        e = new Entry(Exif.ASCII);        
        e.setValue(0, 'N');
        exif.setTagValue(Exif.GPSLatitudeRef,-1, e, true);
        //设置具体的精度
		e = new Entry(Exif.RATIONAL);
		e.setValue(0, new Rational(31, 1));
		e.setValue(1, new Rational(21, 1));
		e.setValue(2, new Rational(323, 1));
		exif.setTagValue(Exif.GPSLatitude, -1, e, true);

		// Set Longitude
		e = new Entry(Exif.ASCII);
		e.setValue(0, 'E');
		exif.setTagValue(Exif.GPSLongitudeRef, -1, e, true);

		// 设置具体的纬度
		e = new Entry(Exif.RATIONAL);
		e.setValue(0, new Rational(120, 1));
		e.setValue(1, new Rational(58, 1));
		e.setValue(2, new Rational(531, 1));
		exif.setTagValue(Exif.GPSLongitude, -1, e, true);

		llj.refreshAppx(); // Recreate Marker Data for changes done
		// 改写后的文件，文件必须存在
		OutputStream out = new BufferedOutputStream(new FileOutputStream("C://Users//DELL//Desktop//2.jpg"));
		// Transfer remaining of image to output with new header.
		llj.xferInfo(null, out, LLJTran.REPLACE, LLJTran.REPLACE);
        fip.close();
        out.close();
        llj.freeMemory();
    }
}
