package com.creativemd.littletiles.common.container;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException.NotEnoughVolumeExcepion;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.mods.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerWrench extends SubContainer{
	
	public InventoryBasic basic = new InventoryBasic("default", false, 2);
	
	public SubContainerWrench(EntityPlayer player) {
		super(player);
	}

	@Override
	public void createControls() {
		addSlotToContainer(new Slot(basic, 0, 5, 5));
		addSlotToContainer(new Slot(basic, 1, 35, 5));
		
		addPlayerSlotsToContainer(player);
	}

	
	/*public static ArrayList<BlockEntry> getMissing(ArrayList<LittleTilePreview> tiles, ArrayList<BlockEntry> entries)
	{
		ArrayList<BlockEntry> missing = new ArrayList<>();
		for (int i = 0; i < tiles.size(); i++) {
			Block blockofTile = tiles.get(i).getPreviewBlock();
			int meta = tiles.get(i).getPreviewBlockMeta();
			double size = tiles.get(i).size.getPercentVolume();
			int j = 0;
			boolean found = false;
			while(j < entries.size())
			{
				if(blockofTile == entries.get(j).block && meta == entries.get(j).meta)
				{
					double amount = Math.min(entries.get(j).value, size);
					entries.get(j).value -= amount;
					size -= amount;
					
					if(entries.get(j).value <= 0)
					{
						entries.remove(j);
						if(size > 0)
							continue;
					}
					
					if(size <= 0)
					{
						found = true;
						break;
					}
				}
				j++;
			}
			if(!found)
			{
				BlockEntry entry = new BlockEntry(blockofTile, meta, size);
				int index = missing.indexOf(entry);
				if(index == -1)
					missing.add(entry);
				else
					missing.get(index).value += size;
			}
		}
		return missing;
	}*/
	
	@Override
	public void onClosed()
	{
		for (int i = 0; i < basic.getSizeInventory(); i++) {
			if(basic.getStackInSlot(i) != null)
				player.dropItem(basic.getStackInSlot(i), false);
		}
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		ItemStack stack1 = basic.getStackInSlot(0);
		ItemStack stack2 = basic.getStackInSlot(1);
		if(!stack1.isEmpty())
		{
			if(stack1.getItem() instanceof ItemRecipe)
			{
				if(stack1.getTagCompound() != null && !stack1.getTagCompound().hasKey("x"))
				{
					List<LittleTilePreview> tiles = LittleTilePreview.getPreview(stack1);
					
					try {
						if(LittleAction.drainPreviews(player, tiles))
						{
							ItemStack stack = new ItemStack(LittleTiles.multiTiles);
							stack.setTagCompound((NBTTagCompound) stack1.getTagCompound().copy());
							if(!player.inventory.addItemStackToInventory(stack))
								WorldUtils.dropItem(player, stack);
						}
					} catch (NotEnoughIngredientsException e) {
						e.printStackTrace();
					}
				}
			}else if(ChiselsAndBitsManager.isChiselsAndBitsStructure(stack1)){
				List<LittleTilePreview> previews = ChiselsAndBitsManager.getPreviews(stack1);
				if(previews != null && !previews.isEmpty() && stack2.isEmpty())
				{
					stack2 = new ItemStack(LittleTiles.multiTiles);
					LittleTilePreview.savePreviewTiles(previews, stack2);
					basic.setInventorySlotContents(0, ItemStack.EMPTY);
					basic.setInventorySlotContents(1, stack2);
				}
			}else{
				ILittleTile tile = PlacementHelper.getLittleInterface(stack1);
				if(tile != null && !stack2.isEmpty() && stack2.getItem() instanceof ItemRecipe)
				{
					stack2.setTagCompound((NBTTagCompound) stack1.getTagCompound().copy());
				}
			}				
		}
	}

}
