package com.rwtema.denseores;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/*  Mostly the same from 1.6.,
 *  couple of names have changed but everything works the
 *  same as before
 * 
 * I'm using the 16 metadata values to store each ore block.
 *  (We don't really need to worry about block ids in 1.7
 *   but that's no reason to be wasteful)
 */

public class BlockDenseOre extends BlockOre {

	// no constructor needed here but you still need to specify a material for
	// other blocks.

	// Ore Entry stuff
	public DenseOre[] entry = new DenseOre[16];
	public Block[] baseBlocks = new Block[16];
	public boolean[] valid = new boolean[16];
	public boolean init = false;

	public static Block getBlock(String name) {
		return GameData.blockRegistry.get(name);
	}

	public static Block getBlock(DenseOre ore) {
		return ore != null ? getBlock(ore.baseBlock) : null;
	}

	public void init() {
		init = true;

		for (int i = 0; i < 16; i++) {
			baseBlocks[i] = getBlock(entry[i]);
			valid[i] = baseBlocks[i] != null;
		}
	}

	public Block getBlock(int id) {
		if (!init)
			init();

		return baseBlocks[id];
	}

	public boolean isValid(int id) {
		if (!init)
			init();

		return valid[id];
	}

	public void setEntry(int id, DenseOre ore) {
		this.entry[id] = ore;
	}

	// add creative blocks
	@SideOnly(Side.CLIENT)
	@Override
	public void func_149666_a(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
		for (int i = 0; i < 16; i++)
			if (isValid(i))
				p_149666_3_.add(new ItemStack(p_149666_1_, 1, i));

	}

	public IIcon[] icons = new IIcon[16];

	// get icon from side/metadata
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon func_149691_a(int side, int meta) {

		return icons[meta];

	}

	// register icons
	@SideOnly(Side.CLIENT)
	@Override
	public void func_149651_a(IIconRegister register) {
		if (register instanceof TextureMap) { // should always be true (...but
												// you never know)
			TextureMap map = (TextureMap) register;
			for (int i = 0; i < 16; i++) {
				if (isValid(i)) {

					// Registering custom icon classes

					// name of custom icon ( must equal getIconName() )
					String name = TextureOre.getDerivedName(entry[i].texture);
					// see if there's already an icon of that name
					TextureAtlasSprite texture = map.getTextureExtry(name);
					if (texture == null) {
						// if not create one and put it in the register
						texture = new TextureOre(entry[i].texture, entry[i].underlyingBlock);
						map.setTextureEntry(name, texture);
					}

					icons[i] = map.getTextureExtry(name);
				}
			}
		}
	}

	// metadata dropped
	@Override
	public int func_149692_a(int p_149692_1_) {
		return p_149692_1_;
	}

	// get drops
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		if (isValid(metadata)) {
			Block base = getBlock(metadata);
			int m = entry[metadata].metadata;

			// get base drops 3 times
			for (int j = 0; j < 3; j++) {
				int count = quantityDropped(m, fortune, world.rand);
				for (int i = 0; i < count; i++) {
					Item item = base.func_149650_a(m, world.rand, fortune);
					if (item != null) {
						ret.add(new ItemStack(item, 1, base.func_149692_a(m)));
					}
				}
			}
		}
		return ret;
	}

	// get hardness
	@Override
	public float func_149712_f(World world, int x, int y, int z) {
		int metadata = world.getBlockMetadata(x, y, z);
		if (isValid(metadata)) {
			Block base = getBlock(metadata);
			float t = this.field_149782_v;

			// quickly change metadata to match what is expected
			world.setBlockMetadataWithNotify(x, y, z, entry[metadata].metadata, 0);
			try {
				t = base.func_149712_f(world, x, y, z);
			} catch (Exception e) {
				// oh oh, it seems it didn't like having a different block id.
				System.out.println("The ore block " + entry[metadata].id + "(" + entry[metadata].baseBlock + ")"
						+ " has thrown an error while getting the hardness value. It is likely not compatible with Dense ores");
				e.printStackTrace();
				world.setBlockMetadataWithNotify(x, y, z, entry[metadata].metadata, 0); // just
																						// in
																						// case
				throw new RuntimeException(e);
			}

			// set it back
			world.setBlockMetadataWithNotify(x, y, z, metadata, 0);
			return t;
		}
		return this.field_149782_v;
	}

}