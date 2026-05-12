package clonebang.client;

import clonebang.CloneBang;
import net.minecraft.core.BlockPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public record SavedStructure(String name, BlockPos first, BlockPos second, LocalDateTime savedAt) {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

	public static SavedStructure fromSelection(String name, CloneSelection selection) {
		return new SavedStructure(name, selection.first(), selection.second(), LocalDateTime.now());
	}

	public Path path() {
		return CloneBangConfig.structuresDir().resolve(name + ".properties");
	}

	public void save() {
		try {
			Files.createDirectories(CloneBangConfig.structuresDir());
			Properties properties = new Properties();
			properties.setProperty("name", name);
			properties.setProperty("savedAt", savedAt.toString());
			writePos(properties, "first", first);
			writePos(properties, "second", second);
			try (var writer = Files.newBufferedWriter(path())) {
				properties.store(writer, "CloneBang saved structure");
			}
		} catch (IOException e) {
			CloneBang.LOGGER.warn("Failed to save structure {}", name, e);
		}
	}

	public CloneSelection toSelection() {
		CloneSelection selection = new CloneSelection();
		selection.setFirst(first);
		selection.setSecond(second);
		return selection;
	}

	public static String defaultName() {
		return "structure_" + FORMATTER.format(LocalDateTime.now());
	}

	public static List<SavedStructure> list() {
		List<SavedStructure> structures = new ArrayList<>();
		try {
			Files.createDirectories(CloneBangConfig.structuresDir());
			try (var paths = Files.list(CloneBangConfig.structuresDir())) {
				paths.filter(path -> path.getFileName().toString().endsWith(".properties"))
						.map(SavedStructure::load)
						.flatMap(Optional::stream)
						.sorted(Comparator.comparing(SavedStructure::savedAt).reversed())
						.forEach(structures::add);
			}
		} catch (IOException e) {
			CloneBang.LOGGER.warn("Failed to list saved structures", e);
		}
		return structures;
	}

	public static Optional<SavedStructure> load(Path path) {
		Properties properties = new Properties();
		try (var reader = Files.newBufferedReader(path)) {
			properties.load(reader);
			return Optional.of(new SavedStructure(
					properties.getProperty("name", path.getFileName().toString().replace(".properties", "")),
					readPos(properties, "first"),
					readPos(properties, "second"),
					LocalDateTime.parse(properties.getProperty("savedAt", LocalDateTime.now().toString()))
			));
		} catch (Exception e) {
			CloneBang.LOGGER.warn("Failed to load structure {}", path, e);
			return Optional.empty();
		}
	}

	private static void writePos(Properties properties, String prefix, BlockPos pos) {
		properties.setProperty(prefix + ".x", Integer.toString(pos.getX()));
		properties.setProperty(prefix + ".y", Integer.toString(pos.getY()));
		properties.setProperty(prefix + ".z", Integer.toString(pos.getZ()));
	}

	private static BlockPos readPos(Properties properties, String prefix) {
		return new BlockPos(
				Integer.parseInt(properties.getProperty(prefix + ".x", "0")),
				Integer.parseInt(properties.getProperty(prefix + ".y", "0")),
				Integer.parseInt(properties.getProperty(prefix + ".z", "0"))
		);
	}
}
