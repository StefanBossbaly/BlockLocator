package stefan.blocklocator;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BlockLocatorPlayerListener implements Listener {

	private Map<Player, Holder> map;

	public BlockLocatorPlayerListener() {
		map = new HashMap<Player, Holder>();
	}

	public void addPlayer(Player player, Location location) {
		Holder holder = new Holder();
		holder.blockLocation = location;
		map.put(player, holder);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();

		if (player == null || !player.isOnline())
			return;

		Holder holder = map.get(player);

		if (holder == null)
			return;

		if (holder.lastLocation == null) {
			holder.lastLocation = player.getLocation();
			return;
		}

		double oldDistance = BlockLocator.distance(holder.lastLocation,
				holder.blockLocation);
		double newDistance = BlockLocator.distance(player.getLocation(),
				holder.blockLocation);

		if (oldDistance > newDistance) {
			player.sendMessage("Getting Hotter");
			holder.lastLocation = player.getLocation();
		} else {
			player.sendMessage("Getting Colder");
			holder.lastLocation = player.getLocation();
		}
	}

	private class Holder {
		public Location lastLocation;
		public Location blockLocation;
	}
}
