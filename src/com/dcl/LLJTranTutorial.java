package com.dcl;  
  
/* MediaUtil LLJTran - $RCSfile: LLJTranTutorial.java,v $ 
 * Copyright (C) 1999-2005 Dmitriy Rogatkin, Suresh Mahalingam.  All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met: 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 *  $Id: LLJTranTutorial.java,v 1.4 2005/08/18 04:35:34 drogatkin Exp $ 
 * 
 * Some ideas and algorithms were borrowed from: 
 * Thomas G. Lane, and James R. Weeks 
 */  
  
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import mediautil.gen.Log;
import mediautil.gen.directio.InStreamFromIterativeWriter;
import mediautil.gen.directio.IterativeWriter;
import mediautil.gen.directio.SplitInputStream;
import mediautil.image.ImageResources;
import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.image.jpeg.Entry;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.LLJTran;
  
/** 
 * This is a Tutorial for LLJTran in MediaUtil. This helps in getting quickly 
 * started on the various features of the LLJTran API for Lossless Jpeg 
 * Transformation and handling Exif information. To compile, include the 
 * MediaUtil jar file in your classpath.<p> 
 * 
 * Each of the mainXYZ method in this Tutorial file is an example. To run it 
 * first rename it to main and recompile. The examples are well commented. Each 
 * example program has an input file an output file and more parameters 
 * depending on the example. Type: 
 * java LLJTranTutorial 
 * for usage information. 
 * Please try each of the examples with 2 different input jpeg files. One with 
 * Exif header and one without. This directory has 3 kinds of files:<p> 
 * 
 * picture.jpg: With Exif Header<br> 
 * image.jpg: Without Exif Header<br> 
 * partialMCU.jpg: With partial MCU blocks for width and height and without Exif 
 *                 Header. Use this to test the LLJTran.OPT_XFORM_ADJUST_EDGES 
 *                 and LLJTran.OPT_XFORM_TRIM options. 
 */  
public class LLJTranTutorial  
{  
      
    /** 
     * Utility Method to get a Thumbnail Image in a byte array from an 
     * InputStream to a full size image. The full size image is read and scaled 
     * to a Thumbnail size using Java API. 
     */  
    private static byte[] getThumbnailImage(InputStream ip)  
        throws IOException  
    {  
        ImageReader reader;  
        ImageInputStream iis = ImageIO.createImageInputStream(ip);  
        reader = (ImageReader)ImageIO.getImageReaders(iis).next();  
        reader.setInput(iis);  
        BufferedImage image = reader.read(0);  
        iis.close();  
          
        // Scale the image to around 160x120/120x160 pixels, may not conform  
        // exactly to Thumbnail requirements of 160x120.  
        int t, longer, shorter;  
        longer = image.getWidth();  
        shorter = image.getHeight();  
        if (shorter > longer)  
        {  
            t = longer;  
            longer = shorter;  
            shorter = t;  
        }  
        double factor = 160 / (double)longer;  
        double factor1 = 120 / (double)shorter;  
        if (factor1 > factor)  
            factor = factor1;  
        AffineTransform tx = new AffineTransform();  
        tx.scale(factor, factor);  
        AffineTransformOp affineOp = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);  
        image = affineOp.filter(image, null);  
          
        // Write Out the Scaled Image to a ByteArrayOutputStream and return the  
        // bytes  
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(2048);  
        String format = "JPG";  
        ImageIO.write(image, format, byteStream);  
          
        return byteStream.toByteArray();  
    }  
      
    /** 
     * This example demonstrates basic Lossless Transformation.<p> 
     * 
     * Usage: java LLJTranTutorial <inputFile> <outputFile> <transformOperation><p> 
     * 
     * The program does the following:<p> 
     * 
     * 1. Reads the Image from inputFile<br> 
     * 2. Transforms it with the specified transformOperation except CROP<br> 
     * 3. Saves a vertical mirror of this transformed Image into vmirror.jpg 
     *    removing any Exif header information<br> 
     * 4. Saves the transormed Image (The Vertical Mirroring is not included) to 
     *    outputFile 
     */  
    public static void main1(String[] args)  
        throws Exception  
    {  
        int op = 0;  
          
        if (args.length != 3 || (op = Integer.parseInt(args[2])) < 0 || op > 7)  
        {  
            if (args.length == 3)  
                System.out.println("Invalid Transform Operation: " + op);  
              
            System.out.println("Usage: java LLJTranTutorial <inputFile> <outputFile> <transformOperation>\n");  
            System.out.println("  Use the following codes for <transformOperation>:");  
            System.out.println("    0: NONE");  
            System.out.println("    1: FLIP_H");  
            System.out.println("    2: FLIP_V");  
            System.out.println("    3: TRANSPOSE");  
            System.out.println("    4: TRANSVERSE");  
            System.out.println("    5: ROTATE  90 degrees clockwise");  
            System.out.println("    6: ROTATE 180 degrees clockwise");  
            System.out.println("    7: ROTATE 270 degrees clockwise");  
            System.out.println("Also creates vmirror.jpg, Vertical mirror of outputFile without Exif");  
            System.exit(1);  
        }  
          
        // Raise the Debug Level which is normally LEVEL_INFO. Only Warning  
        // messages will be printed by MediaUtil.  
        Log.debugLevel = Log.LEVEL_WARNING;  
          
        // 1. Initialize LLJTran and Read the entire Image including Appx markers  
        LLJTran llj = new LLJTran(new File(args[0]));  
        // If you pass the 2nd parameter as false, Exif information is not  
        // loaded and hence will not be written.  
        llj.read(LLJTran.READ_ALL, true);  
          
        // 2. Transform the image using default options along with  
        // transformation of the Orientation tags. Try other combinations of  
        // LLJTran_XFORM.. flags. Use a jpeg with partial MCU (partialMCU.jpg)  
        // for testing LLJTran.XFORM_TRIM and LLJTran.XFORM_ADJUST_EDGES  
        int options = LLJTran.OPT_DEFAULTS | LLJTran.OPT_XFORM_ORIENTATION;  
        llj.transform(op, options);  
          
        // 3. Save the Vertical mirror of the Transformed image without Exif  
        // header.  
        OutputStream out = new BufferedOutputStream(new FileOutputStream("vmirror.jpg"));  
        // Turn off OPT_WRITE_APPXS flag to Skip writing Exif.  
        // Also try writing with Appxs  
        options = LLJTran.OPT_DEFAULTS & ~LLJTran.OPT_WRITE_APPXS;  
        // Save with vertical transformation without changing the llj image.  
        llj.transform(out, LLJTran.FLIP_V, options);  
        out.close();  
          
        // 4. Save the Image which is already transformed as specified by the  
        //    input transformation in Step 2, along with the Exif header.  
        out = new BufferedOutputStream(new FileOutputStream(args[1]));  
        llj.save(out, LLJTran.OPT_WRITE_ALL);  
        out.close();  
          
        // Cleanup  
        llj.freeMemory();  
    }  
      
    /** 
     * This example demonstrates the CROP operation, directio and modifying the 
     * Thumbnail.<p> 
     * 
     * Usage: java LLJTranTutorial <inputFile> <outputFile> 
     * <cropX> <cropY> <cropWidth> <cropHeight><p> 
     * 
     * The program does the following:<p> 
     * 
     * 1. Reads the Image from inputFile<br> 
     * 2. Crops it to the specified Bounds<br> 
     * 3. If the Image has an Exif Header it recreates the Thumbnail. For 
     * this java reads the cropped image directly from LLJTran using directio. 
     * Then the image is scaled to thumbnail size and this image is set as the 
     * new Thumbnail<br> 
     * 4. Saves the transormed Image to outputFile with the new Thumbnail 
     */  
    public static void main2(String[] args)  
        throws Exception  
    {  
        if (args.length != 6)  
        {  
            System.out.println("Usage: java LLJTranTutorial <inputFile> <outputFile> <cropX> <cropY> <cropWidth> <cropHeight>");  
            System.exit(1);  
        }  
          
        // 1. Initialize LLJTran and Read the entire Image including Appx markers  
        LLJTran llj = new LLJTran(new File(args[0]));  
        // If you pass the 2nd parameter as false, Exif information is not  
        // loaded and hence will not be written.  
        llj.read(LLJTran.READ_ALL, true);  
          
        // 2. Crop it to the specified Bounds  
        Rectangle cropArea = new Rectangle();  
        cropArea.x = Integer.parseInt(args[2]);  
        cropArea.y = Integer.parseInt(args[3]);  
        cropArea.width = Integer.parseInt(args[4]);  
        cropArea.height = Integer.parseInt(args[5]);  
        llj.transform(LLJTran.CROP, LLJTran.OPT_DEFAULTS, cropArea);  
          
        // 3. If Image has an Exif header set/change the Thumbnail to the  
        // downscale of new Image  
        if (llj.getImageInfo() instanceof Exif)  
        {  
            // Read the image in llj and get a Thumbnail Image from it.  
            //  
            // In the regular usage you can save the image in llj to an  
            // OutputStream.  
            //  
            // However since llj implements an IterativeWriter the image can be  
            // directly read  
            InStreamFromIterativeWriter iwip = new InStreamFromIterativeWriter();  
            IterativeWriter iWriter =  
                llj.initWrite(iwip.getWriterOutputStream(), LLJTran.NONE, LLJTran.OPT_WRITE_ALL, null, 0, false);  
            iwip.setIterativeWriter(iWriter);  
            byte newThumbnail[] = getThumbnailImage(iwip);  
            llj.wrapupIterativeWrite(iWriter);  
              
            // Set the new Thumbnail  
            if (llj.setThumbnail(newThumbnail, 0, newThumbnail.length, ImageResources.EXT_JPG))  
                System.out.println("Successfully Set New Thumbnail");  
            else  
                System.out.println("Error Setting New Thumbnail");  
        }  
        else  
            System.out.println("Cannot Set Thumbnail Since There is no EXIF Header");  
          
        // 4. Save the Image with the new Thumbnail  
        OutputStream out = new BufferedOutputStream(new FileOutputStream(args[1]));  
        llj.save(out, LLJTran.OPT_WRITE_ALL);  
        out.close();  
          
        // Cleanup  
        llj.freeMemory();  
    }  
      
    /** 
     * This example demonstrates modifyig just the Exif Header using xferInfo. 
     * It also shows the use of SplitInputStream for LLJTran to Share the Image 
     * Input with jdk's ImageReader.<p> 
     * 
     * Usage: java LLJTranTutorial <inputFile> <outputFile><p> 
     * 
     * inputFile must be a jpeg file without Exif or with Exif but without a 
     * Thumbnail image. 
     * 
     * The program does the following:<p> 
     * 
     * 1. Reads the Image from inputFile upto READ_HEADER along with the 
     *    ImageReader using SplitInputStream<br> 
     * 2. If the image has a Thumbnail (Which means it has a Exif Header) it 
     *    prints a message and exits.<br> 
     * 3. If the Image does not have an Exif Header it creates a dummy Exif 
     *    Header<br> 
     * 4. The image is scaled to thumbnail size and this image is set as the 
     *    new Thumbnail 
     * 5. Transfers the image from inputFile to outputFile replacing the new 
     *    Exif with the Thumbnail so that outputFile has a Thumbnail. 
     */  
    public static void main3(String[] args)  
        throws Exception  
    {  
        if (args.length != 2)  
        {  
            System.out.println("Usage: java LLJTranTutorial <inputFile> <outputFile>");  
            System.exit(1);  
        }  
          
        // 1. Read the Image from inputFile upto READ_HEADER along with the  
        //    ImageReader using SplitInputStream and Generate a Thumbnail from  
        //    the Image.  
        InputStream fip = new FileInputStream(args[0]); // No need to buffer  
        SplitInputStream sip = new SplitInputStream(fip);  
        // Create a substream for LLJTran to use  
        InputStream subIp = sip.createSubStream();  
        LLJTran llj = new LLJTran(subIp);  
        // Normally it would be better to read the entire image when reading  
        // shared. But LLJTran only needs to read upto header for loading  
        // imageInfo and using xferInfo.  
        llj.initRead(LLJTran.READ_HEADER, true, true);  
        sip.attachSubReader(llj, subIp);  
        // LLJTran reads the image when the below API reads from sip via  
        // nextRead() calls made by sip.  
        byte newThumbnail[] = getThumbnailImage(sip);  
        sip.wrapup();  
        fip.close();  
          
        // Check llj for errors  
        String msg = llj.getErrorMsg();  
        if (msg != null)  
        {  
            System.out.println("Error in LLJTran While Loading Image: " + msg);  
            Exception e = llj.getException();  
            if (e != null)  
            {  
                System.out.println("Got an Exception, throwing it..");  
                throw e;  
            }  
            System.exit(1);  
        }  
          
        // 2. If the image has a Thumbnail (Which means it has a Exif Header)  
        //    print a message and exit.  
        AbstractImageInfo imageInfo = llj.getImageInfo();  
        if (imageInfo.getThumbnailLength() > 0)  
        {  
            System.out.println("Image already has a Thumbnail. Exitting..");  
            System.exit(1);  
        }  
          
        // 3. If the Image does not have an Exif Header create a dummy Exif  
        //    Header  
        if (!(imageInfo instanceof Exif))  
        {  
            System.out.println("Adding a Dummy Exif Header");  
            llj.addAppx(LLJTran.dummyExifHeader, 0, LLJTran.dummyExifHeader.length, true);  
            imageInfo = llj.getImageInfo(); // This would have changed  
              
            Exif exif = (Exif)imageInfo;  
              
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
            if (imageWidth > 0 && imageHeight > 0)  
            {  
                entry = exif.getTagValue(Exif.EXIFIMAGEWIDTH, true);  
                if (entry != null)  
                    entry.setValue(0, new Integer(imageWidth));  
                entry = exif.getTagValue(Exif.EXIFIMAGELENGTH, true);  
                if (entry != null)  
                    entry.setValue(0, new Integer(imageHeight));  
            }  
        }  
          
        // 4. Set the new Thumbnail  
        if (llj.setThumbnail(newThumbnail, 0, newThumbnail.length, ImageResources.EXT_JPG))  
            System.out.println("Successfully Set New Thumbnail");  
        else  
            System.out.println("Error Setting New Thumbnail");  
          
        // 5. Transfer the image from inputFile to outputFile replacing the new  
        //    Exif with the Thumbnail so that outputFile has a Thumbnail.  
        fip = new BufferedInputStream(new FileInputStream(args[0]));  
        OutputStream out = new BufferedOutputStream(new FileOutputStream(args[1]));  
        // Replace the new Exif Header in llj while copying the image from fip  
        // to out  
        llj.xferInfo(fip, out, LLJTran.REPLACE, LLJTran.RETAIN);  
        fip.close();  
        out.close();  
          
        // Cleanup  
        llj.freeMemory();  
    }  
} 