package schauweg.smoothswapping.config;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
	public static final Client CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;

	static {
		Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
		CLIENT_SPEC = specPair.getRight();
		CLIENT = specPair.getLeft();
	}

	public static class Client {
		public final ForgeConfigSpec.IntValue animationSpeed;
		public final ForgeConfigSpec.ConfigValue<String> easeMode;
		public final ForgeConfigSpec.IntValue easeSpeed;

		public Client(ForgeConfigSpec.Builder builder) {
			animationSpeed = builder
					.comment(Component.translatable("Setting Swap Item Animation Speed [1 ~ 400]").getString())
					.defineInRange("Animation Speed", 40, 1, 400);

			easeMode = builder
					.comment("Setting Ease Swap Item Animation Style. [linear, ease-in, ease-in-out]")
					.define("Ease Animation Style", "linear");

			easeSpeed = builder
					.comment("Setting Ease Swap Item Animation Speed [1 ~ 1000]")
					.defineInRange("Ease Animation Speed", 400, 10, 1000);

		}

		public float getEaseSpeedFormatted() {
			return easeSpeed.get() / 100F;
		}

		public float getAnimationSpeedFormatted() {
			return animationSpeed.get() / 100F;
		}
	}
}
