package LeveledListInjector;

import java.util.HashMap;
import java.util.Map;

// TIERS INFO ------------------------------------------------------------------
abstract class TierBase {
    // tier identifier
    abstract String EDID();
    // in each array zero element is esp name and after formid list
    abstract Map<String, String[]> FormIDList();
    // UseAll flag
    boolean UseAll(){
        return false;
    }
    // CalcAllLevelsEqualOrBelowPC flag
    boolean CalcAllLevelsEqualOrBelowPC(){
        return false;
    }
    // CalcForEachItemInCount flag
    boolean CalcForEachItemInCount(){
        return false;
    }
}

class ThalmorTier9 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Thalmor_Tier_9";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Skyrim.esm", new String[] {
            "01391a", // boots
            "01391d", // helm
            "01392a", // cuirass
            "01391c", // gloves
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return true;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return false;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class NecromancerTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Necromancer_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Skyrim.esm", new String[] {
            "0c36e8", // boots
            "105251", // robesList
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return true;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return false;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class WarlockTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Warlock_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Skyrim.esm", new String[] {
            "0c5d12", // boots
            "105ef9", // robesList
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return true;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return false;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class ImperialHeavyTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_ImperialHeavy_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "0115B0", // LLOutfitL
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class ImperialLightTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_ImperialLight_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "0115AF", // LLOutfitL
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class ImperialBossTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_ImperialBoss_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "0115B1", // LLOutfitL
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class StormcloakTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Stormcloak_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "0115AD", // LLOutfitL
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class StormcloakBossTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_StormcloakBoss_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "0115AE", // LLOutfitL
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class VampireTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Vampire_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "00C48F", // LLOutfit
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class VampireBossTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_VampireBoss_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "00C490", // LLOutfit
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class ChildTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Child_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "007376", // LLChildOutfit
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class NobleTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Noble_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "01159A", // LLOutfitL
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class JarlTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Jarl_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "01159F", // LLOutfitL
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class RedguardTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Redguard_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "0115A3", // LLOutfitL
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class FarmTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Farm_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "00C47A", // LLOutfit
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class DraugrTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Draugr_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            /*
            "0021C7", // OutftDienesDraugr02Helmet01Beard02Outfit
            "0021C8", // OutftDienesDraugr02Helmet01Outfit
            "0021C9", // OutftDienesDraugr04Helmet02Beard01Outfit
            "0021CA", // OutftDienesDraugr04Helmet02Beard02Outfit
            "0021CB", // OutftDienesDraugr04Helmet02Outfit
            "0021CC", // OutftDienesDraugr05Helmet03Beard01Outfit
            "0021CD", // OutftDienesDraugr05Helmet03Beard02Outfit
            "0021CE", // OutftDienesDraugr05Helmet03Outfit
            "0021CF", // OutftDienesDraugrBeard01Outfit
            "0021D0", // OutftDienesDraugrBeard02Outfit
            "0021D1", // OutftDienesDraugrHair01Beard01
            "0021D2", // OutftDienesDraugrHair01Beard02
            "0021D3", // OutftDienesDraugrHair01Outfit
            "0021D4", // OutftDienesDraugrHair02Beard01
            "0021D5", // OutftDienesDraugrHair02Beard02
            "0021D6", // OutftDienesDraugrHair02Outfit
            */
            "00C47F", // DraugrOutfit
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class VigilantTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Vigilant_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "0166F9", // LLOutfit
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class DawnguardTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Dawnguard_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "00C486", // LLOutfit
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class DawnguardHeavyTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_DawnguardHeavy_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "00C487", // LLOutfitH
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class DawnguardLightTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_DawnguardLight_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "00C488", // LLOutfitL
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class SkaalTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Skaal_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "0115A8", // LLOutfitL
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class HunterTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Hunter_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "0166F2", // LLOutfit
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class WenchTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Wench_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Lootification.esm", new String[] {
            "016718", // LLOutfit
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return false;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return true;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class BladesTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Blades_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Skyrim.esm", new String[] {
            "04B288", // boots
            "04B28F", // helm
            "04B28B", // cuirass
            "04B28D", // gloves
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return true;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return false;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class ForswornBossTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_ForswornBoss_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Skyrim.esm", new String[] {
            "0442FE", // boots
            "0442FF", // cuirass
            "0647AE", // gloves
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return true;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return false;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}

class ForswornTier0 extends TierBase {
    @Override
    String EDID() {
        return "DienesLVLI_Forsworn_Tier_0";
    }
    @Override
    Map<String, String[]> FormIDList() {
        Map<String, String[]> m = new HashMap<>();
        m.put("Skyrim.esm", new String[] {
            "043BD4", // boots
            "0647B2", // helm
            "043BCE", // cuirass
            "0647B0", // gloves
        });
        return m;
    }
    @Override
    boolean UseAll() {
        return true;
    }
    @Override
    boolean CalcAllLevelsEqualOrBelowPC() {
        return false;
    }
    @Override
    boolean CalcForEachItemInCount() {
        return false;
    }
}
class TiersList {
    static TierBase[] Get() {
        return new TierBase[] {
            new ThalmorTier9(),
            new NecromancerTier0(),
            new WarlockTier0(),
            new ImperialHeavyTier0(),
            new ImperialLightTier0(),
            new ImperialBossTier0(),
            new StormcloakTier0(),
            new StormcloakBossTier0(),
            new VampireTier0(),
            new VampireBossTier0(),
            new ChildTier0(),
            new NobleTier0(),
            new JarlTier0(),
            new RedguardTier0(),
            new FarmTier0(),
            new DraugrTier0(),
            new VigilantTier0(),
            new DawnguardTier0(),
            new DawnguardHeavyTier0(),
            new DawnguardLightTier0(),
            new SkaalTier0(),
            new HunterTier0(),
            new WenchTier0(),
            new BladesTier0(),
            new ForswornBossTier0(),
            new ForswornTier0(),
        };
    }
}

// TIERS INFO ------------------------------------------------------------------