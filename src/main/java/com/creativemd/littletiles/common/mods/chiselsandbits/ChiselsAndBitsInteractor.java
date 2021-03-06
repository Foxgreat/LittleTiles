package com.creativemd.littletiles.common.mods.chiselsandbits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockLTTransparentColored;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class ChiselsAndBitsInteractor {
	
	public static boolean isChiselsAndBitsStructure(ItemStack stack)
	{
		Block block = Block.getBlockFromItem(stack.getItem());
		Map blocks = ChiselsAndBits.getBlocks().getConversions();
		for (Iterator iterator = blocks.values().iterator(); iterator.hasNext();) {
			Block block2 = (Block) iterator.next();
			if(block == block2)
				return true;
		}
		return false;
	}
	
	public static List<LittleTile> getTiles(VoxelBlob blob)
	{
		List<LittleTile> tiles = new ArrayList<>();
		for (int x = 0; x < ChiselsAndBitsManager.convertingFrom; x++) {
			for (int y = 0; y < ChiselsAndBitsManager.convertingFrom; y++) {
				for (int z = 0; z < ChiselsAndBitsManager.convertingFrom; z++) {
					IBlockState state = ModUtil.getStateById(blob.get(x, y, z));
					if(state.getBlock() == Blocks.WATER)
						state = LittleTiles.transparentColoredBlock.getDefaultState().withProperty(BlockLTTransparentColored.VARIANT, BlockLTTransparentColored.EnumType.water);
					if(state.getBlock() != Blocks.AIR)
					{
						LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
						tile.box = new LittleTileBox(new LittleTileVec(x, y, z));
						tiles.add(tile);
					}
				}
			}
		}
		TileEntityLittleTiles.combineTilesList(tiles);
		return tiles;
	}
	
	public static List<LittleTilePreview> getPreviews(VoxelBlob blob)
	{
		List<LittleTile> tiles = getTiles(blob);
		List<LittleTilePreview> previews = new ArrayList<>();
		for (int i = 0; i < tiles.size(); i++) {
			previews.add(tiles.get(i).getPreviewTile());
		}
		return previews;
	}
	
	public static List<LittleTilePreview> getPreviews(ItemStack stack)
	{
		if(isChiselsAndBitsStructure(stack))
			return getPreviews(ModUtil.getBlobFromStack(stack, null));
		return null;
	}
	
	public static List<LittleTilePreview> getPreviews(TileEntity te)
	{
		if(te instanceof TileEntityBlockChiseled)
			return getPreviews(((TileEntityBlockChiseled) te).getBlob());
		return null;
	}
	
	public static boolean isChiselsAndBitsStructure(TileEntity te)
	{
		return te instanceof TileEntityBlockChiseled;
	}
	
	public static List<LittleTile> getTiles(TileEntity te)
	{
		if(te instanceof TileEntityBlockChiseled)
			return getTiles(((TileEntityBlockChiseled) te).getBlob());
		return null;
	}
	
	public static VoxelBlob getVoxelBlob(TileEntityLittleTiles te, boolean force) throws Exception
	{
		if(LittleTile.gridSize != ChiselsAndBitsManager.convertingFrom)
			throw new Exception("Invalid grid size of " + LittleTile.gridSize + "!");
		
		VoxelBlob blob = new VoxelBlob();
		for (LittleTile tile : te.getTiles()) {
			boolean convert;
			if(tile.getClass() == LittleTileBlock.class)
				convert = true;
			else if(force)
			{
				if(tile.getClass() == LittleTileBlockColored.class)
					convert = true;
				else
					continue;
			}
			else
				throw new Exception("Cannot convert " + tile.getClass() + " tile!");
			
			if(convert)
			{
				if(!force && tile.box.getClass() != LittleTileBox.class)
					throw new Exception("Cannot convert " + tile.box.getClass() + " box!");
					
				for(int x = tile.box.minX; x < tile.box.maxX; x++)
					for(int y = tile.box.minY; y < tile.box.maxY; y++)
						for(int z = tile.box.minZ; z < tile.box.maxZ; z++)
							if(tile.box.isCompletelyFilled() || tile.box.isVecInsideBox(x, y, z))
								blob.set(x, y, z, Block.getStateId(((LittleTileBlock) tile).getBlockState()));
			}
		}
		return blob;
	}
}
