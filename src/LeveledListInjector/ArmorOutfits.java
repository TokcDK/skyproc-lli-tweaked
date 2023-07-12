package LeveledListInjector;

import java.util.HashMap;
import java.util.Map;

// OUTFITS ---------------------------------------------------------------------
    
    class OutfitList {
        static OutfitBase[] Get() {
            return new OutfitBase[] {
                new BanditOutfit(),
                new ThalmorOutfit(),
                new NecromancerOutfit(),
                new WarlockOutfit(),
                new ImperialOutfit(),
                new StormcloakOutfit(),
                new VampireOutfit(),
                new ChildOutfit(),
                new NobleOutfit(),
                new JarlOutfit(),
                new RedguardOutfit(),
                new FarmOutfit(),
                new DraugrOutfit(),
                new VigilantOutfit(),
                new DawnguardOutfit(),
                new SkaalOutfit(),
                new HunterOutfit(),
                new WenchOutfit(),
                new BladesOutfit(),
                new ForswornOutfit()
            };
        }
    }
    
    abstract class OutfitBase {        
        abstract String Name();
        
        // getBits
        String GetTierKey(String name) {
            return GetBitsOrKey(name, true);
        }
        String GetBits(String name) {
            return GetBitsOrKey(name, false);
        }
        String GetBitsOrKey(String name, boolean getTierKey) {
            if(BitsByEDIDEquals().containsKey(name)){
                if (getTierKey){
                    if (name.contains("Boss")){
                        return Name() + "Boss_Tier_";
                    }
                    else if (name.contains("Heavy")){
                        return Name() + "Heavy_Tier_";
                    }
                    else if (name.contains("Light")){
                        return Name() + "Light_Tier_";
                    }
                    else{
                        return Name() + "_Tier_";
                    }
                }
                return BitsByEDIDEquals().get(name);
            }           
            for (String edid : BitsByEDIDStartsWith().keySet()){
                if (name.startsWith(edid)){
                    return BitsByEDIDStartsWith().get(edid);
                }
            }
            return "";
        }
        Map<String, String> BitsByEDIDStartsWith(){
            return new HashMap<>();
        }
        Map<String, String> BitsByEDIDEquals(){
            return new HashMap<>();
        }      
        
        protected String AllButHelmetGloves = "CBFVTLRAEIJKMWDNOPQUXYZ-_";
        protected String AllButHelmet = "CGBFVTLRAEIJKMWDNOPQUXYZ-_";
        protected String AllButGloves = "HCBFVTLRAEIJKMWDNOPQUXYZ-_";
        protected String AllWithHelmet = "HCGBFVTLRAEIJKMWDNOPQUXYZ-_";
    }
    class BanditOutfit extends OutfitBase { 
        @Override
        String Name() {
            return "Bandit";
        }
        
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("BanditArmorHeavyBossNoShieldOutfit", AllWithHelmet);
            edidbits.put("BanditArmorHeavyBossOutfit", AllWithHelmet);
            edidbits.put("BanditArmorMeleeHeavyNoShieldOutfit", AllWithHelmet);
            edidbits.put("BanditArmorMeleeHeavyOutfit", AllWithHelmet);
            edidbits.put("BanditArmorMeleeNoShieldOutfit", AllButHelmet);
            edidbits.put("BanditArmorMeleeShield20Outfit", AllButHelmet);
            edidbits.put("BanditArmorMissileOutfit", AllButGloves);
            edidbits.put("BanditMageOutfit", AllButHelmetGloves);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("BanditArmorHeavy", AllWithHelmet);
            edidbits.put("BanditArmor", AllButHelmet);
            edidbits.put("BanditMageOutfit", AllButHelmetGloves);
            return edidbits;
        }   
    }
    class ThalmorOutfit extends OutfitBase { 
        @Override
        String Name() {
            return "Thalmor";
        }
        
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ThalmorArmorNoHelmetOutfit", AllButHelmet);
            edidbits.put("ThalmorArmorWithHelmetOutfit", AllWithHelmet);
            edidbits.put("ThalmorOutfit01", AllWithHelmet);
            edidbits.put("ThalmorOutfitNoHood", AllButHelmet);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ThalmorArmor", AllWithHelmet);
            edidbits.put("ThalmorOutfit", AllWithHelmet);
            return edidbits;
        }   
    }
    class WarlockOutfit extends OutfitBase { 
        @Override
        String Name() {
            return "Warlock";
        }
        
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("WarlockOutfitLeveled", AllButHelmetGloves);
            edidbits.put("WarlockOutfitSimple", AllButHelmetGloves);
            edidbits.put("WarlockOutfitSimpleWithHood", AllWithHelmet);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("WarlockOutfit", AllWithHelmet);
            return edidbits;
        }   
    }
    class NecromancerOutfit extends OutfitBase { 
        @Override
        String Name() {
            return "Necromancer";
        }
        
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("NecromancerOutfit", AllButHelmet);
            edidbits.put("NecromancerOutfitHood50", AllWithHelmet);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("NecromancerOutfit", AllWithHelmet);
            return edidbits;
        }   
    }
    class ImperialOutfit extends OutfitBase { 
        @Override
        String Name() {
            return "Imperial";
        }
        
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ArmorImperialHeavyOutfit", AllWithHelmet);
            edidbits.put("ArmorImperialHeavyOutfitNoHelmet", AllButHelmet);
            edidbits.put("ArmorImperialHeavyOutfitOfficer", AllWithHelmet);
            edidbits.put("ArmorImperialLightOutfit", AllWithHelmet);
            edidbits.put("ArmorImperialLightOutfitNoHelmet", AllButHelmet);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ArmorImperial", AllWithHelmet);
            return edidbits;
        }   
    }
    class StormcloakOutfit extends OutfitBase { 
        @Override
        String Name() {
            return "Stormcloak";
        }
        
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ArmorStormcloakOutfit", AllWithHelmet);
            edidbits.put("ArmorStormcloakOutfitNoHelmet", AllButHelmet);
            edidbits.put("SonsOfSkyrimCommanderOutfit", AllWithHelmet);
            edidbits.put("SonsOfSkyrimForelhostGrifter", AllWithHelmet);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ArmorStormcloak", AllWithHelmet);
            edidbits.put("SonsOfSkyrim", AllWithHelmet);
            return edidbits;
        }   
    }
    class VampireOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Vampire";
        }
        
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("vampireOutfit", AllButHelmet);
            edidbits.put("DLC1vampireOutfit", AllButHelmetGloves);
            edidbits.put("VampireArmorBossOutfit", AllWithHelmet);
            edidbits.put("DLC1VampireClotheOnly", AllWithHelmet);
            edidbits.put("DLC1vampireOutfitHigh", AllButHelmetGloves);
            edidbits.put("DLC1VolkiharVampireOutfitGray", AllButHelmet);
            edidbits.put("DLC1VolkiharVampireOutfitGrayLight", AllButHelmet);
            edidbits.put("DLC1VolkiharVampireOutfitLightRed", AllButHelmet);
            edidbits.put("DLC1VolkiharVampireOutfitRandom", AllButHelmet);
            edidbits.put("DLC1VolkiharVampireOutfitRed", AllButHelmet);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            return edidbits;
        }    
    }
    class ChildOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Child";
        }
        
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ChildOutfit01", AllButHelmetGloves);
            edidbits.put("ChildOutfit02", AllButHelmetGloves);
            edidbits.put("ChildOutfit03", AllButHelmetGloves);
            edidbits.put("ChildOutfit04", AllButHelmetGloves);
            edidbits.put("ChildOutfit05", AllButHelmetGloves);
            edidbits.put("DLC2SkaalOutfitChild", AllButHelmetGloves);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ChildOutfit", AllWithHelmet);
            edidbits.put("DLC2SkaalOutfitChild", AllWithHelmet);
            return edidbits;
        }    
    }
    class FarmOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Farm";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("FarmClothesOutfit01", AllButHelmetGloves);
            edidbits.put("FarmClothesOutfit01WithHat", AllButGloves);
            edidbits.put("FarmClothesOutfit02", AllButHelmetGloves);
            edidbits.put("FarmClothesOutfit02Variant", AllButHelmetGloves);
            edidbits.put("FarmClothesOutfit02WithHat", AllButGloves);
            edidbits.put("FarmClothesOutfit03", AllButHelmetGloves);
            edidbits.put("FarmClothesOutfit03Variant", AllButHelmetGloves);
            edidbits.put("FarmClothesOutfit03withextras", AllButHelmet);
            edidbits.put("FarmClothesOutfit03withHat", AllButGloves);
            edidbits.put("FarmClothesOutfit03withHat01", AllButGloves);
            edidbits.put("FarmClothesOutfit03withHat02", AllButGloves);
            edidbits.put("FarmClothesOutfit03withHat02withHat03", AllButGloves);
            edidbits.put("FarmClothesOutfit03withHideBootsandBracers", AllButGloves);
            edidbits.put("FarmClothesOutfit04", AllButHelmetGloves);
            edidbits.put("FarmClothesOutfit04Variant", AllButHelmetGloves);
            edidbits.put("FarmClothesRandom", AllButGloves);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("FarmClothes", AllWithHelmet);
            return edidbits;
        }    
    }
    class NobleOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Noble";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("FineClothesOutfit01", AllButHelmetGloves);
            edidbits.put("FineClothesOutfit01Variant", AllButHelmetGloves);
            edidbits.put("FineClothesOutfit01WithHat", AllButHelmetGloves);
            edidbits.put("FineClothesOutfit02", AllButHelmetGloves);
            edidbits.put("FineClothesOutfit02Variant", AllButHelmetGloves);
            edidbits.put("FineClothesOutfit02VariantWithHat", AllButGloves);
            edidbits.put("FineClothesOutfit02withGloves", AllButHelmet);
            edidbits.put("FineClothesOutfit02WithHat", AllButGloves);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("FineClothesOutfit", AllButHelmetGloves);
            return edidbits;
        }    
    }
    class JarlOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Jarl";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("JarlClothesOutfit01", AllButHelmetGloves);
            edidbits.put("JarlClothesOutfit02", AllButHelmetGloves);
            edidbits.put("JarlClothesOutfit03", AllButHelmetGloves);
            edidbits.put("JarlClothesBalgruuf", AllButHelmetGloves);
            edidbits.put("JarlClothesBryling", AllButHelmetGloves);
            edidbits.put("JarlClothesElisif", AllButHelmetGloves);
            edidbits.put("JarlClothesIgmund", AllButHelmetGloves);
            edidbits.put("JarlClothesKraldar", AllButHelmetGloves);
            edidbits.put("JarlClothesLaila", AllButHelmetGloves);
            edidbits.put("JarlClothesSiddgeir", AllButHelmetGloves);
            edidbits.put("JarlClothesSkaldTheElder", AllButHelmetGloves);
            edidbits.put("JarlClothesTorygg", AllButHelmetGloves);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("JarlClothes", AllWithHelmet);
            return edidbits;
        }    
    }
    class RedguardOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Redguard";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("RedguardClothesOutfit01", AllWithHelmet);
            edidbits.put("RedguardClothesOutfitDB01", AllWithHelmet);
            edidbits.put("RedguardClothesOutfitNoHat", AllButHelmet);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("RedguardClothesOutfit", AllWithHelmet);
            return edidbits;
        }    
    }
    class DraugrOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Draugr";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();            
            edidbits.put("DraugrHair01Outfit", AllButHelmet);
            edidbits.put("DraugrHair02Outfit", AllButHelmet);
            edidbits.put("DraugrBeard01Outfit", AllButHelmet);
            edidbits.put("DraugrBeard02Outfit", AllButHelmet);
            edidbits.put("DraugrHair01Beard01", AllButHelmet);
            edidbits.put("DraugrHair01Beard02", AllButHelmet);
            edidbits.put("DraugrHair02Beard01", AllButHelmet);
            edidbits.put("DraugrHair02Beard02", AllButHelmet);
            edidbits.put("Draugr02Helmet01Beard01Outfit", AllWithHelmet);
            edidbits.put("Draugr02Helmet01Beard02Outfit", AllWithHelmet);
            edidbits.put("Draugr04Helmet02Beard01Outfit", AllWithHelmet);
            edidbits.put("Draugr04Helmet02Beard02Outfit", AllWithHelmet);
            edidbits.put("Draugr05Helmet03Beard01Outfit", AllWithHelmet);
            edidbits.put("Draugr05Helmet03Beard02Outfit", AllWithHelmet);
            edidbits.put("Draugr02Helmet01Outfit", AllWithHelmet);
            edidbits.put("Draugr04Helmet02Outfit", AllWithHelmet);
            edidbits.put("Draugr05Helmet03Outfit", AllWithHelmet);
            return edidbits;
        } 
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("DraugrHair", AllButHelmet);
            edidbits.put("DraugrBeard", AllButHelmet);
            edidbits.put("Draugr01Helmet", AllWithHelmet);
            edidbits.put("Draugr02Helmet", AllWithHelmet);
            edidbits.put("Draugr03Helmet", AllWithHelmet);
            edidbits.put("Draugr04Helmet", AllWithHelmet);
            edidbits.put("Draugr05Helmet", AllWithHelmet);
            
            return edidbits;
        }    
    }
    class VigilantOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Vigilant";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("VigilantOfStendarrOutfit", AllWithHelmet);
            edidbits.put("VigilantOfStendarrOutfitHood", AllWithHelmet);
            edidbits.put("VigilantOfStendarrOutfitNoHood", AllButHelmet);
            return edidbits;
        }   
        @Override 
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("VigilantOfStendarr", AllWithHelmet);
            
            return edidbits;
        }
    }
    class DawnguardOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Dawnguard";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("DLC1OutfitDawnguard01Heavy", AllWithHelmet);
            edidbits.put("DLC1OutfitDawnguard02Heavy", AllWithHelmet);
            edidbits.put("DLC1OutfitDawnguard03", AllWithHelmet);
            edidbits.put("DLC1OutfitDawnguard04", AllWithHelmet);
            edidbits.put("DLC1OutfitDawnguard05", AllWithHelmet);
            edidbits.put("DLC1OutfitDawnguardAll", AllWithHelmet);
            edidbits.put("DawnguardArmorMelee", AllWithHelmet);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("DLC1OutfitDawnguard", AllWithHelmet);
            edidbits.put("DawnguardOutfit", AllWithHelmet);
            return edidbits;
        }    
    }
        
    class SkaalOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Skaal";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("DLC2SkaalOutfitChanceHat", AllWithHelmet);
            edidbits.put("DLC2SkaalOutfitHat", AllWithHelmet);
            edidbits.put("DLC2SkaalOutfitNoHat", AllButHelmet);
            return edidbits;
        }  
    }
        
    class HunterOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Hunter";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("HunterClothesRND", AllWithHelmet);
            edidbits.put("HunterOutfit01", AllButHelmet);
            edidbits.put("HunterOutfit01Hooded", AllWithHelmet);
            edidbits.put("HunterOutfit02", AllButHelmet);
            edidbits.put("HunterOutfit02Hooded", AllWithHelmet);
            edidbits.put("HunterOutfit03", AllButHelmet);
            edidbits.put("HunterOutfit04", AllButHelmet);
            edidbits.put("HunterOutfit04Hooded", AllWithHelmet);
            return edidbits;
        }
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("HunterOutfit", AllButHelmet);
            return edidbits;
        }    
    }
        
    class WenchOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Wench";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ClothesTavernWenchOutfit", "CBFVTLRAEIJKMWDNOPQUXYZ-_");
            return edidbits;
        } 
    }

    class BladesOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Blades";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ArmorBladesOutfitNoHelmet", AllButHelmet);
            edidbits.put("ArmorBladesOutfit", AllWithHelmet);
            return edidbits;
        } 
    }

    class ForswornOutfit extends OutfitBase {
        @Override
        String Name() {
            return "Forsworn";
        }
        @Override
        Map<String, String> BitsByEDIDEquals() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ForswornArmorBossOutfit", AllWithHelmet);
            edidbits.put("ForswornArmorMeleeOutfit", AllWithHelmet);
            edidbits.put("ForswornArmorMissileOutfit", AllWithHelmet);
            edidbits.put("ForswornArmorMagicOutfit", AllWithHelmet);
            edidbits.put("ForswornArmorBossOutfit", AllWithHelmet);
            return edidbits;
        }  
        @Override
        Map<String, String> BitsByEDIDStartsWith() {
            HashMap<String, String> edidbits = new HashMap<>();
            edidbits.put("ForswornArmor", AllWithHelmet);
            return edidbits;
        }  
    }
// -----------------------------------------------------------------------------
