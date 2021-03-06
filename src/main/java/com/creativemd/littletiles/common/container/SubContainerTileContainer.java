package com.creativemd.littletiles.common.container;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.InventoryUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.controls.container.SlotControl;
import com.creativemd.creativecore.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.gui.premade.SubContainerHeldItem;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.gui.controls.SlotControlBlockIngredient;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.ColorUnit;
import com.creativemd.littletiles.common.ingredients.CombinedIngredients;
import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

public class SubContainerTileContainer extends SubContainerHeldItem {
	
	public SubContainerTileContainer(EntityPlayer player) {
		super(player);
	}
	
	@CustomEventSubscribe
	public void onSlotChange(SlotChangeEvent event)
	{
		if(event.source instanceof SlotControl)
		{
			if(event.source instanceof SlotControlBlockIngredient)
			{
				SlotControlBlockIngredient slot = (SlotControlBlockIngredient) event.source;
				
				if(slot.slot.getStack().isEmpty())
					slot.ingredient = null;
				else if(slot.ingredient != null)
					slot.ingredient.value = slot.slot.getStack().getCount() / (double) LittleTile.maxTilesPerBlock;
				
				List<BlockIngredient> inventory = new ArrayList<>();
				for (int y = 0; y < ItemTileContainer.inventoryHeight; y++) {
					for (int x = 0; x < ItemTileContainer.inventoryWidth; x++) {
						int index = x + y*ItemTileContainer.inventoryWidth;
						BlockIngredient ingredient = ((SlotControlBlockIngredient) get("item" + index)).ingredient;
						if(ingredient != null)
							inventory.add(ingredient);
					}
				}
				
				ItemTileContainer.saveInventory(stack, inventory);
				
				reloadControls();
			}else if(event.source.name.startsWith("input")){
				
				ItemStack input = ((SlotControl) event.source).slot.getStack();
				
				CombinedIngredients ingredients = LittleAction.getIngredientsOfStack(input);
				
				boolean containedColor = false;
				
				if(ingredients != null)
				{
					ColorUnit result = ItemTileContainer.storeColor(stack, ingredients.color, true);
					if(result != null && result.equals(ingredients.color))
						return ;
					
					containedColor = !ingredients.color.isEmpty();
					
					while(!input.isEmpty())
					{
						if(ItemTileContainer.storeBlocks(stack, ingredients.block.copy(), true) != null)
							break;
						input.shrink(1);
						ItemTileContainer.storeBlocks(stack, ingredients.block.copy(), false);
						if(ItemTileContainer.storeColor(stack, ingredients.color, false) != null)
							break;
					}
					
					updateSlots();
						
					
					player.playSound(SoundEvents.ENTITY_ITEMFRAME_PLACE, 1.0F, 1.0F);
				}else if(input.getItem() instanceof ItemDye){
					ColorUnit color = ColorUnit.getColors(ItemDye.DYE_COLORS[input.getItemDamage()]);
					ColorUnit result = ItemTileContainer.storeColor(stack, color, true);
					if(result != null && result.equals(color))
						return ;
					while(!input.isEmpty())
					{
						input.shrink(1);
						if(ItemTileContainer.storeColor(stack, color, false) != null)
							break;
					}
					
					containedColor = true;
					
				}
				
				if(containedColor)
				{
					reloadControls();
					
					player.playSound(SoundEvents.BLOCK_BREWING_STAND_BREW, 1.0F, 1.0F);
				}
			}
			
			
			
		}
	}
	
	public void reloadControls()
	{
		controls.clear();
		createControls();
		refreshControls();
		NBTTagCompound nbt = stack.getTagCompound().copy();
		nbt.setBoolean("reload", true);
		sendNBTToGui(nbt);
	}
	
	public InventoryBasic bagInventory;
	
	public void updateSlots()
	{
		List<BlockIngredient> inventory = ItemTileContainer.loadInventory(stack);
		for (int y = 0; y < ItemTileContainer.inventoryHeight; y++) {
			for (int x = 0; x < ItemTileContainer.inventoryWidth; x++) {
				int index = x + y*ItemTileContainer.inventoryWidth;
				bagInventory.setInventorySlotContents(index, index < inventory.size() ? inventory.get(index).getTileItemStack() : ItemStack.EMPTY);
				((SlotControlBlockIngredient) get("item" + index)).ingredient = index < inventory.size() ? inventory.get(index) : null;
			}
		}
	}

	@Override
	public void createControls() {
		
		List<BlockIngredient> inventory = ItemTileContainer.loadInventory(stack);
		bagInventory = new InventoryBasic("item", false, ItemTileContainer.inventorySize)
		{
			@Override
			public int getInventoryStackLimit()
		    {
		        return ItemTileContainer.maxStackSizeOfTiles;
		    }
		};
		for (int y = 0; y < ItemTileContainer.inventoryHeight; y++) {
			for (int x = 0; x < ItemTileContainer.inventoryWidth; x++) {
				int index = x + y*ItemTileContainer.inventoryWidth;
				bagInventory.setInventorySlotContents(index, index < inventory.size() ? inventory.get(index).getTileItemStack() : ItemStack.EMPTY);
				controls.add(new SlotControlBlockIngredient(new Slot(bagInventory, index, 5+x*18, 5+y*18){
					@Override
					public boolean isItemValid(ItemStack stack) {
						return false;
					}
				}, index < inventory.size() ? inventory.get(index) : null));
			}
		}
		
		InventoryBasic input = new InventoryBasic("input", false, 1);
		addSlotToContainer(new Slot(input, 0, 120, 5));
		
		addPlayerSlotsToContainer(player);
		
	}
	
	@Override
	public void onClosed()
	{
		player.inventory.mainInventory.set(currentIndex, stack);
		ItemStack stack = ((SlotControl) get("input0")).slot.getStack();
		if(!stack.isEmpty())
			WorldUtils.dropItem(player, stack);
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		
	}

}
