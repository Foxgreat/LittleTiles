package com.creativemd.littletiles.common.tiles.preview;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleTilePreviewHandler {
	
	public static DefaultPreviewHandler defaultHandler = new DefaultPreviewHandler();
	
	public static class DefaultPreviewHandler extends LittleTilePreviewHandler
	{

		@Override
		public boolean canBeConvertedToBlockEntry(LittleTilePreview preview) {
			return true;
		}

		@Override
		public String getPreviewBlockName(LittleTilePreview preview) {
			return preview.getTileData().getString("block");
		}

		@Override
		public Block getPreviewBlock(LittleTilePreview preview) {
			if(preview.getTileData().hasKey("block"))
				return Block.getBlockFromName(preview.getTileData().getString("block"));
			return Blocks.AIR;
		}

		@Override
		public int getPreviewBlockMeta(LittleTilePreview preview) {
			return preview.getTileData().getInteger("meta");
		}

		@Override
		public boolean hasColor(LittleTilePreview preview) {
			return preview.getTileData().hasKey("color");
		}

		@Override
		public int getColor(LittleTilePreview preview) {
			if(preview.getTileData().hasKey("color"))
				return preview.getTileData().getInteger("color");
			return -1;
		}
		
		@Override
		public void setColor(LittleTilePreview preview, int color) {
			if(ColorUtils.isWhite(color))
			{
				if(preview.getTileData().getString("tID").equals("BlockTileColored"))
					preview.getTileData().setString("tID", "BlockTileBlock");
				preview.getTileData().removeTag("color");
			}
			else
			{
				if(preview.getTileData().getString("tID").equals("BlockTileBlock"))
					preview.getTileData().setString("tID", "BlockTileColored");
				preview.getTileData().setInteger("color", color);
			}
				
			
		}

		@SideOnly(Side.CLIENT)
		public RenderCubeObject getCubeBlock(LittleTilePreview preview)
		{
			RenderCubeObject cube = preview.box.getRenderingCube(getPreviewBlock(preview), getPreviewBlockMeta(preview));
			if(preview.getTileData().hasKey("color"))
				cube.color = preview.getTileData().getInteger("color");
			return cube;
		}

		@Override
		public BlockIngredient getBlockIngredient(LittleTilePreview preview) {
			return new BlockIngredient(preview.getPreviewBlock(), preview.getPreviewBlockMeta(), preview.getPercentVolume());
		}

		@Override
		public void flipPreview(Axis axis, LittleTilePreview preview, LittleTileVec doubledCenter) {
			
		}

		@Override
		public void rotatePreview(Rotation direction, LittleTilePreview preview, LittleTileVec doubledCenter) {
			
		}

		@Override
		public boolean canBeNBTGrouped() {
			return true;
		}

		
	}
	
	public abstract boolean canBeConvertedToBlockEntry(LittleTilePreview preview);
	
	public abstract String getPreviewBlockName(LittleTilePreview preview);
	
	public abstract Block getPreviewBlock(LittleTilePreview preview);
	
	public abstract int getPreviewBlockMeta(LittleTilePreview preview);
	
	public abstract boolean hasColor(LittleTilePreview preview);
	
	public abstract int getColor(LittleTilePreview preview);
	
	public abstract void setColor(LittleTilePreview preview, int color);
	
	@SideOnly(Side.CLIENT)
	public abstract RenderCubeObject getCubeBlock(LittleTilePreview preview);
	
	public abstract BlockIngredient getBlockIngredient(LittleTilePreview preview);
	
	public abstract void flipPreview(Axis axis, LittleTilePreview previewn, LittleTileVec doubledCenter);
	
	public abstract void rotatePreview(Rotation direction, LittleTilePreview preview, LittleTileVec doubledCenter);
	
	public abstract boolean canBeNBTGrouped();
}
