package com.creativemd.littletiles.common.items;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.container.SubContainerWrench;
import com.creativemd.littletiles.common.gui.SubGuiWrench;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLittleWrench extends Item implements IGuiCreator{
	
	public ItemLittleWrench()
	{
		setCreativeTab(LittleTiles.littleTab);
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add("used to create structures");
		tooltip.add("from recipes and");
		tooltip.add("create recipes from structures");
		tooltip.add("rightclick on a block");
		tooltip.add("will combine tiles");
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
		if(hand == EnumHand.OFF_HAND)
			return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand)); 
		if(!world.isRemote)
			GuiHandler.openGuiItem(player, world);
        return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			if(!world.isRemote)
			{
				((TileEntityLittleTiles) tileEntity).combineTiles();
				((TileEntityLittleTiles) tileEntity).convertBlockToVanilla();
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
    }

	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiWrench();
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubContainerWrench(player);
	}

}
