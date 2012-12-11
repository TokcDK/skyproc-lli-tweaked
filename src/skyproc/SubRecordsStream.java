package skyproc;


import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import lev.LChannel;
import lev.LFileChannel;
import lev.Ln;
import skyproc.MajorRecord;
import skyproc.SubRecordsDerived;
import skyproc.SubPrototype;
import skyproc.Type;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author Justin Swanson
 */
class SubRecordsStream extends SubRecordsDerived {

    protected Map<Type, RecordLocation> pos = new HashMap<>(0);
    MajorRecord major;

    SubRecordsStream(SubPrototype proto) {
	super(proto);
    }

    @Override
    public void setMajor(MajorRecord in) {
	major = in;
    }

    @Override
    public boolean shouldExport(Type t) {
	if (map.containsKey(t)) {
	    return shouldExport(map.get(t));
	} else if (pos.containsKey(t)) {
	    SubRecord s = get(t);
	    return shouldExport(s);
	} else {
	    return shouldExport(prototype.get(t));
	}
    }

    @Override
    public SubRecord get(Type in) {
	SubRecord s = null;
	if (map.containsKey(in)) {
	    s = map.get(in);
	} else if (prototype.contains(in)) {
	    s = createFromPrototype(in);
	    try {
		loadFromPosition(s);
	    } catch (Exception ex) {
		SPGlobal.logException(ex);
		return s;
	    }
	    standardize(s);
	}
	return s;
    }

    @Override
    public void remove(Type in) {
	super.remove(in);
	if (pos.containsKey(in)) {
	    pos.remove(in);
	}
    }

    void standardize(SubRecord record) {
	record.standardize(major);
	record.fetchStringPointers(major);
    }

    @Override
    void importSubRecord(LChannel in) throws BadRecord, DataFormatException, BadParameter {
	Type nextType = Record.getNextType(in);
	if (nextType == Type.NAM1) {
	    int wer = 23;
	}
	if (contains(nextType)) {
	    if (SPGlobal.streamMode && (in instanceof RecordShrinkArray || in instanceof LFileChannel)) {
		Type standardType = prototype.get(nextType).getType();
		if (!pos.containsKey(standardType)) {
		    long position = in.pos();
		    pos.put(standardType, new RecordLocation(position));
		    if (SPGlobal.logging()) {
			SPGlobal.logSync(nextType.toString(), nextType.toString() + " is at position: " + Ln.printHex(position));
		    }
		} else {
		    pos.get(standardType).num++;
		}
		in.skip(prototype.get(nextType).getRecordLength(in));
	    } else {
		SubRecord record = getSilent(nextType);
		record.parseData(record.extractRecordData(in));
		standardize(record);
	    }
	} else {
	    throw new BadRecord(getTypes().get(0).toString() + " doesn't know what to do with a " + nextType.toString() + " record.");
	}
    }

    public SubRecord getSilent(Type nextType) {
	if (map.containsKey(nextType)) {
	    return map.get(nextType);
	} else {
	    return createFromPrototype(nextType);
	}
    }

    void loadFromPosition(SubRecord s) throws BadRecord, BadParameter, DataFormatException {
	if (SPGlobal.streamMode) {
	    RecordLocation position = pos.get(s.getType());
	    if (position != null) {
		try {
		    major.srcMod.input.pos(position.pos);
		    if (SPGlobal.logging()) {
			if (!major.equals(SPGlobal.lastStreamed)) {
			    SPGlobal.logSync("Stream", "Streaming from " + major);
			    SPGlobal.lastStreamed = major;
			}
		    }
		    for (int i = 0; i < position.num; i++) {
			s.parseData(s.extractRecordData(major.srcMod.input));
		    }
		    pos.remove(s.getType());
		} catch (Exception e) {
		    SPGlobal.logError("Stream Error", "Error streaming subrecord type " + s.getType() + " from " + major);
		    throw e;
		}
	    }
	}
    }

    protected static class RecordLocation {

	long pos;
	int num = 1;

	RecordLocation(long pos) {
	    this.pos = pos;
	}
    }
}