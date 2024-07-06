package br.com.pulse.ranked.misc.listeners;

import br.com.pulse.ranked.Main;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.LastHit;
import com.tomkeuper.bedwars.arena.team.BedWarsTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.EnumMap;
import java.util.UUID;


// Desativado
public class FireballListener implements Listener {

    BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    private final double fireballExplosionSize, fireballHorizontal, fireballVertical;
    private final double damageSelf, damageEnemy, damageTeammates;
    private final double fireballSpeedMultiplier, fireballCooldown;
    private final boolean fireballMakeFire;

    public FireballListener() {
        fireballExplosionSize = 3;
        fireballHorizontal = 0.80;
        fireballVertical = 0.65;

        damageSelf = 0.5;
        damageEnemy = 0.5;
        damageTeammates = 0.5;

        fireballSpeedMultiplier = 3.5;
        fireballCooldown = 0.5;

        fireballMakeFire = true;
    }

    @EventHandler
    public void onFireballInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack handItem = e.getItem();
        Action action = e.getAction();

        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR || handItem == null) {
            return;
        }

        if (bedwarsAPI.getArenaUtil().isPlaying(player)) {
            IArena arena = Arena.getArenaByPlayer(player);
            String group = arena.getGroup();
            if (group.equalsIgnoreCase("RankedSolo") || group.equalsIgnoreCase("RankedDuplas")
                    || group.equalsIgnoreCase("Ranked1v1") || group.equalsIgnoreCase("Ranked4v4")
                    || group.equalsIgnoreCase("Ranked2v2CM")) {

                e.setCancelled(true);

                long cooldown = (long) (fireballCooldown * 1000);
                long timeDifference = System.currentTimeMillis() - arena.getFireballCooldowns().getOrDefault(player.getUniqueId(), 0L);
                if (timeDifference <= cooldown) {
                    if (fireballCooldown >= 1.0) {
                        player.sendMessage(Language.getMsg(player, Messages.ARENA_FIREBALL_COOLDOWN)
                                .replace("%bw_cooldown%", String.valueOf((cooldown - timeDifference)/1000)));
                    }
                    return;
                }

                arena.getFireballCooldowns().put(player.getUniqueId(), System.currentTimeMillis());
                Fireball fireball = player.launchProjectile(Fireball.class);
                Vector direction = player.getEyeLocation().getDirection();
                fireball = Main.nms.setFireballDirection(fireball, direction);
                fireball.setVelocity(fireball.getDirection().multiply(fireballSpeedMultiplier));
                fireball.setYield((float) fireballExplosionSize);
                fireball.setMetadata("bw2023", new FixedMetadataValue(Main.plugin, "ceva"));
                Main.nms.minusAmount(player, handItem, 1);
            }
        }
    }

    @EventHandler
    public void fireballHit(ProjectileHitEvent e) {

        if (!(e.getEntity() instanceof Fireball)) {
            return;
        }

        Location location = e.getEntity().getLocation();
        ProjectileSource projectileSource = e.getEntity().getShooter();
        if (!(projectileSource instanceof Player source)) {
            return;
        }

        IArena arena = Arena.getArenaByPlayer(source);

        if (arena == null || arena.getStatus() != GameState.playing) {
            return;
        }

        String group = arena.getGroup();
        if (!(group.equalsIgnoreCase("RankedSolo") || group.equalsIgnoreCase("RankedDuplas")
                || group.equalsIgnoreCase("Ranked1v1") || group.equalsIgnoreCase("Ranked4v4"))) {
            return;
        }

        Vector vector = location.toVector();
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        Collection<Entity> nearbyEntities = world.getNearbyEntities(location, fireballExplosionSize, fireballExplosionSize, fireballExplosionSize);

        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player player)) {
                continue;
            }

            if (!Arena.isInArena(player) || arena.isSpectator(player) || arena.isReSpawning(player)) {
                continue;
            }

            UUID playerUUID = player.getUniqueId();
            long respawnInvulnerability = BedWarsTeam.reSpawnInvulnerability.getOrDefault(playerUUID, 0L);

            if (respawnInvulnerability > System.currentTimeMillis()) {
                continue;
            }
            BedWarsTeam.reSpawnInvulnerability.remove(playerUUID);

            Vector playerVector = player.getLocation().toVector();
            Vector normalizedVector = vector.subtract(playerVector).normalize();
            Vector horizontalVector = normalizedVector.multiply(fireballHorizontal);
            double y = normalizedVector.getY();
            if (y < 0) {
                y += 1.5;
            }
            if (y <= 0.5) {
                y = fireballVertical * 1.5; // kb for not jumping
            } else {
                y = y * fireballVertical * 1.5; // kb for jumping
            }
            player.setVelocity(horizontalVector.setY(y));

            LastHit lh = LastHit.getLastHit(player);
            if (lh != null) {
                lh.setDamager(source);
                lh.setTime(System.currentTimeMillis());
            } else {
                new LastHit(player, source, System.currentTimeMillis());
            }
            if (player.equals(source)) {
                if (damageSelf > 0) {
                    player.damage(damageSelf); // damage shooter
                }
            } else {
                ITeam playerTeam = arena.getTeam(player);
                ITeam sourceTeam = arena.getTeam(source);

                if (playerTeam != null && playerTeam.equals(sourceTeam)) {
                    damagePlayer(player, damageTeammates);
                } else {
                    damagePlayer(player, damageEnemy);
                }
            }
        }
    }

    private void damagePlayer(Player player, double damageTeammates) {
        if (damageTeammates > 0) {
            EntityDamageEvent damageEvent = new EntityDamageEvent(
                    player,
                    EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                    new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, damageTeammates)),
                    new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(damageTeammates)))
            );
            player.setLastDamageCause(damageEvent);
            player.damage(damageTeammates); // damage teammates
        }
    }

    @EventHandler
    public void fireballDirectHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Fireball) || !(e.getEntity() instanceof Player player)) {
            return;
        }

        if (!Arena.isInArena(player)) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void fireballPrime(ExplosionPrimeEvent e) {
        if (!(e.getEntity() instanceof Fireball fireball)) {
            return;
        }

        ProjectileSource shooter = fireball.getShooter();

        if (!(shooter instanceof Player) || !Arena.isInArena((Player) shooter)) {
            return;
        }
        e.setFire(fireballMakeFire);
    }

}
