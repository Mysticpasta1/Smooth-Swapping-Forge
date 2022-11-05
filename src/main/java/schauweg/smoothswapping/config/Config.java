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
					.comment(Component.translatable("smoothswapping.config.option.animationspeed.desc").getString())
					.defineInRange(Component.translatable("smoothswapping.config.option.animationspeed").getString(), 100, 1, 400);

			easeMode = builder
					.comment(Component.translatable("smoothswapping.config.option.ease.desc").getString())
					.define(Component.translatable("smoothswapping.config.option.ease").getString(), "linear");

			easeSpeed = builder
					.comment(Component.translatable("smoothswapping.config.option.easespeed.desc").getString())
					.defineInRange(Component.translatable("smoothswapping.config.option.easespeed").getString(), 400, 100, 1000);

		}

		public float getEaseSpeedFormatted() {
			return easeSpeed.get() / 100F;
		}

		public float getAnimationSpeedFormatted() {
			return animationSpeed.get() / 100F;
		}
	}
}
