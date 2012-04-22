package stefan.blocklocator;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HotColdManager {
	private static final Timer worker = new Timer();

	private Map<Player, Holder> map;
	private Plugin plugin;

	public HotColdManager(Plugin plugin) {
		this.plugin = plugin;
		map = new HashMap<Player, Holder>();
	}

	public boolean isRecivingUpdates(Player player) {
		Holder holder = map.get(player);

		if (holder == null)
			return false;

		if (holder.timer == null)
			return false;

		return true;
	}

	public void unregisterForUpdates(Player player) {
		if (!isRecivingUpdates(player))
			return;

		map.get(player).timer.cancel();
		map.put(player, null);
	}

	public void registerForUpdates(Player player, Location blockLocation) {
		if (isRecivingUpdates(player))
			unregisterForUpdates(player);

		map.put(player, new Holder(player, player.getLocation(), blockLocation));
	}

	public void stopAllUpdates() {
		worker.cancel();
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
					double oldDistance = BlockLocator.distance(lastLocation, blockLocation);
					double newDistance = BlockLocator.distance(player.getLocation(), blockLocation);

					if (newDistance <= 2.0) {
						player.sendMessage("You found it!");
						timer.cancel();
						map.put(player, null);
					} else if (oldDistance > newDistance) {
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
