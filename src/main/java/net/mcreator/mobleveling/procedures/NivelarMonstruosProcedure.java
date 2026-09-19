package net.mcreator.mobleveling.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import javax.annotation.Nullable;
import java.util.Random;

@Mod.EventBusSubscriber
public class NivelarMonstruosProcedure {
	@SubscribeEvent
	public static void onEntitySpawned(EntityJoinLevelEvent event) {
		execute(event, event.getLevel(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;

		// --- EL FILTRO EXCLUSIVO DE MONSTRUOS HOSTILES ---
		if (entity instanceof Monster && entity instanceof LivingEntity _livEnt) {
			
			// --- 1. CÁLCULO DE SECTORES POR DISTANCIA (Cada 500 bloques de radio) ---
			double distancia = Math.sqrt((x * x) + (z * z));
			double nivel_final = Math.round(distancia / 500.0);

			// --- 2. FACTOR SORPRESA ALEATORIO (Dado de 0 a 5 niveles adicionales) ---
			double levelBonus = new Random().nextInt(6);
			nivel_final = nivel_final + levelBonus;

			// Candado definitivo para no romper el juego
			if (nivel_final > 100) {
				nivel_final = 100;
			}

			// --- 3. LECTURA DINÁMICA DE ATRIBUTOS BASE (Soporta cualquier Mod) ---
			double vidaBase = _livEnt.getMaxHealth();
			double damageBase = 3.0; 
			
			AttributeInstance damageAttr = _livEnt.getAttribute(Attributes.ATTACK_DAMAGE);
			if (damageAttr != null) {
				damageBase = damageAttr.getBaseValue(); // Lee el daño original de cualquier criatura
			}

			// --- 4. MULTIPLICADOR DEL +10% POR NIVEL ---
			double nuevaVida = vidaBase * (1.0 + (nivel_final * 0.1));
			double nuevoDamage = damageBase * (1.0 + (nivel_final * 0.1));

			// --- 5. INYECCIÓN NATIVA COMPATIBLE CON ARMAS DE TACZ ---
			if (world instanceof ServerLevel _level) {
				// Inyectar Vida Máxima Expandida
				_level.getServer().getCommands().performPrefixedCommand(
					new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
					"execute as @s run attribute @s minecraft:generic.max_health base set " + nuevaVida
				);
				// Inyectar Daño de Golpe Multiplicado Progresivo
				_level.getServer().getCommands().performPrefixedCommand(
					new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
					"execute as @s run attribute @s minecraft:generic.attack_damage base set " + nuevoDamage
				);
			}

			// --- 6. CURACIÓN INSTANTÁNEA AL NACER ---
			_livEnt.setHealth(_livEnt.getMaxHealth());
		}
	}
}
