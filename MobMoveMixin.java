package com.melissa.flylimit.mixin;

import com.melissa.flylimit.FlyLimit30;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives every MobEntity a 30-block upward movement ceiling measured from the
 * ground height it was standing on when it last became grounded.
 *
 * This intentionally does NOT teleport entities downward. It only removes the
 * upward component of their velocity once they reach the ceiling, allowing the
 * mob's normal AI to decide when to descend.
 */
@Mixin(MobEntity.class)
public abstract class MobMoveMixin {
    @Unique private static final double FLY_LIMIT_30$HEIGHT = 30.0D;
    @Unique private double flyLimit30$groundY = Double.NaN;
    @Unique private boolean flyLimit30$wasGrounded = false;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void flyLimit30$beforeMovement(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;

        // A normal landing establishes a new local reference height. This means
        // mountains, hills and mobs that walk uphill get their own 30-block range.
        if (mob.isOnGround()) {
            flyLimit30$groundY = mob.getY();
            flyLimit30$wasGrounded = true;
            return;
        }

        // Only create a launch reference when the mob actually leaves the ground.
        // If it was spawned/teleported in mid-air, don't reinterpret that teleport
        // as a new takeoff point.
        if (flyLimit30$wasGrounded) {
            flyLimit30$wasGrounded = false;
        }

        if (Double.isNaN(flyLimit30$groundY)) {
            flyLimit30$groundY = flyLimit30$findGroundY(mob);
        }

        if (Double.isNaN(flyLimit30$groundY)) {
            return;
        }

        final double ceiling = flyLimit30$groundY + FLY_LIMIT_30$HEIGHT;
        final double y = mob.getY();
        Vec3d velocity = mob.getVelocity();

        // Never force a mob down. At/above the ceiling, only cancel upward motion.
        if (y >= ceiling && velocity.y > 0.0D) {
            mob.setVelocity(velocity.x, 0.0D, velocity.z);
        }
    }

    @Unique
    private static double flyLimit30$findGroundY(MobEntity mob) {
        BlockPos.Mutable pos = new BlockPos.Mutable(
                mob.getBlockX(),
                Math.min(mob.getBlockY() - 1, mob.getWorld().getTopY() - 1),
                mob.getBlockZ());

        int bottom = mob.getWorld().getBottomY();
        for (int y = pos.getY(); y >= bottom; y--) {
            pos.setY(y);
            if (!mob.getWorld().getBlockState(pos).isAir()) {
                return y + 1.0D;
            }
        }
        return Double.NaN;
    }
}
