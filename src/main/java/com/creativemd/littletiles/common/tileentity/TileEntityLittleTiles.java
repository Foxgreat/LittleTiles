package com.creativemd.littletiles.common.tileentity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.creativemd.creativecore.common.tileentity.TileEntityCreative;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.TickUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.BlockLayerRenderBuffer;
import com.creativemd.littletiles.client.render.LittleChunkDispatcher;
import com.creativemd.littletiles.client.render.RenderCubeLayerCache;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.common.api.te.ILittleTileTE;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.mods.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.combine.BasicCombiner;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox.LittleTileFace;
import com.creativemd.littletiles.common.utils.nbt.LittleNBTCompressionTools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityLittleTiles extends TileEntityCreative implements ITickable, ILittleTileTE {
	
	public static CopyOnWriteArrayList<LittleTile> createTileList()
	{
		return new CopyOnWriteArrayList<LittleTile>();
	}
	
	public TileEntityLittleTiles() {
		
	}
	
	private CopyOnWriteArrayList<LittleTile> tiles = createTileList();
	
	private CopyOnWriteArrayList<LittleTile> updateTiles = createTileList();
	
	@SideOnly(Side.CLIENT)
	private CopyOnWriteArrayList<LittleTile> renderTiles;
	
	@SideOnly(Side.CLIENT)
	public CopyOnWriteArrayList<LittleTile> getRenderTiles()
	{
		if(renderTiles == null)
			renderTiles = createTileList();
		return renderTiles;
	}
	
	public CopyOnWriteArrayList<LittleTile> getTiles()
	{
		return tiles;
	}
	
	private boolean hasLoaded = false;
	
	public boolean hasLoaded()
	{
		return hasLoaded;
	}
	
	public void setLoaded()
	{
		hasLoaded = true;
	}
	
	/*public void setTiles(CopyOnWriteArrayList<LittleTile> tiles)
	{
		this.tiles = tiles;
	}*/
	
	//@SideOnly(Side.CLIENT)
	//public boolean forceChunkRenderUpdate;
	
	//@SideOnly(Side.CLIENT)
	//public boolean isRendering;
	
	@SideOnly(Side.CLIENT)
	public int renderIndex;
	
	@SideOnly(Side.CLIENT)
	public boolean hasLightChanged;
	
	@SideOnly(Side.CLIENT)
	public boolean hasNeighborChanged;
	
	public int collisionChecks = 0;
	
	public final SideSolidCache sideCache = new SideSolidCache();
	
	public boolean shouldCheckForCollision()
	{
		return collisionChecks > 0;
	}
	
	@SideOnly(Side.CLIENT)
	public EntityDoorAnimation waitingAnimation;
	
	/*@SideOnly(Side.CLIENT)
	private HashMap<BlockRenderLayer, HashMap<EnumFacing, QuadCache[]>> quadCache;
	
	public HashMap<BlockRenderLayer, HashMap<EnumFacing, QuadCache[]>> getRenderCacheQuads()
	{
		if(quadCache == null)
			quadCache = new HashMap<>();
		return quadCache;
	}
	
	@SideOnly(Side.CLIENT)
	public void setQuadCache(QuadCache[] cache, BlockRenderLayer layer, EnumFacing facing)
	{
		HashMap<EnumFacing, QuadCache[]> facingCache = getRenderCacheQuads().get(layer);
		if(facingCache == null)
			facingCache = new HashMap<>();
		facingCache.put(facing, cache);
		getRenderCacheQuads().put(layer, facingCache);
	}
	
	@SideOnly(Side.CLIENT)
	public QuadCache[] getQuadCache(BlockRenderLayer layer, EnumFacing facing)
	{
		HashMap<EnumFacing, QuadCache[]> facingCache = getRenderCacheQuads().get(layer);
		if(facingCache != null)
			return facingCache.get(facing);
		return null;
	}*/
	
	/*@SideOnly(Side.CLIENT)
	private AtomicBoolean hasBeenAddedToBuffer;
	
	public AtomicBoolean getBeenAddedToBuffer()
	{
		if(hasBeenAddedToBuffer == null)
			hasBeenAddedToBuffer = new AtomicBoolean(false);
		return hasBeenAddedToBuffer;
	}*/
	
	@SideOnly(Side.CLIENT)
	public RenderChunk lastRenderedChunk;
	
	@SideOnly(Side.CLIENT)
	public void updateQuadCache(RenderChunk chunk)
	{
		//System.out.println("update cache at pos=" + getPos());
		lastRenderedChunk = chunk;
		
		//getBeenAddedToBuffer().set(false);
		
		if(renderIndex != LittleChunkDispatcher.currentRenderIndex.get())
			getCubeCache().clearCache();
		
		if(waitingAnimation != null && !getCubeCache().doesNeedUpdate())
		{
			waitingAnimation.removeWaitingTe(this);
			waitingAnimation = null;
		}
		
		boolean doesNeedUpdate = getCubeCache().doesNeedUpdate() || hasNeighborChanged || hasLightChanged;
		
		hasLightChanged = false;
		hasNeighborChanged = false;
		
		if(doesNeedUpdate)
			addToRenderUpdate(); //worldObj.getBlockState(pos).getActualState(worldObj, pos));
		//else if(!rendering.get())
			//RenderUploader.finishChunkUpdateNonThreadSafe(this);
	}
	
	@SideOnly(Side.CLIENT)
	private AtomicReference<BlockLayerRenderBuffer> buffer;
	
	/*@SideOnly(Side.CLIENT)
	private AtomicReference<BlockLayerRenderBuffer> oldBuffer;
	
	public void deleteOldBuffer()
	{
		if(oldBuffer != null)
		{
			oldBuffer.get().deleteBufferData();
			oldBuffer = null;
		}
	}*/
	
	@SideOnly(Side.CLIENT)
	public void setBuffer(BlockLayerRenderBuffer buffer)
	{
		if(this.buffer == null)
			this.buffer = new AtomicReference<BlockLayerRenderBuffer>(buffer);
		else
			this.buffer.set(buffer);
	}
	
	@SideOnly(Side.CLIENT)
	public BlockLayerRenderBuffer getBuffer()
	{
		if(buffer == null)
			buffer = new AtomicReference<>(null);
		return buffer.get();
	}
	
	@SideOnly(Side.CLIENT)
	private RenderCubeLayerCache cubeCache;
	
	public RenderCubeLayerCache getCubeCache()
	{
		if(cubeCache == null)
			cubeCache = new RenderCubeLayerCache();
		return cubeCache;
	}
	
	private boolean removeLittleTile(LittleTile tile)
	{
		boolean result = tiles.remove(tile);
		updateTiles.remove(tile);
		if(isClientSide())
			removeLittleTileClient(tile);
		return result;
	}
	
	@SideOnly(Side.CLIENT)
	private void removeLittleTileClient(LittleTile tile)
	{
		synchronized (getRenderTiles())
		{
			getRenderTiles().remove(tile);
		}
	}
	
	public void removeTiles(Collection<LittleTile> tiles)
	{
		for (LittleTile tile : tiles) {
			removeLittleTile(tile);
		}
		updateTiles();
	}
	
	
	public boolean removeTile(LittleTile tile)
	{
		boolean result = removeLittleTile(tile);
		updateTiles();
		return result;
	}
	
	@SideOnly(Side.CLIENT)
	private void addLittleTileClient(LittleTile tile)
	{
		if(tile.needCustomRendering())
		{
			synchronized (getRenderTiles())
			{
				getRenderTiles().add(tile);
			}
		}
	}
	
	private boolean addLittleTile(LittleTile tile)
	{
		if(isClientSide())
			addLittleTileClient(tile);
		if(tile.shouldTick())
			updateTiles.add(tile);
		return tiles.add(tile);
	}
	
	public void addTiles(Collection<LittleTile> tiles)
	{
		for (LittleTile tile : tiles) {
			addLittleTile(tile);
		}
		updateTiles();
	}
	
	public boolean addTile(LittleTile tile)
	{
		boolean result = addLittleTile(tile);
		updateTiles();
		return result;
	}
	
	public void updateLighting()
	{
		world.checkLight(getPos());
	}
	
	private static Field processingLoadedTiles = ReflectionHelper.findField(World.class, "processingLoadedTiles", "field_147481_N");
	
	@SideOnly(Side.CLIENT)
	private void clientCustomUpdate(Runnable run)
	{
		Minecraft.getMinecraft().addScheduledTask(run);
	}
	
	private void customTilesUpdate()
	{
		if(updateTiles.isEmpty() == ticking)
		{
			try {
				if(!processingLoadedTiles.getBoolean(world))
				{
					if(!WorldUtils.isMainThread(world))
					{
						Runnable run = new Runnable() {
							@Override
							public void run() {
								if(updateTiles.isEmpty())
									world.tickableTileEntities.remove(TileEntityLittleTiles.this);
								else
									world.tickableTileEntities.add(TileEntityLittleTiles.this);
							}
						};
						if(world.isRemote)
							clientCustomUpdate(run);
						else
							world.getMinecraftServer().addScheduledTask(run);
					}else{
						if(updateTiles.isEmpty())
							world.tickableTileEntities.remove(this);
						else
							world.tickableTileEntities.add(this);
					}
					ticking = !updateTiles.isEmpty();
				}else
					System.out.println("Could not remove/add ticking tileentity.");
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void updateTiles()
	{
		if(preventUpdate)
			return ;
		
		sideCache.reset();
		
		updateCollisionCache();
		
		if(world != null)
		{
			updateBlock();
			updateNeighbor();
			updateLighting();
		}
		if(isClientSide())
			updateCustomRenderer();
		
		customTilesUpdate();
		
		if(!world.isRemote && tiles.size() == 0)
			world.setBlockToAir(getPos());
	}
	
	@SideOnly(Side.CLIENT)
	public void updateCustomRenderer()
	{
		updateRenderBoundingBox();
		updateRenderDistance();
		getCubeCache().clearCache();
		//getBuffer().clear();
		addToRenderUpdate();
		
		//lastRenderedLightValue = 0;
	}
	
	
	@SideOnly(Side.CLIENT)
	public void onNeighBorChangedClient()
	{
		//getBuffer().clear();
		
		addToRenderUpdate();
		hasNeighborChanged = true;
		
		//updateRender();
	}
	
	@SideOnly(Side.CLIENT)
	public AtomicBoolean rendering;
	
	@SideOnly(Side.CLIENT)
	public void addToRenderUpdate()
	{
		if(rendering == null)
			rendering = new AtomicBoolean(false);
		if(!rendering.get())
			RenderingThread.addCoordToUpdate(this);
	}
	
	/**
	 * Tries to convert the TileEntity to a vanilla block
	 * @return whether it could convert it or not
	 */
	public boolean convertBlockToVanilla()
	{
		LittleTile firstTile = null;
		if(tiles.isEmpty())
		{
			world.setBlockToAir(pos);
			return true;
		}else if(tiles.size() == 1){
			if(!tiles.get(0).canBeConvertedToVanilla() || !tiles.get(0).doesFillEntireBlock())
				return false;
			firstTile = tiles.get(0);
		}else{
			boolean[][][] filled = new boolean[LittleTile.gridSize][LittleTile.gridSize][LittleTile.gridSize];
			for (LittleTile tile : tiles) {
				if(firstTile == null)
				{
					if(tile.getClass() != LittleTileBlock.class)
						return false;
					
					firstTile = tile;
				}else if(!firstTile.canBeCombined(tile) || !tile.canBeCombined(firstTile))
					return false;
				
				tile.fillInSpace(filled);
			}
			
			for (int x = 0; x < filled.length; x++) {
				for (int y = 0; y < filled[x].length; y++) {
					for (int z = 0; z < filled[x][y].length; z++) {
						if(!filled[x][y][z])
							return false;
					}
				}
			}
		}
		
		world.setBlockState(pos, ((LittleTileBlock) firstTile).getBlockState());
		
		return true;
	}
	
	public boolean isBoxFilled(LittleTileBox box)
	{
		LittleTileSize size = box.getSize();
		boolean[][][] filled = new boolean[size.sizeX][size.sizeY][size.sizeZ];
		
		for (LittleTile tile : tiles) {
			tile.fillInSpace(box, filled);
		}
		
		for (int x = 0; x < filled.length; x++) {
			for (int y = 0; y < filled[x].length; y++) {
				for (int z = 0; z < filled[x][y].length; z++) {
					if(!filled[x][y][z])
						return false;
				}
			}
		}
		return true;
	}
	
	public void updateNeighbor()
	{
		for (Iterator iterator = updateTiles.iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			tile.onNeighborChangeInside();
		}
		if(isClientSide())
			hasNeighborChanged = true;
		world.notifyNeighborsOfStateChange(getPos(), LittleTiles.blockTile, true);
	}
	
	@Override
	public boolean shouldRenderInPass(int pass)
    {
        return pass == 0 && getRenderTiles().size() > 0;
    }
	
	@SideOnly(Side.CLIENT)
	private double cachedRenderDistance;
	
	@SideOnly(Side.CLIENT)
	public void updateRenderDistance()
	{
		cachedRenderDistance = 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
    	if(cachedRenderDistance == 0)
    	{
			double renderDistance = 262144; //512 blocks
			for (Iterator iterator = getRenderTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				renderDistance = Math.max(renderDistance, tile.getMaxRenderDistanceSquared());
			}
			cachedRenderDistance = renderDistance;
    	}
    	return cachedRenderDistance;
    }
	
	@Override
	public boolean hasFastRenderer()
    {
        return false;
    }
		
	@SideOnly(Side.CLIENT)
	private AxisAlignedBB cachedRenderBoundingBox;
	
	@SideOnly(Side.CLIENT)
	private boolean requireRenderingBoundingBoxUpdate;
	
	@SideOnly(Side.CLIENT)
	public void updateRenderBoundingBox()
	{
		//cachedRenderBoundingBox = null;
		requireRenderingBoundingBoxUpdate = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
    	if(requireRenderingBoundingBoxUpdate || cachedRenderBoundingBox == null)
    	{
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double minZ = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			double maxY = Double.MIN_VALUE;
			double maxZ = Double.MIN_VALUE;
			boolean found = false;
			for (Iterator iterator = tiles.iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				if(tile.needCustomRendering())
				{
					AxisAlignedBB box = tile.getRenderBoundingBox().offset(pos);
					minX = Math.min(box.minX, minX);
					minY = Math.min(box.minY, minY);
					minZ = Math.min(box.minZ, minZ);
					maxX = Math.max(box.maxX, maxX);
					maxY = Math.max(box.maxY, maxY);
					maxZ = Math.max(box.maxZ, maxZ);
					found = true;
				}
			}
			if(found)
				cachedRenderBoundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
			else
				cachedRenderBoundingBox = new AxisAlignedBB(pos);
			
			requireRenderingBoundingBoxUpdate = false;
    	}
    	return cachedRenderBoundingBox;
    }
	
	public AxisAlignedBB getSelectionBox()
	{
		int minX = LittleTile.gridSize;
		int minY = LittleTile.gridSize;
		int minZ = LittleTile.gridSize;
		int maxX = LittleTile.minPos;
		int maxY = LittleTile.minPos;
		int maxZ = LittleTile.minPos;
		for (LittleTile tile : tiles) {
			LittleTileBox box = tile.getCompleteBox();
			minX = Math.min(box.minX, minX);
			minY = Math.min(box.minY, minY);
			minZ = Math.min(box.minZ, minZ);
			maxX = Math.max(box.maxX, maxX);
			maxY = Math.max(box.maxY, maxY);
			maxZ = Math.max(box.maxZ, maxZ);
		}
		return new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ).getBox(pos);
	}
	
	//public boolean needFullUpdate = true;

	public boolean preventUpdate = false;
	
	/*public LittleTile getTileFromPosition(int x, int y, int z)
	{
		for (LittleTile tile : tiles) {
			if(tile.isAt(x, y, z))
				return tile;
		}
		return null;
	}*/
	
	/**Used for rendering*/
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(EnumFacing facing, LittleTileFace face, LittleTile rendered)
	{
		for (Iterator iterator = tiles.iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			if(tile != rendered && (tile.doesProvideSolidFace(facing) || tile.canBeRenderCombined(rendered)))
				tile.fillFace(face);
		}
		return !face.isFilled();
	}
	
	public boolean isSpaceForLittleTile(LittleTileBox box)
	{
		for (Iterator iterator = tiles.iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			if(tile.intersectsWith(box))
				return false;
			
		}
		return true;
	}
	
	public boolean isSpaceForLittleTile(LittleTileBox box, LittleTile ignoreTile)
	{
		for (Iterator iterator = tiles.iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			if(ignoreTile != tile && tile.intersectsWith(box))
				return false;
		}
		return true;
	}
	
	public LittleTile getIntersectingTile(LittleTileBox box, LittleTile ignoreTile)
	{
		for (Iterator iterator = tiles.iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			if(ignoreTile != tile && tile.intersectsWith(box))
				return tile;
		}
		return null;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        
        if(tiles != null)
        	tiles.clear();
        if(updateTiles != null)
        	updateTiles.clear();
        collisionChecks = 0;
        preventUpdate = true;
        
        if(nbt.hasKey("tilesCount"))
        {
        
	        int count = nbt.getInteger("tilesCount");
	        for (int i = 0; i < count; i++) {
	        	NBTTagCompound tileNBT = new NBTTagCompound();
	        	tileNBT = nbt.getCompoundTag("t" + i);
				LittleTile tile = LittleTile.CreateandLoadTile(this, world, tileNBT);
				if(tile != null)
					addLittleTile(tile);
	        }
        }else{
        	List<LittleTile> tiles = LittleNBTCompressionTools.readTiles(nbt.getTagList("tiles", 10), this);
        	
        	for (int i = 0; i < tiles.size(); i++) {
				addLittleTile(tiles.get(i));
			}
        }
        
        preventUpdate = false;
        if(world != null)
        {
        	updateBlock();
        	customTilesUpdate();
        }
    }

	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);        
        nbt.setTag("tiles", LittleNBTCompressionTools.writeTiles(tiles));
		return nbt;
    }
    
    @Override
    public void getDescriptionNBT(NBTTagCompound nbt)
	{
    	int i = 0;
    	for (Iterator iterator = tiles.iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			NBTTagCompound tileNBT = new NBTTagCompound();
			NBTTagCompound packet = new NBTTagCompound();
			tile.saveTile(tileNBT);
			if(tile.supportsUpdatePacket())
			{
				if(tile.needsFullUpdate)
					tile.needsFullUpdate = false;
				else
					tileNBT.setTag("update", tile.getUpdateNBT());
			}
			
			nbt.setTag("t" + i, tileNBT);
			i++;
		}
        nbt.setInteger("tilesCount", tiles.size());
    }
    
    @Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
    	handleUpdatePacket(net, pkt.getNbtCompound());       
        super.onDataPacket(net, pkt);
    }
    
    public void handleUpdatePacket(NetworkManager net, NBTTagCompound nbt)
    {
    	ArrayList<LittleTile> exstingTiles = new ArrayList<LittleTile>();
    	ArrayList<LittleTile> tilesToAdd = new ArrayList<LittleTile>();
    	exstingTiles.addAll(tiles);
        int count = nbt.getInteger("tilesCount");
        for (int i = 0; i < count; i++) {
        	NBTTagCompound tileNBT = new NBTTagCompound();
        	tileNBT = nbt.getCompoundTag("t" + i);
        	
        	NBTTagList list = tileNBT.getTagList("boxes", 11);
        	LittleTile tile = null;
        	if(list.tagCount() > 0)
        	{
        		tile = getTile(LittleTileBox.createBox(list.getIntArrayAt(0)).getIdentifier());
        	}
			if(!exstingTiles.contains(tile))
				tile = null;
			
			boolean isIdentical = tile != null ? tile.isIdenticalToNBT(tileNBT) : false;
			if(isIdentical)
			{
				if(tile.supportsUpdatePacket() && tileNBT.hasKey("update"))
					tile.receivePacket(tileNBT.getCompoundTag("update"), net);
				else
					tile.loadTile(this, tileNBT);
				
				exstingTiles.remove(tile);
			}
			else
			{
				if(tile != null && tile.isLoaded())
					tile.structure.removeTile(tile);
				tile = LittleTile.CreateandLoadTile(this, world, tileNBT);
				if(tile != null)
					tilesToAdd.add(tile);
			}
		}
        
        synchronized (tiles)
        {
        	synchronized(updateTiles)
        	{
		        for (int i = 0; i < exstingTiles.size(); i++) {
		        	if(exstingTiles.get(i).isStructureBlock && exstingTiles.get(i).isLoaded())
		        		exstingTiles.get(i).structure.removeTile(exstingTiles.get(i));
		        	removeLittleTile(exstingTiles.get(i));
				}
		        for (int i = 0; i < tilesToAdd.size(); i++) {
					addLittleTile(tilesToAdd.get(i));
					if(tilesToAdd.get(i).isStructureBlock)
		        		tilesToAdd.get(i).checkForStructure();
				}
        	}
        }
        
        updateTiles();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
	public void handleUpdateTag(NBTTagCompound nbt)
    {
    	handleUpdatePacket(Minecraft.getMinecraft().getConnection().getNetworkManager(), nbt);
    }
    
    @Override
	public NBTTagCompound getUpdateTag()
    {
    	NBTTagCompound nbt = super.getUpdateTag();
    	
    	getDescriptionNBT(nbt);
        return nbt;
    }
    
    /**
     * uses the corner and is therefore faster
     */
    public LittleTile getTile(int[] identifier)
    {
    	for (Iterator iterator = tiles.iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			if(tile.is(identifier))
				return tile;
		}
    	return null;
    }
    
    public RayTraceResult rayTrace(EntityPlayer player)
    {
    	RayTraceResult hit = null;
		
		Vec3d pos = player.getPositionEyes(TickUtils.getPartialTickTime());
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(TickUtils.getPartialTickTime());
		Vec3d vec32 = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
		return rayTrace(pos, vec32);
    }
    
    public RayTraceResult rayTrace(Vec3d pos, Vec3d look)
    {
    	RayTraceResult hit = null;
    	for (Iterator iterator = tiles.iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			RayTraceResult Temphit = tile.rayTrace(pos, look);
			if(Temphit != null)
			{
				if(hit == null || hit.hitVec.distanceTo(pos) > Temphit.hitVec.distanceTo(pos))
				{
					hit = Temphit;
				}
			}
		}
		return hit;
    }
	
	public LittleTile getFocusedTile(EntityPlayer player)
	{
		if(!isClientSide())
			return null;
		Vec3d pos = player.getPositionEyes(TickUtils.getPartialTickTime());
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(TickUtils.getPartialTickTime());
		Vec3d vec32 = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
		return getFocusedTile(pos, vec32);
	}
	
	public LittleTile getFocusedTile(Vec3d pos, Vec3d look)
	{	
		LittleTile tileFocus = null;
		RayTraceResult hit = null;
		double distance = 0;
		for (Iterator iterator = tiles.iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			RayTraceResult Temphit = tile.rayTrace(pos, look);
			if(Temphit != null)
			{
				if(hit == null || distance > Temphit.hitVec.distanceTo(pos))
				{
					distance = Temphit.hitVec.distanceTo(pos);
					hit = Temphit;
					tileFocus = tile;
				}
			}
		}
		return tileFocus;
	}	
	
	@Override
	public void onLoad()
    {
		setLoaded();
		
		customTilesUpdate();
    }
	
	public boolean ticking = true;
	
	@Override
	public void update()
	{		
		if(updateTiles.isEmpty())
		{
			System.out.println("Ticking tileentity which shouldn't " + pos);
			return ;
		}
		
		for (Iterator iterator = updateTiles.iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			tile.updateEntity();
		}
	}
	
	public void combineTiles(LittleStructure structure) {
		BasicCombiner.combineTiles(tiles, structure);
		updateTiles();
	}
	
	public void updateCollisionCache()
	{
		collisionChecks = 0;
		for (Iterator<LittleTile> iterator = tiles.iterator(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			if(tile.shouldCheckForCollision())
				collisionChecks++;
		}
	}
	
	public void combineTiles() {
		combineTilesList(tiles);
		
		updateBlock();
		updateCollisionCache();
	}

	public static void combineTilesList(List<LittleTile> tiles) {
		BasicCombiner.combineTiles(tiles);
	}

	public boolean shouldTick()
	{
		return !updateTiles.isEmpty();
	}

	@Override
	@Method(modid = ChiselsAndBitsManager.chiselsandbitsID)
	public Object getVoxelBlob(boolean force) throws Exception
	{
		return ChiselsAndBitsManager.getVoxelBlob(this, force);
	}
	
	public class SideSolidCache
	{
		Boolean DOWN;
		Boolean UP;
		Boolean NORTH;
		Boolean SOUTH;
		Boolean WEST;
		Boolean EAST;
		
		public void reset()
		{
			DOWN = null;
			UP = null;
			NORTH = null;
			SOUTH = null;
			WEST = null;
			EAST = null;
		}
		
		protected boolean calculate(EnumFacing facing)
		{
			LittleTileBox box;
			switch(facing)
			{
			case EAST:
				box = new LittleTileBox(LittleTile.gridSize-1, 0, 0, LittleTile.gridSize, LittleTile.gridSize, LittleTile.gridSize);
				break;
			case WEST:
				box = new LittleTileBox(0, 0, 0, 1, LittleTile.gridSize, LittleTile.gridSize);
				break;
			case UP:
				box = new LittleTileBox(0, LittleTile.gridSize-1, 0, LittleTile.gridSize, LittleTile.gridSize, LittleTile.gridSize);
				break;
			case DOWN:
				box = new LittleTileBox(0, 0, 0, LittleTile.gridSize, 1, LittleTile.gridSize);
				break;
			case SOUTH:
				box = new LittleTileBox(0, 0, LittleTile.gridSize-1, LittleTile.gridSize, LittleTile.gridSize, LittleTile.gridSize);
				break;
			case NORTH:
				box = new LittleTileBox(0, 0, 0, LittleTile.gridSize, LittleTile.gridSize, 1);
				break;
			default:
				box = null;
				break;
			}
			return TileEntityLittleTiles.this.isBoxFilled(box);
		}
		
		public boolean get(EnumFacing facing)
		{
			Boolean result;
			
			switch(facing)
			{
			case DOWN:
				result = DOWN;
				break;
			case UP:
				result = UP;
				break;
			case NORTH:
				result = NORTH;
				break;
			case SOUTH:
				result = SOUTH;
				break;
			case WEST:
				result = WEST;
				break;
			case EAST:
				result = EAST;
				break;
			default:
				result = false;
			}
			
			if(result == null)
			{
				result = calculate(facing);
				set(facing, result);
			}
			
			return result;
		}
		
		public void set(EnumFacing facing, boolean value)
		{
			switch(facing)
			{
			case DOWN:
				DOWN = value;
				break;
			case UP:
				UP = value;
				break;
			case NORTH:
				NORTH = value;
				break;
			case SOUTH:
				SOUTH = value;
				break;
			case WEST:
				WEST = value;
				break;
			case EAST:
				EAST = value;
				break;
			}
		}
		
	}
	
}
