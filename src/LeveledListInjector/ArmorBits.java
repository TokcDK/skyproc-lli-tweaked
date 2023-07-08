package LeveledListInjector;

import java.util.HashMap;
import java.util.Map;
import skyproc.genenums.FirstPersonFlags;

class BitsInfo {    
    static Map<String, FirstPersonFlags[]> Get(){
        Map<String, FirstPersonFlags[]> bits = new HashMap<>();
        bits.put("H", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.CIRCLET,
            skyproc.genenums.FirstPersonFlags.HEAD,
            skyproc.genenums.FirstPersonFlags.HAIR
        });
        bits.put("C", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BODY
        });
        bits.put("G", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.HANDS
        });
        bits.put("B", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.FEET
        });
        bits.put("S", new FirstPersonFlags[]{
            skyproc.genenums.FirstPersonFlags.SHIELD
        });
        bits.put("F", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.FOREARMS
        });
        bits.put("V", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.CALVES
        });
        bits.put("T", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.TAIL
        });
        bits.put("L", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.LONG_HAIR
        });
        bits.put("R", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.EARS
        });
        bits.put("A", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn3
        });
        bits.put("E", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn4
        });
        bits.put("I", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn5
        });
        bits.put("J", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn6
        });
        bits.put("K", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn7
        });
        bits.put("M", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn8
        });
        bits.put("W", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.DecapitateHead
        });
        bits.put("D", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.Decapitate
        });
        bits.put("N", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn9
        });
        bits.put("O", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn10
        });
        bits.put("P", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn11
        });
        bits.put("Q", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn12
        });
        bits.put("U", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn13
        });
        bits.put("X", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn14
        });
        bits.put("Y", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn15
        });
        bits.put("Z", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn16
        });
        bits.put("-", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.BodyAddOn17
        });
        bits.put("_", new FirstPersonFlags[] {
            skyproc.genenums.FirstPersonFlags.FX01
        });
        
        return bits;
    }    
}
