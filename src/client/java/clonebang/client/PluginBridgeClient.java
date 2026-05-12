package clonebang.client;

import clonebang.CloneBang;
import clonebang.network.PluginHelloPayload;
import clonebang.network.PluginStatusPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;

public final class PluginBridgeClient {
	private static boolean pluginPresent;
	private static boolean handshakeSent;
	private static int protocolVersion;
	private static String pluginVersion = "";
	private static boolean safeMode;
	private static boolean optimizedCloneCommands;
	private static boolean structureStorageApi;

	private PluginBridgeClient() {
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(PluginStatusPayload.TYPE, PluginStatusPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PluginHelloPayload.TYPE, PluginHelloPayload.CODEC);

		ClientPlayNetworking.registerGlobalReceiver(PluginStatusPayload.TYPE, (payload, context) ->
				context.client().execute(() -> applyStatus(payload)));

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			reset();
			if (ClientPlayNetworking.canSend(PluginHelloPayload.TYPE)) {
				handshakeSent = true;
				ClientPlayNetworking.send(new PluginHelloPayload(CloneBang.PLUGIN_API_PROTOCOL, modVersion()));
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
	}

	private static void applyStatus(PluginStatusPayload payload) {
		pluginPresent = true;
		protocolVersion = payload.protocolVersion();
		pluginVersion = payload.pluginVersion();
		safeMode = payload.safeMode();
		optimizedCloneCommands = payload.optimizedCloneCommands();
		structureStorageApi = payload.structureStorageApi();
	}

	public static void reset() {
		pluginPresent = false;
		handshakeSent = false;
		protocolVersion = 0;
		pluginVersion = "";
		safeMode = false;
		optimizedCloneCommands = false;
		structureStorageApi = false;
	}

	public static boolean isPluginPresent() {
		return pluginPresent;
	}

	public static boolean isHandshakeSent() {
		return handshakeSent;
	}

	public static boolean isProtocolCompatible() {
		return pluginPresent && protocolVersion == CloneBang.PLUGIN_API_PROTOCOL;
	}

	public static String statusTranslationKey() {
		if (pluginPresent) {
			return isProtocolCompatible() ? "screen.clonebang.plugin.connected" : "screen.clonebang.plugin.incompatible";
		}
		return handshakeSent ? "screen.clonebang.plugin.waiting" : "screen.clonebang.plugin.missing";
	}

	public static String statusDetail() {
		if (!pluginPresent) {
			return "";
		}
		return pluginVersion + " / api " + protocolVersion
				+ " / safe=" + safeMode
				+ " / clone=" + optimizedCloneCommands
				+ " / storage=" + structureStorageApi;
	}

	private static String modVersion() {
		return FabricLoader.getInstance()
				.getModContainer(CloneBang.MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("unknown");
	}
}
