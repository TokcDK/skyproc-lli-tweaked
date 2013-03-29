/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skyproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.DataFormatException;
import lev.LChannel;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

/**
 *
 * @author Justin Swanson
 */
public class SubListMulti<T extends SubRecord> extends SubList {

    Map<String, SubRecord> prototypes = new HashMap<>(2);

    SubListMulti(SubRecord ... prototypes) {
	super(prototypes[0]);
	for (SubRecord s : prototypes) {
	    this.prototypes.put(s.getType(), s);
	}
    }

    SubListMulti(SubListMulti<T> rhs) {
	super(rhs);
	for (SubRecord s : rhs.prototypes.values()) {
	    this.prototypes.put(s.getType(), s);
	}
    }

    @Override
    void parseData(LChannel in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
	parseData(in, srcMod, getNextType(in));
    }

    @Override
    void parseData(LChannel in, Mod srcMod, String nextType) throws BadRecord, DataFormatException, BadParameter {
	if (prototypes.containsKey(nextType)) {
	    SubRecord newRecord = prototypes.get(nextType).getNew(nextType);
	    newRecord.parseData(in, srcMod);
	    add(newRecord);
	} else {
	    get(size() - 1).parseData(in, srcMod);
	}
    }

    @Override
    ArrayList<String> getTypes() {
	HashSet<String> set = new HashSet<>();
	for (SubRecord s : prototypes.values()) {
	    set.addAll(s.getTypes());
	}
	return new ArrayList<>(set);
    }

    @Override
    SubRecord getNew(String type) {
	return new SubListMulti(this);
    }
}