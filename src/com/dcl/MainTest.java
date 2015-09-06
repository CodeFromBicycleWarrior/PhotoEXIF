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
  
public class MainTest  
{  
      
    /** <一句话功能简述> 
     * <功能详细描述> 
     * @param args 
     * @see [类、类#方法、类#成员] 
     */  
    public static void main(String[] args)  
    {  
        //旧文件的流  
        InputStream inOld = null;  
        SplitInputStream sipOld = null;  
        InputStream subIpOld = null;  
        LLJTran lljOld = null;  
        InputStream inNew = null;  
        OutputStream outNew = null;  
        try  
        {  
            //从旧文件中获取图片信息  
            inOld = new BufferedInputStream(new FileInputStream("C://Users//DELL//Desktop//1.jpg"));  
            sipOld = new SplitInputStream(inOld);  
            subIpOld = sipOld.createSubStream();  
            lljOld = new LLJTran(subIpOld);  
            lljOld.initRead(LLJTran.READ_HEADER, true, true);  
            sipOld.attachSubReader(lljOld, subIpOld);  
            sipOld.wrapup();  
            //            AbstractImageInfo imageInfoOld = lljOld.getImageInfo();  
            //            int thumbnailLengthOld = imageInfoOld.getThumbnailLength();  
            //            int offsetOld = imageInfoOld.getThumbnailOffset();//旧文件的偏移量  
            //            String thumbnailExtensionOld = imageInfoOld.getThumbnailExtension();//旧文件的扩展名  
            //            InputStream thumbnailStreamOld = lljOld.getThumbnailAsStream();  
            //            byte[] thumbOld = new byte[thumbnailLengthOld];  
            //            thumbnailStreamOld.read(thumbOld);  
            //            thumbnailStreamOld.close();  
            //            lljOld.setThumbnail(thumbOld, offsetOld, thumbnailLengthOld, thumbnailExtensionOld);  
              
            inNew = new BufferedInputStream(new FileInputStream("C://Users//DELL//Desktop//2.jpg"));  
            outNew = new BufferedOutputStream(new FileOutputStream("C://Users//DELL//Desktop//2.jpg"));  
            lljOld.xferInfo(inNew, outNew, LLJTran.REPLACE, LLJTran.REPLACE);  
        }  
        catch (FileNotFoundException e)  
        {  
            e.printStackTrace();  
        }  
        catch (LLJTranException e)  
        {  
            e.printStackTrace();  
        }  
        catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
    }  
      
}