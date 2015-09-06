package com.dcl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mediautil.gen.directio.SplitInputStream;
import mediautil.image.jpeg.LLJTran;
import mediautil.image.jpeg.LLJTranException;

public class CopyPictureHeader {

	/**
	 * <一句话功能简述> <功能详细描述>
	 * 
	 * @param args
	 * @see [类、类#方法、类#成员]
	 */
	public static void main(String[] args) {
		// 旧文件的流
		InputStream inOld = null;
		SplitInputStream sipOld = null;
		InputStream subIpOld = null;
		LLJTran lljOld = null;
		// 新文件的流
		InputStream inNew = null;
		SplitInputStream sipNew = null;
		InputStream subIpNew = null;
		LLJTran lljNew = null;

		OutputStream out = null;
		try {
			// 从旧文件中获取图片信息
			inOld = new BufferedInputStream(new FileInputStream("C://Users//DELL//Desktop//1.jpg"));
			sipOld = new SplitInputStream(inOld);
			subIpOld = sipOld.createSubStream();
			lljOld = new LLJTran(subIpOld);
			lljOld.initRead(LLJTran.READ_HEADER, true, true);
			sipOld.attachSubReader(lljOld, subIpOld);
			sipOld.wrapup();

			out = new BufferedOutputStream(new FileOutputStream("C://Users//DELL//Desktop//2.jpg"));
			lljOld.xferInfo(inOld, out, LLJTran.REPLACE, LLJTran.REPLACE);

			// 新图片信息
			inNew = new BufferedInputStream(new FileInputStream("C://Users//DELL//Desktop//2.jpg"));
			sipNew = new SplitInputStream(inNew);
			subIpNew = sipOld.createSubStream();
			lljNew = new LLJTran(subIpNew);
			lljNew.initRead(LLJTran.READ_HEADER, true, true);
			sipNew.attachSubReader(lljNew, subIpNew);
			sipNew.wrapup();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (LLJTranException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally
		// 关闭所有流,释放资源
		{
			try {
				if (inOld != null) {
					inOld.close();
				}
				if (sipOld != null) {
					sipOld.close();
				}
				if (subIpOld != null) {
					subIpOld.close();
				}
				if (lljOld != null) {
					lljOld.freeMemory();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}