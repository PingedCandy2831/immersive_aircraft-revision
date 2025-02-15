package immersive_aircraft.entity.weapons;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.bullet.BulletEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import immersive_aircraft.util.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static immersive_aircraft.Entities.BULLET;

public class RotaryCannon extends BulletWeapon {
    private final RotationalManager rotationalManager = new RotationalManager(this);

    public RotaryCannon(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    protected Vector4f getBarrelOffset() {
        return new Vector4f(0.0f, 1.125f, 0.0f, 1.0f);
    }

    public float getVelocity() {
        return 6.0f;
    }

    public float getInaccuracy() {
        return 0.0f;
    }

    @Override
    protected Entity getBullet(Entity shooter, Vector4f position, Vector3f direction) {
        BulletEntity bullet = BULLET.get().create(shooter.level());
        assert bullet != null;
        bullet.setPos(position.x(), position.y(), position.z());
        bullet.setOwner(shooter);
        bullet.shoot(direction.x(), direction.y(), direction.z(), getVelocity(), getInaccuracy());
        return bullet;
    }

    @Override
    public void tick() {
        rotationalManager.tick();
        rotationalManager.pointTo(getEntity());
    }

    @Override
    public void fire(Vector3f direction) {
        if (spentAmmo(Config.getInstance().gunpowderAmmunition, 10)) {
            super.fire(direction);
        }
    }

    private Vector3f getDirection() {
        return rotationalManager.screenToGlobal(getEntity());
    }

    public Quaternionf getHeadTransform(float tickDelta) {
        Quaternionf quaternion = Utils.fromXYZ(0.0f, 0.0f, (float) (-getEntity().getRoll(tickDelta) / 180.0 * Math.PI));
        quaternion.mul(Utils.fromXYZ(0.0f, -rotationalManager.getYaw(tickDelta), 0.0f));
        quaternion.mul(Utils.fromXYZ(rotationalManager.getPitch(tickDelta), 0.0f, 0.0f));
        quaternion.mul(Utils.fromXYZ(0.0f, 0.0f, rotationalManager.getRoll(tickDelta)));
        return quaternion;
    }

    @Override
    public void clientFire(int index) {
        float old = rotationalManager.roll;
        rotationalManager.roll += 1.0f;

        if (Math.floor(old) != Math.floor(rotationalManager.roll)) {
            NetworkHandler.sendToServer(new FireMessage(getSlot(), index, getDirection()));
        }
    }
}
