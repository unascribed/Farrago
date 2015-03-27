package com.gameminers.farrago.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.gameminers.farrago.FarragoMod;
import com.gameminers.farrago.tileentity.TileEntityTicker;

public class BlockCollector extends Block implements NameDelegate {
	private String[] types = {
			"unfiltered",
			"sunlight",
			"starlight",
			"moonlight"
	};
	private IIcon[] topI = new IIcon[types.length];
	private IIcon sideI;
	private IIcon bottomI;
	public BlockCollector() {
		super(Material.iron);setHarvestLevel("pickaxe", 1);
		setCreativeTab(FarragoMod.creativeTab);
		setHardness(5.0f);
		setStepSound(soundTypeMetal);
		setResistance(10.0f);
		setBlockBounds(0f, 0f, 0f, 1f, 0.25f, 1f);
	}
	
	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityTicker();
	}
	
	@Override
	public int tickRate(World p_149738_1_) {
		return 5;
	}
	
	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		int meta = world.getBlockMetadata(x, y, z);
		boolean operate = false;
		int time = (int) (world.getWorldTime() % 24000);
		switch (meta) {
			case 0:
				operate = true;
				break;
			case 1:
				operate = time > 0 && time < 12000;
				break;
			case 2:
				operate = time > 12000;
				break;
			case 3:
				operate = time > 14000;
				break;
		}
		if (!world.isRemote) {
			
		} else {
			if (operate) {
				String particle = "angryVillager";
				double vel = 0.05;
				switch (meta) {
					case 0:
						particle = "instantSpell";
						break;
					case 1:
						particle = "flame";
						break;
					case 2:
						particle = "witchMagic";
						break;
					case 3:
						particle = "magicCrit";
						vel = 0.5;
						break;
				}
				world.spawnParticle(particle, (x+0.5)+(rand.nextGaussian()/4D), y+0.25, (z+0.5)+(rand.nextGaussian()/4D), 0, vel, 0);
			}
		}
	}
	
	@Override
	public int damageDropped(int meta) {
		return meta;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public IIcon getIcon(int side, int meta) {
		if (side == 1) {
			return meta < 0 || meta >= types.length ? topI[0] : topI[meta];
		} else if (side == 0) {
			return bottomI;
		} else {
			return sideI;
		}
	}
	
	@Override
	public void registerBlockIcons(IIconRegister register) {
		for (int i = 0; i < types.length; i++) {
			topI[i] = register.registerIcon("farrago:collector_"+types[i]);
		}
		sideI = register.registerIcon("farrago:collector_side");
		bottomI = register.registerIcon("farrago:collector_bottom");
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < types.length; i++) {
    		list.add(new ItemStack(item, 1, i));
    	}
	}
	
	@Override
	public String getUnlocalizedName(int meta) {
		if (meta < 0 || meta >= types.length) return "tile.collector_"+types[0];
		return "tile.collector_"+types[meta];
	}

}
