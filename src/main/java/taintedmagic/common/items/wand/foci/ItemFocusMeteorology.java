package taintedmagic.common.items.wand.foci;

import lotr.common.LOTRDimension;
import lotr.common.LOTRLevelData;
import lotr.common.world.LOTRWorldProvider;
import lotr.common.world.LOTRWorldTypeMiddleEarth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.command.CommandWeather;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import taintedmagic.common.TaintedMagic;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXWisp;
import thaumcraft.common.items.wands.ItemWandCasting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import static taintedmagic.ModChecker.hasLOTR;

public class ItemFocusMeteorology extends ItemFocusBasic
{
	public IIcon depthIcon;
	public IIcon iconOverlay;

	private static final AspectList cost = new AspectList().add(Aspect.AIR, 1000).add(Aspect.WATER, 1000).add(Aspect.FIRE, 1000).add(Aspect.EARTH, 1000).add(Aspect.ORDER, 1000).add(Aspect.ENTROPY, 1000);

	public ItemFocusMeteorology ()
	{
		this.setCreativeTab(TaintedMagic.tabTM);
		this.setUnlocalizedName("ItemFocusMeteorology");
	}

	@SideOnly (Side.CLIENT)
	public void registerIcons (IIconRegister ir)
	{
		this.icon = ir.registerIcon("taintedmagic:ItemFocusMeteorology");
		this.iconOverlay = ir.registerIcon("taintedmagic:ItemFocusTime_overlay");
		this.depthIcon = ir.registerIcon("taintedmagic:ItemFocusMeteorology_depth");
	}

	public IIcon getFocusDepthLayerIcon (ItemStack s)
	{
		return this.depthIcon;
	}

	public String getSortingHelper (ItemStack s)
	{
		return "RAIN" + super.getSortingHelper(s);
	}

	public int getFocusColor (ItemStack s)
	{
		return 0x23D9EA;
	}

	public AspectList getVisCost (ItemStack s)
	{
		return new AspectList().add(Aspect.AIR, 1000).add(Aspect.WATER, 1000).add(Aspect.FIRE, 1000).add(Aspect.EARTH, 1000).add(Aspect.ORDER, 1000).add(Aspect.ENTROPY, 1000);
	}

	public int getActivationCooldown (ItemStack s)
	{
		return 30000;
	}

	public boolean isVisCostPerTick (ItemStack s)
	{
		return false;
	}

	@SideOnly (Side.CLIENT)
	public int getRenderPasses (int m)
	{
		return 2;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass (int meta, int pass)
	{
		return (pass == 0) ? this.icon : this.iconOverlay;
	}

	@SideOnly (Side.CLIENT)
	public boolean requiresMultipleRenderPasses ()
	{
		return true;
	}

	public ItemFocusBasic.WandFocusAnimation getAnimation (ItemStack s)
	{
		return ItemFocusBasic.WandFocusAnimation.WAVE;
	}

	public static void switchWorldRaining(World world){
		WorldInfo worldinfo = world.getWorldInfo();
		boolean worldRainingFlag = worldinfo.isRaining();
		worldinfo.setRainTime(worldRainingFlag ? 24000 : 0);
		worldinfo.setRaining(!worldRainingFlag);
	}

	@Override
	public ItemStack onFocusRightClick(ItemStack s, World w, EntityPlayer p, MovingObjectPosition mop)
	{
		ItemWandCasting wand = (ItemWandCasting) s.getItem();

		if (wand.consumeAllVis(s, p, getVisCost(s), true, false))
		{
//			System.out.println("tm_weather_core:"+w + w.provider.dimensionId);//so both client and server world will perform these all.
			switchWorldRaining(w);
			p.playSound("thaumcraft:runicShieldCharge", 0.3F, 1.0F + w.rand.nextFloat() * 0.5F);
			p.playSound("thaumcraft:wand", 0.3F, 1.0F + w.rand.nextFloat() * 0.5F);

			if (w.isRemote) {
				spawnParticles(w, p);
			}else {

				if (hasLOTR() && w.provider.dimensionId == LOTRDimension.MIDDLE_EARTH.dimensionID){
					switchWorldRaining(MinecraftServer.getServer().worldServers[0]);
				}
			}


		}
		return s;
	}

	@SideOnly (Side.CLIENT)
	public EnumRarity getRarity (ItemStack s)
	{
		return TaintedMagic.rarityCreation;
	}

	@SideOnly (Side.CLIENT)
	void spawnParticles (World w, EntityPlayer p)
	{
		for (int i = 1; i < 200; i++)
		{
			double xp = (-Math.random() * 2.0F) + (Math.random() * 2.0F);
			double zp = (-Math.random() * 2.0F) + (Math.random() * 2.0F);
			double yp = (-Math.random() * 2.0F) + (Math.random() * 2.0F);
			double off = Math.random() * 0.1;

			float green = p.worldObj.rand.nextFloat();
			float blue = p.worldObj.rand.nextFloat();

			FXWisp ef = new FXWisp(w, p.posX + xp + off, p.posY + 10 + yp + off, p.posZ + zp + off, 0.5F + ((float) Math.random() * 0.25F), 0.25F, green, blue);
			ef.setGravity(-1.5F);
			ef.shrink = true;
			ef.noClip = true;

			ef.addVelocity(xp * 0.5D, 0.0D, zp * 0.5D);

			ParticleEngine.instance.addEffect(w, ef);
		}
	}
}
