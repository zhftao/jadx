package jadx.cli.commands;

import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import jadx.cli.JCommanderWrapper;
import jadx.plugins.tools.JadxPluginsList;
import jadx.plugins.tools.JadxPluginsTools;
import jadx.plugins.tools.data.JadxPluginMetadata;
import jadx.plugins.tools.data.JadxPluginUpdate;

@Parameters(commandDescription = "manage jadx plugins")
public class CommandPlugins implements ICommand {

	@Parameter(names = { "-i", "--install" }, description = "install plugin with locationId")
	protected String install;

	@Parameter(names = { "-j", "--install-jar" }, description = "install plugin from jar file")
	protected String installJar;

	@Parameter(names = { "-l", "--list" }, description = "list installed plugins")
	protected boolean list;

	@Parameter(names = { "-a", "--available" }, description = "list available plugins")
	protected boolean available;

	@Parameter(names = { "-u", "--update" }, description = "update installed plugins")
	protected boolean update;

	@Parameter(names = { "--uninstall" }, description = "uninstall plugin with pluginId")
	protected String uninstall;

	@Parameter(names = { "-h", "--help" }, description = "print this help", help = true)
	protected boolean printHelp = false;

	@Override
	public String name() {
		return "plugins";
	}

	@Override
	public void process(JCommanderWrapper<?> jcw, JCommander subCommander) {
		if (printHelp) {
			jcw.printUsage(subCommander);
			return;
		}
		if (install != null) {
			installPlugin(install);
		}
		if (installJar != null) {
			installPlugin("file:" + installJar);
		}
		if (uninstall != null) {
			boolean uninstalled = JadxPluginsTools.getInstance().uninstall(uninstall);
			System.out.println(uninstalled ? "Uninstalled" : "Plugin not found");
		}
		if (update) {
			List<JadxPluginUpdate> updates = JadxPluginsTools.getInstance().updateAll();
			if (updates.isEmpty()) {
				System.out.println("No updates");
			} else {
				System.out.println("Installed updates: " + updates.size());
				for (JadxPluginUpdate update : updates) {
					System.out.println("  " + update.getPluginId() + ": " + update.getOldVersion() + " -> " + update.getNewVersion());
				}
			}
		}
		if (list) {
			List<JadxPluginMetadata> installed = JadxPluginsTools.getInstance().getInstalled();
			System.out.println("Installed plugins: " + installed.size());
			int i = 1;
			for (JadxPluginMetadata plugin : installed) {
				System.out.println(" " + (i++) + ") "
						+ plugin.getPluginId() + " (" + plugin.getVersion() + ") - "
						+ plugin.getName() + ": " + plugin.getDescription());
			}
		}

		if (available) {
			List<JadxPluginMetadata> availableList = JadxPluginsList.getInstance().get();
			System.out.println("Available plugins: " + availableList.size());
			int i = 1;
			for (JadxPluginMetadata plugin : availableList) {
				System.out.println(" " + (i++) + ") "
						+ plugin.getName() + ": " + plugin.getDescription()
						+ " (" + plugin.getLocationId() + ")");
			}
		}
	}

	private void installPlugin(String locationId) {
		JadxPluginMetadata plugin = JadxPluginsTools.getInstance().install(locationId);
		System.out.println("Plugin installed: " + plugin.getPluginId() + ":" + plugin.getVersion());
	}
}
