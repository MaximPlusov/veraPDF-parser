package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class COSStream extends COSDictionary {

	private static final Logger LOGGER = Logger.getLogger(COSStream.class.getCanonicalName());

	private ASInputStream stream;
	private FilterFlags flags;

	private boolean streamKeywordCRLFCompliant = true;
	private boolean endstreamKeywordCRLFCompliant = true;
	private long realStreamSize;

	protected COSStream() {
		super();
		this.flags = FilterFlags.RAW_DATA;

		setIndirectLength(0);
	}

	protected COSStream(final ASInputStream stream) {
		super();
		this.stream = stream;
		this.flags = FilterFlags.RAW_DATA;

		setIndirectLength(0);
	}

	protected COSStream(final String string) {
		super();
		this.stream = new ASMemoryInStream(string.getBytes());
		this.flags = FilterFlags.RAW_DATA;
	}

	protected COSStream(final COSDictionary dictionary) {
		super(dictionary);
	}

	protected COSStream(final COSDictionary dictionary, final ASInputStream stream, final FilterFlags flags) {
		super(dictionary);
		this.stream = stream;
		this.flags = flags;
	}

	protected COSStream(final COSDictionary dictionary, final String string, final FilterFlags flags) {
		super(dictionary);
		this.stream = new ASMemoryInStream(string.getBytes());
		this.flags = flags;
	}

	public static COSObject construct() {
		return new COSObject(new COSStream());
	}

	public static COSObject construct(final ASInputStream stream) {
		return new COSObject(new COSStream(stream));
	}

	public static COSObject construct(final String string) {
		return new COSObject(new COSStream(string));
	}

	public static COSObject construct(final COSDictionary dictionary) {
		return new COSObject(new COSStream(dictionary));
	}

	public static COSObject construct(final COSDictionary dictionary, final ASInputStream stream) {
		return construct(dictionary, stream, FilterFlags.RAW_DATA);
	}

	public static COSObject construct(final COSDictionary dictionary, final ASInputStream stream, final FilterFlags flags) {
		return new COSObject(new COSStream(dictionary, stream, flags));
	}

	public static COSObject construct(final COSDictionary dictionary, final String string) {
		return construct(dictionary, string, FilterFlags.RAW_DATA);
	}

	public static COSObject construct(final COSDictionary dictionary, final String string, final FilterFlags flags) {
		return new COSObject(new COSStream(dictionary, string, flags));
	}

	@Override
	public COSObjType getType() {
		return COSObjType.COS_STREAM;
	}

	@Override
	public void accept(final IVisitor visitor) {
		visitor.visitFromStream(this);
	}

	@Override
	public Object accept(final ICOSVisitor visitor) {
		return visitor.visitFromStream(this);
	}

	@Override
	public ASInputStream getData() {
		return getData(FilterFlags.RAW_DATA);
	}

	@Override
	public ASInputStream getData(final FilterFlags filterFlags) {
		try {
			if (filterFlags == FilterFlags.RAW_DATA || this.flags != FilterFlags.RAW_DATA) {
				this.stream.reset();
				return this.stream;
			}
			ASInputStream result = getFilters().getInputStream(stream, this.getKey(ASAtom.DECODE_PARMS));
			result.reset();
			return result;
		} catch (IOException e) {
			LOGGER.log(Level.FINE, "Can't get stream data", e);
			return null;
		}
	}

	@Override
	public boolean setData(final ASInputStream stream) {
		return setData(stream, FilterFlags.RAW_DATA);
	}

	@Override
	public boolean setData(final ASInputStream stream, FilterFlags flags) {
		this.stream = stream;
		this.flags = flags;
		return true;
	}

	@Override
	public Boolean isStreamKeywordCRLFCompliant() {
		return Boolean.valueOf(streamKeywordCRLFCompliant);
	}

	@Override
	public boolean setStreamKeywordCRLFCompliant(boolean streamKeywordCRLFCompliant) {
		this.streamKeywordCRLFCompliant = streamKeywordCRLFCompliant;
		return true;
	}

	@Override
	public Boolean isEndstreamKeywordCRLFCompliant() {
		return Boolean.valueOf(endstreamKeywordCRLFCompliant);
	}

	@Override
	public boolean setEndstreamKeywordCRLFCompliant(boolean endstreamKeywordCRLFCompliant) {
		this.endstreamKeywordCRLFCompliant = endstreamKeywordCRLFCompliant;
		return true;
	}

	@Override
	public Long getRealStreamSize() {
		return Long.valueOf(realStreamSize);
	}

	@Override
	public boolean setRealStreamSize(long realStreamSize) {
		this.realStreamSize = realStreamSize;
		return true;
	}

	public COSFilters getFilters() {
		return new COSFilters(getKey(ASAtom.FILTER));
	}

	public void setFilters(final COSFilters filters) {
		setKey(ASAtom.FILTER, filters.getObject());
	}

	public FilterFlags getFilterFlags() {
		return this.flags;
	}

	public void setFilterFlags(final FilterFlags flags) {
		this.flags = flags;
	}

	public long getLength() {
		return getIntegerKey(ASAtom.LENGTH).longValue();
	}

    public void setLength(final long length) {
		setIntegerKey(ASAtom.LENGTH, length);
	}

	public void setIndirectLength(final long length) {
		COSObject obj = getKey(ASAtom.LENGTH);
		obj.setInteger(length);
		if (obj.isIndirect().booleanValue()) {
			obj = COSIndirect.construct(obj);
			setKey(ASAtom.LENGTH, obj);
		}
	}

	public enum FilterFlags {
		RAW_DATA,
		DECODE,
		DECRYPT,
		DECRYPT_AND_DECODE
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof COSStream)) return false;
		if (!super.equals(o)) return false;

		COSStream cosStream = (COSStream) o;

		if (streamKeywordCRLFCompliant != cosStream.streamKeywordCRLFCompliant) return false;
		if (endstreamKeywordCRLFCompliant != cosStream.endstreamKeywordCRLFCompliant) return false;
		if (realStreamSize != cosStream.realStreamSize) return false;
		try {
			if (stream != null ? !equalsStreams(stream, cosStream.stream) : cosStream.stream != null) return false;
		} catch (IOException e) {
			LOGGER.log(Level.FINE, "Exception during comparing streams", e);
			return false;
		}
		return flags == cosStream.flags;
	}

	private static boolean equalsStreams(ASInputStream first, ASInputStream second) throws IOException {
		first.reset();
		second.reset();
		byte[] tempOne = new byte[1024];
		byte[] tempTwo = new byte[1024];
		int readFromOne;
		int readFromTwo;
		do {
			readFromOne = first.read(tempOne, tempOne.length);
			readFromTwo = second.read(tempTwo, tempTwo.length);
			if (readFromOne != readFromTwo || !Arrays.equals(tempOne, tempTwo)) {
				return false;
			}
		} while (readFromOne != -1);

		return true;
	}
}
