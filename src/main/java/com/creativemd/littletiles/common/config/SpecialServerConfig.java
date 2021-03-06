package com.creativemd.littletiles.common.config;

import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.translation.I18n;

public class SpecialServerConfig {
	
	public static boolean strictMining = false;
	
	public static boolean limitEditBlocksSurvival = false;
	public static int maxEditBlocks = 10;
	
	public static boolean editUnbreakable = false;
	
	public static boolean limitPlaceBlocksSurvival = false;
	public static int maxPlaceBlocks = 10;
	
	public static boolean isEditLimited(EntityPlayer player)
	{
		if(limitEditBlocksSurvival)
			return !player.isCreative();
		return false;
	}
	
	public static boolean isPlaceLimited(EntityPlayer player)
	{
		if(limitPlaceBlocksSurvival)
			return !player.isCreative();
		return false;
	}
	
	public static class NotAllowedToEditException extends LittleActionException {

		public NotAllowedToEditException() {
			super("exception.permission.edit");
		}
		
		@Override
		public String getLocalizedMessage()
		{
			return I18n.translateToLocalFormatted(getMessage(), maxEditBlocks);
		}
		
	}
	
	public static class NotAllowedToPlaceException extends LittleActionException {

		public NotAllowedToPlaceException() {
			super("exception.permission.place");
		}
		
		@Override
		public String getLocalizedMessage()
		{
			return I18n.translateToLocalFormatted(getMessage(), maxPlaceBlocks);
		}
		
	}
	
}
