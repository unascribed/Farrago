package com.gameminers.kahur;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

public class EntityKahurProjectile extends EntityThrowable {
	private int damage = 2;
	
	public EntityKahurProjectile(World p_i1773_1_) {
        super(p_i1773_1_);
    }

    public EntityKahurProjectile(World p_i1774_1_, EntityLivingBase p_i1774_2_) {
        super(p_i1774_1_, p_i1774_2_);
    }

    public EntityKahurProjectile(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_) {
        super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
    }

    @Override
    protected void entityInit() {
    	dataWatcher.addObjectByDataType(12, 5);
    }
    
    public int getDamage() {
		return damage;
	}
    
    public ItemStack getItem() {
    	return dataWatcher.getWatchableObjectItemStack(12);
	}
    
    public void setDamage(int damage) {
		this.damage = damage;
	}
    
    public void setItem(ItemStack item) {
		dataWatcher.updateObject(12, item);
	}
    
    @Override
    public void writeEntityToNBT(NBTTagCompound p_70014_1_) {
    	super.writeEntityToNBT(p_70014_1_);
    	if (getItem() != null) {
    		p_70014_1_.setTag("KahurItem", getItem().writeToNBT(new NBTTagCompound()));
    	}
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound p_70037_1_) {
    	super.readEntityFromNBT(p_70037_1_);
    	if (p_70037_1_.hasKey("KahurItem")) {
    		setItem(ItemStack.loadItemStackFromNBT(p_70037_1_.getCompoundTag("KahurItem")));
    	}
    }
    
    @Override
    public void onUpdate() {
    	super.onUpdate();
    	if (entity != null) {
    		entity.onUpdate();
    	}
    }
    
    @Override
    public void onEntityUpdate() {
    	super.onEntityUpdate();
    	if (entity != null) {
    		entity.onEntityUpdate();
    	}
    }
    
	@Override
	protected void onImpact(MovingObjectPosition pos) {
		if (pos.entityHit != null) {
			if (pos.entityHit instanceof EntityLiving) {
				((EntityLiving)pos.entityHit).attackEntityFrom(DamageSource.causeThrownDamage(getThrower(), this), damage);
			}
		}
		setDead();
		if (Block.getBlockFromItem(getItem().getItem()) == Blocks.tnt) {
			worldObj.createExplosion(this, posX, posY, posZ, 2.7f, true);
			return;
		}
		if (getItem().isItemStackDamageable()) {
			if (getThrower() != null) {
				getItem().damageItem(20, getThrower());
			}
		}
		if (getItem().getItem() == Items.ender_pearl) {
			if (!worldObj.isRemote) {
				if (getThrower() != null && getThrower() instanceof EntityPlayerMP) {
	                EntityPlayerMP entityplayermp = (EntityPlayerMP)getThrower();
	                if (entityplayermp.playerNetServerHandler.func_147362_b().isChannelOpen() && entityplayermp.worldObj == worldObj) {
	                    EnderTeleportEvent event = new EnderTeleportEvent(entityplayermp, posX, posY, posZ, 5.0F);
	                    if (!MinecraftForge.EVENT_BUS.post(event)) {
		                    if (getThrower().isRiding()) {
		                        getThrower().mountEntity((Entity)null);
		                    }
	
		                    getThrower().setPositionAndUpdate(event.targetX, event.targetY, event.targetZ);
		                    getThrower().fallDistance = 0.0F;
		                    getThrower().attackEntityFrom(DamageSource.fall, event.attackDamage);
		                    return;
	                    }
	                }
	            }
			}
		}
		if (!worldObj.isRemote) {
			entityDropItem(getItem(), 0.2f);
		}
	}

	private Entity entity;
	
	public Entity getDummyEntity() {
		if (entity == null && getItem() != null) {
			entity = new EntityItem(worldObj, posX, posY, posZ, getItem());
			Entity newEntity = getItem().getItem().createEntity(worldObj, entity, getItem());
			if (newEntity != null) {
				entity = newEntity;
			}
		}
		return entity;
	}
}
