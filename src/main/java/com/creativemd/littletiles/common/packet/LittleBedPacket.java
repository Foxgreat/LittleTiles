package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.structure.LittleBed;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileAbsoluteCoord;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class LittleBedPacket extends CreativeCorePacket {
	
	public LittleTileAbsoluteCoord coord;
	public int playerID;
	
	public LittleBedPacket() {
		
	}
	
	public LittleBedPacket(LittleTileAbsoluteCoord coord) {
		this.coord = coord;
		this.playerID = -1;
	}
	
	public LittleBedPacket(LittleTileAbsoluteCoord coord, EntityPlayer player) {
		this(coord);
		this.playerID = player.getEntityId();
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		LittleAction.writeAbsoluteCoord(coord, buf);
		buf.writeInt(playerID);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		coord = LittleAction.readAbsoluteCoord(buf);
		playerID = buf.readInt();
	}

	@Override
	public void executeClient(EntityPlayer player) {
		Entity entity = playerID == -1 ? player : player.world.getEntityByID(playerID);
		if(entity instanceof EntityPlayer)
		{
			LittleTile tile;
			try {
				tile = LittleAction.getTile(player.world, coord);
				if(tile.isLoaded() && tile.structure instanceof LittleBed)
				{
					((LittleBed) tile.structure).trySleep((EntityPlayer) entity, tile.structure.getHighestCenterPoint());
				}
			} catch (LittleActionException e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
	
}
