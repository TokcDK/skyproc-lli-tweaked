/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LeveledListInjector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import skyproc.*;
import skyproc.exceptions.BadParameter;
import skyproc.genenums.ArmorType;
import skyproc.genenums.FirstPersonFlags;

/**
 *
 * @author David Tynan
 * Updated to 0.7LE+ by TokcDK
 */
public class ArmorTools {

    public static ArrayList<Pair<KYWD, KYWD>> armorMatches;
    private static ArrayList<ArrayList<FormID>> armorVariants = new ArrayList<>(0);
    private static ArrayList<Pair<KYWD, ArrayList<ARMO>>> matchingSets = new ArrayList<>(0);
    //private static Mod merger;
    //private static Mod patch;

    public static class Pair<L, R> {

        private L l;
        private R r;

        public Pair(L l, R r) {
            this.l = l;
            this.r = r;
        }

        public L getBase() {
            return l;
        }

        public R getVar() {
            return r;
        }

        public void setBase(L l) {
            this.l = l;
        }

        public void setVar(R r) {
            this.r = r;
        }
    }

//    public static void setMergeAndPatch(Mod m, Mod p) {
//        merger = m;
//        patch = p;
//    }
    static void buildOutfitsArmors(FLST baseArmorKeysFLST, Mod merger, Mod patch) throws BadParameter {
//        FormID curForm;//Эти три вроде не используются asdf
//        ARMO curARMO;
//        OTFT curOTFT;

//        FormID f = new FormID("107347", "Skyrim.esm");
        //SPGlobal.log("outfits glist", f.toString());
//        LVLI glist = (LVLI) merger.getMajor(f, GRUP_TYPE.LVLI);
        //SPGlobal.log("outfits glist", glist + "");
        //setupSets(merger, patch);

//        glist.set(LeveledRecord.LVLFlag.UseAll, false);
        for (OTFT lotft : merger.getOutfits()) {
            String lotftName = lotft.getEDID();
            boolean tiered = isTiered(lotftName);
            if (tiered && LeveledListInjector.save.getBool(YourSaveFile.Settings.USE_MATCHING_OUTFITS)) {
                //lotft.clearInventoryItems();
                ArrayList<FormID> inv = lotft.getInventoryList();
                for (FormID form : inv) {
                    if (form.getMaster().print().startsWith("Skyrim")) {
                        lotft.removeInventoryItem(form);
                    }
                }

                String bits = getBits(lotftName);
                LVLI subList = new LVLI("DienesLVLI" + lotftName + bits + "List");
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                String tierKey = getTierKey(lotftName);


                insertTieredArmors(subList, tierKey, bits, merger, patch);
                try {
                if (subList.getEntry(0).getLevel() > 1) {
                    subList.getEntry(0).setLevel(1);
                }
                } catch (RuntimeException e) {
                    String error = e.getMessage() + "\nOutfit: " + lotft + " is coded to have a matching set but could not find any."
                            + "\nYou are almost certainly not using a tesedit merged patch correctly.";
                    RuntimeException ex = new RuntimeException(error);
                    ex.setStackTrace(e.getStackTrace());
                    throw ex;
                }
                lotft.addInventoryItem(subList.getForm());

                if (needsShield(lotftName)) {
                    lotft.addInventoryItem(shieldForm(lotftName));
                }

                patch.addRecord(subList);
                patch.addRecord(lotft);
            } else {
                ArrayList<FormID> a = lotft.getInventoryList();
                boolean changed = false;
                if (LeveledListInjector.save.getBool(YourSaveFile.Settings.USE_MATCHING_OUTFITS)) {
                    FormID form1;
                    for (int i = 0; i < a.size(); i++) {
                        form1 = a.get(i);
                        ARMO arm = (ARMO) merger.getMajor(form1, GRUP_TYPE.ARMO);
                        if (arm != null) {
                            KYWD k = hasKeyStartsWith(arm, "dienes_outfit", merger);
                            if (k != null) {
                                ArrayList<ARMO> b = getAllWithKey(k, a, merger);
                                if (b.size() > 1) {
                                    String lvliName = getNameFromArrayWithKey(b, k, merger);
                                    LVLI list = (LVLI) patch.getMajor(lvliName, GRUP_TYPE.LVLI);
                                    if (list != null) {
                                        for (ARMO arm2 : b) {
                                            //asdf
                                            lotft.removeInventoryItem(arm2.getForm());
                                        }
                                        lotft.addInventoryItem(list.getForm());
                                    } else {
                                        for (ARMO arm2 : b) {
                                            lotft.removeInventoryItem(arm2.getForm());
                                        }
                                        LVLI newList = new LVLI(lvliName);
                                        newList.set(LeveledRecord.LVLFlag.UseAll, true);
                                        newList.set(LeveledRecord.LVLFlag.CalcAllLevelsEqualOrBelowPC, false);
                                        newList.set(LeveledRecord.LVLFlag.CalcForEachItemInCount, false);
//                                LVLI subList = (LVLI) patch.makeCopy(glist, lvliName.replace("Outfit", "OutfitSublist"));
//                                addArmorFromArray(subList, b);
//                                patch.addRecord(subList);
//                                newList.addEntry(subList.getForm(), 1, 1);
                                        addAlternateOutfits(newList, b, merger, patch);

                                        lotft.addInventoryItem(newList.getForm());
                                        patch.addRecord(newList);

                                    }
                                    changed = true;
                                    i = -1;
                                    a = lotft.getInventoryList();
                                }
                            }
                        }
                    }
                }

                //matching set armor moved to sublist, link any remaining weapons or armor
                //first refresh whats in the outfit
                a = lotft.getInventoryList();
                for (FormID form : a) {
                    ARMO obj = (ARMO) merger.getMajor(form, GRUP_TYPE.ARMO);
                    if (obj != null) {
                        KYWD baseKey = armorHasAnyKeyword(obj, baseArmorKeysFLST, merger);
                        //asdf
                        if ((baseKey != null) && (hasVariant(obj))) {
                            String eid = "DienesLVLI" + obj.getEDID();
                            //SPGlobal.log("Line 162 eid=", eid);//asdf
                            MajorRecord r = merger.getMajor(eid, GRUP_TYPE.LVLI);
                            if (r == null) {
                                LVLI subList = new LVLI(eid);
                                InsertArmorVariants(subList, form);
                                patch.addRecord(subList);
                                lotft.removeInventoryItem(form);
                                lotft.addInventoryItem(subList.getForm());
                                changed = true;
                            } else {
                                lotft.removeInventoryItem(form);
                                lotft.addInventoryItem(r.getForm());
                                changed = true;
                            }
                        }
                    }
                }
                if (changed) {
                    patch.addRecord(lotft);
                }
            }
        }
    }

    public static ArrayList<FormID> containsArmorSet(ArrayList<FormID> inventory, Mod merger) {
        ArrayList<FormID> set = new ArrayList<>(0);
        ArrayList<String> suffixes = new ArrayList<>(Arrays.asList("Boots", "Cuirass", "Gauntlets", "Helmet", "Shield"));//asdfe
        boolean matchFound = false;
        for (int count = 0; count < inventory.size() && matchFound == false; count++) {
            ARMO obj = (ARMO) merger.getMajor(inventory.get(count), GRUP_TYPE.ARMO);
            if (obj != null) {
                String armorType;
                String name = obj.getEDID();
                if (name.startsWith("Ench")) {
                    name = name.substring(4);
                }
                int i;
                for (String s : suffixes) {
                    i = name.indexOf(s);
                    //SPGlobal.log("active Mod");
                    if (i > 0) {
                        name = name.substring(0, i);
                        //SPGlobal.log("Line 203 containsArmorSet  name=", name);
                    }
                }
                armorType = name;
                for (int rest = count; rest < inventory.size(); rest++) {
                    ARMO other = (ARMO) merger.getMajor(inventory.get(count), GRUP_TYPE.ARMO);
                    if (other != null) {
                        String compare = other.getEDID();
                        if (compare.contains(armorType)) {
                            set.add(other.getForm());
                            matchFound = true;
                        }
                    }
                }
                if (matchFound) {
                    set.add(obj.getForm());
                }
            }
        }
        return set;
    }

    private static ArrayList<FormID> lvliContainsArmorSet(LVLI llist, Mod merger) {
        ArrayList<FormID> contentForms = new ArrayList<>(0);
        ArrayList<LeveledEntry> levContents = llist.getEntries();

        for (LeveledEntry levEntry : levContents) {
            contentForms.add(levEntry.getForm());
        }
        ArrayList<FormID> ret = containsArmorSet(contentForms, merger);

        return ret;
    }

    static void linkLVLIArmors(FLST baseArmorKeysFLST, Mod merger, Mod patch) {
//        FormID f = new FormID("107347", "Skyrim.esm");
//        LVLI glist = (LVLI) merger.getMajor(f, GRUP_TYPE.LVLI);
//        glist.set(LeveledRecord.LVLFlag.UseAll, false);

        for (LVLI llist : merger.getLeveledItems()) {
            //SPGlobal.log("Link Armor List", llist.getEDID());

//            //check if LVLI is one we made
            boolean found = false;
            if (llist.getEDID().startsWith("DienesLVLI")) {
                found = true;
            }
//            for (FormID set : matchingSetVariants) {
//                if (llist.getForm().equals(set)) {
//                    found = true;
//                }
//            }


            if (found == false) {

                boolean changed = false;
//                if (llist.get(LeveledRecord.LVLFlag.UseAll)) {
//                    //remove any matching outfits
//                    //ArrayList<FormID> set = lvliContainsArmorSet(llist, merger);
//                    while (set.size() > 0) {
//                        linkArmorSet(llist, set, merger, patch);
//                        set = lvliContainsArmorSet(llist, merger);
//                        changed = true;
//                    }
//                }
                //SPGlobal.log(llist.getEDID(), "num entries" + llist.numEntries());
                for (int i = 0; i < llist.numEntries(); i++) {
                    LeveledEntry entry = llist.getEntry(i);
                    FormID test = entry.getForm();
                    //SPGlobal.log("list entry " + i, entry.getForm() + "");
                    ARMO obj = (ARMO) merger.getMajor(test, GRUP_TYPE.ARMO);
                    if (obj != null) {
                        //SPGlobal.log("list entry " + i, obj.getEDID());
                        KYWD base = armorHasAnyKeyword(obj, baseArmorKeysFLST, merger);

                        boolean hasVar = hasVariant(obj);
                        if ((base != null) && (hasVar)) {
                            //SPGlobal.log(obj.getEDID(), "has keyword" + base);

                            String eid = "DienesLVLI" + obj.getEDID();
                            MajorRecord r;

                            r = merger.getMajor(eid, GRUP_TYPE.LVLI);
                            if (r == null) {
                                r = patch.getMajor(eid, GRUP_TYPE.LVLI);
                            }
                            if (r == null) {
                                //SPGlobal.log(obj.getEDID(), "new sublist needed");
                                LVLI subList = new LVLI(eid);
                                InsertArmorVariants(subList, entry.getForm());
                                patch.addRecord(subList);
                                llist.removeEntry(i);
                                llist.addEntry(new LeveledEntry(subList.getForm(), entry.getLevel(), entry.getCount()));
                                i = -1;
                                changed = true;
                            } else {
                                //SPGlobal.log(obj.getEDID(), "sublist found " + r.getEDID());
                                llist.removeEntry(i);
                                llist.addEntry(new LeveledEntry(r.getForm(), entry.getLevel(), entry.getCount()));
                                changed = true;
                                i = -1;
                            }
                        }
                    }
                }
                if (changed) {
                    patch.addRecord(llist);
                }
            }
        }
    }

    static void buildArmorBases(Mod merger, FLST baseKeys) {
        for (ARMO armor : merger.getArmors()) {
            KYWD baseKey = armorHasAnyKeyword(armor, baseKeys, merger);
            if (baseKey != null) {
                //SPGlobal.log(armor.getEDID(), "is base armor");
                ArrayList<FormID> alts = new ArrayList<>(0);
                alts.add(0, armor.getForm());
                armorVariants.add(alts);
            }
        }
    }

    static void buildArmorVariants(Mod merger, Mod patch, FLST baseKeys, FLST varKeys) {
        ////SPGlobal.log("line 329 Build Variants", "Building Base Armors");
        //buildArmorBases(merger, baseKeys);
        //SPGlobal.log(" line 331 Build Variants", "Building Variant Armors");

        for (ARMO armor : merger.getArmors()) {
            //SPGlobal.log("armor", armor.getEDID());
            KYWD variantKey = armorHasAnyKeyword(armor, varKeys, merger);
            if (variantKey != null) {
                //SPGlobal.log(armor.getEDID(), "is variant");
                FormID ench = armor.getEnchantment();
                if (ench.isNull()) {
                    for (int j = 0; j < armorVariants.size(); j++) {
                        ArrayList<FormID> a2 = armorVariants.get(j);
                        ARMO form = (ARMO) merger.getMajor((FormID) a2.get(0), GRUP_TYPE.ARMO);

                        boolean passed = true;
                        ////SPGlobal.log("line 345 comparing to", form.getEDID());


                        if (armorHasKeyword(form, getBaseArmor(variantKey), merger)) {

                            //SPGlobal.log(form.getEDID(), "has base keyword line 350");

                            ARMO replace = form;
                            FormID tmp = replace.getTemplate();
                            if (!tmp.isNull()) {
                                replace = (ARMO) merger.getMajor(tmp, GRUP_TYPE.ARMO);
                            }
                            for (skyproc.genenums.FirstPersonFlags c : skyproc.genenums.FirstPersonFlags.values()) {
                                //skyproc.genenums.FirstPersonFlags[] test = skyproc.genenums.FirstPersonFlags.values();
                                ////SPGlobal.log("line 359 getFlags", c.toString());
                                boolean armorFlag = armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, c);
                                boolean formFlag = replace.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, c);
                                //asdf варианты брони
                                boolean flagMatch = (armorFlag == formFlag);
                                ////SPGlobal.log("line 364 flag match" + c, armorFlag + " " + formFlag + " " + flagMatch);
                                if (flagMatch == false) {
                                    passed = false;
                                }
                            }
                            if (!passed) {
                                KYWD helm = (KYWD) merger.getMajor("ArmorHelmet", GRUP_TYPE.KYWD);
                                if (armorHasKeyword(replace, helm, merger) && armorHasKeyword(armor, helm, merger)) {
                                    passed = true;
                                }
                            }
                            if (passed) {
                                //SPGlobal.log("variant found", armor.getEDID() + " is variant of " + form.getEDID());
                                FormID template = form.getTemplate();
                                //SPGlobal.log("template", template.getFormStr());
                                if (template.isNull()) {
                                    a2.add(armor.getForm());
                                    //SPGlobal.log("variant added", a2.contains(armor.getForm()) + " " + a2.size());
                                } else {
                                    //SPGlobal.log("Enchant found", armor.getEDID() + "  " + form.getEDID());
                                    String name = generateArmorName(armor, form, merger);
                                    String newEdid = generateArmorEDID(armor, form, merger);
                                    ARMO armorDupe = (ARMO) patch.makeCopy(armor, "DienesARMO" + newEdid);
                                    //SPGlobal.log("armor copied", armorDupe.getEDID());
                                    armorDupe.setEnchantment(form.getEnchantment());
                                    SPGlobal.log("line 389 form.getEnchantment()="+form.getEnchantment());
                                    armorDupe.setName(name);
                                    SPGlobal.log("line 391 name="+name);
                                    armorDupe.setTemplate(armor.getForm());
                                    a2.add(armorDupe.getForm());
                                    patch.addRecord(armorDupe);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static String getSetName(ArrayList<FormID> set) {
        String name = String.valueOf(set.hashCode());
        return name;
    }

    static KYWD getBaseArmor(KYWD k) {
        KYWD ret = null;
        for (Pair p : armorMatches) {
            KYWD var = (KYWD) p.getVar();
            //SPGlobal.log("getBaseArmor", k.getEDID() + " " + var.getEDID() + " " + var.equals(k));
            if (var.equals(k)) {
                ret = (KYWD) p.getBase();
            }
        }
        return ret;
    }

    static void setupArmorMatches(FLST base, FLST var, Mod merger) {
        armorMatches = new ArrayList<>();
        ArrayList<FormID> bases = base.getFormIDEntries();
        ArrayList<FormID> vars = var.getFormIDEntries();
        for (int i = 0; i < bases.size(); i++) {
            KYWD newBase = (KYWD) merger.getMajor(bases.get(i), GRUP_TYPE.KYWD);
            KYWD newVar = (KYWD) merger.getMajor(vars.get(i), GRUP_TYPE.KYWD);
            //SPGlobal.log("Armor pair", newBase.getEDID() + " " + newVar.getEDID());
            Pair<KYWD, KYWD> p = new Pair(newBase, newVar);
            armorMatches.add(p);
            //SPGlobal.log("Armor pair", p.getBase().getEDID() + " " + p.getVar().getEDID());
        }
    }

    static String generateArmorEDID(ARMO newArmor, ARMO armor, Mod m) {
//        String name = newArmor.getEDID();
//        String baseName = armor.getEDID();
//        String prefix = "";
//        String suffix = "";
//        ARMO template = (ARMO) m.getMajor(armor.getTemplate(), GRUP_TYPE.ARMO);
//        int prefixLen = baseName.indexOf(template.getEDID());
//        if (prefixLen > 0) {
//            prefix = baseName.substring(0, prefixLen);
//        }
//        int suffixLen = baseName.length() - template.getEDID().length() + prefixLen;
//        if (suffixLen > 0) {
//            suffix = baseName.substring(template.getEDID().length() + prefixLen);
//        }
//        String ret = prefix + name + suffix;
//        return ret;

        String name = newArmor.getEDID();
        String baseName = armor.getEDID();
        String templateName;
        String ret = "";
        ARMO template = (ARMO) m.getMajor(armor.getTemplate(), GRUP_TYPE.ARMO);
        if (template != null) {
            templateName = template.getEDID();
            if (baseName.contains(templateName)) {
                ret = baseName.replace(templateName, name);
            } else {
                String lcseq = lcs(baseName, templateName);
                if (baseName.contains(lcseq)) {
                    ret = baseName.replace(lcseq, name);
                } else {
                    String gcs = longestCommonSubstring(baseName, templateName);
                    ret = baseName.replace(gcs, name);
                }
            }
        }

        return ret;
    }

    static String generateArmorName(ARMO newArmor, ARMO armor, Mod m) {
//        String name = newArmor.getName();
//        String baseName = armor.getName();
//        String prefix = "";
//        String suffix = "";
//        ARMO template = (ARMO) m.getMajor(armor.getTemplate(), GRUP_TYPE.ARMO);
//        //SPGlobal.log(armor.getName(), template.getName());
//        int prefixLen = baseName.indexOf(template.getName());
//        //SPGlobal.log(name, "" + prefixLen);
//        if (prefixLen > 0) {
//            prefix = baseName.substring(0, prefixLen);
//        }
//        int suffixLen = baseName.length() - template.getName().length() + prefixLen;
//        if (suffixLen > 0) {
//            suffix = baseName.substring(template.getName().length() + prefixLen);
//        }
//        String ret = prefix + name + suffix;
//        return ret;


        String name = newArmor.getName();
        String baseName = armor.getName();
        //String s = name.toString("Cp1251");
        //SPGlobal.log("line 496 name="+name,"baseName="+baseName);
        String templateName;
        String ret = "";
        ARMO template = (ARMO) m.getMajor(armor.getTemplate(), GRUP_TYPE.ARMO);
        //SPGlobal.log("line 500 template="+template);
        if (template != null) {
            templateName = template.getName();
            //SPGlobal.log("line 503 templateName="+templateName);
            if (baseName.contains(templateName)) {
                ret = baseName.replace(templateName, name);
                //SPGlobal.log("line 506 ret="+ret,"name="+name);
            } else {

                //К хуям все проверки, если не прошла первая, когда проверяется по шаблону чар, то просто не менять имя
                ret = name;

                /*
                String[] s = baseName.split(" ");
                //String[] s1 = templateName.split(" ");
                String tname = "";
                for (int i = 0; i < s.length; i++) {
                    if (templateName.contains(s[i])){
                            if (i == 0){
                                tname = baseName.replace(s[i], "");
                                //tname += " "+s[i];
                            }else{
                                tname = tname.replace(" "+s[i], "");
                                //tname += s[i];
                            }

                    }
                }

                ret = name + tname.replace("  ", " ");*/
                            //return ret;
                //фыва
                //SPGlobal.log( "baseName.contains(templateName)="+(baseName.contains(templateName)) );
                //ret = name+" ("+baseName+"|"+templateName+")";
                /*
                String lcseq = lcs(baseName, templateName);
                //SPGlobal.log("line 509 lcseq="+lcseq);
                if (lcseq.length() > 1 && baseName.contains(lcseq)) {
                    ret = baseName.replace(lcseq, name);
                    //SPGlobal.log("line 512 ret="+ret,"name="+name, "lcseq="+lcseq);
                } else {
                    String gcs = longestCommonSubstring(baseName, templateName);
                    if (gcs.length() > 1)
                    {
                        ret = baseName.replace(gcs, name);
                        //SPGlobal.log("line 516 gcs="+gcs,"ret="+ret,"name="+name);
                    } else
                    {
                        //Когда функция lcs возвратила пустое значение " "(поэтому добавлена проверка длины возвращаемой строки > 1) оставить имя без изменений
                        ret = name;
                    }

                }*/
            }
        }

        return ret;
    }

    static KYWD armorHasAnyKeyword(ARMO rec, FLST f, Mod m) {
        ArrayList<FormID> a = f.getFormIDEntries();
        KYWD hasKey = null;
        for (int i = 0; i < a.size(); i++) {
            FormID temp = (FormID) a.get(i);
            KYWD armorKey = (KYWD) m.getMajor(temp, GRUP_TYPE.KYWD);
            if (armorHasKeyword(rec, armorKey, m)) {
                hasKey = armorKey;
                //continue;
            }
        }
        //SPGlobal.log("HasAnyKeyword", rec.toString() + " " + hasKey);
        return hasKey;
    }

    static boolean armorHasKeyword(ARMO rec, KYWD varKey, Mod m) {
        ArrayList<FormID> a;
        boolean hasKey = false;
        ARMO replace = rec;
        FormID tmp = replace.getTemplate();
        //SPGlobal.log("hasKeyword", varKey.getEDID() + " " + replace.getEDID() + " " + tmp.getFormStr());
        if (!tmp.isNull()) {
            replace = (ARMO) m.getMajor(tmp, GRUP_TYPE.ARMO);
        }
        //SPGlobal.log(replace.getEDID(), varKey.getEDID());

        KeywordSet k;
        try {
            k = replace.getKeywordSet();
        } catch (Exception e) {
            String error = "Armor: " + rec.getEDID() + ", from " + rec.getFormMaster().toString() + ", has unresolvable template entry: " + tmp.toString();
            SPGlobal.logSpecial(LeveledListInjector.lk.err, "Bad Data", error);
            SPGlobal.logError("ERROR!", error);
            throw (e);
        }
            a = k.getKeywordRefs();
            for (FormID temp : a) {
                KYWD refKey = (KYWD) m.getMajor(temp, GRUP_TYPE.KYWD);
                //SPGlobal.log("formid", temp.toString());
                //SPGlobal.log("KYWD compare", refKey.getEDID() + " " + varKey.getEDID() + " " + (varKey.equals(refKey)));
                if (varKey.equals(refKey)) {
                    hasKey = true;
                }
            }
        return hasKey;
    }

    static private void InsertArmorVariants(LVLI list, FormID base) {
        ArrayList<LeveledEntry> listEntries = list.getEntries();
        ArrayList<FormID> forms = new ArrayList<>(0);
        for (LeveledEntry e : listEntries) {
            FormID f = e.getForm();
            forms.add(f);
        }
        for (ArrayList a : armorVariants) {
            if (a.contains(base)) {
                for (int i = 0; i < a.size(); i++) {
                    FormID f = (FormID) a.get(i);
                    if (!forms.contains(f)) {
                        list.addEntry(new LeveledEntry(f, 1, 1));
                    }
                }
            }
        }
    }

    static private void linkArmorSet(LVLI llist, ArrayList<FormID> set, Mod merger, Mod patch) {
        String eid = "DienesLVLI" + getSetName(set) + "level1";
        LVLI r = (LVLI) merger.getMajor(eid, GRUP_TYPE.LVLI);
        FormID f = new FormID("107347", "Skyrim.esm");
        MajorRecord glist = merger.getMajor(f, GRUP_TYPE.LVLI);

        if (r == null) {
            LVLI setList = (LVLI) patch.makeCopy(glist, eid);
            for (int index = 0; index < set.size(); index++) {
                FormID item = set.get(index);
                ARMO temp = (ARMO) merger.getMajor(item, GRUP_TYPE.ARMO);
                if (temp != null) {
                    setList.addEntry(item, 1, 1);
                    for (int i = 0; i < llist.numEntries(); i++) {
                        FormID tempForm = llist.getEntry(i).getForm();
                        if (item.equals(tempForm)) {
                            llist.removeEntry(i);
                            //continue;
                        }
                    }

                    index = index - 1;
                }
            }
            merger.addRecord(setList);
            llist.addEntry(setList.getForm(), 1, 1);
            //matchingSetVariants.add(setList.getForm());

        } else {
            for (int index = 0; index < set.size(); index++) {
                FormID item = set.get(index);
                for (int i = 0; i < llist.numEntries(); i++) {
                    FormID tempForm = llist.getEntry(i).getForm();
                    if (item.equals(tempForm)) {
                        llist.removeEntry(i);
                        continue;
                    }
                }

                index = index - 1;
            }
            llist.addEntry(r.getForm(), 1, 1);

        }
    }

    private static boolean hasVariant(ARMO base) {
        boolean ret = false;
        for (ArrayList<FormID> vars : armorVariants) {
            //SPGlobal.log("hasVariant", base.getForm() + " " + vars.size());
            boolean contains = vars.contains(base.getForm());
            if ((contains) && ((vars.size() > 1) || LeveledListInjector.listify)) {
//            if (vars.contains(base.getForm())) {
                ret = true;
            }
        }

        return ret;
    }

    public static void modLVLIArmors(Mod merger, Mod patch) {
        for (LVLI llist : merger.getLeveledItems()) {
            String lname = llist.getEDID();
            if (lname.contains("DienesLVLI")) {
                ARMO armor = (ARMO) merger.getMajor(llist.getEntry(0).getForm(), GRUP_TYPE.ARMO);
                if (armor != null) {
                    if (hasVariant(armor)) {
                        InsertArmorVariants(llist, armor.getForm());
                        patch.addRecord(llist);
                    }
                }
            }
        }
    }

    private static String longestCommonSubstring(String s1, String s2) {
        int start = 0;
        int max = 0;
        int s1Length = s1.length();
        int s2Length = s2.length();
        for (int i = 0; i < s1Length; i++) {
            for (int j = 0; j < s2Length; j++) {
                int x = 0;
                while (s1.charAt(i + x) == s2.charAt(j + x)) {
                    x++;
                    if (((i + x) >= s1Length) || ((j + x) >= s2Length)) {
                        break;
                    }
                }
                if (x > max) {
                    max = x;
                    start = i;
                }
            }
        }
        return s1.substring(start, (start + max));
    }

    public static String lcs(String a, String b) {
        int[][] lengths = new int[a.length() + 1][b.length() + 1];
        //SPGlobal.log("line 688 a="+a," b="+b);
        // row 0 and column 0 are initialized to 0 already

        for (int i = 0; i < a.length(); i++) {
            for (int j = 0; j < b.length(); j++) {
                if (a.charAt(i) == b.charAt(j)) {
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                } else {
                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);
                }
            }
        }

        // read the substring out from the matrix
        //StringBuffer sb = new StringBuffer();
        StringBuilder sb = new StringBuilder();
        for (int x = a.length(), y = b.length(); x != 0 && y != 0;) {
            if (lengths[x][y] == lengths[x - 1][y]) {
                x--;
            } else if (lengths[x][y] == lengths[x][y - 1]) {
                y--;
            } else {
                assert a.charAt(x - 1) == b.charAt(y - 1);
                sb.append(a.charAt(x - 1));
                x--;
                y--;
            }
        }
        //SPGlobal.log("line 728 sb.reverse().toString()="+sb.reverse().toString()," sb.toString()="+sb.toString());
        return sb.reverse().toString();
    }

    static void setupSets(Mod merger, Mod patch) {
        for (ARMO armor : merger.getArmors()) {
            if (armor.getTemplate().equals(FormID.NULL)) {

                /*//----------------asdf попытка внедрить поиск  по всем ключам с "dienes_outfit"----------------------------

                //KYWD[] ret = null;

                ArrayList<FormID> a;

                ARMO replace = armor;
                FormID tmp = replace.getTemplate();
                //SPGlobal.log("hasKeyword", varKey.getEDID() + " " + replace.getEDID() + " " + tmp.getFormStr());
                if (!tmp.isNull()) {
                    replace = (ARMO) merger.getMajor(tmp, GRUP_TYPE.ARMO);
                }
                //SPGlobal.log(replace.getEDID(), varKey.getEDID());
                SPGlobal.log(replace.getEDID());
                KeywordSet k = replace.getKeywordSet();
                a = k.getKeywordRefs();

                //for (FormID temp : a) {
                //    KYWD refKey = (KYWD) merger.getMajor(temp, GRUP_TYPE.KYWD);
                    //SPGlobal.log("formid", temp.toString());
                   //SPGlobal.log("KYWD compare", refKey.getEDID() + " " + varKey.getEDID() + " " + (varKey.equals(refKey)));

                    //if (refKey.getEDID().startsWith("dienes_outfit")) {
                        boolean found = false;
                        for (Pair<KYWD, ArrayList<ARMO>> p : matchingSets) {

                        for (FormID temp : a) {
                        KYWD refKey = (KYWD) merger.getMajor(temp, GRUP_TYPE.KYWD);
                        //for (int i = 0; i < ret.length; i++){
                            if (refKey.getEDID().startsWith("dienes_outfit")) {
                            if (p.getBase().equals(refKey)) {
                                if (!p.getVar().contains(armor)) {
                                    //outfitKey = ret[i];
                                    p.getVar().add(armor);
                                    found = true;
                                    //break;
                                }
                            }
                            }
                        //}
                        }
                        //if (found == true){
                        //    break;
                        //}
                        }

                        if (found == false) {
                        KYWD outfitKey = hasKeyStartsWith(armor, "dienes_outfit", merger);
                        //for (FormID temp : a) {
                        //    KYWD refKey = (KYWD) merger.getMajor(temp, GRUP_TYPE.KYWD);

                        //if (refKey.getEDID().startsWith("dienes_outfit")) {
                        if (outfitKey != null){
                            Pair<KYWD, ArrayList<ARMO>> q = new Pair<>(outfitKey, new ArrayList<ARMO>(0));
                            q.getVar().add(armor);
                            matchingSets.add(q);
                        }
                        //}
                        //}

                        }
                    //}
                //}


                //if (ret != null) {
                //SPGlobal.log("line 768 outfitKey="+outfitKey.toString());


                //}

                //--------------------------------------------*/
                boolean found;// = false;

                //---------------------добавление оригинального кода для теста
                KYWD outfitKey = hasKeyStartsWith(armor, "dienes_outfit", merger);
                if (outfitKey != null) {
                    //SPGlobal.log("line 768 outfitKey="+outfitKey.toString());
                    found = false;
                    for (Pair<KYWD, ArrayList<ARMO>> p : matchingSets) {
                        if (p.getBase().equals(outfitKey)) {
                            //SPGlobal.log("line 848 outfitKey="+outfitKey,"p.getBase()="+p.getBase());
                            if (!p.getVar().contains(armor)) {
                            p.getVar().add(armor);
                            found = true;
                            break;
                            }
                        }
                    }
                    if (found == false) {
                        //SPGlobal.log("line 857 outfitKey="+outfitKey);
                        Pair<KYWD, ArrayList<ARMO>> q = new Pair<>(outfitKey, new ArrayList<ARMO>(0));
                        q.getVar().add(armor);
                        matchingSets.add(q);
                    }
                }

                //---------------------добавление оригинального кода для теста



                //>Второй вариант попытки внедрить проверку по всем ключам--------------------------------------------

                //KYWD outfitKey = hasKeyStartsWith(armor, "dienes_outfit", merger);
                //asdf222
                KYWD[] outfitKeys = hasArrayOfKeysStartsWith(armor, "dienes_outfit", merger);
                //int indx = 0;
                //found = false;
                for (KYWD outfitKey1 : outfitKeys) {
                    if (outfitKey1 != null && outfitKey1 != outfitKey) {
                        found = false;
                        //SPGlobal.log("line 768 outfitKey="+outfitKey.toString());
                        //boolean found = false;
                        for (Pair<KYWD, ArrayList<ARMO>> p : matchingSets) {
                            if (p.getBase().equals(outfitKey1)) {
                                //SPGlobal.log("line 853 outfitKey="+outfitKey1,", p.getBase()="+p.getBase(),", armorEDID="+armor.getEDID(), ", p.getVar().contains(armor)=" + (p.getVar().contains(armor)));
                                if (!p.getVar().contains(armor)) {
                                    p.getVar().add(armor);
                                    found = true;
                                    //SPGlobal.log("p.getVar().contains(armor)=" + (p.getVar().contains(armor)));
                                    break;
                                }
                            }
                        }
                        if (found == false) {
                            Pair<KYWD, ArrayList<ARMO>> q = new Pair<>(outfitKey1, new ArrayList<ARMO>(0));
                            //SPGlobal.log("line 863 outfitKey1="+outfitKey1 , ", line 865 q="+q);
                            q.getVar().add(armor);
                            matchingSets.add(q);
                        }
                    }
                }
                //<Второй вариант попытки внедрить проверку по всем ключам--------------------------------------------


                /*//это оригинальный код
                KYWD outfitKey = hasKeyStartsWith(armor, "dienes_outfit", merger);
                if (outfitKey != null) {
                    //SPGlobal.log("line 768 outfitKey="+outfitKey.toString());
                    boolean found = false;
                    for (Pair<KYWD, ArrayList<ARMO>> p : matchingSets) {
                        if (p.getBase().equals(outfitKey)) {
                            SPGlobal.log("line 848 outfitKey="+outfitKey,"p.getBase()="+p.getBase());
                            if (!p.getVar().contains(armor)) {
                            p.getVar().add(armor);
                            found = true;
                            break;
                            }
                        }
                    }
                    if (found == false) {
                        SPGlobal.log("line 857 outfitKey="+outfitKey);
                        Pair<KYWD, ArrayList<ARMO>> q = new Pair<>(outfitKey, new ArrayList<ARMO>(0));
                        q.getVar().add(armor);
                        matchingSets.add(q);
                    }
                }*/
            }
        }
    }

    static KYWD hasKeyStartsWith(ARMO armor, String start, Mod merger) {
        //asdf
        KYWD ret = null;

        ArrayList<FormID> a;

        ARMO replace = armor;
        FormID tmp = replace.getTemplate();
        //SPGlobal.log("hasKeyword", varKey.getEDID() + " " + replace.getEDID() + " " + tmp.getFormStr());
        if (!tmp.isNull()) {
            replace = (ARMO) merger.getMajor(tmp, GRUP_TYPE.ARMO);
        }
        //SPGlobal.log(replace.getEDID(), varKey.getEDID());
        KeywordSet k = replace.getKeywordSet();
        a = k.getKeywordRefs();
        for (FormID temp : a) {
            KYWD refKey = (KYWD) merger.getMajor(temp, GRUP_TYPE.KYWD);
            //SPGlobal.log("formid", temp.toString());
            //SPGlobal.log("KYWD compare", refKey.getEDID() + " " + varKey.getEDID() + " " + (varKey.equals(refKey)));

            if (refKey.getEDID().startsWith(start)) {
                ret = refKey;
                break;
            }
        }

        return ret;
    }


    static KYWD[] hasArrayOfKeysStartsWith(ARMO armor, String start, Mod merger) {
        //asdf222
        KYWD[] ret = new KYWD[30];
        ArrayList<FormID> a;
        //ArrayList<KYWD> ret1 = new ArrayList(0);

        ARMO replace = armor;
        FormID tmp = replace.getTemplate();
        //SPGlobal.log("hasKeyword", varKey.getEDID() + " " + replace.getEDID() + " " + tmp.getFormStr());
        if (!tmp.isNull()) {
            replace = (ARMO) merger.getMajor(tmp, GRUP_TYPE.ARMO);
        }
        //SPGlobal.log(replace.getEDID(), varKey.getEDID());
        KeywordSet k = replace.getKeywordSet();
        a = k.getKeywordRefs();
        int indx = 0;
        KYWD refKey;
        for (FormID temp : a) {
            refKey = (KYWD) merger.getMajor(temp, GRUP_TYPE.KYWD);
            //SPGlobal.log("formid", temp.toString());
            //SPGlobal.log("KYWD compare", refKey.getEDID() + " " + varKey.getEDID() + " " + (varKey.equals(refKey)));

            if (refKey.getEDID().startsWith(start)) {
                ret[indx] = refKey;
                //ret1.add(refKey);
                indx++;
            }
            //indx++;
        }

        return ret;
    }

    static ArrayList<ARMO> getAllWithKey(KYWD key, ArrayList<FormID> a, Mod merger) {
        ArrayList<ARMO> ret = new ArrayList<>(0);
        for (FormID f : a) {
            ARMO arm = (ARMO) merger.getMajor(f, GRUP_TYPE.ARMO);
            if (arm != null) {
                if (armorHasKeyword(arm, key, merger)) {
                    if (!ret.contains(arm)){  //asdf   Проверрка если не содержит
                        ret.add(arm);
                    }
                }
            }
        }
        return ret;
    }

    static ArrayList<ARMO> getAllWithKeyARMO(KYWD key, ArrayList<ARMO> a, Mod merger) {
        ArrayList<ARMO> ret = new ArrayList<>(0);
        for (ARMO arm : a) {

            if (armorHasKeyword(arm, key, merger)) {
                    if (!ret.contains(arm)){  //asdf   Проверрка если не содержит
                        ret.add(arm);
                    }
            }

        }
        return ret;
    }

    static String getNameFromArrayWithKey(ArrayList<ARMO> ar, KYWD k, Mod merger) {
        String ret;// = null;
        if (k.getEDID().contains("dienes_outfit")) {
            ret = "DienesLVLIOutfit" + k.getEDID().substring(13);
        } else {
            ret = "DienesLVLIOutfit" + k.getEDID();
        }
        
        Map<String, FirstPersonFlags[]> bitsInfo = BitsInfo.Get();
        for (String sBit : bitsInfo.keySet()){
            for (ARMO a : ar) {
                boolean bitAdded = false;
                for (FirstPersonFlags flag : bitsInfo.get(sBit)){
                    if (a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, flag)){
                        ret += sBit;
                        
                        bitAdded = true;
                        break;
                    }
                }
                if(bitAdded){
                    break;
                }
            }
        }
        
        
        
//        boolean h = false;
//        for (ARMO arm : a) {
//            if        (arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HEAD)
//                    || arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CIRCLET)
//                    || arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HAIR)) {
//                h = true;
//                break;
//            }
//        }
//        boolean c = false;
//        for (ARMO arm : a) {
//            if        (arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BODY)) {
//                c = true;
//                break;
//            }
//        }
//        boolean g = false;
//        for (ARMO arm : a) {
//            if        (arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HANDS)) {
//                g = true;
//                break;
//            }
//        }
//        boolean b = false;
//        for (ARMO arm : a) {
//            if        (arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FEET)) {
//                b = true;
//                break;
//            }
//        }
//        KYWD shield = (KYWD) merger.getMajor("ArmorShield", GRUP_TYPE.KYWD);
//        boolean s = false;
//        for (ARMO arm : a) {
//            if (armorHasKeyword(arm, shield, merger)) {
//                s = true;
//                break;
//            }
//        }
////--------------------------------------------------------------------------------------------
//        boolean l = false;
//        for (ARMO arm : a)
//		{
//            if        (	    arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR))
//		    {
//                l = true;
//                break;
//            }
//        }
//        boolean r = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.EARS))
//		    {
//                r = true;
//                break;
//            }
//        }
//        boolean w = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.DecapitateHead))
//		    {
//                w = true;
//                break;
//            }
//        }
//        boolean d = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.Decapitate))
//		    {
//                d = true;
//                break;
//            }
//        }
////--------------------------------------------------------------------------------------------
//        boolean f = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FOREARMS))
//		    {
//                f = true;
//                break;
//            }
//        }
////--------------------------------------------------------------------------------------------
//        boolean v = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CALVES))
//		    {
//                v = true;
//                break;
//            }
//        }
////--------------------------------------------------------------------------------------------
//        boolean t = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.TAIL))
//		    {
//                t = true;
//                break;
//            }
//        }
//
//        boolean z3 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn3))
//		    {
//                z3 = true;
//                break;
//            }
//        }
//
//        boolean z4 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn4))
//		    {
//                z4 = true;
//                break;
//            }
//        }
//
//        boolean z5 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn5))
//		    {
//                z5 = true;
//                break;
//            }
//        }
//
//        boolean z6 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn6))
//		    {
//                z6 = true;
//                break;
//            }
//        }
//
//        boolean z7 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn7))
//		    {
//                z7 = true;
//                break;
//            }
//        }
//
//        boolean z8 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn8))
//		    {
//                z8 = true;
//                break;
//            }
//        }
//
//        boolean z9 = false;
//        /*KYWD z9k = (KYWD) merger.getMajor("BodyAddOn9", GRUP_TYPE.KYWD);*/
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn9)
//                            /*|| armorHasKeyword(arm, z9k, merger)*/)
//		    {
//                z9 = true;
//                break;
//            }
//        }
//
//        boolean z10 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn10))
//		    {
//                z10 = true;
//                break;
//            }
//        }
//
//        boolean z11 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn11))
//		    {
//                z11 = true;
//                break;
//            }
//        }
//
//        boolean z12 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn12))
//		    {
//                z12 = true;
//                break;
//            }
//        }
//
//        boolean z13 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn13))
//		    {
//                z13 = true;
//                break;
//            }
//        }
//
//        boolean z14 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn14))
//		    {
//                z14 = true;
//                break;
//            }
//        }
//
//        boolean z15 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn15))
//		    {
//                z15 = true;
//                break;
//            }
//        }
//
//        boolean z16 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn16))
//		    {
//                z16 = true;
//                break;
//            }
//        }
//
//        boolean z17 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn17))
//		    {
//                z17 = true;
//                break;
//            }
//        }
//
//        boolean fx01 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FX01))
//		    {
//                fx01 = true;
//                break;
//            }
//        }
////------------------------------------------------------------
//        if (h) {
//            ret = ret + "H";
//        }
//        if (c) {
//            ret = ret + "C";
//        }
//        if (g) {
//            ret = ret + "G";
//        }
//        if (b) {
//            ret = ret + "B";
//        }
//        if (s) {
//            ret = ret + "S";
//        }
////---------------------------------
///*
//H
//C
//G
//B
//S
//"FARMS"; = F
//"CLVS"; = V
//"TL"; = T
//"LHR"; = L
//"ERS"; = R
//"DCH"; = W
//"DC"; = D
//Z3 = A
//Z4 = E
//Z5 = I
//Z6 = J
//Z7 = K
//Z8 = M
//Z9 = N
//Z10 = O
//Z11 = P
//Z12 = Q
//Z13 = U
//Z14 = X
//Z15 = Y
//Z16 = Z
//Z17 = -
//FX01 = _
//*/
//        if (f) {
//            ret = ret + "F";
//        }
//        if (v) {
//            ret = ret + "V";
//        }
//        if (t) {
//            ret = ret + "T";
//        }
//        if (l) {
//            ret = ret + "L";
//        }
//        if (r) {
//            ret = ret + "R";
//        }
//        if (z3) {
//            ret = ret + "A";
//        }
//        if (z4) {
//            ret = ret + "E";
//        }
//        if (z5) {
//            ret = ret + "I";
//        }
//        if (z6) {
//            ret = ret + "J";
//        }
//        if (z7) {
//            ret = ret + "K";
//        }
//        if (z8) {
//            ret = ret + "M";
//        }
//        if (w) {
//            ret = ret + "W";
//        }
//        if (d) {
//            ret = ret + "D";
//        }
//        if (z9) {
//            ret = ret + "N";
//        }
//        if (z10) {
//            ret = ret + "O";
//        }
//        if (z11) {
//            ret = ret + "P";
//        }
//        if (z12) {
//            ret = ret + "Q";
//        }
//        if (z13) {
//            ret = ret + "U";
//        }
//        if (z14) {
//            ret = ret + "X";
//        }
//        if (z15) {
//            ret = ret + "Y";
//        }
//        if (z16) {
//            ret = ret + "Z";
//        }
//        if (z17) {
//            ret = ret + "-";
//        }
//        if (fx01) {
//            ret = ret + "_";
//        }
//---------------------------------

        return ret;
    }

    static void addArmorFromArray(LVLI list, ArrayList<ARMO> a, Mod merger, Mod patch) {
//        FormID f = new FormID("107347", "Skyrim.esm");
//        LVLI glist = (LVLI) merger.getMajor(f, GRUP_TYPE.LVLI);
        //asdf ArmorParts
        ArrayList<ARMO> h = getAllWithKeyARMO((KYWD) merger.getMajor("ArmorHelmet", GRUP_TYPE.KYWD), a, merger);
        ArrayList<ARMO> c = getAllWithKeyARMO((KYWD) merger.getMajor("ArmorCuirass", GRUP_TYPE.KYWD), a, merger);
        ArrayList<ARMO> g = getAllWithKeyARMO((KYWD) merger.getMajor("ArmorGauntlets", GRUP_TYPE.KYWD), a, merger);
        ArrayList<ARMO> b = getAllWithKeyARMO((KYWD) merger.getMajor("ArmorBoots", GRUP_TYPE.KYWD), a, merger);
        ArrayList<ARMO> s = getAllWithKeyARMO((KYWD) merger.getMajor("ArmorShield", GRUP_TYPE.KYWD), a, merger);
        ArrayList<ARMO> f = getAllWithKeyARMO((KYWD) merger.getMajor("FOREARMS", GRUP_TYPE.KYWD), a, merger);//34
        ArrayList<ARMO> v = getAllWithKeyARMO((KYWD) merger.getMajor("CALVES", GRUP_TYPE.KYWD), a, merger);//38
        ArrayList<ARMO> t = getAllWithKeyARMO((KYWD) merger.getMajor("TAIL", GRUP_TYPE.KYWD), a, merger);//40
        ArrayList<ARMO> l = getAllWithKeyARMO((KYWD) merger.getMajor("LONG_HAIR", GRUP_TYPE.KYWD), a, merger);//41
        ArrayList<ARMO> r = getAllWithKeyARMO((KYWD) merger.getMajor("EARS", GRUP_TYPE.KYWD), a, merger);//43
        ArrayList<ARMO> z3 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn3", GRUP_TYPE.KYWD), a, merger);//44
        ArrayList<ARMO> z4 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn4", GRUP_TYPE.KYWD), a, merger);//45
        ArrayList<ARMO> z5 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn5", GRUP_TYPE.KYWD), a, merger);//46
        ArrayList<ARMO> z6 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn6", GRUP_TYPE.KYWD), a, merger);//47
        ArrayList<ARMO> z7 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn7", GRUP_TYPE.KYWD), a, merger);//48
        ArrayList<ARMO> z8 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn8", GRUP_TYPE.KYWD), a, merger);//49
        ArrayList<ARMO> w = getAllWithKeyARMO((KYWD) merger.getMajor("DecapitateHEAD", GRUP_TYPE.KYWD), a, merger);//50
        ArrayList<ARMO> d = getAllWithKeyARMO((KYWD) merger.getMajor("Decapitate", GRUP_TYPE.KYWD), a, merger);//51
        ArrayList<ARMO> z9 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn9", GRUP_TYPE.KYWD), a, merger);//52
        ArrayList<ARMO> z10 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn10", GRUP_TYPE.KYWD), a, merger);//53
        ArrayList<ARMO> z11 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn11", GRUP_TYPE.KYWD), a, merger);//54
        ArrayList<ARMO> z12 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn12", GRUP_TYPE.KYWD), a, merger);//55
        ArrayList<ARMO> z13 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn13", GRUP_TYPE.KYWD), a, merger);//56
        ArrayList<ARMO> z14 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn14", GRUP_TYPE.KYWD), a, merger);//57
        ArrayList<ARMO> z15 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn15", GRUP_TYPE.KYWD), a, merger);//58
        ArrayList<ARMO> z16 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn16", GRUP_TYPE.KYWD), a, merger);//59
        ArrayList<ARMO> z17 = getAllWithKeyARMO((KYWD) merger.getMajor("BodyAddOn17", GRUP_TYPE.KYWD), a, merger);//60
        ArrayList<ARMO> fx01 = getAllWithKeyARMO((KYWD) merger.getMajor("FX01", GRUP_TYPE.KYWD), a, merger);//61

        if (h.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(h.get(0), "dienes_outfit", merger).getEDID() + "HelmetsSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name);//(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : h) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (h.size() == 1) {
            list.addEntry(h.get(0).getForm(), 1, 1);
        }
        if (c.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(c.get(0), "dienes_outfit", merger).getEDID() + "CuirassesSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : c) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (c.size() == 1) {
            list.addEntry(c.get(0).getForm(), 1, 1);
        }
        if (g.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(g.get(0), "dienes_outfit", merger).getEDID() + "GauntletsSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : g) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (g.size() == 1) {
            list.addEntry(g.get(0).getForm(), 1, 1);
        }
        if (b.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(b.get(0), "dienes_outfit", merger).getEDID() + "BootsSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : b) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (b.size() == 1) {
            list.addEntry(b.get(0).getForm(), 1, 1);
        }
        if (s.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(s.get(0), "dienes_outfit", merger).getEDID() + "ShieldsSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : s) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (s.size() == 1) {
            list.addEntry(s.get(0).getForm(), 1, 1);
        }
//--------------------------------------------------------

        if (f.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(f.get(0), "dienes_outfit", merger).getEDID() + "FOREARMSSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : f) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (f.size() == 1) {
            list.addEntry(f.get(0).getForm(), 1, 1);
        }
        if (v.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(v.get(0), "dienes_outfit", merger).getEDID() + "CALVESSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : v) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (v.size() == 1) {
            list.addEntry(v.get(0).getForm(), 1, 1);
        }
        if (t.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(l.get(0), "dienes_outfit", merger).getEDID() + "TALESublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : l) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (l.size() == 1) {
            list.addEntry(l.get(0).getForm(), 1, 1);
        }
        if (l.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(l.get(0), "dienes_outfit", merger).getEDID() + "LONGHAIRSSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : l) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (l.size() == 1) {
            list.addEntry(l.get(0).getForm(), 1, 1);
        }
        if (r.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(r.get(0), "dienes_outfit", merger).getEDID() + "EarsSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : r) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (r.size() == 1) {
            list.addEntry(r.get(0).getForm(), 1, 1);
        }
        if (z3.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z3.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn3Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z3) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (z3.size() == 1) {
            list.addEntry(z3.get(0).getForm(), 1, 1);
        }
        if (z4.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z4.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn4Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z4) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (z4.size() == 1) {
            list.addEntry(z4.get(0).getForm(), 1, 1);
        }
        if (z5.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z5.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn5Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z5) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                patch.addRecord(subList);
            }
        } else if (z5.size() == 1) {
            list.addEntry(z5.get(0).getForm(), 1, 1);
        }
        if (z6.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z6.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn6Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z6) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line 1478 subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z6.size() == 1) {
            list.addEntry(z6.get(0).getForm(), 1, 1);
        }
        if (z7.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z7.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn7Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z7) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z7.size() == 1) {
            list.addEntry(z7.get(0).getForm(), 1, 1);
        }
        if (z8.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z8.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn8Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z8) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z8.size() == 1) {
            list.addEntry(z8.get(0).getForm(), 1, 1);
        }
        if (w.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(w.get(0), "dienes_outfit", merger).getEDID() + "DecapitateHeadSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : w) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (w.size() == 1) {
            list.addEntry(w.get(0).getForm(), 1, 1);
        }
        if (d.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(d.get(0), "dienes_outfit", merger).getEDID() + "DecapitateSublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : d) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (d.size() == 1) {
            list.addEntry(d.get(0).getForm(), 1, 1);
        }
        if (z9.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z9.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn9Sublist";
            //SPGlobal.log("Line 1566 name="+name);//asdf
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z9) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());//asdf
                patch.addRecord(subList);
            }
        } else if (z9.size() == 1) {
            list.addEntry(z9.get(0).getForm(), 1, 1);
        }
        if (z10.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z10.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn10Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z10) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z10.size() == 1) {
            list.addEntry(z10.get(0).getForm(), 1, 1);
        }
        if (z11.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z11.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn11Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z11) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z11.size() == 1) {
            list.addEntry(z11.get(0).getForm(), 1, 1);
        }
        if (z12.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z12.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn12Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z12) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z12.size() == 1) {
            list.addEntry(z12.get(0).getForm(), 1, 1);
        }
        if (z13.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z13.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn13Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z13) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z13.size() == 1) {
            list.addEntry(z13.get(0).getForm(), 1, 1);
        }
        if (z14.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z14.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn14Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z14) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z14.size() == 1) {
            list.addEntry(z14.get(0).getForm(), 1, 1);
        }
        if (z15.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z15.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn15Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z15) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z15.size() == 1) {
            list.addEntry(z15.get(0).getForm(), 1, 1);
        }
        if (z16.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z16.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn16Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z16) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z16.size() == 1) {
            list.addEntry(z16.get(0).getForm(), 1, 1);
        }
        if (z17.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(z17.get(0), "dienes_outfit", merger).getEDID() + "BodyAddOn17Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : z17) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (z17.size() == 1) {
            list.addEntry(z17.get(0).getForm(), 1, 1);
        }
        if (fx01.size() > 1) {
            String name = "DienesLVLI_" + hasKeyStartsWith(fx01.get(0), "dienes_outfit", merger).getEDID() + "FX01Sublist";
            LVLI subList = (LVLI) merger.getMajor(name, GRUP_TYPE.LVLI);
            if (subList == null) {
                subList = (LVLI) patch.getMajor(name, GRUP_TYPE.LVLI);
            }
            if (subList != null) {
                list.addEntry(subList.getForm(), 1, 1);
            } else {
                subList = new LVLI(name); //(LVLI) patch.makeCopy(glist, name);
                subList.set(LeveledRecord.LVLFlag.UseAll, false);
                for (ARMO arm : fx01) {
                    subList.addEntry(arm.getForm(), 1, 1);
                }
                //SPGlobal.log("Line subList.toString()="+subList.toString());
                patch.addRecord(subList);
            }
        } else if (fx01.size() == 1) {
            list.addEntry(fx01.get(0).getForm(), 1, 1);
        }


    }


    static void addAlternateSets(LVLI list, ArrayList<ARMO> a, Mod merger, Mod patch) throws BadParameter {
//        FormID f = new FormID("107347", "Skyrim.esm");
//      SPGlobal.log("outfits glist", f.toString());
//        LVLI glist = (LVLI) merger.getMajor(f, GRUP_TYPE.LVLI);
        //asdf addAlternateSets

        //----asdf ---------------------------------------



        //----asdf ---------------------------------------

        KYWD k = null;
        //KYWD[] k0 = null;//KYWD k = null;
        ArrayList<Pair<KYWD, ArrayList<ARMO>>> varSets = new ArrayList<>(0);
        for (ARMO arm : a) {



            k = hasKeyStartsWith(arm, "dienes_outfit", merger);
            //asdf111
            //k0 = hasArrayOfKeysStartsWith(arm, "dienes_outfit", merger);
            //for (KYWD k1 : k0){
            //if (k1 != null){
            //k = k1;
            for (Pair<KYWD, ArrayList<ARMO>> p1 : matchingSets) {
                /*boolean b = false;
                for (KYWD k1 : k0){
                    if (k1 != null){
                        k = k1;
                        if (k1.equals(p1.getBase())){
                            b = true;
                            break;
                        }
                    }
                }
                boolean key = k1.equals(p1.getBase());*/
                boolean key = false;
                //asdf 310518 вставка проверки по всем ключам из массива asdf
                KYWD[] outfitKeys = hasArrayOfKeysStartsWith(arm, "dienes_outfit", merger);
                for (KYWD outfitKey1 : outfitKeys) {
                    if (outfitKey1 != null) {
                        k = outfitKey1;
                        key = k.equals(p1.getBase());
                        if (key){
                            break;
                        }
                    }
                }

                //boolean key = k.equals(p1.getBase());
                //SPGlobal.log("line 2003 key=" + key, "k=" + k, " p1.getBase()=" + p1.getBase());
                //if (b = true) {
                if (key) {
                    for (ARMO armor : p1.getVar()) {
                        boolean passed = true;
                        for (skyproc.genenums.FirstPersonFlags c : skyproc.genenums.FirstPersonFlags.values()) {
                            boolean armorFlag = armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, c);
                            boolean formFlag = arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, c);

                            boolean flagMatch = (armorFlag == formFlag);
                            //SPGlobal.log("line 1727 flag match" + c, armorFlag + " " + formFlag + " " + flagMatch);
                            //SPGlobal.log("line 1728 flag match" + c, "armor=" + armor, " form=" + arm);
                            if (flagMatch == false) {
                                passed = false;
                            }
                        }
                        if (!passed) {
                            KYWD helm = (KYWD) merger.getMajor("ArmorHelmet", GRUP_TYPE.KYWD);
                            /*KYWD z9k = (KYWD) merger.getMajor("BodyAddOn9", GRUP_TYPE.KYWD);*/
                            if (  ( armorHasKeyword(arm, helm, merger) && armorHasKeyword(armor, helm, merger) ) /*|| ( armorHasKeyword(arm, z9k, merger) && armorHasKeyword(armor, z9k, merger) )*/  ) {
                                passed = true;
                            }
                        }
                        if (passed) {
                            boolean found = false;
                            //asdf getSlotKYWD
                            KYWD slotKey = getSlotKYWD(armor, merger);
                            if (slotKey != null) {
                                for (Pair<KYWD, ArrayList<ARMO>> p : varSets) {
                                    if (p.getBase().equals(slotKey)) {
                                        ArrayList<ARMO> q = p.getVar();
                                        if (!q.contains(armor)) {
                                            q.add(armor);
                                        }
                                        found = true;
                                        break;
                                    }
                                }
                                if (found == false) {
                                    Pair<KYWD, ArrayList<ARMO>> p = new Pair(slotKey, new ArrayList<ARMO>(0));
                                    p.getVar().add(armor);
                                    //SPGlobal.log("line 1760 p.toString()="+p.toString());
                                    varSets.add(p);
                                }
                            }
                        }
                    }
                }
            }
        }

        String bits = getBitsFromArray(a, merger);
        for (char c : bits.toCharArray()) {
            for (Pair<KYWD, ArrayList<ARMO>> p : varSets) {

                if (arrayHasBits(p.getVar(), String.valueOf(c), merger)) {
                    if (p.getVar().size() > 1) {
                        //asdf имя списка с вариантами
                        String lvliName = getNameFromArrayWithKey(p.getVar(), k, merger) + "variants";
                        //SPGlobal.log("line 1889 lvliName="+lvliName);
                        LVLI list2 = (LVLI) patch.getMajor(lvliName, GRUP_TYPE.LVLI);
                        if (list2 != null) {
                            if ( !list.contains(list2) ){//asdf added condition, After check it seems it prevents duplicate sublists but steel can be list like different list for 47,48 slot part and different list for 47 slot outfit but they all must be in one list
                                //есть недостаток, например, список для слотов 53,48 будет отдельно от списка для слота 48, в идеале содержимое списка для слота надо вставлять в список для слотов 48,53
                                //list2.setChanceNone(50);
                                list.addEntry(list2.getForm(), 1, 1);
                            }
                            //SPGlobal.log("list.toString()="+list.toString());
                            patch.addRecord(list);
                        } else {
                            LVLI subList = new LVLI(lvliName); //(LVLI) patch.makeCopy(glist, lvliName);
                            subList.set(LeveledRecord.LVLFlag.UseAll, false);
                            addArmorByBit(subList, p.getVar(), String.valueOf(c), merger);
                            patch.addRecord(subList);
                            list.addEntry(subList.getForm(), 1, 1);
                            //SPGlobal.log("list.toString()="+list.toString());
                            patch.addRecord(list);
                        }
                    } else {
                        boolean found = false;
                        for (LeveledEntry entry : list) {
                            if (entry.getForm().equals(p.getVar().get(0).getForm())) {
                                found = true;
                            }
                        }
                        if (!found) {
                            list.addEntry(p.getVar().get(0).getForm(), 1, 1);
                            //SPGlobal.log("list.toString()="+list.toString());
                        }
                    }
                }
            }
        }
    }

    static void addAlternateOutfits(LVLI list, ArrayList<ARMO> a, Mod merger, Mod patch) throws BadParameter {
//        FormID f = new FormID("107347", "Skyrim.esm");
        //SPGlobal.log("outfits glist", f.toString());
//        LVLI glist = (LVLI) merger.getMajor(f, GRUP_TYPE.LVLI);
        //Вместо просто стальных перчаток и остальных частей брони добавляет ур. списки с их вариантами, всех перчаток, что в xml имели улючевое слово ARMOR_STEEL, т.е. Стальные перчатки будут заменены на ур. список с именем вроде LLIGlovesSTEELVariants, где будет много перчаток имевших ARMOR_STEEL ы ключах
        //asdf222 Похоже, что это добавляемые уровневые списки для наборов брони LLI_BASE и обычные, т.е. вместо, например, стальных перчаток будет ур. список с разными вариантами стальных перчаток. Обработка по обеим вариантам BASE и обычным здесь, видимо, будет лишней, т.к. замечено, что тогда просто добавляются одни и те же списки для обоих вариантов, т.е. для стальных перчаток будут добавлены ур. списки с вариатами стальных перчаток с ключевым словом ARMOR_STEEL в двух ур. списках с LLI_BASE и обычный и в игре будут добавлены перчатки из обоих
        KYWD k;// = null;
        KYWD[] outfitKeys = null;
        KYWD[] outfitKeys1 = null;
        ArrayList<Pair<KYWD, ArrayList<ARMO>>> varSets = new ArrayList<>(0);
        for (ARMO arm : a) {

            outfitKeys = hasArrayOfKeysStartsWith(arm, "LLI_BASE", merger);
            outfitKeys1 = hasArrayOfKeysStartsWith(arm, "dienes_outfit", merger);

            //k = hasKeyStartsWith(arm, "LLI_BASE", merger);
            boolean notBase;// = false;

            if (outfitKeys[0] != null)
            {
                for (KYWD outfitKey1 : outfitKeys)
                {
                    if (outfitKey1 != null)
                    {
                        notBase = false;
                        k = outfitKey1;

                        //if (k == null) {
                        //    k = hasKeyStartsWith(arm, "dienes_outfit", merger);
                        //    notBase = true;
                        //}

                        for (Pair<KYWD, ArrayList<ARMO>> p1 : matchingSets)
                        {

                            boolean key;// = false;
                            if (notBase) {
                                key = p1.getBase().equals(k);
                            } else {
                                KYWD ret = null;
                                for (Pair p : armorMatches) {
                                    KYWD var = (KYWD) p.getBase();
                                    //SPGlobal.log("getBaseArmor", k.getEDID() + " " + var.getEDID() + " " + var.equals(k));
                                    if (var.equals(k)) {
                                        ret = (KYWD) p.getVar();
                                    }
                                }
                                key = armorHasKeyword(p1.getVar().get(0), ret, merger) || armorHasKeyword(p1.getVar().get(0), k, merger);
                            }

                            if (key) {
                                for (ARMO armor : p1.getVar()) {
                                    boolean passed = true;
                                    for (skyproc.genenums.FirstPersonFlags c : skyproc.genenums.FirstPersonFlags.values()) {
                                        boolean armorFlag = armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, c);
                                        boolean formFlag = arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, c);

                                        boolean flagMatch = (armorFlag == formFlag);

                                        if (flagMatch == false) {
                                            passed = false;
                                        }
                                    }
                                    //asdf outfits
                                    if (!passed) {
                                        KYWD helm = (KYWD) merger.getMajor("ArmorHelmet", GRUP_TYPE.KYWD);
                                        if (armorHasKeyword(arm, helm, merger) && armorHasKeyword(armor, helm, merger)) {
                                            passed = true;
                                        }
                                    }
                                    if (passed) {
                                        boolean found = false;
                                        KYWD slotKey = getSlotKYWD(armor, merger);
                                        if (slotKey == null) {
                                            //SPGlobal.log("line 1865 slotKey.toString()="+slotKey.toString());
                                            //int test = 1;
                                        } else {
                                            for (Pair<KYWD, ArrayList<ARMO>> p : varSets) {
                                                if (p.getBase().equals(slotKey)) {
                                                    ArrayList<ARMO> q = p.getVar();
                                                    if (!q.contains(armor)) {
                                                        q.add(armor);
                                                    }
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            if (found == false) {
                                                Pair<KYWD, ArrayList<ARMO>> p = new Pair(slotKey, new ArrayList<ARMO>(0));
                                                p.getVar().add(armor);
                                                varSets.add(p);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (outfitKeys1[0] != null && outfitKeys[0] == null)
            {
                for (KYWD outfitKey2 : outfitKeys1)
                {
                    if (outfitKey2 != null)
                    {
                        k = outfitKey2;
                        notBase = true;

                        //if (k == null) {
                        //    k = hasKeyStartsWith(arm, "dienes_outfit", merger);
                        //    notBase = true;
                        //}

                        for (Pair<KYWD, ArrayList<ARMO>> p1 : matchingSets)
                        {

                            boolean key;// = false;
                            if (notBase) {
                                key = p1.getBase().equals(k);
                            } else {
                                KYWD ret = null;
                                for (Pair p : armorMatches) {
                                    KYWD var = (KYWD) p.getBase();
                                    //SPGlobal.log("getBaseArmor", k.getEDID() + " " + var.getEDID() + " " + var.equals(k));
                                    if (var.equals(k)) {
                                        ret = (KYWD) p.getVar();
                                    }
                                }
                                key = armorHasKeyword(p1.getVar().get(0), ret, merger) || armorHasKeyword(p1.getVar().get(0), k, merger);
                            }

                            if (key) {
                                for (ARMO armor : p1.getVar()) {
                                    boolean passed = true;
                                    for (skyproc.genenums.FirstPersonFlags c : skyproc.genenums.FirstPersonFlags.values()) {
                                        boolean armorFlag = armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, c);
                                        boolean formFlag = arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, c);

                                        boolean flagMatch = (armorFlag == formFlag);

                                        if (flagMatch == false) {
                                            passed = false;
                                        }
                                    }
                                    //asdf outfits
                                    if (!passed) {
                                        KYWD helm = (KYWD) merger.getMajor("ArmorHelmet", GRUP_TYPE.KYWD);
                                        if (armorHasKeyword(arm, helm, merger) && armorHasKeyword(armor, helm, merger)) {
                                            passed = true;
                                        }
                                    }
                                    if (passed) {
                                        boolean found = false;
                                        KYWD slotKey = getSlotKYWD(armor, merger);
                                        if (slotKey == null) {
                                            //SPGlobal.log("line 1865 slotKey.toString()="+slotKey.toString());
                                            //int test = 1;
                                        } else {
                                            for (Pair<KYWD, ArrayList<ARMO>> p : varSets) {
                                                if (p.getBase().equals(slotKey)) {
                                                    ArrayList<ARMO> q = p.getVar();
                                                    if (!q.contains(armor)) {
                                                        q.add(armor);
                                                    }
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            if (found == false) {
                                                Pair<KYWD, ArrayList<ARMO>> p = new Pair(slotKey, new ArrayList<ARMO>(0));
                                                p.getVar().add(armor);
                                                varSets.add(p);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (outfitKeys != null && outfitKeys[0] != null)
        {
            for (KYWD outfitKey1 : outfitKeys)
            {
                if (outfitKey1 != null)
                {
                    k = outfitKey1;
                    String bits = getBitsFromArray(a, merger);
                    for (char c : bits.toCharArray())
                    {
                        for (Pair<KYWD, ArrayList<ARMO>> p : varSets) {

                            if (arrayHasBits(p.getVar(), String.valueOf(c), merger)) {
                                if (p.getVar().size() > 1) {
                                    String lvliName = getNameFromArrayWithKey(p.getVar(), k, merger) + "variants";
                                    //SPGlobal.log("line 2009 lvliName="+lvliName);
                                    LVLI list2 = (LVLI) patch.getMajor(lvliName, GRUP_TYPE.LVLI);
                                    if (list2 != null) {
                                        if ( !list.contains(list2) ){//asdf added condition, After check it seems it prevents duplicate sublists but steel can be list like different lists for 47,48 slot parts and different for 47 slot outfit bat they all must be in one list
                                            //list2.setChanceNone(50);
                                            list.addEntry(list2.getForm(), 1, 1);
                                        }
                                        patch.addRecord(list);
                                    } else {
                                        LVLI subList = new LVLI(lvliName); //(LVLI) patch.makeCopy(glist, lvliName);
                                        addArmorByBit(subList, p.getVar(), String.valueOf(c), merger);
                                        patch.addRecord(subList);
                                        list.addEntry(subList.getForm(), 1, 1);
                                        patch.addRecord(list);
                                    }
                                } else {
                                    boolean found = false;
                                    for (LeveledEntry entry : list) {
                                        if (entry.getForm().equals(p.getVar().get(0).getForm())) {
                                            found = true;
                                        }
                                    }
                                    if (!found) {
                                        list.addEntry(p.getVar().get(0).getForm(), 1, 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (outfitKeys1 == null || outfitKeys1[0] == null || outfitKeys[0] != null){
            return;
        }
        for (KYWD outfitKey2 : outfitKeys1){
            if (outfitKey2 == null){
                continue;
            }
            k = outfitKey2;
            String bits = getBitsFromArray(a, merger);
            for (char c : bits.toCharArray()) {
                for (Pair<KYWD, ArrayList<ARMO>> p : varSets) {
                    if (!arrayHasBits(p.getVar(), String.valueOf(c), merger)) {
                        continue;
                    }
                    if (p.getVar().size() > 1) {
                        String lvliName = getNameFromArrayWithKey(p.getVar(), k, merger) + "variants";
                        //SPGlobal.log("line 2009 lvliName="+lvliName);
                        LVLI list2 = (LVLI) patch.getMajor(lvliName, GRUP_TYPE.LVLI);
                        if (list2 != null) {
                            if ( !list.contains(list2) ){//asdf added condition, After check it seems it prevents duplicate sublists but steel can be list like different lists for 47,48 slot parts and different for 47 slot outfit bat they all must be in one list
                                //list2.setChanceNone(50);
                                list.addEntry(list2.getForm(), 1, 1);
                            }
                            patch.addRecord(list);
                        } else {
                            LVLI subList = new LVLI(lvliName); //(LVLI) patch.makeCopy(glist, lvliName);
                            addArmorByBit(subList, p.getVar(), String.valueOf(c), merger);
                            patch.addRecord(subList);
                            list.addEntry(subList.getForm(), 1, 1);
                            patch.addRecord(list);
                        }
                    } else {
                        boolean found = false;
                        for (LeveledEntry entry : list) {
                            if (entry.getForm().equals(p.getVar().get(0).getForm())) {
                                found = true;
                            }
                        }
                        if (!found) {
                            list.addEntry(p.getVar().get(0).getForm(), 1, 1);
                        }
                    }
                }
            }
        }
    }

    static void insertTieredArmors(LVLI list, String keyPrefix, String bits, Mod merger, Mod patch) throws BadParameter {
//        FormID f = new FormID("107347", "Skyrim.esm");
//        LVLI glist = (LVLI) merger.getMajor(f, GRUP_TYPE.LVLI);
        boolean changed = false;
        //asdf
        if (keyPrefix.contains("Boss") || keyPrefix.contains("Thalmor")) {
            //SPGlobal.log("line 1930 keyPrefix="+keyPrefix);
            list.set(LeveledRecord.LVLFlag.CalcAllLevelsEqualOrBelowPC, false);
        }
        
        Map<String, TierBase> tiersList = new HashMap<>();
        for (TierBase tier : TiersList.Get()) {
            tiersList.put(tier.EDID(), tier);
        }

        for (int lev = 1; lev < 100; lev++) {
            int tier = lev / 3;
            //SPGlobal.log("line 1935 tier="+tier);
            String tierName = keyPrefix + String.valueOf(tier);
            //SPGlobal.log("line 1937 tierName="+tierName.toString());
            KYWD key = (KYWD) merger.getMajor(tierName, GRUP_TYPE.KYWD);
            if (key != null) {
                //SPGlobal.log("line 1940 key="+key.toString());
                ArrayList<ArrayList<ARMO>> array = getArrayOfTieredArmorSetsByKeyword(key, merger);
                //SPGlobal.log("line 1942 array="+array.toString());
                String edid = "DienesLVLI_" + keyPrefix + String.valueOf(tier);
                //SPGlobal.log("line 1944 edid="+edid);
                for (ArrayList<ARMO> ar : array) {
                    if (arrayHasBits(ar, bits, merger)) {
                        //SPGlobal.log("line 1947 ar="+ar.toString(),"bits="+bits);

                        LVLI subList = (LVLI) patch.getMajor(edid, GRUP_TYPE.LVLI);
                        if (subList == null) {
                            SPGlobal.logError("LLI Error:", "Could not find LVLI " + edid, ", List will be created");
                            subList = new LVLI(edid); //(LVLI) patch.makeCopy(glist, edid);
                            subList.set(LeveledRecord.LVLFlag.UseAll, false);

                            patch.addRecord(subList);
                        }
                        //SPGlobal.log("line 1955 subList="+subList.toString(),"list="+list.toString(),"lev="+lev);
                        boolean change = addListIfNotLevel(list, subList, lev);
                        if (change) {
                            changed = true;
                        }
                        //asdf Leveled list name
                        String setListName = "DienesLVLI_" + hasKeyStartsWith(ar.get(0), "dienes_outfit", merger).getEDID().substring(14) + bits;
                        LVLI setList = (LVLI) merger.getMajor(setListName, GRUP_TYPE.LVLI);
                        LVLI setList2 = (LVLI) patch.getMajor(setListName, GRUP_TYPE.LVLI);
                        if (setList == null) {
                            setList = setList2;
                        }
                        if (setList != null) {
                            change = addListIfNotLevel(subList, setList, 1);
                            if (change) {
                                changed = true;
                            }
                        } else {
                            LVLI set = new LVLI(setListName); //(LVLI) patch.makeCopy(glist, setListName);
                            set.set(LeveledRecord.LVLFlag.UseAll, true);
                            set.set(LeveledRecord.LVLFlag.CalcAllLevelsEqualOrBelowPC, false);
                            set.set(LeveledRecord.LVLFlag.CalcForEachItemInCount, false);
                            ArrayList<ArrayList<ARMO>> abits = new ArrayList<>(0);
                            for (char c : bits.toCharArray()) {
                                abits.add(addArmorByBitToArray(ar, String.valueOf(c), merger));
                                //SPGlobal.log("line 1974 c="+String.valueOf(c),"ar="+ar);
                            }
                            for (ArrayList<ARMO> a : abits) {
                                addAlternateSets(set, a, merger, patch);
                            }
                            patch.addRecord(set);
                            subList.addEntry(set.getForm(), 1, 1);
                            patch.addRecord(subList);
                            changed = true;
                        }

                    }
                }
                
                if (tiersList.containsKey(edid)){
                    TierBase foundTierRecord = tiersList.get(edid);
                    
                    LVLI subList = new LVLI(edid); //(LVLI) patch.makeCopy(glist, edid);
                    
                    // set leveled list flags
                    subList.set(LeveledRecord.LVLFlag.UseAll, foundTierRecord.UseAll());
                    subList.set(LeveledRecord.LVLFlag.CalcAllLevelsEqualOrBelowPC, foundTierRecord.CalcAllLevelsEqualOrBelowPC());
                    subList.set(LeveledRecord.LVLFlag.CalcForEachItemInCount, foundTierRecord.CalcForEachItemInCount());
                    
                    // add formIDs
                    for(String espName : foundTierRecord.FormIDList().keySet()){
                        for(String formID : foundTierRecord.FormIDList().get(espName)){
                            FormID formIDrecord = new FormID(formID, espName);
                            subList.addEntry(formIDrecord, 1, 1);
                        }
                    }                    
                    
                    addListIfNotLevel(list, subList, lev);
                    patch.addRecord(subList);
                    changed = true;                    
                }
            }
        }
        if (changed) {
            patch.addRecord(list);
        }
    }

    static ArrayList getArrayOfTieredArmorSetsByKeyword(KYWD key, Mod merger) {
        ArrayList<ArrayList<ARMO>> ret = new ArrayList<>(0);
        for (Pair<KYWD, ArrayList<ARMO>> p : matchingSets) {
            if (armorHasKeyword(p.getVar().get(0), key, merger)) {
                ret.add(p.getVar());
            }
        }

        return ret;
    }

    static boolean arrayHasBits(ArrayList<ARMO> ar, String bits, Mod merger) {



//        boolean ret = true;
        boolean passed = false;

//--------code start
        if (bits.contains("H")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HEAD)
                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CIRCLET)
                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HAIR)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("C")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BODY)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("G")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (      a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HANDS)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("B")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FEET)
                   ){
                        passed = true;
                    }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("S")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                KYWD k = hasKeyStartsWith(a, "ArmorShield", merger);
                if (k != null) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }       
        
        //--------------------------------------------------------------------------------------------

        if (bits.contains("L")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("R")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.EARS)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("W")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.DecapitateHead)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("D")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.Decapitate)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
//--------------------------------------------------------------------------------------------

        if (bits.contains("F")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FOREARMS)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
//--------------------------------------------------------------------------------------------

        if (bits.contains("V")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CALVES)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
//--------------------------------------------------------------------------------------------

        if (bits.contains("T")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.TAIL)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("A")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn3)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("E")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn4)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("I")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn5)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("J")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn6)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("K")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn7)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("M")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn8)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("N")) {
            //boolean passed = false;
            /*KYWD z9k = (KYWD) merger.getMajor("BodyAddOn9", GRUP_TYPE.KYWD);*/
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn9)
                        /*|| armorHasKeyword(a, z9k, merger)*/
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("O")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn10)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("P")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn11)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("Q")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn12)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("U")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn13)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("X")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn14)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("Y")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn15)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("Z")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn16)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("-")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn17)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
        if (bits.contains("_")) {
            //boolean passed = false;
            for (ARMO a : ar) {
                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FX01)
                   ) {
                    passed = true;
                }
            }
//            if (passed == false) {
//                ret = false;
//            }
        }
//--------code finish

//ret = passed;
//------------------------------------------------------------
        //asdf4

        return passed;
    }
//asdf
    
    static String getBitsFromArray(ArrayList<ARMO> ar, Mod merger) {        
        String ret = "";
        
        Map<String, FirstPersonFlags[]> bitsInfo = BitsInfo.Get();
        for (String sBit : bitsInfo.keySet()){
            for (ARMO a : ar) {
                boolean bitAdded = false;
                for (FirstPersonFlags flag : bitsInfo.get(sBit)){
                    if (a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, flag)){
                        ret += sBit;
                        
                        bitAdded = true;
                        break;
                    }
                }
                if(bitAdded){
                    break;
                }
            }
            //if (bits.contains(sBit)){
//                boolean isShield = false;
//                if ("S".equals(sBit)){
//                    isShield = true;
//                }
//                for (ARMO a : ar) {
//                    boolean isSet = false;
//                    if (isShield){
//                        KYWD k = hasKeyStartsWith(a, "ArmorShield", merger);
//                        if (k != null) {
//                            isSet = true;
//                        }
//                    }
//                    else{
//                        for (FirstPersonFlags flag : bitsInfo.get(sBit)){
//                            if (a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, flag)){
//                                isSet = true;
//                                break;
//                            }
//                        }
//                    }
//                    
//                    if (isSet){
//                        ret += sBit;
//                    }
//                }                
            //}
        }
        
        
//        //KYWD helm = (KYWD) merger.getMajor("ArmorHelmet", GRUP_TYPE.KYWD);
//        boolean h = false;
//        for (ARMO arm : a) {
//            if (       arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CIRCLET)
//                    || arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HEAD)
//                    || arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HAIR)) {
//                h = true;
//                break;
//            }
//        }
//        //KYWD cuirass = (KYWD) merger.getMajor("ArmorCuirass", GRUP_TYPE.KYWD);
//        boolean c = false;
//        for (ARMO arm : a) {
//            if (       arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BODY)) {
//                c = true;
//                break;
//            }
//        }
//        //KYWD gauntlets = (KYWD) merger.getMajor("ArmorGauntlets", GRUP_TYPE.KYWD);
//        boolean g = false;
//        for (ARMO arm : a) {
//            if (       arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HANDS)) {
//                g = true;
//                break;
//            }
//        }
//        //KYWD boots = (KYWD) merger.getMajor("ArmorBoots", GRUP_TYPE.KYWD);
//        boolean b = false;
//        for (ARMO arm : a) {
//            if (       arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FEET)) {
//                b = true;
//                break;
//            }
//        }
//        KYWD shield = (KYWD) merger.getMajor("ArmorShield", GRUP_TYPE.KYWD);
//        boolean s = false;
//        for (ARMO arm : a) {
//            if (armorHasKeyword(arm, shield, merger)) {
//                s = true;
//                break;
//            }
//        }
//
////--------------------------------------------------------------------------------------------
//        boolean l = false;
//        for (ARMO arm : a)
//		{
//            if        (	    arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR))
//		    {
//                l = true;
//                break;
//            }
//        }
//        boolean r = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.EARS))
//		    {
//                r = true;
//                break;
//            }
//        }
//        boolean w = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.DecapitateHead))
//		    {
//                w = true;
//                break;
//            }
//        }
//        boolean d = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.Decapitate))
//		    {
//                d = true;
//                break;
//            }
//        }
////--------------------------------------------------------------------------------------------
//        boolean f = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FOREARMS))
//		    {
//                f = true;
//                break;
//            }
//        }
////--------------------------------------------------------------------------------------------
//        boolean v = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CALVES))
//		    {
//                v = true;
//                break;
//            }
//        }
////--------------------------------------------------------------------------------------------
//        boolean t = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.TAIL))
//		    {
//                t = true;
//                break;
//            }
//        }
//
//        boolean z3 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn3))
//		    {
//                z3 = true;
//                break;
//            }
//        }
//
//        boolean z4 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn4))
//		    {
//                z4 = true;
//                break;
//            }
//        }
//
//        boolean z5 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn5))
//		    {
//                z5 = true;
//                break;
//            }
//        }
//
//        boolean z6 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn6))
//		    {
//                z6 = true;
//                break;
//            }
//        }
//
//        boolean z7 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn7))
//		    {
//                z7 = true;
//                break;
//            }
//        }
//
//        boolean z8 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn8))
//		    {
//                z8 = true;
//                break;
//            }
//        }
//
//        boolean z9 = false;
//        /*KYWD z9k = (KYWD) merger.getMajor("BodyAddOn9", GRUP_TYPE.KYWD);*/
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn9)
//                            /*|| armorHasKeyword(arm, z9k, merger)*/)
//		    {
//                z9 = true;
//                break;
//            }
//        }
//
//        boolean z10 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn10))
//		    {
//                z10 = true;
//                break;
//            }
//        }
//
//        boolean z11 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn11))
//		    {
//                z11 = true;
//                break;
//            }
//        }
//
//        boolean z12 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn12))
//		    {
//                z12 = true;
//                break;
//            }
//        }
//
//        boolean z13 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn13))
//		    {
//                z13 = true;
//                break;
//            }
//        }
//
//        boolean z14 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn14))
//		    {
//                z14 = true;
//                break;
//            }
//        }
//
//        boolean z15 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn15))
//		    {
//                z15 = true;
//                break;
//            }
//        }
//
//        boolean z16 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn16))
//		    {
//                z16 = true;
//                break;
//            }
//        }
//
//        boolean z17 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn17))
//		    {
//                z17 = true;
//                break;
//            }
//        }
//
//        boolean fx01 = false;
//        for (ARMO arm : a)
//		{
//            if        (     arm.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FX01))
//		    {
//                fx01 = true;
//                break;
//            }
//        }
////------------------------------------------------------------
//
//        if (h) {
//            ret = ret + "H";
//        }
//        if (c) {
//            ret = ret + "C";
//        }
//        if (g) {
//            ret = ret + "G";
//        }
//        if (b) {
//            ret = ret + "B";
//        }
//        if (s) {
//            ret = ret + "S";
//        }
//
////---------------------------------
//        if (f) {
//            ret = ret + "F";
//        }
//        if (v) {
//            ret = ret + "V";
//        }
//        if (t) {
//            ret = ret + "T";
//        }
//        if (l) {
//            ret = ret + "L";
//        }
//        if (r) {
//            ret = ret + "R";
//        }
//        if (z3) {
//            ret = ret + "A";
//        }
//        if (z4) {
//            ret = ret + "E";
//        }
//        if (z5) {
//            ret = ret + "I";
//        }
//        if (z6) {
//            ret = ret + "J";
//        }
//        if (z7) {
//            ret = ret + "K";
//        }
//        if (z8) {
//            ret = ret + "M";
//        }
//        if (w) {
//            ret = ret + "W";
//        }
//        if (d) {
//            ret = ret + "D";
//        }
//        if (z9) {
//            ret = ret + "N";
//        }
//        if (z10) {
//            ret = ret + "O";
//        }
//        if (z11) {
//            ret = ret + "P";
//        }
//        if (z12) {
//            ret = ret + "Q";
//        }
//        if (z13) {
//            ret = ret + "U";
//        }
//        if (z14) {
//            ret = ret + "X";
//        }
//        if (z15) {
//            ret = ret + "Y";
//        }
//        if (z16) {
//            ret = ret + "Z";
//        }
//        if (z17) {
//            ret = ret + "-";
//        }
//        if (fx01) {
//            ret = ret + "_";
//        }
//---------------------------------

        return ret;
    }

    static boolean addListIfNotLevel(LVLI list, LVLI subList, int level) {
        for (LeveledEntry l : list.getEntries()) {
            if (l.getLevel() == level) {
                if (l.getForm().equals(subList.getForm())) {
                    return false;
                }
            }
        }
        list.addEntry(subList.getForm(), level, 1);
        return true;
    }

    static void addArmorByBit(LVLI set, ArrayList<ARMO> ar, String bits, Mod merger) {
        
        Map<String, FirstPersonFlags[]> bitsInfo = BitsInfo.Get();
        for (String sBit : bitsInfo.keySet()){
            if (bits.contains(sBit)){
                for (ARMO a : ar) {
                    for (FirstPersonFlags flag : bitsInfo.get(sBit)){
                        if (a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, flag)){
                            if (!set.contains(a)){
                                set.addEntry(a.getForm(), 1, 1);
                            }
                            break;
                        }
                    }
                }
//                boolean isShield = false;
//                if ("S".equals(sBit)){
//                    isShield = true;
//                }
//                for (ARMO a : ar) {
//                    boolean isSet = false;
//                    if (isShield){
//                        KYWD k = hasKeyStartsWith(a, "ArmorShield", merger);
//                        if (k != null) {
//                            isSet = true;
//                        }
//                    }
//                    else{
//                        for (FirstPersonFlags flag : bitsInfo.get(sBit)){
//                            if (a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, flag)){
//                                isSet = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (isSet && !set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                }
            }
        }

//        //asdf
//        //SPGlobal.log("line 2876 set="+set, "ar="+ar,"bits="+bits);
//        //int s = 0;//для отладки
////---code start
//        if (bits.contains("H")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HEAD)
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CIRCLET)
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HAIR)/*
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR)
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.EARS)
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.DecapitateHead)
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.Decapitate)*/) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//        //asdfe Bipeds
//        if (bits.contains("C")) {
//            for (ARMO a : ar) {
//                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BODY)/*
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.TAIL)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn3)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn4)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn5)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn6)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn7)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn8)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn9)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn10)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn11)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn12)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn13)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn14)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn15)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn16)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn17)*/) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//        if (bits.contains("G")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HANDS)/*
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FOREARMS)*/) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//        if (bits.contains("B")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FEET)/*
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CALVES)*/) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//        if (bits.contains("S")) {
//            for (ARMO a : ar) {
//                KYWD k = hasKeyStartsWith(a, "ArmorShield", merger);
//                if (k != null) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//
////------------------------------------------------------------
//
//        if (bits.contains("F")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FOREARMS)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
////------------------------------------------------------------
//
//        if (bits.contains("V")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CALVES)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        //------------------------------------------------------------
//
//        if (bits.contains("L")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("R")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.EARS)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("W")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.DecapitateHead)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("D")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.Decapitate)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
////------------------------------------------------------------
//
//
//        if (bits.contains("T")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.TAIL)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("A")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn3)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("E")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn4)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("I")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn5)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("J")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn6)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("K")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn7)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("M")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn8)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("N")) {
//            /*KYWD z9k = (KYWD) merger.getMajor("BodyAddOn9", GRUP_TYPE.KYWD);*/
//
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn9)
//                        /*|| armorHasKeyword(a, z9k, merger)*/) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("O")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn10)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("P")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn11)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("Q")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn12)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("U")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn13)) {
//                    set.addEntry(a.getForm(), 1, 1);
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("X")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn14)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("Y")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn15)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("Z")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn16)) {
//                    set.addEntry(a.getForm(), 1, 1);
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("-")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn17)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }
//
//        if (bits.contains("_")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FX01)) {
//                    if (!set.contains(a)){
//                        set.addEntry(a.getForm(), 1, 1);
//                    }
//                    //s++;
//                }
//            }
//        }

        //SPGlobal.log("line 3025 set.toString()="+set,"s="+s);

//------------------------------------------------------------


    }

    static ArrayList<ARMO> addArmorByBitToArray(ArrayList<ARMO> ar, String bits, Mod merger) {
        ArrayList<ARMO> ret = new ArrayList<>(0);
        
        Map<String, FirstPersonFlags[]> bitsInfo = BitsInfo.Get();
        for (String sBit : bitsInfo.keySet()){
            if (bits.contains(sBit)){
                for (ARMO a : ar) {
                    for (FirstPersonFlags flag : bitsInfo.get(sBit)){
                        if (a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, flag)){
                            if (!ret.contains(a)){
                                ret.add(a);
                            }
                            break;
                        }
                    }
                }
//                boolean isShield = false;
//                if ("S".equals(sBit)){
//                    isShield = true;
//                }
//                for (ARMO a : ar) {
//                    boolean isSet = false;
//                    if (isShield){
//                        KYWD k = hasKeyStartsWith(a, "ArmorShield", merger);
//                        if (k != null) {
//                            isSet = true;
//                        }
//                    }
//                    else{
//                        for (FirstPersonFlags flag : bitsInfo.get(sBit)){
//                            if (a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, flag)){
//                                isSet = true;
//                                break;
//                            }
//                        }
//                    }
//                    
//                    if (isSet && !ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
            }
        }
        

       //SPGlobal.log("line 3033 addArmorByBitToArray bits=",bits);
//        if (bits.contains("H")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HEAD)
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CIRCLET)
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HAIR)/*
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR)
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.EARS)
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.DecapitateHead)
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.Decapitate)*/) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//        if (bits.contains("C")) {
//            for (ARMO a : ar) {
//                if (   a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BODY)/*
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.TAIL)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn3)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn4)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn5)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn6)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn7)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn8)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn9)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn10)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn11)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn12)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn13)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn14)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn15)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn16)
//                    || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn17)*/) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//        if (bits.contains("G")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HANDS)/*
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FOREARMS)*/) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//        if (bits.contains("B")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FEET)/*
//                        || a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CALVES)*/) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//        if (bits.contains("S")) {
//            for (ARMO a : ar) {
//                KYWD k = hasKeyStartsWith(a, "ArmorShield", merger);
//                if (k != null) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//
//        //------------------------------------------------------------
//
//        if (bits.contains("L")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("R")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.EARS)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("W")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.DecapitateHead)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("D")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.Decapitate)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//
////------------------------------------------------------------
//
//        if (bits.contains("F")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FOREARMS)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
////------------------------------------------------------------
//
//        if (bits.contains("V")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CALVES)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
////------------------------------------------------------------
//
//
//        if (bits.contains("T")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.TAIL)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("A")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn3)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("E")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn4)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("I")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn5)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("J")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn6)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("K")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn7)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("M")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn8)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("N")) {
//            /*KYWD z9k = (KYWD) merger.getMajor("BodyAddOn9", GRUP_TYPE.KYWD);*/
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn9)
//                        /*|| armorHasKeyword(a, z9k, merger)*/
//                        ) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("O")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn10)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("P")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn11)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("Q")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn12)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("U")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn13)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("X")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn14)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("Y")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn15)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("Z")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn16)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("-")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn17)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//
//        if (bits.contains("_")) {
//            for (ARMO a : ar) {
//                if (       a.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FX01)) {
//                    if (!ret.contains(a)){
//                        ret.add(a);
//                    }
//                }
//            }
//        }
//---code finish


        //SPGlobal.log("Line 3436 ret.toString()="+ret.toString());
//------------------------------------------------------------

        //asdf

        return ret;
    }

    static boolean isTiered(String name) {
        
        for (OutfitBase outfit : OutfitList.Get()) {
            if(outfit.BitsByEDIDEquals().containsKey(name)){
                return true;
            }
        }
        
        return false;
        
//        boolean ret = false;
//        ArrayList<String> names = new ArrayList<>();
//        //Bandit
//        names.add("BanditArmorMeleeHeavyOutfit");
//        names.add("BanditArmorMeleeHeavyNoShieldOutfit");
//        names.add("BanditArmorHeavyBossOutfit");
//        names.add("BanditArmorHeavyBossNoShieldOutfit");
//        names.add("BanditArmorMeleeShield20Outfit");
//        names.add("BanditArmorMeleeNoShieldOutfit");
//        names.add("BanditMageOutfit");
//        //Thalmor
//        names.add("ThalmorArmorWithHelmetOutfit");
//        //Warlock
//        names.add("WarlockOutfitLeveled");
//        //Necromancer
//        names.add("NecromancerOutfit");
//        names.add("NecromancerOutfitHood50");
//        //Imperial
//        names.add("ArmorImperialHeavyOutfit");
//        names.add("ArmorImperialHeavyOutfitNoHelmet");
//        names.add("ArmorImperialHeavyOutfitOfficer");
//        names.add("ArmorImperialLightOutfit");
//        names.add("ArmorImperialLightOutfitNoHelmet");
//        //Stormcloak
//        names.add("ArmorStormcloakOutfit");
//        names.add("ArmorStormcloakOutfitNoHelmet");
//        names.add("SonsOfSkyrimCommanderOutfit");
//        names.add("SonsOfSkyrimForelhostGrifter");
//        //Vampire
//        names.add("vampireOutfit");
//        names.add("DLC1vampireOutfit");
//        names.add("VampireArmorBossOutfit");
//        //ignore
//        /*
//        names.add("ArmorDwarvenAllOutfit");
//        names.add("ArmorDwarvenNoHelmetOutfit");
//        names.add("ArmorDwarvenSimpleOutfit");
//        names.add("ArmorEbonyAllOutfit");
//        names.add("ArmorEbonyNoHelmetOutfit");
//        names.add("ArmorEbonySimpleOutfit");
//        names.add("ArmorElvenAllOutfit");
//        names.add("ArmorElvenNoHelmetOutfit");
//        names.add("ArmorElvenSimpleOutfit");
//        names.add("ArmorGlassAllOutfit");
//        names.add("ArmorHideAllOutfit");
//        names.add("ArmorIronAllOutfit");
//        names.add("ArmorLeatherAllOutfit");
//        names.add("ArmorLeatherNoHelmetOutfit");
//        names.add("ArmorLeatherSimpleOutfit");
//        names.add("ArmorOrcishAllOutfit");
//        names.add("ArmorOrcishNoHelmetOutfit");
//        names.add("ArmorOrcStrongholdOutfit");
//        names.add("ArmorOrcStrongholdNoHelmetOutfit");
//        names.add("ArmorScaledAllOutfit");
//        names.add("ArmorScaledNoHelmetOutfit");
//        names.add("ArmorScaledSimpleOutfit");
//        names.add("ArmorScaledBAllOutfit");
//        names.add("ArmorScaledBNoHelmetOutfit");
//        names.add("ArmorScaledBSimpleOutfit");
//        names.add("ArmorSteelAllOutfit");
//        names.add("ArmorSteelNoHelmetOutfit");
//        names.add("ArmorSteelPlateAllOutfit");
//        names.add("ArmorSteelPlateNoHelmetOutfit");
//        names.add("ArmorStuddedSimpleOutfit");
//        names.add("BanditMageOutfit");
//        */
//        //Child
//        names.add("ChildOutfit01");
//        names.add("ChildOutfit02");
//        names.add("ChildOutfit03");
//        names.add("ChildOutfit04");
//        names.add("ChildOutfit05");
//        names.add("DLC2SkaalOutfitChild");/*
//        //Companion
//        names.add("ArmorCompanionsAllOutfit");
//        names.add("ArmorCompanionsOutfitNoHelmet");
//        names.add("ArmorCompanionsOutfitNoHelmetNoGloves");*/
//        //Farm
//        names.add("FarmClothesOutfit01");
//        names.add("FarmClothesOutfit01WithHat");
//        names.add("FarmClothesOutfit02");
//        names.add("FarmClothesOutfit02Variant");
//        names.add("FarmClothesOutfit02WithHat");
//        names.add("FarmClothesOutfit03");
//        names.add("FarmClothesOutfit03Variant");
//        names.add("FarmClothesOutfit03withextras");
//        names.add("FarmClothesOutfit03withHat");
//        names.add("FarmClothesOutfit03withHat01");
//        names.add("FarmClothesOutfit03withHat02");
//        names.add("FarmClothesOutfit03withHat02withHat03");
//        names.add("FarmClothesOutfit03withHideBootsandBracers");
//        names.add("FarmClothesOutfit04");
//        names.add("FarmClothesOutfit04Variant");
//        names.add("FarmClothesRandom");
//        //Noble
//        names.add("FineClothesOutfit01");
//        names.add("FineClothesOutfit01Variant");
//        names.add("FineClothesOutfit01WithHat");
//        names.add("FineClothesOutfit02");
//        names.add("FineClothesOutfit02Variant");
//        names.add("FineClothesOutfit02VariantWithHat");
//        names.add("FineClothesOutfit02withGloves");
//        names.add("FineClothesOutfit02WithHat");
//        //Jarl
//        names.add("JarlClothesOutfit01");
//        names.add("JarlClothesOutfit02");
//        names.add("JarlClothesOutfit03");
//        names.add("JarlClothesBalgruuf");
//        names.add("JarlClothesBryling");
//        names.add("JarlClothesElisif");
//        names.add("JarlClothesIgmund");
//        names.add("JarlClothesKraldar");
//        names.add("JarlClothesLaila");
//        names.add("JarlClothesSiddgeir");
//        names.add("JarlClothesSkaldTheElder");
//        names.add("JarlClothesTorygg");
//        //Redguard
//        names.add("RedguardClothesOutfit01");
//        names.add("RedguardClothesOutfitDB01");
//        names.add("RedguardClothesOutfitNoHat");
//        //Draugr
//        names.add("DraugrHair01Outfit");
//        names.add("DraugrHair02Outfit");
//        names.add("DraugrBeard01Outfit");
//        names.add("DraugrBeard02Outfit");
//        names.add("DraugrHair01Beard01");
//        names.add("DraugrHair01Beard02");
//        names.add("DraugrHair02Beard01");
//        names.add("DraugrHair02Beard02");
//        names.add("Draugr02Helmet01Beard01Outfit");
//        names.add("Draugr02Helmet01Beard02Outfit");
//        names.add("Draugr04Helmet02Beard01Outfit");
//        names.add("Draugr04Helmet02Beard02Outfit");
//        names.add("Draugr05Helmet03Beard01Outfit");
//        names.add("Draugr05Helmet03Beard02Outfit");
//        names.add("Draugr02Helmet01Outfit");
//        names.add("Draugr04Helmet02Outfit");
//        names.add("Draugr05Helmet03Outfit");
//        //Vigilant
//        names.add("VigilantOfStendarrOutfit");
//        names.add("VigilantOfStendarrOutfitHood");
//        names.add("VigilantOfStendarrOutfitNoHood");
//        //Dawnguard
//        names.add("DLC1OutfitDawnguard01Heavy");
//        names.add("DLC1OutfitDawnguard02Heavy");
//        names.add("DLC1OutfitDawnguard03");
//        names.add("DLC1OutfitDawnguard04");
//        names.add("DLC1OutfitDawnguard05");
//        names.add("DLC1OutfitDawnguardAll");
//        names.add("DawnguardArmorMelee");
//        //Skaal
//        names.add("DLC2SkaalOutfitChanceHat");
//        names.add("DLC2SkaalOutfitHat");
//        names.add("DLC2SkaalOutfitNoHat");
//        //Hunter
//        names.add("HunterClothesRND");
//        names.add("HunterOutfit01");
//        names.add("HunterOutfit01Hooded");
//        names.add("HunterOutfit02");
//        names.add("HunterOutfit02Hooded");
//        names.add("HunterOutfit03");
//        names.add("HunterOutfit04");
//        names.add("HunterOutfit04Hooded");
//        //Wench
//        names.add("ClothesTavernWenchOutfit");
//        //Blades
//        names.add("ArmorBladesOutfitNoHelmet");
//        names.add("ArmorBladesOutfit");
//        //Forsworn
//        names.add("ForswornArmorBossOutfit");
//        names.add("ForswornArmorMeleeOutfit");
//        names.add("ForswornArmorMissileOutfit");
//        names.add("ForswornArmorMagicOutfit");
//        names.add("ForswornArmorBossOutfit");
//
//        for (String s : names) {
//            if (name.contentEquals(s)) {
//                ret = true;
//                break;
//            }
//        }
//
//        return ret;
    }

    static String getTierKey(String name) {
        
        for (OutfitBase outfit : OutfitList.Get()) {
            String tierKey = outfit.GetTierKey(name);
            if(!tierKey.isEmpty()){
                return tierKey;
            }
        }
        return null;        
        
//        String ret = null;
//        //Bandit
//        if (name.startsWith("BanditArmorMeleeHeavyOutfit") || name.startsWith("BanditArmorMeleeHeavyNoShieldOutfit")) {
//            ret = "BanditHeavy_Tier_";
//        }
//        else if (name.startsWith("BanditArmorMeleeShield20Outfit") || name.startsWith("BanditArmorMeleeNoShieldOutfit")) {
//            ret = "BanditLight_Tier_";
//        }
//        else if (name.startsWith("BanditArmorHeavyBossOutfit") || name.startsWith("BanditArmorHeavyBossNoShieldOutfit")) {
//            ret = "BanditBoss_Tier_";
//        }
//        //Thalmor
//        else if (name.startsWith("ThalmorArmorWithHelmetOutfit")) {
//            ret = "Thalmor_Tier_";
//        }
//        //Warlock
//        else if (name.startsWith("WarlockOutfitLeveled")) {
//            ret = "Warlock_Tier_";
//        }
//        //Necromancer
//        else if (name.startsWith("NecromancerOutfit")) {
//            ret = "Necromancer_Tier_";
//        }
//        //Imperial
//        else if (name.startsWith("ArmorImperialHeavyOutfit")) {
//            ret = "ImperialHeavy_Tier_";
//        }
//        else if (name.startsWith("ArmorImperialHeavyOutfitOfficer")) {
//            ret = "ImperialBoss_Tier_";
//        }
//        else if (name.startsWith("ArmorImperialLightOutfit")) {
//            ret = "ImperialLight_Tier_";
//        }
//        //Stormcloak
//        else if (name.startsWith("ArmorStormcloakOutfit")) {
//            ret = "Stormcloak_Tier_";
//        }
//        else if (name.contentEquals("SonsOfSkyrimCommanderOutfit") || name.contentEquals("SonsOfSkyrimForelhostGrifter")) {
//            ret = "StormcloakBoss_Tier_";
//        }
//        //Vampire
//        else if ( name.startsWith("VampireArmorBossOutfit") ) {
//            ret = "VampireBoss_Tier_";
//        }
//        else if (name.contentEquals("vampireOutfit") || name.contentEquals("DLC1vampireOutfit")) {
//            ret = "Vampire_Tier_";
//        }
//        //Ignore
//        /*
//        else if (name.contentEquals("ArmorDwarvenAllOutfit")) {
//            ret = "Dwarven_Tier_";
//        }
//        else if (name.contentEquals("ArmorDwarvenNoHelmetOutfit")) {
//            ret = "Dwarven_Tier_";
//        }
//        else if (name.contentEquals("ArmorDwarvenSimpleOutfit")) {
//            ret = "Dwarven_Tier_";
//        }
//        else if (name.contentEquals("ArmorEbonyAllOutfit")) {
//            ret = "Ebony_Tier_";
//        }
//        else if (name.contentEquals("ArmorEbonyNoHelmetOutfit")) {
//            ret = "Ebony_Tier_";
//        }
//        else if (name.contentEquals("ArmorEbonySimpleOutfit")) {
//            ret = "Ebony_Tier_";
//        }
//        else if (name.contentEquals("ArmorElvenAllOutfit")) {
//            ret = "Elven_Tier_";
//        }
//        else if (name.contentEquals("ArmorElvenNoHelmetOutfit")) {
//            ret = "Elven_Tier_";
//        }
//        else if (name.contentEquals("ArmorElvenSimpleOutfit")) {
//            ret = "Elven_Tier_";
//        }
//        else if (name.contentEquals("ArmorGlassAllOutfit")) {
//            ret = "Glass_Tier_";
//        }
//        else if (name.contentEquals("ArmorHideAllOutfit")) {
//            ret = "Hide_Tier_";
//        }
//        else if (name.contentEquals("ArmorIronAllOutfit")) {
//            ret = "Iron_Tier_";
//        }
//        else if (name.contentEquals("ArmorLeatherAllOutfit")) {
//            ret = "Leather_Tier_";
//        }
//        else if (name.contentEquals("ArmorLeatherNoHelmetOutfit")) {
//            ret = "Leather_Tier_";
//        }
//        else if (name.contentEquals("ArmorLeatherSimpleOutfit")) {
//            ret = "Leather_Tier_";
//        }
//        else if (name.contentEquals("ArmorOrcishAllOutfit")) {
//            ret = "Orcish_Tier_";
//        }
//        else if (name.contentEquals("ArmorOrcishNoHelmetOutfit")) {
//            ret = "Orcish_Tier_";
//        }
//        else if (name.contentEquals("ArmorOrcStrongholdOutfit")) {
//            ret = "Orcish_Tier_";
//        }
//        else if (name.contentEquals("ArmorOrcStrongholdNoHelmetOutfit")) {
//            ret = "Orcish_Tier_";
//        }
//        else if (name.contentEquals("ArmorScaledAllOutfit")) {
//            ret = "Scaled_Tier_";
//        }
//        else if (name.contentEquals("ArmorScaledNoHelmetOutfit")) {
//            ret = "Scaled_Tier_";
//        }
//        else if (name.contentEquals("ArmorScaledSimpleOutfit")) {
//            ret = "Scaled_Tier_";
//        }
//        else if (name.contentEquals("ArmorScaledBAllOutfit")) {
//            ret = "ScaledHorned_Tier_";
//        }
//        else if (name.contentEquals("ArmorScaledBNoHelmetOutfit")) {
//            ret = "ScaledHorned_Tier_";
//        }
//        else if (name.contentEquals("ArmorScaledBSimpleOutfit")) {
//            ret = "ScaledHorned_Tier_";
//        }
//        else if (name.contentEquals("ArmorSteelAllOutfit")) {
//            ret = "Steel_Tier_";
//        }
//        else if (name.contentEquals("ArmorSteelNoHelmetOutfit")) {
//            ret = "Steel_Tier_";
//        }
//        else if (name.contentEquals("ArmorSteelPlateAllOutfit")) {
//            ret = "SteelPlate_Tier_";
//        }
//        else if (name.contentEquals("ArmorSteelPlateNoHelmetOutfit")) {
//            ret = "SteelPlate_Tier_";
//        }
//        else if (name.contentEquals("ArmorStuddedSimpleOutfit")) {
//            ret = "StuddedPlate_Tier_";
//        }
//        else if (name.contentEquals("BanditMageOutfit")) {
//            ret = "BanditMage_Tier_";
//        }*/
//        //Child
//        else if ( name.startsWith("ChildOutfit") || name.contentEquals("DLC2SkaalOutfitChild") ) {
//            ret = "Child_Tier_";
//        }
//        //Ignore
//        /*
//        else if (name.contentEquals("ArmorCompanionsAllOutfit")) {
//            ret = "Companions_Tier_";
//        }
//        else if (name.contentEquals("ArmorCompanionsOutfitNoHelmet")) {
//            ret = "Companions_Tier_";
//        }
//        else if (name.contentEquals("ArmorCompanionsOutfitNoHelmetNoGloves")) {
//            ret = "Companions_Tier_";
//        }*/
//        //Farm
//        else if (name.startsWith("FarmClothes")) {
//            ret = "Farm_Tier_";
//        }
//        //Noble
//        else if (name.startsWith("FineClothesOutfit")) {
//            ret = "Noble_Tier_";
//        }
//        //Jarl
//        else if (name.startsWith("JarlClothes")) {
//            ret = "Jarl_Tier_";
//        }
//        //Redguard
//        else if (name.startsWith("RedguardClothesOutfit")) {
//            ret = "Redguard_Tier_";
//        }
//        //Draugr
//        else if ( name.startsWith("DraugrHair") || name.startsWith("DraugrBeard") || name.startsWith("Draugr01Helmet") || name.startsWith("Draugr02Helmet") || name.startsWith("Draugr03Helmet") || name.startsWith("Draugr04Helmet") || name.startsWith("Draugr05Helmet") ) {
//            ret = "Draugr_Tier_";
//        }
//        //Vigilant
//        else if (name.startsWith("VigilantOfStendarrOutfit")) {
//            ret = "Vigilant_Tier_";
//        }
//        //Dawnguard
//        else if ( name.contentEquals("DLC1OutfitDawnguardAll") || name.contentEquals("DawnguardArmorMelee") ) {
//            ret = "Dawnguard_Tier_";
//        }
//        else if ( name.contentEquals("DLC1OutfitDawnguard01Heavy") || name.contentEquals("DLC1OutfitDawnguard02Heavy") ) {
//            ret = "DawnguardHeavy_Tier_";
//        }
//        else if (name.startsWith("DLC1OutfitDawnguard") || name.startsWith("DawnguardOutfit")) {
//            ret = "DawnguardLight_Tier_";
//        }
//        //Skaal
//        else if (name.contentEquals("DLC2SkaalOutfitChanceHat") || name.contentEquals("DLC2SkaalOutfitHat") || name.contentEquals("DLC2SkaalOutfitNoHat") ) {
//            ret = "Skaal_Tier_";
//        }
//        //Hunter
//        else if (name.contentEquals("HunterClothesRND") || name.startsWith("HunterOutfit")) {
//            ret = "Hunter_Tier_";
//        }
//        //Wench
//        else if (name.contentEquals("ClothesTavernWenchOutfit")) {
//            ret = "Wench_Tier_";
//        }
//        //Blades
//        else if (name.startsWith("ArmorBladesOutfit")) {
//            ret = "Blades_Tier_";
//        }
//        //Forsworn
//        else if (name.contentEquals("ForswornArmorBossOutfit")) {
//            ret = "ForswornBoss_Tier_";
//        }
//        else if (name.startsWith("ForswornArmor")) {
//            ret = "Forsworn_Tier_";
//        }
//
//        return ret;
    }

    static String getBits(String name) {
        
        for (OutfitBase outfit : OutfitList.Get()) {
            String bits = outfit.GetBits(name);
            if(!bits.isEmpty()){
                return bits;
            }
        }
        
        return null;
        
//        String ret = null;
//        //Bandit
//        if (name.startsWith("BanditArmorMeleeHeavyOutfit")) {
//            //ret = "HCGB";
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.startsWith("BanditArmorMeleeHeavyNoShieldOutfit")) {
//            //ret = "HCGB";
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.startsWith("BanditArmorHeavyBossOutfit")) {
//            //ret = "HCGB";
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.startsWith("BanditArmorHeavyBossNoShieldOutfit")) {
//            //ret = "HCGB";
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.startsWith("BanditArmorMeleeShield20Outfit")) {
//            //ret = "HCBG";
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.startsWith("BanditArmorMeleeNoShieldOutfit")) {
//            //ret = "CBG";
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Thalmor
//        else if (name.startsWith("ThalmorArmorWithHelmetOutfit")) {
//            //ret = "HCBG";
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Warlock
//        else if (name.startsWith("WarlockOutfitLeveled")) {
//            //ret = "HCBG";
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Necromancer
//        else if (name.contentEquals("NecromancerOutfit")) {
//            //ret = "HCGB";
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("NecromancerOutfitHood50")) {
//            //ret = "HCGB";
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Imperial
//        else if (name.contentEquals("ArmorImperialHeavyOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorImperialHeavyOutfitNoHelmet")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorImperialHeavyOutfitOfficer")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorImperialLightOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorImperialLightOutfitNoHelmet")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Stormcloak
//        else if (name.contentEquals("ArmorStormcloakOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorStormcloakOutfitNoHelmet")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("SonsOfSkyrimCommanderOutfit") || name.contentEquals("SonsOfSkyrimForelhostGrifter")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Vampire
//        else if (name.contentEquals("vampireOutfit") || name.contentEquals("DLC1vampireOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("VampireArmorBossOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Ignore
//        /*
//        else if (name.contentEquals("ArmorDwarvenAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorDwarvenNoHelmetOutfit")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorDwarvenSimpleOutfit")) {
//            ret = "CBVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorEbonyAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorEbonyNoHelmetOutfit")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorEbonySimpleOutfit")) {
//            ret = "CBVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorElvenAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorElvenNoHelmetOutfit")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorElvenSimpleOutfit")) {
//            ret = "CBVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorGlassAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorHideAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorIronAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorLeatherAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorLeatherNoHelmetOutfit")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorLeatherSimpleOutfit")) {
//            ret = "CBVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorOrcishAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorOrcishNoHelmetOutfit")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorOrcStrongholdOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorOrcStrongholdNoHelmetOutfit")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorScaledAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorScaledNoHelmetOutfit")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorScaledSimpleOutfit")) {
//            ret = "CBVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorScaledBAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorScaledBNoHelmetOutfit")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorScaledBSimpleOutfit")) {
//            ret = "CBVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorSteelAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorSteelNoHelmetOutfit")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorSteelPlateAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorSteelPlateNoHelmetOutfit")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorStuddedSimpleOutfit")) {
//            ret = "CGBVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("BanditMageOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }*/
//        //Child
//        else if ( name.startsWith("ChildOutfit") || name.contentEquals("DLC2SkaalOutfitChild") ) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Ignore
//        /*        
//        else if (name.contentEquals("ArmorCompanionsAllOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorCompanionsOutfitNoHelmet")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorCompanionsOutfitNoHelmetNoGloves")) {
//            ret = "CBVTLRAEIJKMWDNOPQUXYZ-_";
//        }*/
//        //Farm
//        else if (name.startsWith("FarmClothes")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Noble
//        else if (name.startsWith("FineClothesOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Jarl
//        else if (name.startsWith("JarlClothes")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Ignore
//        /*варианты
//        else if (name.contentEquals("FineClothesOutfit01")) {
//            ret = "CBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("FineClothesOutfit01Variant")) {
//            ret = "CBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("FineClothesOutfit01WithHat")) {
//            ret = "HCBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("FineClothesOutfit02")) {
//            ret = "CBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("FineClothesOutfit02Variant")) {
//            ret = "CBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("FineClothesOutfit02VariantWithHat")) {
//            ret = "HCBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("FineClothesOutfit02withGloves")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("FineClothesOutfit02WithHat")) {
//            ret = "HCBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }*/
//        //Redguard
//        else if (name.contentEquals("RedguardClothesOutfitNoHat")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.startsWith("RedguardClothesOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Draugr
//        else if ( name.startsWith("DraugrHair") || name.startsWith("DraugrBeard")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if ( name.startsWith("Draugr01Helmet") || name.startsWith("Draugr02Helmet") || name.startsWith("Draugr03Helmet") || name.startsWith("Draugr04Helmet") || name.startsWith("Draugr05Helmet") ) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Vigilant
//        else if (name.startsWith("VigilantOfStendarrOutfitNoHood")) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.startsWith("VigilantOfStendarrOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Dawnguard
//        else if ( name.contentEquals("DLC1OutfitDawnguardAll") || name.contentEquals("DawnguardArmorMelee") ) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if ( name.contentEquals("DLC1OutfitDawnguard01Heavy") || name.contentEquals("DLC1OutfitDawnguard02Heavy") ) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.startsWith("DLC1OutfitDawnguard") || name.startsWith("DawnguardOutfit")) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Skaal
//        else if (name.contentEquals("DLC2SkaalOutfitChanceHat") || name.contentEquals("DLC2SkaalOutfitHat") ) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("DLC2SkaalOutfitNoHat") ) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Hunter
//        else if (name.contentEquals("HunterClothesRND") || name.contentEquals("HunterOutfit01Hooded") || name.contentEquals("HunterOutfit02Hooded") || name.contentEquals("HunterOutfit03Hooded") || name.contentEquals("HunterOutfit04Hooded") ) {
//            ret = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.startsWith("HunterOutfit") ) {
//            ret = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Wench
//        else if (name.contentEquals("ClothesTavernWenchOutfit") ) {
//            ret = "CBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Blades
//        else if (name.contentEquals("ArmorBladesOutfit") ) {
//            ret = "HCBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.contentEquals("ArmorBladesOutfitNoHelmet") ) {
//            ret = "CBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        //Forsworn
//        else if (name.contentEquals("AForswornArmorBossOutfit") ) {
//            ret = "HCBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//        else if (name.startsWith("ForswornArmor") ) {
//            ret = "HCBFVTLRAEIJKMWDNOPQUXYZ-_";
//        }
//
//        return ret;
    }

    static boolean needsShield(String name) {
        boolean ret = false;
        if (name.startsWith("BanditArmorMeleeHeavyOutfit")) {
            ret = true;
        }
        else if (name.startsWith("BanditArmorHeavyBossOutfit")) {
            ret = true;
        }
        else if (name.startsWith("BanditArmorMeleeShield20Outfit")) {
            ret = true;
        }
        //Blades
        else if (name.startsWith("ArmorBladesOutfit")) {
            ret = true;
        }

        return ret;
    }

    static FormID shieldForm(String name) {
        FormID ret = FormID.NULL;
        if (name.startsWith("BanditArmorMeleeHeavyOutfit")) {
            ret = new FormID("039d2d", "Skyrim.esm");
        }
        else if (name.startsWith("BanditArmorHeavyBossOutfit")) {
            ret = new FormID("03df22", "Skyrim.esm");
        }
        else if (name.startsWith("BanditArmorMeleeShield20Outfit")) {
            ret = new FormID("0c0196", "Skyrim.esm");
        }
        else if (name.startsWith("ArmorBladesOutfit")) {
            ret = new FormID("04F912", "Skyrim.esm");
        }

        return ret;
    }

    static KYWD getSlotKYWD(ARMO armor, Mod merger) {
        KYWD ret = null;
        //asdf Armor Keys
        if ( armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HEAD)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HAIR)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR)
                /*
		|| armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.EARS)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.DecapitateHead)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.Decapitate)*/) {
            
            if(armor.getBodyTemplate().getArmorType(BodyTemplate.BodyTemplateType.Biped).equals(ArmorType.CLOTHING)){
                ret = (KYWD) merger.getMajor("ClothingHead" , GRUP_TYPE.KYWD);
            }else{
                ret = (KYWD) merger.getMajor("ArmorHelmet", GRUP_TYPE.KYWD);
            }
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CIRCLET)) {            
            if(armor.getBodyTemplate().getArmorType(BodyTemplate.BodyTemplateType.Biped).equals(ArmorType.CLOTHING)){
                ret = (KYWD) merger.getMajor("ClothingCirclet", GRUP_TYPE.KYWD);
            }else{
                ret = (KYWD) merger.getMajor("ArmorHelmet", GRUP_TYPE.KYWD);
            }
        }
        else if (  armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BODY)/*
		|| armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.TAIL)
		|| armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn3)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn4)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn5)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn6)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn7)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn8)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn9)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn10)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn11)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn12)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn13)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn14)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn15)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn16)
                || armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn17)*/) {
            ret = (KYWD) merger.getMajor("ArmorCuirass", GRUP_TYPE.KYWD);
                }
        else if (  armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.HANDS)/*
		|| armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FOREARMS)*/) {
            ret = (KYWD) merger.getMajor("ArmorGauntlets", GRUP_TYPE.KYWD);
                }
        else if (  armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FEET)/*
		|| armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CALVES)*/) {
            ret = (KYWD) merger.getMajor("ArmorBoots", GRUP_TYPE.KYWD);
                }
        else if (  armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.SHIELD)) {
            ret = (KYWD) merger.getMajor("ArmorShield", GRUP_TYPE.KYWD);
                }
        else if (  armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.RING)) {
            ret = (KYWD) merger.getMajor("ClothingRing", GRUP_TYPE.KYWD);
                }
        else if (  armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.AMULET)) {
            ret = (KYWD) merger.getMajor("ClothingNecklace", GRUP_TYPE.KYWD);
                }
/*
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FOREARMS)) {
            ret = (KYWD) merger.getMajor("FOREARMS", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CALVES)) {
            ret = (KYWD) merger.getMajor("CALVES", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.TAIL)) {
            ret = (KYWD) merger.getMajor("TAIL", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR)) {
            ret = (KYWD) merger.getMajor("LONG_HAIR", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.EARS)) {
            ret = (KYWD) merger.getMajor("EARS", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn3)) {
            ret = (KYWD) merger.getMajor("BodyAddOn3", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn4)) {
            ret = (KYWD) merger.getMajor("BodyAddOn4", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn5)) {
            ret = (KYWD) merger.getMajor("BodyAddOn5", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn6)) {
            ret = (KYWD) merger.getMajor("BodyAddOn6", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn7)) {
            ret = (KYWD) merger.getMajor("BodyAddOn7", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn8)) {
            ret = (KYWD) merger.getMajor("BodyAddOn8", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.DecapitateHead)) {
            ret = (KYWD) merger.getMajor("DecapitateHead", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.Decapitate)) {
            ret = (KYWD) merger.getMajor("Decapitate", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn9)) {
            ret = (KYWD) merger.getMajor("BodyAddOn9", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn10)) {
            ret = (KYWD) merger.getMajor("BodyAddOn10", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn11)) {
            ret = (KYWD) merger.getMajor("BodyAddOn11", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn12)) {
            ret = (KYWD) merger.getMajor("BodyAddOn12", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn13)) {
            ret = (KYWD) merger.getMajor("BodyAddOn13", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn14)) {
            ret = (KYWD) merger.getMajor("BodyAddOn14", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn15)) {
            ret = (KYWD) merger.getMajor("BodyAddOn15", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn16)) {
            ret = (KYWD) merger.getMajor("BodyAddOn16", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn17)) {
            ret = (KYWD) merger.getMajor("BodyAddOn17", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FX01)) {
            ret = (KYWD) merger.getMajor("FX01", GRUP_TYPE.KYWD);
        }
*/
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FOREARMS)) {
            ret = (KYWD) merger.getMajor("ClothingHands", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.CALVES)) {
            ret = (KYWD) merger.getMajor("ClothingFeet", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.TAIL)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        //else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.LONG_HAIR)) {
        //    ret = (KYWD) merger.getMajor(armor.getBodyTemplate().getArmorType(BodyTemplate.BodyTemplateType.Biped).equals(ArmorType.CLOTHING)?"ClothingHead":"ArmorHelmet", GRUP_TYPE.KYWD);
        //}
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.EARS)) {
            ret = (KYWD) merger.getMajor("ClothingHead", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn3)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn4)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn5)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn6)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn7)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn8)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.DecapitateHead)) {
            ret = (KYWD) merger.getMajor("ClothingHead", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.Decapitate)) {
            ret = (KYWD) merger.getMajor("ClothingHead", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn9)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn10)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn11)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn12)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn13)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn14)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn15)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn16)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.BodyAddOn17)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        else if (armor.getBodyTemplate().get(BodyTemplate.BodyTemplateType.Biped, skyproc.genenums.FirstPersonFlags.FX01)) {
            ret = (KYWD) merger.getMajor("ClothingBody", GRUP_TYPE.KYWD);
        }
        //SPGlobal.log("getSlotKYWD returned ret=", ret.toString());
        return ret;
    }
}