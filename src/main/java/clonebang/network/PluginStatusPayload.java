package clonebang.network;

import clonebang.CloneBang;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PluginStatusPayload(
		int protocolVersion,
		String pluginVersion,
		boolean safeMode,
		boolean optimizedCloneCommands,
		boolean structureStorageApi
) implements CustomPacketPayload {
	public static final Type<PluginStatusPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CloneBang.MOD_ID, "plugin_status"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PluginStatusPayload> CODEC = StreamCodec.ofMember(PluginStatusPayload::write, PluginStatusPayload::read);

	private void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeVarInt(protocolVersion);
		buffer.writeUtf(pluginVersion, 64);
		buffer.writeBoolean(safeMode);
		buffer.writeBoolean(optimizedCloneCommands);
		buffer.writeBoolean(structureStorageApi);
	}

	private static PluginStatusPayload read(RegistryFriendlyByteBuf buffer) {
		return new PluginStatusPayload(
				buffer.readVarInt(),
				buffer.readUtf(64),
				buffer.readBoolean(),
				buffer.readBoolean(),
				buffer.readBoolean()
		);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
