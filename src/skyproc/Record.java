package skyproc;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.DataFormatException;
import lev.*;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

/**
 * Abstract class outlining functions for a generic record.
 *
 * @author Justin Swanson
 */
public abstract class Record implements Serializable {

    final static HashMap<String, Type> allTypes;

    static {
	Type[] ta = Type.values();
	allTypes = new HashMap<>(ta.length);
	for (Type t : ta) {
	    allTypes.put(t.toString(), t);
	}
    }

    Record() {
    }

    void parseData(LChannel in) throws BadRecord, BadParameter, DataFormatException {
	in.skip(getIdentifierLength() + getSizeLength());
    }

    final void parseData(ByteBuffer in) throws BadRecord, BadParameter, DataFormatException {
	parseData(new LShrinkArray(in));
    }

    Boolean isValid() {
	return true;
    }

    /**
     * Returns a short summary/title of the record.
     *
     * @return A short summary/title of the record.
     */
    @Override
    public abstract String toString();

    /**
     * Returns the contents of the record, or exports them to a log, depending
     * on the record type.
     *
     * @return The contents of the record, OR the empty string, depending on the
     * record type.
     */
    public abstract String print();

    abstract Type[] getTypes();

    Type getType() {
	return getTypes()[0];
    }

    Record getNew() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    static Type matchType(String str) throws BadRecord {
	Type out = allTypes.get(str);
	if (out != null) {
	    return out;
	}
	throw new BadRecord("Record " + str + " ("
		+ Ln.printHex(Ln.toIntArray(str), true, false) + ") Could not be found on the type list.");
    }

    static Type getNextType(LChannel in) throws BadRecord {
	return (matchType(Ln.arrayToString(in.getInts(0, 4))));
    }

    void export(LExporter out, Mod srcMod) throws IOException {
	if (isValid()) {
	    out.write(getTypes()[0].toString());
	    out.write(getContentLength(srcMod));
	}
    }

    /**
     *
     * @param in
     * @return
     */
    public int getRecordLength(LChannel in) {
	return Ln.arrayToInt(in.getInts(getIdentifierLength(), getSizeLength()))
		+ getSizeLength() + getIdentifierLength() + getFluffLength();
    }

    LChannel extractRecordData(LChannel in) {
	return extractData(in, getRecordLength(in));
    }

    LChannel extractData(LChannel in, int size) {
	LChannel extracted;
	if (SPGlobal.streamMode && (in instanceof RecordShrinkArray || in instanceof LFileChannel)) {
	    extracted = new RecordShrinkArray(in, size);
	} else {
	    extracted = new LShrinkArray(in, size);
	}
	in.skip(size);
	return extracted;
    }

    int getHeaderLength() {
	return getIdentifierLength() + getSizeLength() + getFluffLength();
    }

    final int getIdentifierLength() {
	return 4;
    }

    abstract int getSizeLength();

    abstract int getFluffLength();

    int getTotalLength(Mod srcMod) {
	return getContentLength(srcMod) + getHeaderLength();
    }

    abstract int getContentLength(Mod srcMod);

    void newSyncLog(String fileName) {
	SPGlobal.newSyncLog(fileName);
    }

    boolean logging() {
	return SPGlobal.logging();
    }

    void logMain(String header, String... log) {
	SPGlobal.logMain(header, log);
    }

    void logSync(String header, String... log) {
	SPGlobal.logSync(getTypes()[0].toString(), log);
    }

    void logError(String header, String... log) {
	SPGlobal.logError(header, log);
    }

    void logSpecial(Enum e, String header, String... log) {
	SPGlobal.logSpecial(e, header, log);
    }

    void log(String header, String... log) {
	SPGlobal.log(header, log);
    }

    void newLog(String fileName) {
	SPGlobal.newLog(fileName);
    }

    void flush() {
	SPGlobal.flush();
    }
}
