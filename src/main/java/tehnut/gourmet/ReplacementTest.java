package tehnut.gourmet;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import tehnut.gourmet.core.data.Harvest;
import tehnut.gourmet.item.ItemEdible;

@Mod(modid = "replacementtest", dependencies = "after:gourmet;")
@Mod.EventBusSubscriber(modid = "replacementtest")
public class ReplacementTest {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {

        Harvest harvest = new Harvest.Builder("toast", 0, 0.0F)
                .build();

        IForgeRegistryModifiable<Item> registry = (IForgeRegistryModifiable<Item>) event.getRegistry();
        registry.register(new ItemEdible(harvest).setRegistryName(new ResourceLocation(Gourmet.MODID, "food_toast")));
    }
}
