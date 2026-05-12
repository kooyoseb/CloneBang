package clonebang.client;

import clonebang.CloneBang;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class CloneBangConfig {
	public enum Shape {
		CUBOID,
		OUTLINE,
		FLAT
	}

	public enum SelectionRenderMode {
		PARTICLE,
		NATIVE
	}

	private static final String CONFIG_FILE = "config.properties";
	public boolean outlineOnly;
	public boolean previewAfterSelection = true;
	public boolean overlapBlocks = true;
	public boolean allowBlocks = true;
	public boolean allowEntities = true;
	public Shape shape = Shape.CUBOID;
	public SelectionRenderMode selectionRenderMode = SelectionRenderMode.PARTICLE;
	public final Set<Identifier> excludedBlocks = new LinkedHashSet<>();

	public static Path rootDir() {
		return FabricLoader.getInstance().getGameDir().resolve("moddata").resolve(CloneBang.MOD_ID);
	}

	public static Path structuresDir() {
		return rootDir().resolve("structures");
	}

	public static CloneBangConfig load() {
		CloneBangConfig config = new CloneBangConfig();
		Path path = rootDir().resolve(CONFIG_FILE);
		if (!Files.exists(path)) {
			config.save();
			return config;
		}

		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(path)) {
			properties.load(reader);
		} catch (IOException e) {
			CloneBang.LOGGER.warn("Failed to read CloneBang config", e);
			return config;
		}

		config.outlineOnly = Boolean.parseBoolean(properties.getProperty("outlineOnly", "false"));
		config.previewAfterSelection = Boolean.parseBoolean(properties.getProperty("previewAfterSelection", "true"));
		config.overlapBlocks = Boolean.parseBoolean(properties.getProperty("overlapBlocks", "true"));
		config.allowBlocks = Boolean.parseBoolean(properties.getProperty("allowBlocks", "true"));
		config.allowEntities = Boolean.parseBoolean(properties.getProperty("allowEntities", "true"));
		config.shape = Shape.valueOf(properties.getProperty("shape", Shape.CUBOID.name()));
		config.selectionRenderMode = SelectionRenderMode.valueOf(properties.getProperty("selectionRenderMode", SelectionRenderMode.PARTICLE.name()));
		for (String raw : properties.getProperty("excludedBlocks", "").split(",")) {
			if (!raw.isBlank()) {
				config.excludedBlocks.add(Identifier.parse(raw.trim()));
			}
		}
		return config;
	}

	public void save() {
		try {
			Files.createDirectories(rootDir());
			Files.createDirectories(structuresDir());
			Properties properties = new Properties();
			properties.setProperty("outlineOnly", Boolean.toString(outlineOnly));
			properties.setProperty("previewAfterSelection", Boolean.toString(previewAfterSelection));
			properties.setProperty("overlapBlocks", Boolean.toString(overlapBlocks));
			properties.setProperty("allowBlocks", Boolean.toString(allowBlocks));
			properties.setProperty("allowEntities", Boolean.toString(allowEntities));
			properties.setProperty("shape", shape.name());
			properties.setProperty("selectionRenderMode", selectionRenderMode.name());
			properties.setProperty("excludedBlocks", String.join(",", excludedBlocks.stream().map(Identifier::toString).toList()));
			try (Writer writer = Files.newBufferedWriter(rootDir().resolve(CONFIG_FILE))) {
				properties.store(writer, "CloneBang settings");
			}
		} catch (IOException e) {
			CloneBang.LOGGER.warn("Failed to save CloneBang config", e);
		}
	}

	public void reset() {
		outlineOnly = false;
		previewAfterSelection = true;
		overlapBlocks = true;
		allowBlocks = true;
		allowEntities = true;
		shape = Shape.CUBOID;
		selectionRenderMode = SelectionRenderMode.PARTICLE;
		excludedBlocks.clear();
		save();
	}

	public boolean isExcluded(Block block) {
		return excludedBlocks.contains(BuiltInRegistries.BLOCK.getKey(block));
	}
}
