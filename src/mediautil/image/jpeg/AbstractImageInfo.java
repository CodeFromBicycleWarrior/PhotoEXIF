/* MediaUtil LLJTran - $RCSfile: AbstractImageInfo.java,v $
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
 *	$Id: AbstractImageInfo.java,v 1.4 2006/05/13 07:05:54 msuresh Exp $
 *
 * Some ideas and algorithms were borrowed from:
 * Thomas G. Lane, and James R. Weeks
 */
package mediautil.image.jpeg;

import java.util.Date;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.awt.Dimension;
import javax.swing.Icon;

import mediautil.gen.MediaInfo;
import mediautil.gen.Rational;
import mediautil.image.ImageResources;
import mediautil.gen.FileFormatException;

/** This class represent additional information about image, such information
 * usually supplied with an image in additional headers. Currently only Exif
 * among derived classes provides the full capability to view and modify the
 * Thumbnail and to modify Image Header Information through the methods
 * writeInfo, getThumbnailOffset and getThumbnailLength.
 *
 * To provide more common solution, this calss has to extend javax.imageio.metadata.IIOMetadata
 * @author dmitriy
 */
public abstract class AbstractImageInfo <F extends LLJTran> extends BasicJpegIo implements MediaInfo {
    /** Default Thumbnail Size */
	public static final Dimension DEFAULT_THUMB_SIZE = new Dimension (120, 96);
	public final static DateFormat dateformat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	public final static DecimalFormat fnumberformat = new DecimalFormat("F1:#0.0#");
	public static final String NA = "n/a";
	public final static byte [] BMP_SIG = { 0x42, 0x4D };
	public final static int BMP24_HDR_SIZE = 54;

	protected static final Class [] EMPTY_PARAMS = {};

    protected AdvancedImage advancedImage;
    protected F format;

	// conversions
	public final static double[] AV_TO_FSTOP =
	{1, 1.4, 2, 2.8, 4, 5.6, 8, 11, 16, 22, 32 };
	public final static Rational[] TV_TO_SEC =
	{new Rational(1,1), new Rational(1,2), new Rational(1,4), new Rational(1,8),
			new Rational(1,15), new Rational(1,30), new Rational(1,60), new Rational(1,125),
			new Rational(1,250), new Rational(1,500), new Rational(1,1000), new Rational(1,2000),
			new Rational(1,4000), new Rational(1,8000), new Rational(1,16000) };

	public AbstractImageInfo() {
	}

    /**
     * Loads the ImageInfo using information supplied. Relies on the
     * implementation of {@link #readInfo()} by the deriving class.
     * @param is Image input. Note that LLJTran does not pass the actual
     * ImageInput but only the Marker Data. This is because LLJTran
     * will have to read further from the same Input Stream.
     * @param data Image Header Information Marker Data excluding the 4 jpeg
     * marker  bytes
     * @param offset Offset of marker within Image Input
     * @param name Name of the Image File
     * @param comments Image comments
     * @param format Image Object of type LLJTran
     */
	public AbstractImageInfo(InputStream is, byte[] data, int offset, String name, String comments, F format) throws FileFormatException {
		this.is = is;
		this.data = data;
		this.offset = offset;
		this.name = name;
		this.comments = comments;
        this.format = format;
		readInfo();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public void setAdvancedImage(AdvancedImage advancedImage)
    {
        this.advancedImage = advancedImage;
    }

    public AdvancedImage getAdvancedImage()
    {
        return advancedImage;
    }

    /**
     * writeInfo method without actual imageWidth and imageHeight
	 * @see #writeInfo(byte[],OutputStream,int,int,boolean,int,int,String)
     */
	public void writeInfo(byte markerData[], OutputStream out, int op, int options, boolean modifyImageInfo) throws IOException {
		writeInfo(markerData, out, op, options, modifyImageInfo, -1, -1);
	}

    /**
     * writeInfo method using default encoding of ISO8859_1
	 * @see #writeInfo(byte[],OutputStream,int,int,boolean,int,int,String)
     */
	public void writeInfo(byte markerData[], OutputStream out, int op, int options, boolean modifyImageInfo, int imageWidth, int imageHeight) throws IOException {
		writeInfo(markerData, out, op, options, modifyImageInfo, imageWidth, imageHeight, "ISO8859_1");
	}

    /**
     * Writes modified or not Exif to out. APP header and its length are not
     * included so any wrapper should do that calculation.<p>
     *
     * This method is mainly for use by LLJTran to regenerate the Appx marker
     * Data for the imageInfo. The default implementation does nothing and
     * is expected to be implemented by the deriving class.
     *
     * @param markerData The existing markerData
     * @param out Output Stream to write out the new markerData
     * @param op The transformation option. This is used to switch the width and
     * height in imageInfo if op is a ROT_90 like transform and transform
     * the orientation tag and Thumbnail if opted for.
     * @param options OPT_XFORM_.. options of LLJTran. LLJTran passes its
     * options directly to this method. This uses the imageInfo related flags
     * {@link LLJTran#OPT_XFORM_THUMBNAIL} and
     * {@link LLJTran#OPT_XFORM_ORIENTATION} and makes the necessary
     * changes to imageInfo depending on the transform specified by <b>op</b>
     * before writing.
     * @param modifyImageInfo If true the changes made to imageInfo are
     * retained, otherwise the state is restored at the end of the call.
     * @param imageWidth Actual Image Width. If this and imageHeight are
     * positive then they are used for the width and height in imageInfo and no
     * switching of width and height is done for ROT_90 like transforms.
     * @param imageHeight Actaul Image Height
     * @param encoding Encoding to be used when for writing out Character
     * information like comments.
	 */
	public void writeInfo(byte markerData[], OutputStream out, int op, int options, boolean modifyImageInfo, int imageWidth, int imageHeight, String encoding) throws IOException {
	}

    /**
     * Reads the imageInfo from the Input supplied in Constructor. This is for
     * derived class to implement.
     */
	public abstract void readInfo() throws FileFormatException;

	public abstract String getFormat();

	public abstract int getResolutionX();

	public abstract int getResolutionY();

	public abstract String getMake();

	public abstract String getModel();

	public abstract String getDataTimeOriginalString();

	public abstract float getFNumber();

	public abstract Rational getShutter();

	public abstract boolean isFlash();

	public abstract String getQuality();

	public abstract float getFocalLength();

	public abstract int getMetering(); // matrix, dot, CenterWeightedAverage..

	public abstract int getExpoProgram(); // full automatic, ...

	public abstract String getReport();

    /**
     * Method to get the offset of the Thumbnail within the imageInfo data.<p>
     *
     * The default implementation returns -1 since this method is expected to be
     * implemented by the deriving class.
     *
     * @return Offset of the Thumbnail within the Appx marker data
     */
    public int getThumbnailOffset()
    {
        return -1;
    }

    /**
     * Method to get the length of the Thumbnail.<p>
     *
     * The default implementation returns -1 since this method is expected to be
     * implemented by the deriving class.
     *
     * @return Length of the Thumbnail
     */
    public int getThumbnailLength()
    {
        return -1;
    }

    /**
     * Method to write the imageInfo with a new Thumbnail.<p>
     *
     * This method changes the imageInfo for the new Thumbnail and writes out
     * the corresponding Appx header data (without jpeg markers) with the new
     * Thumbnail.<p>
     *
     * The default implementation does nothing since this method is expected to be
     * implemented by the deriving class.
     *
     * @param newThumbnailData New Thumbnail image data
     * @param startIndex Offset within newThumbnailData where the image starts
     * @param len Length of Thumbnail Image
     * @param thumbnailExt Extension of the Thumbnail Image from
     * {@link ImageResources#EXT_JPEG ImageResources}
     * which identifies the format of the Thumbnail image.
     * @param newAppHdrOp Output to write out the new Appx data
     * @return True if successful, false otherwise
     */
    public boolean setThumbnail(byte newThumbnailData[], int startIndex,
            int len, String thumbnailExt, OutputStream newAppHdrOp)
    throws IOException
    {
        return false;
    }

    /**
     * Removes the Thumbnail Tags in the imageInfo. Thus the next time the Appx
     * is written using
     * {@link #writeInfo(byte[],OutputStream,int,int,boolean,int,int,String) writeInfo(..)}
     * it will be without a Thumbnail.
     * @return True if successful. False if failed or if the feature is not
     * supported.  Since this method should be implemented by Subclasses the
     * default implementation just returns false.
     */
    public boolean removeThumbnailTags()
    {
        return false;
    }

	public abstract Icon getThumbnailIcon(Dimension size);

	public String toString() {
		String result = getReport();
		if (result != null && result.length() > 0)
			return result;
		return super.toString();
	}
	/** returns for format such attributes as: title, artist, album, year, file
	 */
	public Object[] getFiveMajorAttributes() {
		return fiveObjects;
	}

	public Icon getThumbnailIcon() {
		return getThumbnailIcon(null);
	}

    /**
     * Gets the extension of the Thumbnail Image format.
     * Returns null if the image has no Thumbnail.
     * @return Thumbnail Extension as defined in ImageResources. The default
     * implementation returns ImageResources.EXT_JPEG to indicate JPEG format.
     * @see ImageResources
     */
	public String getThumbnailExtension() {
		return ImageResources.EXT_JPEG;
	}

	public String getComments() {
		return comments;
	}

    public File getImageFile() {
        return format.getFile();
    }

	/** saves thumbnail image to specified path
	 */
	public boolean saveThumbnailImage(OutputStream os/*, Dimension size*/) throws IOException {
		if (os == null)
			return false;
		if (getAdvancedImage() != null) {
			try {
				// try advanced image API
				getAdvancedImage().saveThumbnailImage(getImageFile().getPath(), os, null);
				return true;
			} catch(Throwable e) {
			}
		}
		return false;
	}

	public Date getDateTimeOriginal() {
		try {
			return dateformat.parse(getDataTimeOriginalString());
		} catch (NullPointerException e) {
		} catch (ParseException e) {
			  System.err.println(""+e);
		}
		return new Date();
	}

	// conversions
	public float apertureToFnumber(float aperture) {
		try {
			int si = (int)aperture;
			float result = (float)AV_TO_FSTOP[si];
			aperture -= si;
			if (aperture != 0)
				result += (AV_TO_FSTOP[si+1]-AV_TO_FSTOP[si])*aperture;
			return result;
		} catch(ArrayIndexOutOfBoundsException e) {
		}
		return -1;
	}
	// interface AbstractInfo

	public void setAttribute(String name, Object value) {
		if (COMMENTS.equals(name))
			comments = value.toString();
		else
			throw new RuntimeException("Calling this method not allowed by AbstractImageInfo implementation.");
	}

	public Object getAttribute(String name) {
		// TODO: get index from lookup map and use switch
		if (ESS_CHARACHTER.equals(name))
			return getShutter();
		else if (ESS_TIMESTAMP.equals(name))
			return getDateTimeOriginal();
		else if (ESS_QUALITY.equals(name))
			return getQuality();
		else if (ESS_MAKE.equals(name))
			return getMake();
		else
			return getGenericAttribute(name);
	}

	public int getIntAttribute(String name) {
		if (ESS_CHARACHTER.equals(name))
			return (int)getFocalLength();
		else {
			Object result = getGenericAttribute(name);
			if (result != null) {
				if (result instanceof Integer)
					return ((Integer)result).intValue();
			} else
				return 0;
		}
		throw new IllegalArgumentException("Not supported attribute name for int "+name);
	}

	public float getFloatAttribute(String name) {
		if (ESS_CHARACHTER.equals(name))
			return getFNumber();
		else {
			Object result = getGenericAttribute(name);
			if (result != null) {
				if (result instanceof Float)
					return ((Float)result).floatValue();
			} else
				return 0;
		}
		throw new IllegalArgumentException("Not supported attribute name for float "+name);
	}

	public long getLongAttribute(String name) {
		throw new IllegalArgumentException("Not supported attribute name for long "+name);
	}

	public double getDoubleAttribute(String name) {
		throw new IllegalArgumentException("Not supported attribute name for double "+name);
	}

	public boolean getBoolAttribute(String name) {
		if (ESS_CHARACHTER.equals(name))
			return isFlash();
		return getGenericBoolAttribute(name).booleanValue();
	}

	protected Object getGenericAttribute(String name) {
		try {
			return getClass().getMethod("get"+name, EMPTY_PARAMS).invoke(this, (Object [])EMPTY_PARAMS);
		} catch(Throwable t) {
			throw new IllegalArgumentException("Not supported attribute "+name+" <<"+t);
		}
	}

	protected Boolean getGenericBoolAttribute(String name) {
		try {
			return (Boolean)getClass().getMethod("is"+name, EMPTY_PARAMS).invoke(this, (Object [])EMPTY_PARAMS);
		} catch(Throwable t) {
			try {
				return (Boolean)getGenericAttribute(name);
			} catch(Throwable t2) {
				throw new IllegalArgumentException("Not supported boolean attribute "+name+" <<"+t2+" <<"+t);
			}
		}
	}

	transient protected InputStream is;
	protected int offset;
	protected String name, comments;
	protected Object [] fiveObjects = new Object[5];
}
