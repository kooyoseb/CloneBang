package clonebang.network;

import clonebang.CloneBang;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PluginHelloPayload(int protocolVersion, String modVersion) implements CustomPacketPayload {
	public static final Type<PluginHelloPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CloneBang.MOD_ID, "plugin_hello"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PluginHelloPayload> CODEC = StreamCodec.ofMember(PluginHelloPayload::write, PluginHelloPayload::read);

	private void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeVarInt(protocolVersion);
		buffer.writeUtf(modVersion, 64);
	}

	private static PluginHelloPayload read(RegistryFriendlyByteBuf buffer) {
		return new PluginHelloPayload(buffer.readVarInt(), buffer.readUtf(64));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
