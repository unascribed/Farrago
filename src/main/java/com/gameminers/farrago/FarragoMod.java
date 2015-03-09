package com.gameminers.farrago;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCompressed;
import net.minecraft.block.material.MapColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gameminers.farrago.block.BlockCombustor;
import com.gameminers.farrago.block.BlockGlow;
import com.gameminers.farrago.block.BlockOre;
import com.gameminers.farrago.block.BlockScrapper;
import com.gameminers.farrago.client.encyclopedia.Encyclopedia;
import com.gameminers.farrago.entity.EntityBlunderbussProjectile;
import com.gameminers.farrago.entity.EntityKahurProjectile;
import com.gameminers.farrago.entity.EntityRifleProjectile;
import com.gameminers.farrago.enums.MineralColor;
import com.gameminers.farrago.enums.WoodColor;
import com.gameminers.farrago.gen.YttriumGenerator;
import com.gameminers.farrago.item.ItemFondue;
import com.gameminers.farrago.item.resource.ItemCell;
import com.gameminers.farrago.item.resource.ItemDust;
import com.gameminers.farrago.item.resource.ItemIngot;
import com.gameminers.farrago.item.resource.ItemRubble;
import com.gameminers.farrago.item.tool.ItemBlunderbuss;
import com.gameminers.farrago.item.tool.ItemKahur;
import com.gameminers.farrago.item.tool.ItemRifle;
import com.gameminers.farrago.item.tool.ItemVividOrb;
import com.gameminers.farrago.proxy.Proxy;
import com.gameminers.farrago.recipes.RecipesVividOrbDyes;
import com.gameminers.farrago.tileentity.TileEntityCombustor;
import com.gameminers.farrago.tileentity.TileEntityScrapper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(name="Farrago",modid="farrago",dependencies="required-after:KitchenSink;after:GlassPane",version="1.0")
public class FarragoMod {
	public static final Logger log = LogManager.getLogger("Farrago");
	@SidedProxy(clientSide="com.gameminers.farrago.proxy.ClientProxy", serverSide="com.gameminers.farrago.proxy.ServerProxy")
	public static Proxy proxy;
	@Instance("farrago")
	public static FarragoMod inst;
	
	public static BlockCombustor COMBUSTOR;
	public static BlockScrapper SCRAPPER;
	public static Block NETHER_STAR_BLOCK;
	public static BlockOre ORE;
	public static BlockGlow GLOW;
	
	public static ItemVividOrb VIVID_ORB;
	public static Item CAQUELON;
	public static ItemCell CELL;
	public static ItemRubble RUBBLE;
	public static ItemDust DUST;
	public static ItemIngot INGOT;
	public static ItemBlunderbuss BLUNDERBUSS;
	public static ItemFondue FONDUE;
	public static ItemRifle RIFLE;
	public static ItemKahur KAHUR;
	
	public static Map<Long, List<IRecipe>> recipes = new HashMap<Long, List<IRecipe>>();
	public static String brand;
	public static boolean copperlessEnvironment;
	public static CreativeTabs creativeTab = new CreativeTabs("farrago") {
		
		@Override
		public Item getTabIconItem() {
			return Item.getItemFromBlock(COMBUSTOR);
		}
	};
	private YttriumGenerator yttrGen;
	
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		proxy.preInit();
	}
	
	@EventHandler
	public void onInit(FMLInitializationEvent e) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new FarragoGuiHandler());
		COMBUSTOR = new BlockCombustor();
		SCRAPPER = new BlockScrapper();
		ORE = new BlockOre();
		GLOW = new BlockGlow();
		NETHER_STAR_BLOCK = new BlockCompressed(MapColor.adobeColor).setBlockTextureName("farrago:nether_star_block").setHardness(20f).setLightLevel(0.5f).setBlockName("nether_star_block").setCreativeTab(creativeTab);
		NETHER_STAR_BLOCK.setHarvestLevel("pickaxe", 2);
		
		CAQUELON = new Item().setTextureName("farrago:caquelon").setMaxStackSize(1).setUnlocalizedName("caquelon").setCreativeTab(creativeTab);
		VIVID_ORB = new ItemVividOrb();
		RUBBLE = new ItemRubble();
		BLUNDERBUSS = new ItemBlunderbuss();
		DUST = new ItemDust();
		INGOT = new ItemIngot();
		FONDUE = new ItemFondue();
		RIFLE = new ItemRifle();
		CELL = new ItemCell();
		KAHUR = new ItemKahur();
		GameRegistry.registerFuelHandler(new IFuelHandler() {
			private Random rand = new Random();
			@Override
			public int getBurnTime(ItemStack fuel) {
				if (fuel != null && fuel.getItem() == RUBBLE && fuel.getItemDamage() < 5) {
					if (fuel.stackSize/3 == rand.nextInt(16)) {
						fuel.setItemDamage(5);
					}
					return rand.nextInt(30)+5;
				}
				return 0;
			}
		});
		GameRegistry.registerTileEntity(TileEntityCombustor.class, "FarragoCombustor");
		GameRegistry.registerTileEntity(TileEntityScrapper.class, "FarragoScrapper");
		GameRegistry.registerBlock(GLOW, "glow");
		GameRegistry.registerBlock(COMBUSTOR, "combustor");
		GameRegistry.registerBlock(SCRAPPER, "scrapper");
		GameRegistry.registerBlock(NETHER_STAR_BLOCK, "netherStarBlock");
		GameRegistry.registerBlock(ORE, "watashi");
		GameRegistry.registerItem(BLUNDERBUSS, "blunderbuss");
		GameRegistry.registerItem(CAQUELON, "caquelon");
		GameRegistry.registerItem(RUBBLE, "rubble");
		GameRegistry.registerItem(DUST, "dust");
		GameRegistry.registerItem(INGOT, "ingot");
		GameRegistry.registerItem(FONDUE, "fondue");
		GameRegistry.registerItem(VIVID_ORB, "vividOrb");
		GameRegistry.registerItem(CELL, "cell");
		GameRegistry.registerItem(RIFLE, "rifle");
		GameRegistry.registerItem(KAHUR, "kahur");
		EntityRegistry.registerModEntity(EntityKahurProjectile.class, "kahurShot", 0, this, 64, 12, true);
		EntityRegistry.registerModEntity(EntityRifleProjectile.class, "rifleShot", 1, this, 64, 12, true);
		EntityRegistry.registerModEntity(EntityBlunderbussProjectile.class, "blunderbussShot", 2, this, 64, 12, true);
		ORE.registerOres();
		DUST.registerOres();
		INGOT.registerOres();
		for (String s : new String[] {ChestGenHooks.DUNGEON_CHEST, ChestGenHooks.PYRAMID_JUNGLE_CHEST, ChestGenHooks.PYRAMID_DESERT_CHEST, ChestGenHooks.STRONGHOLD_LIBRARY, ChestGenHooks.MINESHAFT_CORRIDOR}) {
			for (int color : new int[] {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF}) {
				ItemStack orb = new ItemStack(VIVID_ORB);
				VIVID_ORB.setColor(orb, color);
				ChestGenHooks.addItem(s, new WeightedRandomChestContent(orb, 0, 2, 25));
			}
		}
		GameRegistry.addRecipe(new RecipesVividOrbDyes());
		GameRegistry.addSmelting(new ItemStack(DUST, 1, 0), new ItemStack(Items.iron_ingot), 0);
		GameRegistry.addSmelting(new ItemStack(DUST, 1, 1), new ItemStack(Items.gold_ingot), 0);
		GameRegistry.addSmelting(new ItemStack(DUST, 1, 2), new ItemStack(Items.emerald), 0);
		GameRegistry.addSmelting(new ItemStack(DUST, 1, 3), new ItemStack(Items.diamond), 0);
		GameRegistry.addSmelting(new ItemStack(DUST, 1, 5), new ItemStack(INGOT, 1, 0), 0);
		GameRegistry.addSmelting(new ItemStack(DUST, 1, 6), new ItemStack(INGOT, 1, 1), 0);
		GameRegistry.addSmelting(new ItemStack(DUST, 1, 7), new ItemStack(INGOT, 1, 2), 0);
		GameRegistry.addSmelting(new ItemStack(DUST, 1, 8), new ItemStack(Items.ender_pearl), 0);
		GameRegistry.addSmelting(new ItemStack(ORE, 1, 0), new ItemStack(INGOT, 1, 0), 0);
		GameRegistry.registerWorldGenerator(yttrGen = new YttriumGenerator(), 0);
		GameRegistry.addRecipe(new ItemStack(Items.nether_star, 9),
				"B",
				'B', NETHER_STAR_BLOCK);
		GameRegistry.addRecipe(new ItemStack(Items.nether_star),
				"123",
				"4D5",
				"d6d",
				'1', new ItemStack(RUBBLE, 1, 0),
				'2', new ItemStack(RUBBLE, 1, 1),
				'3', new ItemStack(RUBBLE, 1, 2),
				'4', new ItemStack(RUBBLE, 1, 3),
				'5', new ItemStack(RUBBLE, 1, 4),
				'6', new ItemStack(RUBBLE, 1, 5),
				'D', new ItemStack(DUST, 1, 4),
				'd', new ItemStack(Blocks.diamond_block)
				);
		ItemStack eow = new ItemStack(Items.diamond_sword);
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList ench = new NBTTagList();
		{
	        NBTTagCompound enchCompound = new NBTTagCompound();
	        enchCompound.setShort("id", (short)Enchantment.sharpness.effectId);
	        enchCompound.setShort("lvl", (short)2149);
	        ench.appendTag(enchCompound);
		}
		{
	        NBTTagCompound enchCompound = new NBTTagCompound();
	        enchCompound.setShort("id", (short)Enchantment.looting.effectId);
	        enchCompound.setShort("lvl", (short)10);
	        ench.appendTag(enchCompound);
		}
		{
	        NBTTagCompound enchCompound = new NBTTagCompound();
	        enchCompound.setShort("id", (short)Enchantment.knockback.effectId);
	        enchCompound.setShort("lvl", (short)12);
	        ench.appendTag(enchCompound);
		}
        compound.setTag("ench", ench);
		compound.setBoolean("Unbreakable", true);
		eow.setTagCompound(compound);
		eow.setStackDisplayName("\u00A7cEater of Worlds");
		GameRegistry.addRecipe(eow,
				" D ",
				"+d+",
				" + ",
				'D', new ItemStack(DUST, 1, 4),
				'd', new ItemStack(Blocks.diamond_block),
				'+', new ItemStack(NETHER_STAR_BLOCK)
				);
		GameRegistry.addRecipe(new ItemStack(NETHER_STAR_BLOCK),
				"+++",
				"+++",
				"+++",
				'+', Items.nether_star
				);
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(VIVID_ORB),
				"QIQ",
				"IEI",
				"QIQ",
				'E', Items.ender_pearl,
				'I', "ingotIron",
				'Q', "blockQuartz"
				));
		GameRegistry.addRecipe(new ItemStack(FONDUE, 1, 3),
				"A",
				"C",
				'A', Items.apple,
				'C', CAQUELON
				);
		GameRegistry.addRecipe(new ItemStack(FONDUE, 1, 0),
				"M",
				"C",
				'M', Items.milk_bucket,
				'C', CAQUELON
				);
		GameRegistry.addRecipe(new ItemStack(FONDUE, 1, 1),
				"cWB",
				" Cw",
				'W', Items.water_bucket,
				'w', Items.wheat,
				'c', Items.chicken,
				'B', Items.beef,
				'C', CAQUELON
				);
		GameRegistry.addRecipe(new ItemStack(FONDUE, 1, 2),
				"ccc",
				" C ",
				'c', new ItemStack(Items.dye, 1, 3),
				'C', CAQUELON
				);
		GameRegistry.addRecipe(new ShapedOreRecipe(CAQUELON, 
				"IBI",
				" I ",
				'I', "ingotIron",
				'B', Items.bucket
				));
		GameRegistry.addRecipe(new ShapedOreRecipe(COMBUSTOR, 
				"III",
				"IBI",
				"IGI",
				'I', "ingotYttrium",
				'B', Blocks.iron_bars,
				'G', Items.gunpowder
				));
		GameRegistry.addRecipe(new ShapedOreRecipe(SCRAPPER, 
				"III",
				"QPQ",
				"BDB",
				'I', "ingotYttrium",
				'Q', Items.quartz,
				'B', "blockIron",
				'D', "gemDiamond",
				'P', Blocks.heavy_weighted_pressure_plate
				));
		GameRegistry.addRecipe(new ShapedOreRecipe(BLUNDERBUSS, 
				" I ",
				"IYG",
				" BC",
				'I', "ingotIron",
				'Y', "ingotYttrium",
				'C', "ingotYttriumCopper",
				'G', Items.gunpowder,
				'B', Items.blaze_rod
				));
		GameRegistry.addRecipe(new ShapedOreRecipe(RIFLE, 
				"D  ",
				" C ",
				" BC",
				'D', "gemDiamond",
				'C', "ingotYttriumCopper",
				'B', Items.blaze_rod
				));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CELL, 4, 0), 
				"YGY",
				"YGY",
				"YCY",
				'Y', "ingotYttrium",
				'C', "ingotYttriumCopper",
				'G', "paneGlass"
				));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(CELL, 1, 1),  new ItemStack(CELL, 1, 0), "dustCopper", "dustRedstone"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(CELL, 1, 2),  new ItemStack(CELL, 1, 0), "dustYttrium", "dustGlowstone"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(CELL, 2, 3),  new ItemStack(CELL, 1, 0), new ItemStack(CELL, 1, 0), "dustGold", "dustGold", "dustDiamond"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(CELL, 1, 4),  new ItemStack(CELL, 1, 0), "dustIron", Items.gunpowder));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(CELL, 1, 5),  new ItemStack(CELL, 1, 0), "dustEnder", "dustEmerald"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(CELL, 1, 6),  new ItemStack(CELL, 1, 0), "dustCopper", "dustRedstone", Items.blaze_powder));
		
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(DUST, 2, 6), "dustCopper", "dustYttrium"));
		OreDictionary.registerOre("dyeRed", new ItemStack(DUST, 1, 5));
		OreDictionary.registerOre("gemEnder", Items.ender_pearl);
		for (WoodColor body : WoodColor.values()) {
			if (body == WoodColor.CREATIVE) continue;
			for (WoodColor drum : WoodColor.values()) {
				if (drum == WoodColor.CREATIVE) continue;
				for (MineralColor pump : MineralColor.values()) {
					if (pump.getSelector().getRepresentation() == null) continue;
					ItemStack kahur = new ItemStack(KAHUR);
					NBTTagCompound tag = new NBTTagCompound();
					tag.setString("KahurBodyMaterial", body.name());
					tag.setString("KahurDrumMaterial", drum.name());
					tag.setString("KahurPumpMaterial", pump.name());
					kahur.setTagCompound(tag);
					GameRegistry.addRecipe(new ShapedOreRecipe(kahur,
							"B  ",
							"PD ",
							" /B",
							'B', new ItemStack(Blocks.planks, 1, body.ordinal()),
							'D', new ItemStack(Blocks.planks, 1, drum.ordinal()),
							'P', pump.getSelector().getRepresentation(),
							'/', "stickWood"));
					GameRegistry.addRecipe(new ShapedOreRecipe(kahur,
							"  B",
							" DP",
							"B/ ",
							'B', new ItemStack(Blocks.planks, 1, body.ordinal()),
							'D', new ItemStack(Blocks.planks, 1, drum.ordinal()),
							'P', pump.getSelector().getRepresentation(),
							'/', "stickWood"));
				}
			}
		}
		for (IRecipe recipe : (List<IRecipe>)CraftingManager.getInstance().getRecipeList()) {
			if (recipe == null) continue;
			ItemStack out = recipe.getRecipeOutput();
			if (out == null) continue;
			List<IRecipe> list;
			Long hash = hashItemStack(out);
			if (recipes.containsKey(hash)) {
				list = recipes.get(hash);
			} else {
				list = new ArrayList<IRecipe>();
				recipes.put(hash, list);
			}
			list.add(recipe);
		}
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		proxy.init();
	}
	@SubscribeEvent
	public void onLightning(EntityStruckByLightningEvent e) {
		if (e.entity.worldObj.isRemote) return;
		if (e.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) e.entity;
			boolean sting = false;
			for (ItemStack is : player.inventory.mainInventory) {
				if (is == null) continue;
				if (is.getItem() == FONDUE) {
					if (is.getItemDamage() == 3) {
						is.setItemDamage(4);
						sting = true;
					}
				}
			}
			if (sting) {
				player.worldObj.playSoundAtEntity(player, "farrago:cyber_sting", 0.5f, 1.0f);
			}
		}
	}
	@SubscribeEvent
	public void onDataSave(ChunkDataEvent.Save e) {
		if (!"yttrium".equals(e.getData().getString("farrago:RetroGenKey"))) {
			e.getData().setString("farrago:RetroGenKey", "yttrium");
		}
	}
	private Deque<Chunk> chunksToGen = new ArrayDeque<Chunk>();
	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent e) {
		if (e.showAdvancedItemTooltips) {
			float massF = Masses.getMass(e.itemStack);
			String massS = (massF <= 0 ? "\u00A7cERROR\u00A79" : ""+massF);
			float magic = Masses.getMagic(e.itemStack);
			if (magic > 0) {
				e.toolTip.add("\u00A79"+magic+" Kahur Magic");
			}
			e.toolTip.add("\u00A79"+massS+" Kahur Mass");
		}
		Encyclopedia.process(e.itemStack, e.entityPlayer, e.toolTip, e.showAdvancedItemTooltips);
	}
	@SubscribeEvent
	public void onDataLoad(ChunkDataEvent.Load e) {
		// TODO: Incremental retrogen
		if (!"yttrium".equals(e.getData().getString("farrago:RetroGenKey"))) {
			chunksToGen.addLast(e.getChunk());
		}
	}
	@SubscribeEvent
	public void onTick(ServerTickEvent e) {
		if (e.phase == Phase.END) {
			if (!chunksToGen.isEmpty()) {
				Chunk chunk = chunksToGen.pop();
				yttrGen.generate(chunk.worldObj.rand, chunk.xPosition, chunk.zPosition, chunk.worldObj, null, null);
				chunk.setChunkModified();
				log.info("Retrogenerating "+chunk.xPosition+", "+chunk.zPosition);
			}
		}
	}
	@EventHandler
	public void onPostInit(FMLPostInitializationEvent e) {
		proxy.postInit();
		if (OreDictionary.getOres("ingotCopper").size() <= 1) {
			copperlessEnvironment = true;
			log.warn("We are running in a copperless environment; enabling fallback copper dust drops from Yttrium ore");
		}
	}
	public static long hashItemStack(ItemStack toHash) {
		long hash = 0;
		hash |= (toHash.getItemDamage() & Short.MAX_VALUE) << Short.SIZE;
		hash |= (Item.getIdFromItem(toHash.getItem()) & Short.MAX_VALUE);
		if (toHash.hasTagCompound()) {
			hash |= (toHash.getTagCompound().hashCode() << 32);
		}
		return hash;
	}
}
