package schauweg.smoothswapping;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import schauweg.smoothswapping.config.Config;
import schauweg.smoothswapping.swaps.InventorySwap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(SmoothSwapping.MOD_ID)
public class SmoothSwapping {

    public static final String MOD_ID = "smoothswapping";
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static boolean clickSwap;

    public static Integer clickSwapStack;

    public static Map<Integer, List<InventorySwap>> swaps;
    public static List<ItemStack> oldStacks;

    public SmoothSwapping() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
    }

    private void setupClient(FMLClientSetupEvent event) {
        swaps = new HashMap<>();
        oldStacks = Lists.newArrayList();
    }

}
