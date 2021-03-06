package com.creativemd.littletiles.common.tiles.place;

import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InsideFixedHandler extends FixedHandler{

	@Override
	public double getDistance(LittleTileVec suggestedPos) {
		return 1;
	}

	@Override
	protected LittleTileBox getNewPos(World world, BlockPos pos, LittleTileBox suggested) {
		LittleTileSize size = suggested.getSize();
		
		double offset = 0;
		if(size.sizeX <= LittleTile.gridSize)
		{
			if(suggested.minX < LittleTile.minPos)
			{
				offset = LittleTile.minPos-suggested.minX;
				
			}else if(suggested.maxX > LittleTile.maxPos){
				offset = LittleTile.maxPos-suggested.maxX;
			}
			suggested.minX += offset;
			suggested.maxX += offset;
		}
		
		if(size.sizeY <= LittleTile.gridSize)
		{
			offset = 0;
			if(suggested.minY < LittleTile.minPos)
			{
				offset = LittleTile.minPos-suggested.minY;
				
			}else if(suggested.maxY > LittleTile.maxPos){
				offset = LittleTile.maxPos-suggested.maxY;
			}
			suggested.minY += offset;
			suggested.maxY += offset;
		}
		
		if(size.sizeZ <= LittleTile.gridSize)
		{
			offset = 0;
			if(suggested.minZ < LittleTile.minPos)
			{
				offset = LittleTile.minPos-suggested.minZ;
				
			}else if(suggested.maxZ > LittleTile.maxPos){
				offset = LittleTile.maxPos-suggested.maxZ;
			}
			suggested.minZ += offset;
			suggested.maxZ += offset;
		}
		
		return suggested;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleRendering(Minecraft mc, double x, double y, double z) {
		
	}

}
