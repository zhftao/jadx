package jadx.plugins.tools.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import dev.dirs.ProjectDirectories;

import static jadx.core.utils.files.FileUtils.makeDirs;

public class PluginFiles {
	private static final ProjectDirectories DIRS = ProjectDirectories.from("io.github", "skylot", "jadx");

	private static final Path PLUGINS_DIR = Paths.get(DIRS.configDir, "plugins");
	public static final Path PLUGINS_JSON = PLUGINS_DIR.resolve("plugins.json");
	public static final Path INSTALLED_DIR = PLUGINS_DIR.resolve("installed");
	public static final Path DROPINS_DIR = PLUGINS_DIR.resolve("dropins");

	private static final Path CACHE_DIR = Paths.get(DIRS.cacheDir);
	public static final Path PLUGINS_LIST_CACHE = CACHE_DIR.resolve("plugin-list.json");

	static {
		makeDirs(INSTALLED_DIR);
		makeDirs(DROPINS_DIR);
	}
}
