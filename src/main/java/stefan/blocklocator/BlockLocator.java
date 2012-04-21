package stefan.blocklocator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the plugin.
 * 
 * @author Stefan Bossbaly
 * 
 */
public class BlockLocator extends JavaPlugin {

	private static final Timer worker = new Timer();

	private Map<Player, Holder> map;
	/**
	 * The tag that will be displayed in the logs for this plugin. Helps the
	 * user/server admin distinguish between logs of different plugins.
	 */
	public static final String TAG = "[BlockLocator]";

	/**
	 * Logger that will be used to output messages to the log.
	 */
	public static final Logger log = Logger.getLogger("Minecraft");

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisable() {
		log.info(TAG + " onDisable() called. Shutting down ...");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnable() {
		log.info(TAG + " onEnabled() called. Starting up ...");
		
		map = new HashMap<Player, Holder>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (command.getName().equalsIgnoreCase("bl")) {

			// The console can not call this command
			if (!(sender instanceof Player)) {
				sender.sendMessage(TAG
						+ " I can not get you location and therefore can't find blocks around you ...");
				return true;
			}

			// Make sure that we have enough parameters or the help command was
			// issued
			if (args.length < 1 || "help".equals(args[0].toLowerCase())) {
				sender.sendMessage(TAG + " /bl [block_id] [radius]");
				return false;
			}

			int blockId;

			// Try to parse the int
			try {
				blockId = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				sender.sendMessage(TAG + " This is not a valid integer!");
				return true;
			}

			// Get all the fun stuff
			Player player = (Player) sender;
			Location loc = player.getLocation();
			World world = player.getWorld();

			// List that will hold the block that were found
			LinkedList<Location> locs = new LinkedList<Location>();

			// Log this command took place
			log.info(TAG + "Player: " + player.getName()
					+ " has called the locate command from location " + loc);

			// TODO change the search radius
			for (int x = loc.getBlockX() - 10; x < loc.getBlockX() + 10; x++) {
				for (int y = loc.getBlockY() - 10; y < loc.getBlockY() + 10; y++) {
					for (int z = loc.getBlockZ() - 10; z < loc.getBlockZ() + 10; z++) {
						if (world.getBlockTypeIdAt(x, y, z) == blockId)
							locs.add(world.getBlockAt(x, y, z).getLocation());
					}
				}
			}

			player.sendMessage(TAG + " Found " + locs.size() + " many blocks");

			Location minLoc = getClosestLocation(locs, loc);

			if (minLoc != null)
				player.sendMessage(TAG + " The closest block is ("
						+ minLoc.getX() + "," + minLoc.getY() + ","
						+ minLoc.getZ() + ") with a distance of "
						+ distance(minLoc, loc));

			return true;
		}

		else if (command.getName().equalsIgnoreCase("blhc")) {
			// The console can not call this command
			if (!(sender instanceof Player)) {
				sender.sendMessage(TAG
						+ " I can not get you location and therefore can't find blocks around you ...");
				return true;
			}

			// Get all the fun stuff
			Player player = (Player) sender;
			Location loc = player.getLocation();
			World world = player.getWorld();

			// List that will hold the block that were found
			LinkedList<Location> locs = new LinkedList<Location>();

			// TODO change the search radius
			for (int x = loc.getBlockX() - 10; x < loc.getBlockX() + 10; x++) {
				for (int y = loc.getBlockY() - 10; y < loc.getBlockY() + 10; y++) {
					for (int z = loc.getBlockZ() - 10; z < loc.getBlockZ() + 10; z++) {
						if (world.getBlockTypeIdAt(x, y, z) == 1)
							locs.add(world.getBlockAt(x, y, z).getLocation());
					}
				}
			}

			Location minLoc = getClosestLocation(locs, loc);

			map.put(player, new Holder(player, player.getLocation(), minLoc));

			return true;
		}

		return false;
	}

	/**
	 * Calculates and returns the distance between the two locations.
	 * 
	 * @param loc1
	 *            the first location
	 * @param loc2
	 *            the second location
	 * @return the distance between the locations
	 */
	public static double distance(Location loc1, Location loc2) {
		return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2.0)
				+ Math.pow(loc1.getY() - loc2.getY(), 2.0)
				+ Math.pow(loc1.getZ() - loc2.getZ(), 2.0));
	}

	/**
	 * Gets the closest locations out of the list of locations to the position
	 * location
	 * 
	 * @param locations
	 *            the list of locations that will be compared
	 * @param position
	 *            the position
	 * @return the location that is closest to the position
	 */
	private static Location getClosestLocation(List<Location> locations,
			Location position) {
		if (locations.size() < 1)
			return null;

		Location minLoc = null;
		double minDistance = Double.MAX_VALUE;

		for (Location temp : locations) {
			double distance = distance(position, temp);
			if (distance == Math.min(minDistance, distance)) {
				minDistance = distance;
				minLoc = temp;
			}
		}

		return minLoc;
	}

	private class Holder {
		public Player player;
		public Location lastLocation;
		public Location blockLocation;
		public TimerTask timer;

		public Holder(Player playerP, Location lastLocationP, Location blockLocationP) {
			player = playerP;
			lastLocation = lastLocationP;
			blockLocation = blockLocationP;
			
			timer = new TimerTask() {

				@Override
				public void run() {
					double oldDistance = BlockLocator.distance(lastLocation,
							blockLocation);
					double newDistance = BlockLocator.distance(
							player.getLocation(), blockLocation);

					if (oldDistance > newDistance) {
						player.sendMessage("Getting Hotter");
						lastLocation = player.getLocation();
					} else {
						player.sendMessage("Getting Colder");
						lastLocation = player.getLocation();
					}
				}
			};
			
			worker.schedule(timer, 1000L, 1000L);
		}
	}
}
