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

public class FireballListener implements Listener {

    BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    private final double defaultFireballExplosionSize, defaultFireballHorizontal, defaultFireballVertical;
    private final double defaultDamageSelf, defaultDamageEnemy, defaultDamageTeammates;
    private final double defaultFireballSpeedMultiplier, defaultFireballCooldown;
    private final boolean defaultFireballMakeFire;

    public FireballListener() {
        defaultFireballExplosionSize = 3;
        defaultFireballHorizontal = 2.5 * -1;
        defaultFireballVertical = 0.5;

        defaultDamageSelf = 2;
        defaultDamageEnemy = 1.0;
        defaultDamageTeammates = 1.0;

        defaultFireballSpeedMultiplier = 4.0;
        defaultFireballCooldown = 0.5;

        defaultFireballMakeFire = true;
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

            double fireballExplosionSize, fireballHorizontal, fireballVertical;
            double damageSelf, damageEnemy, damageTeammates;
            double fireballSpeedMultiplier, fireballCooldown;
            boolean fireballMakeFire;

            if (group.startsWith("Ranked") || group.startsWith("CxC")) {
                fireballExplosionSize = 3;
                fireballHorizontal = 1.10 * -1;
                fireballVertical = 0.80;

                damageSelf = 0.5;
                damageEnemy = 0.5;
                damageTeammates = 0.5;

                fireballSpeedMultiplier = 3.5;
                fireballCooldown = 0.5;

                fireballMakeFire = true;
            } else {
                fireballExplosionSize = defaultFireballExplosionSize;
                fireballHorizontal = defaultFireballHorizontal;
                fireballVertical = defaultFireballVertical;

                damageSelf = defaultDamageSelf;
                damageEnemy = defaultDamageEnemy;
                damageTeammates = defaultDamageTeammates;

                fireballSpeedMultiplier = defaultFireballSpeedMultiplier;
                fireballCooldown = defaultFireballCooldown;

                fireballMakeFire = defaultFireballMakeFire;
            }

            if (arena.getStatus() != GameState.playing || handItem.getType() != Main.nms.materialFireball()) {
                return;
            }

            e.setCancelled(true);

            long cooldown = (long) (fireballCooldown * 1000);
            long timeDifference = System.currentTimeMillis() - arena.getFireballCooldowns().getOrDefault(player.getUniqueId(), 0L);
            if (timeDifference <= cooldown) {
                if (fireballCooldown >= 1.0) {
                    player.sendMessage(Language.getMsg(player, Messages.ARENA_FIREBALL_COOLDOWN)
                            .replace("%bw_cooldown%", String.valueOf((cooldown - timeDifference) / 1000)));
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
        boolean isRankedOrCxC = group.startsWith("Ranked") || group.startsWith("CxC");

        double fireballExplosionSize = isRankedOrCxC ? 3 : defaultFireballExplosionSize;
        double horizontal = isRankedOrCxC ? 1.10 * -1 : defaultFireballHorizontal;
        double vertical = isRankedOrCxC ? 0.80 : defaultFireballVertical;

        double damageSelf = isRankedOrCxC ? 0.5 : defaultDamageSelf;
        double damageEnemy = isRankedOrCxC ? 0.5 : defaultDamageEnemy;
        double damageTeammates = isRankedOrCxC ? 0.5 : defaultDamageTeammates;

        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, fireballExplosionSize, fireballExplosionSize, fireballExplosionSize);

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

            Vector vector = location.toVector();
            Vector playerVector = player.getLocation().toVector();
            Vector normalizedVector = vector.subtract(playerVector).normalize();
            Vector horizontalVector = normalizedVector.multiply(horizontal);
            double y = normalizedVector.getY();
            if (y < 0) {
                y += 1.5;
            }
            if (y <= 0.5) {
                y = vertical * 1.5; // kb for not jumping
            } else {
                y = y * vertical * 1.5; // kb for jumping
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

    private void damagePlayer(Player player, double damageAmount) {
        if (damageAmount > 0) {
            EntityDamageEvent damageEvent = new EntityDamageEvent(
                    player,
                    EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                    new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, damageAmount)),
                    new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(damageAmount)))
            );
            player.setLastDamageCause(damageEvent);
            player.damage(damageAmount); // damage teammates
        }
    }

    @EventHandler
    public void explosionPrime(ExplosionPrimeEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Fireball) || !entity.hasMetadata("bw2023")) {
            return;
        }

        e.setFire(defaultFireballMakeFire);
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Fireball fireball) || !(e.getEntity() instanceof Player player) || !(fireball.getShooter() instanceof Player shooter)) {
            return;
        }

        IArena arena = Arena.getArenaByPlayer(shooter);
        if (arena == null) {
            return;
        }

        String group = arena.getGroup();
        boolean isRankedOrCxC = group.startsWith("Ranked") || group.startsWith("CxC");

        double fireballExplosionSize = isRankedOrCxC ? 3 : defaultFireballExplosionSize;
        double damageSelf = isRankedOrCxC ? 0.5 : defaultDamageSelf;
        double damageEnemy = isRankedOrCxC ? 0.25 : defaultDamageEnemy;
        double damageTeammates = isRankedOrCxC ? 0.25 : defaultDamageTeammates;

        ITeam playerTeam = arena.getTeam(player);
        ITeam shooterTeam = arena.getTeam(shooter);

        if (playerTeam != null && playerTeam.equals(shooterTeam)) {
            e.setDamage(damageTeammates);
        } else if (player.equals(shooter)) {
            e.setDamage(damageSelf);
        } else {
            e.setDamage(damageEnemy);
        }

        // Reduz o dano de queda pela metade
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setDamage(e.getDamage() / 2);
        }
    }
}