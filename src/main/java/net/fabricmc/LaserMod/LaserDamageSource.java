package net.fabricmc.LaserMod;

import java.util.Random;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class LaserDamageSource extends DamageSource {
  public static LaserDamageSource LASER_DAMAGE_SOURCE = new LaserDamageSource();

  protected LaserDamageSource() {
    super("laser");
  }

  public Text getDeathMessage(LivingEntity entity) {
    return new TranslatableText("death.lasermod.laser.variation"+(new Random()).nextInt(), entity.getDisplayName());
  }
}
