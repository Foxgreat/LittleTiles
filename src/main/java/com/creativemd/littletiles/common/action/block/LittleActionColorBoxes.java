package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.HashMapDouble;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.container.SubContainerGrabber;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.ColorUnit;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleActionColorBoxes extends LittleActionBoxes {
	
	public int color;
	public boolean toVanilla;
	
	public LittleActionColorBoxes(List<LittleTileBox> boxes, int color, boolean toVanilla) {
		super(boxes);
		this.color = color;
		this.toVanilla = toVanilla;
	}
	
	public LittleActionColorBoxes() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		super.writeBytes(buf);
		buf.writeInt(color);
		buf.writeBoolean(toVanilla);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		super.readBytes(buf);
		color = buf.readInt();
		toVanilla = buf.readBoolean();
	}
	
	public HashMapList<Integer, LittleTileBox> revertList;
	
	public void addRevert(int color, List<LittleTileBox> boxes, LittleTileVec offset)
	{
		List<LittleTileBox> newBoxes = new ArrayList<>();
		for (LittleTileBox box : boxes) {
			box = box.copy();
			box.addOffset(offset);
			newBoxes.add(box);
		}
		revertList.add(color, newBoxes);
	}
	
	public ColorUnit action(TileEntityLittleTiles te, List<LittleTileBox> boxes, ColorUnit gained, boolean simulate)
	{
		LittleTileVec offset = new LittleTileVec(te.getPos());
		
		double colorVolume = 0;
		
		for (Iterator<LittleTile> iterator = te.getTiles().iterator(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			
			LittleTileBox intersecting = null;
			boolean intersects = false;
			for (int j = 0; j < boxes.size(); j++) {
				if(tile.intersectsWith(boxes.get(j)))
				{
					intersects = true;
					intersecting = boxes.get(j);
					break;
				}
			}
			
			if(!intersects || !(tile.getClass() == LittleTileBlock.class || tile instanceof LittleTileBlockColored) || (tile.isStructureBlock && (!tile.isLoaded() || !tile.structure.hasLoaded())))
				continue;
			
			if(!LittleTileBlockColored.needsToBeRecolored((LittleTileBlock) tile, color))
				continue;
			
			if(tile.canBeSplitted() && !tile.equalsBox(intersecting))
			{				
				if(simulate)
				{
					double volume = 0;
					List<LittleTileBox> cutout = new ArrayList<>();
					tile.cutOut(boxes, cutout);
					for (LittleTileBox box2 : cutout) {
						colorVolume += box2.getPercentVolume();
						volume += box2.getPercentVolume();
					}
					
					gained.addColorUnit(ColorUnit.getRequiredColors(tile.getPreviewTile(), volume));
					
				}else{
					List<LittleTileBox> cutout = new ArrayList<>();
					List<LittleTileBox> newBoxes = tile.cutOut(boxes, cutout);
					
					if(newBoxes != null)
					{						
						addRevert(LittleTileBlockColored.getColor((LittleTileBlock) tile), cutout, offset);
						
						LittleTile tempTile = tile.copy();
						LittleTile changedTile = LittleTileBlockColored.setColor((LittleTileBlock) tempTile, color);
						if(changedTile == null)
							changedTile = tempTile;
						
						if(tile.isStructureBlock)
							tile.structure.removeTile(tile);
						
						for (int i = 0; i < newBoxes.size(); i++) {
							LittleTile newTile = tile.copy();
							newTile.box = newBoxes.get(i);
							newTile.place();
							if(tile.isStructureBlock)
								tile.structure.addTile(newTile);
						}
						
						for (int i = 0; i < cutout.size(); i++) {
							LittleTile newTile = changedTile.copy();
							newTile.box = cutout.get(i);
							newTile.place();
							if(tile.isStructureBlock)
								tile.structure.addTile(newTile);
						}
						
						if(tile.isMainBlock)
							tile.structure.selectMainTile();
						
						if(tile.isStructureBlock)
							tile.structure.updateStructure();
						
						tile.isStructureBlock = false;
						tile.destroy();
					}
				}
			}else{
				if(simulate)
				{
					colorVolume += tile.getPercentVolume();
					gained.addColorUnit(ColorUnit.getRequiredColors(tile.getPreviewTile(), tile.getPercentVolume()));
				}else{
					List<LittleTileBox> oldBoxes = new ArrayList<>();
					oldBoxes.add(tile.box);
					
					addRevert(LittleTileBlockColored.getColor((LittleTileBlock) tile), oldBoxes, offset);
					
					LittleTile changedTile = LittleTileBlockColored.setColor((LittleTileBlock) tile, color);
					if(changedTile != null)
					{
						changedTile.place();
						
						if(tile.isStructureBlock)
						{
							changedTile.isStructureBlock = true;
							changedTile.structure.removeTile(tile);
							changedTile.structure.addTile(changedTile);
							
							if(tile.isStructureBlock)
								tile.structure.updateStructure();
							
							if(tile.isMainBlock)
								changedTile.structure.setMainTile(changedTile);
						}
						
						tile.isStructureBlock = false;
						tile.destroy();
					}
				}
			}
		}
		ColorUnit toDrain = ColorUnit.getRequiredColors(color);
		toDrain.scale(colorVolume);
		
		gained.drain(toDrain);
		
		return toDrain;
	}

	@Override
	public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleTileBox> boxes) throws LittleActionException {
		TileEntity tileEntity = loadTe(player, world, pos, true);
		
		if(tileEntity instanceof TileEntityLittleTiles)
		{			
			TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
			
			List<BlockIngredient> entries = new ArrayList<>();
			
			te.preventUpdate = true;
			
			ColorUnit gained = new ColorUnit();
			
			ColorUnit toDrain = action(te, boxes, gained, true);
			
			if(addIngredients(player, null, gained, true))
			{
				drainIngredients(player, null, toDrain);
				addIngredients(player, null, gained);
				
				action(te, boxes, gained, false);
			}
			
			te.preventUpdate = false;
			
			te.combineTiles();
			
			if(toVanilla)
				te.convertBlockToVanilla();
		}
	}
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		revertList = new HashMapList<>();
		return super.action(player);
	}

	@Override
	public boolean canBeReverted() {
		return true;
	}

	@Override
	public LittleAction revert() {
		LittleAction[] actions = new LittleAction[revertList.size()];
		int i = 0;
		for (Entry<Integer, ArrayList<LittleTileBox>> entry : revertList.entrySet()) {
			actions[i] = new LittleActionColorBoxes(entry.getValue(), entry.getKey(), true);
			i++;
		}
		return new LittleActionCombined(actions);
	}
	
}
