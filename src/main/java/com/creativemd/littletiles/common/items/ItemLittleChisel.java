package com.creativemd.littletiles.common.items;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.container.SubContainerChisel;
import com.creativemd.littletiles.common.container.SubContainerGrabber;
import com.creativemd.littletiles.common.gui.SubGuiChisel;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket.VanillaBlockAction;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.geo.DragShape;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PositionResult;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLittleChisel extends Item implements ICreativeRendered, ILittleTile {
	
	public ItemLittleChisel()
	{
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
    {
        return 0F;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		DragShape shape = getShape(stack);
		tooltip.add("shape: " + shape.key);
		shape.addExtraInformation(stack.getTagCompound(), tooltip);
	}
	
	public static LittleTileVec min;
	
	@SideOnly(Side.CLIENT)
	public static LittleTileVec lastMax;
	
	@Override
	public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player)
    {
        return false;
    }
	
	public static DragShape getShape(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		return DragShape.getShape(stack.getTagCompound().getString("shape"));
	}
	
	public static void setShape(ItemStack stack, DragShape shape)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		stack.getTagCompound().setString("shape", shape.key);
	}
	
	/*public static void setColor(ItemStack stack, int color)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		stack.getTagCompound().setInteger("color", color);
	}
	
	public static int getColor(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		if(!stack.getTagCompound().hasKey("color"))
			setColor(stack, ColorUtils.WHITE);
		
		return stack.getTagCompound().getInteger("color");
	}
	
	public static void setBlockState(ItemStack stack, IBlockState state)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		stack.getTagCompound().setInteger("state", Block.getStateId(state));
	}
	
	public static IBlockState getBlockState(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		IBlockState state = Block.getStateById(stack.getTagCompound().getInteger("state"));
		return state.getBlock() instanceof BlockAir ? Blocks.STONE.getDefaultState() : state;			
	}*/
	
	public static LittleTilePreview getPreview(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		if(stack.getTagCompound().hasKey("preview"))
			return LittleTilePreview.loadPreviewFromNBT(stack.getTagCompound().getCompoundTag("preview"));
		
		IBlockState state = stack.getTagCompound().hasKey("state") ? Block.getStateById(stack.getTagCompound().getInteger("state")) : Blocks.STONE.getDefaultState();
		LittleTile tile = stack.getTagCompound().hasKey("color") ? new LittleTileBlockColored(state.getBlock(), state.getBlock().getMetaFromState(state), stack.getTagCompound().getInteger("color")) : new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
		tile.box = new LittleTileBox(LittleTile.minPos, LittleTile.minPos, LittleTile.minPos, LittleTile.gridSize, LittleTile.gridSize, LittleTile.gridSize);
		LittleTilePreview preview = tile.getPreviewTile();
		setPreview(stack, preview);
		return preview;
	}
	
	public static void setPreview(ItemStack stack, LittleTilePreview preview)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		NBTTagCompound nbt = new NBTTagCompound();	
		preview.writeToNBT(nbt);
		stack.getTagCompound().setTag("preview", nbt);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		return Collections.emptyList();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType)
	{
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.pushMatrix();
		
		IBakedModel model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.modid + ":chisel_background", "inventory"));
		ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
		
		mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
		
		if(cameraTransformType == TransformType.GUI)
		{
			GlStateManager.scale(0.9, 0.9, 0.9);
			
			LittleTilePreview preview = getPreview(stack);
			ItemStack blockStack = new ItemStack(preview.getPreviewBlock(), 1, preview.getPreviewBlockMeta());
			model =  mc.getRenderItem().getItemModelWithOverrides(blockStack, mc.world, mc.player); //getItemModelMesher().getItemModel(blockStack);
			if(!(model instanceof CreativeBakedModel))
				ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
			
			GlStateManager.disableDepth();
			GlStateManager.pushMatrix();
	        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
	        
	        
			try {
				if (model.isBuiltInRenderer())
	            {
	                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	                GlStateManager.enableRescaleNormal();
	                TileEntityItemStackRenderer.instance.renderByItem(blockStack);
	            }else{
					Color color = preview.hasColor() ? ColorUtils.IntToRGBA(preview.getColor()) : ColorUtils.IntToRGBA(ColorUtils.WHITE);
					color.setAlpha(255);
					ReflectionHelper.findMethod(RenderItem.class, "renderModel", "func_191967_a", IBakedModel.class, int.class, ItemStack.class).invoke(mc.getRenderItem(), model, preview.hasColor() ? ColorUtils.RGBAToInt(color) : -1, blockStack);
	            }
	        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			
			GlStateManager.popMatrix();
			
			GlStateManager.enableDepth();
		}
		
		GlStateManager.popMatrix();
		
	}
	
	@Override
	public boolean hasLittlePreview(ItemStack stack)
	{
		return min != null;
	}
	
	private static LittleTileVec cachedPos;
	private static List<LittleTileBox> cachedShape;
	private static boolean cachedLow;
	private static NBTTagCompound cachedSettings;
	
	@SideOnly(Side.CLIENT)
	private static EntityPlayer getPlayer()
	{
		return Minecraft.getMinecraft().player;
	}
	
	public static LittleTileBox getBox()
	{
		if(lastMax == null)
			lastMax = min.copy();
		
		return new LittleTileBox(new LittleTileBox(min), new LittleTileBox(lastMax));
	}
	
	public List<LittleTilePreview> getLittlePreview(ItemStack stack)
	{
		return null;
	}
	
	@Override
	public List<LittleTilePreview> getLittlePreview(ItemStack stack, boolean allowLowResolution, boolean marked)
	{
		if(min != null)
		{
			List<LittleTileBox> boxes = null;
			boolean low = allowLowResolution && !marked;
			if(cachedPos == null || !cachedPos.equals(lastMax) || !cachedSettings.equals(stack.getTagCompound()) || cachedLow != low)
			{
				
				DragShape shape = getShape(stack);
				LittleTileBox newBox = getBox();
				boxes = shape.getBoxes(newBox.getMinVec(), newBox.getMaxVec(), getPlayer(), stack.getTagCompound(), low, min, lastMax);
				cachedPos = lastMax.copy();
				cachedShape = new ArrayList<>(boxes);
				cachedSettings = stack.getTagCompound().copy();
				cachedLow = low;
			}else
				boxes = cachedShape;
			
			List<LittleTilePreview> previews = new ArrayList<>();
			
			LittleTilePreview preview = getPreview(stack);
			//int color = getColor(stack);
			//LittleTileBlock tile = !ColorUtils.isWhite(color) ? new LittleTileBlockColored(state.getBlock(), state.getBlock().getMetaFromState(state), color) : new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
			
			for (int i = 0; i < boxes.size(); i++) {
				LittleTilePreview newPreview = preview.copy();
				newPreview.box = boxes.get(i);
				previews.add(newPreview);
			}
			
			return previews;
		}
		return null;
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, List<LittleTilePreview> previews) {}
	
	@Override
	public void rotateLittlePreview(ItemStack stack, Rotation rotation)
	{
		getShape(stack).rotate(stack.getTagCompound(), rotation);
	}
	
	@Override
	public void flipLittlePreview(ItemStack stack, Axis axis)
	{
		getShape(stack).flip(stack.getTagCompound(), axis);
	}
	
	@Override
	public LittleStructure getLittleStructure(ItemStack stack)
	{
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public float getPreviewAlphaFactor()
	{
		return 0.4F;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void tickPreview(EntityPlayer player, ItemStack stack, PositionResult position, RayTraceResult result)
	{
		//lastMax = new LittleTileVec(result);
		lastMax = position.getAbsoluteVec();
		if(position.facing.getAxisDirection() == AxisDirection.NEGATIVE)
			lastMax.add(position.facing);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldCache()
	{
		return false;
	}
	
	@Override
	public boolean arePreviewsAbsolute()
	{
		return true;
	}

	@Override
	public void onDeselect(EntityPlayer player, ItemStack stack) {
		min = null;
		lastMax = null;
	}
	
	@Override
	public boolean onRightClick(EntityPlayer player, ItemStack stack, RayTraceResult result)
	{
		LittleTileVec absoluteHit = new LittleTileVec(result);
		if(ItemLittleChisel.min == null)
		{
			if(result.sideHit.getAxisDirection() == AxisDirection.NEGATIVE)
				absoluteHit.add(result.sideHit);
			
			ItemLittleChisel.min = absoluteHit;
		}else if(LittleAction.isUsingSecondMode(player))
			ItemLittleChisel.min = null;
		else
			return true;
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean onMouseWheelClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result) {
		IBlockState state = player.world.getBlockState(result.getBlockPos());
		if(SubContainerGrabber.isBlockValid(state.getBlock()))
		{
			PacketHandler.sendPacketToServer(new LittleVanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.CHISEL));
			return true;
		}
		else if(state.getBlock() instanceof BlockTile)
		{
			PacketHandler.sendPacketToServer(new LittleBlockPacket(result.getBlockPos(), player, BlockPacketAction.CHISEL, new NBTTagCompound()));
			return true;
		}
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result)
	{
		GuiHandler.openGui("chisel", new NBTTagCompound(), player);
	}
}
