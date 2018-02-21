package tehnut.gourmet.core.util;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.*;
import tehnut.gourmet.Gourmet;
import tehnut.gourmet.block.BlockBerryBush;
import tehnut.gourmet.block.BlockCrop;
import tehnut.gourmet.core.RegistrarGourmet;
import tehnut.gourmet.core.data.Harvest;
import tehnut.gourmet.item.ItemEdible;
import tehnut.gourmet.item.ItemSeed;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;

public class FuckinHackThatShit {

    public static final Map<Harvest, Block> HARVEST_TO_NEW_BLOCK = Maps.newHashMap();
    public static final Map<Harvest, Item> HARVEST_TO_NEW_ITEM = Maps.newHashMap();

    public static void replaceAddCallbacks() {
        try {
            // Get the field
            Field callbackField = ForgeRegistry.class.getDeclaredField("add");
            callbackField.setAccessible(true);

            // Replace and wrap the block callback
            IForgeRegistry.AddCallback<Block> oldBlockCallback = (IForgeRegistry.AddCallback<Block>) callbackField.get(ForgeRegistries.BLOCKS);
            EnumHelper.setFailsafeFieldValue(callbackField, ForgeRegistries.BLOCKS, new OverrideDetector<>(oldBlockCallback, HARVEST_TO_NEW_BLOCK));
            GourmetLog.DEBUG.info("Wrapped block add callback");

            // Replace and wrap the item callback
            IForgeRegistry.AddCallback<Item> oldItemCallback = (IForgeRegistry.AddCallback<Item>) callbackField.get(ForgeRegistries.ITEMS);
            EnumHelper.setFailsafeFieldValue(callbackField, ForgeRegistries.ITEMS, new OverrideDetector<>(oldItemCallback, HARVEST_TO_NEW_ITEM));
            GourmetLog.DEBUG.info("Wrapped item add callback");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkBlocks() {
        GourmetLog.DEBUG.info("Handling {} block replacements", HARVEST_TO_NEW_BLOCK.size());
        for (Map.Entry<Harvest, Block> entry : HARVEST_TO_NEW_BLOCK.entrySet()) {
            Block block = entry.getValue();
            if (block instanceof BlockCrop)
                RegistrarGourmet.getCrops().put(entry.getKey(), (BlockCrop) block);
            else if (block instanceof BlockBerryBush)
                RegistrarGourmet.getBerryBushes().put(entry.getKey(), (BlockBerryBush) block);
        }

        HARVEST_TO_NEW_BLOCK.clear();
    }

    public static void checkItems() {
        GourmetLog.DEBUG.info("Handling {} item replacements", HARVEST_TO_NEW_ITEM.size());
        for (Map.Entry<Harvest, Item> entry : HARVEST_TO_NEW_ITEM.entrySet()) {
            Item item = entry.getValue();
            if (item instanceof ItemEdible)
                RegistrarGourmet.getEdibles().put(entry.getKey(), (ItemEdible) item);
            else if (item instanceof ItemSeed)
                RegistrarGourmet.getSeeds().put(entry.getKey(), (ItemSeed) item);
        }

        HARVEST_TO_NEW_ITEM.clear();
    }

    private static class OverrideDetector<V extends IForgeRegistryEntry<V>> implements IForgeRegistry.AddCallback<V> {

        private final IForgeRegistry.AddCallback<V> oldCallback;
        private final Map<Harvest, V> overrides;

        public OverrideDetector(IForgeRegistry.AddCallback<V> oldCallback, Map<Harvest, V> overrides) {
            this.oldCallback = oldCallback;
            this.overrides = overrides;
        }

        @Override
        public void onAdd(IForgeRegistryInternal<V> owner, RegistryManager stage, int id, V obj, @Nullable V oldObj) {
            // Call the old one so we don't break fucking everything
            oldCallback.onAdd(owner, stage, id, obj, oldObj);
            // Add our custom shit to detect if our shit is being overridden by other shit
            if (oldObj == null)
                return; // not overriding shit

            if (!obj.getRegistryName().getResourceDomain().equals(Gourmet.MODID))
                return; // our shit is safe

            if (!obj.getRegistryName().equals(oldObj.getRegistryName()))
                return; // not overriding shit

            // Make sure the replacement and the old one are both harvesty thingies
            if (obj instanceof IHarvestContainer && oldObj instanceof IHarvestContainer) {
                IHarvestContainer newOne = (IHarvestContainer) obj;
                IHarvestContainer oldOne = (IHarvestContainer) oldObj;

                // please don't
                if (!newOne.getHarvest().equals(oldOne.getHarvest()))
                    throw new RuntimeException("Attempted to override a Harvest based item with a different Harvest type.");

                // Map the Harvest to it's new value
                GourmetLog.DEBUG.info("Adding override for {} ({})", newOne.getHarvest().getSimpleName(), obj.getRegistryName());
                overrides.put(newOne.getHarvest(), obj);
            }
        }
    }
}
