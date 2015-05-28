package com.gameminers.farrago.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IIcon;

public class ItemUtilityBelt extends ItemArmor {
	private IIcon buckle;
	public ItemUtilityBelt() {
		super(ArmorMaterial.CLOTH, 0, 2);
		setTextureName("farrago:utility_belt");
		setUnlocalizedName("utility_belt");
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
		return "farrago:textures/items/blank.png";
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		int count = getExtraRows(stack);
		if (count == 1) {
			list.add("\u00A77Adds 1 extra hotbar");
		} else {
			list.add("\u00A77Adds "+count+" extra hotbars");
		}
	}
	
	public int getExtraRows(ItemStack stack) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("Rows", 1)) {
			return 1;
		}
		return stack.getTagCompound().getByte("Rows");
	}
	
	public ItemStack setExtraRows(ItemStack stack, int rows) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setByte("Rows", (byte)rows);
		return stack;
	}

	public String getRowName(ItemStack belt, int idx) {
		if (!belt.hasTagCompound() || !belt.getTagCompound().hasKey("Row"+idx+"Name", 8)) {
			return "#"+idx;
		}
		return belt.getTagCompound().getString("Row"+idx+"Name");
	}
	
	public void setRowName(ItemStack stack, int row, String name) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setString("Row"+row+"Name", name);
	}
	
	public int getCurrentRow(ItemStack stack) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("Row", 3)) {
			return 0;
		}
		return stack.getTagCompound().getInteger("Row");
	}
	
	public void setCurrentRow(ItemStack stack, int row) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("Row", row);
	}

	public ItemStack[] getRowContents(ItemStack stack, int idx) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("Row"+idx+"Content", 9)) {
			return new ItemStack[InventoryPlayer.getHotbarSize()];
		}
		ItemStack[] row = new ItemStack[InventoryPlayer.getHotbarSize()];
		NBTTagList list = stack.getTagCompound().getTagList("Row"+idx+"Content", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			row[tag.getInteger("Slot")] = ItemStack.loadItemStackFromNBT(tag);
		}
		return row;
	}
	
	public void setRowContents(ItemStack stack, int idx, ItemStack[] contents) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == null) continue;
			NBTTagCompound tag = contents[i].writeToNBT(new NBTTagCompound());
			tag.setInteger("Slot", i);
			list.appendTag(tag);
		}
		stack.getTagCompound().setTag("Row"+idx+"Content", list);
	}

	public void deleteRow(ItemStack stack, int idx) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Row"+idx+"Content")) {
			stack.getTagCompound().removeTag("Row"+idx+"Content");
		}
	}
	
	@Override
	public boolean requiresMultipleRenderPasses() {
		return true;
	}
	
	@Override
	public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
		return pass == 1 ? buckle : super.getIconFromDamageForRenderPass(meta, pass);
	}
	
	@Override
	public int getColorFromItemStack(ItemStack stack, int pass) {
		return pass == 1 ? getColor(stack) : -1;
	}
	
	@Override
	public void registerIcons(IIconRegister p_94581_1_) {
		super.registerIcons(p_94581_1_);
		buckle = p_94581_1_.registerIcon("farrago:utility_belt_buckle");
	}
	
}
