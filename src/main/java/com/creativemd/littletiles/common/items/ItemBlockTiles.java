package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedBox;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedOrdinaryBox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockTiles extends ItemBlock implements ILittleTile, ICreativeRendered{

	public ItemBlockTiles(Block block, ResourceLocation location) {
		super(block);
		setUnlocalizedName(location.getResourcePath());
		hasSubtypes = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack)
    {
		String result = super.getItemStackDisplayName(stack);
		if(stack.hasTagCompound())
		{
			LittleTileSize size = stack.getTagCompound().hasKey("size") ? new LittleTileSize("size", stack.getTagCompound()) : LittleTileBox.loadBox("bBox", stack.getTagCompound()).getSize();
			result += " (x=" + size.sizeX + ",y=" + size.sizeY + "z=" + size.sizeZ + ")";
		}
		return result;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getUnlocalizedName(ItemStack stack)
    {
		if(stack.hasTagCompound())
		{
			Block block = Block.getBlockFromName(stack.getTagCompound().getString("block"));
			if(block != null && !(block instanceof BlockAir))
				return new ItemStack(block, 1, stack.getTagCompound().getInteger("meta")).getUnlocalizedName();
		}
        return super.getUnlocalizedName(stack);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        
    }
	
	@Override
	public boolean hasLittlePreview(ItemStack stack) {
		return true;
	}
	
	@Override
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack) {
		ArrayList<LittleTilePreview> previews = new ArrayList<LittleTilePreview>();
		previews.add(LittleTilePreview.loadPreviewFromNBT(stack.getTagCompound()));
		return previews;
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, List<LittleTilePreview> previews) {
		if(previews.size() > 0)
		{
			LittleTilePreview preview = previews.get(0);
			NBTTagCompound nbt = preview.getTileData().copy();
			LittleTileBox tempBox = preview.box;
			preview.box = preview.box.copy();
			preview.box.subOffset(preview.box.getMinVec());
			preview.writeToNBT(nbt);
			preview.box = tempBox;
			//preview.size.writeToNBT("size", nbt);
			
			//if(preview.isCustomPreview() && !preview.getTypeID().equals(""))
				//nbt.setString("type", preview.getTypeID());
			
			stack.setTagCompound(nbt);
		}else
			stack.setTagCompound(new NBTTagCompound());
	}
	
	public static ItemStack getStackFromPreview(LittleTilePreview preview)
	{
		ItemStack stack = new ItemStack(LittleTiles.blockTile);
		NBTTagCompound nbt = preview.getTileData().copy();
		
		//preview.size.writeToNBT("size", nbt);
		preview.writeToNBT(nbt);
		
		//if(preview.isCustomPreview() && !preview.getTypeID().equals(""))
			//nbt.setString("type", preview.getTypeID());
		
		stack.setTagCompound(nbt);
		return stack;
	}
	
	public static List<LittleRenderingCube> getItemRenderingCubes(ItemStack stack) {
		ArrayList<LittleRenderingCube> cubes = new ArrayList<LittleRenderingCube>();
		if(stack != null && stack.hasTagCompound())
		{
			if(stack.getTagCompound().hasKey("size"))
			{
				Block block = Block.getBlockFromName(stack.getTagCompound().getString("block"));
				int meta = stack.getTagCompound().getInteger("meta");
				LittleTileSize size = new LittleTileSize("size", stack.getTagCompound());
				if(!(block instanceof BlockAir))
				{
					LittleRenderingCube cube = new LittleTileBox(new LittleTileVec(LittleTile.halfGridSize, LittleTile.halfGridSize, LittleTile.halfGridSize), size).getRenderingCube(block, meta);
					if(stack.getTagCompound().hasKey("color"))
						cube.color = stack.getTagCompound().getInteger("color");
					cubes.add(cube);
				}
			}else{
				LittleTilePreview preview = LittleTilePreview.loadPreviewFromNBT(stack.getTagCompound());
				cubes.add((LittleRenderingCube) preview.getCubeBlock());
			}
		}
		return cubes;
	}

	@Override
	public LittleStructure getLittleStructure(ItemStack stack) {
		return null;
	}

	@Override
	public List<? extends RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		if(stack != null)
			return getItemRenderingCubes(stack);
		return new ArrayList<>();
	}

}
