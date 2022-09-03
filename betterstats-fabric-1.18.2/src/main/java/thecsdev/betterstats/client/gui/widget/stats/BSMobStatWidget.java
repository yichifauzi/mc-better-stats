package thecsdev.betterstats.client.gui.widget.stats;

import static thecsdev.betterstats.BetterStats.lt;
import static thecsdev.betterstats.BetterStats.tt;

import java.awt.Point;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.util.GuiUtils;
import thecsdev.betterstats.client.gui.util.StatUtils.SUMobStat;
import thecsdev.betterstats.config.BSConfig;
import thecsdev.betterstats.config.BSMobStatRenderConfig;

public class BSMobStatWidget extends BSStatWidget
{
	// ==================================================
	//public static final int defaultMobGuiSize = 38; //the value is 50 for 80x80px - UNUSED
	// --------------------------------------------------
	public final SUMobStat mobStat;
	public final String[] mobNameSplit;
	
	public final LivingEntity livingEntity;
	// --------------------------------------------------
	public final int cache_mobSize;
	public final Point cache_mobOffset;
	// ==================================================
	public BSMobStatWidget(BetterStatsScreen parent, SUMobStat mobStat, int x, int y) { this(parent, mobStat, x, y, 50); }
	public BSMobStatWidget(BetterStatsScreen parent, SUMobStat mobStat, int x, int y, int width)
	{
		//define mob info and stuff
		super(parent, x, y, width, width, BSConfig.COLOR_STAT_BG);
		this.mobStat = mobStat;
		this.mobNameSplit = mobStat.entityName.split("([\\r?\\n])|([ ]{1,})");
		livingEntity = (mobStat.entity instanceof LivingEntity) ? (LivingEntity) mobStat.entity : null;
		updateTooltip();
		
		if(livingEntity != null)
			livingEntity.baseTick();
		
		//calculate the mob's gui size and offset in pixels, and then cache it
		cache_mobSize = BSMobStatRenderConfig.getLivingEntityGUISize(livingEntity, width);
		cache_mobOffset = BSMobStatRenderConfig.getLivingEntityGUIPos(livingEntity, width);
	}
	
	@Override
	@SuppressWarnings("deprecation") //idk, i'm gonna do this just in case
	protected void finalize() throws Throwable
	{
		super.finalize();
		mobStat.entity.discard();
	}
	// --------------------------------------------------
	@Override
	protected void onUpdateTooltip() { tooltip = onUpdateTooltip(mobStat); }
	
	public static Text onUpdateTooltip(SUMobStat mobStat)
	{
		String s0 = tt("stat_type.minecraft.killed.none", mobStat.entityName).getString();
		String s1 = tt("stat_type.minecraft.killed_by.none", mobStat.entityName).getString();
		
		if(mobStat.killed != 0)
			s0 = tt("stat_type.minecraft.killed", Integer.toString(mobStat.killed), mobStat.entityName).getString();
		if(mobStat.killedBy != 0)
			s1 = tt("stat_type.minecraft.killed_by", mobStat.entityName, Integer.toString(mobStat.killedBy)).getString();
		
		return lt(s0 + "\n" + s1);
	}
	// ==================================================
	@Override
	public void onRenderStat(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		//scissor y and height
		int bottom = y + height;
		int scBottom = parent.statContentPane.y + parent.statContentPane.getHeight();
		
		int scissorY = this.y;
		int scissorHeight = this.height;
		if(this.y < parent.statContentPane.y)
		{
			int i0 = Math.abs(this.y - parent.statContentPane.y);
			scissorY += i0;
			scissorHeight -= i0;
		}
		if(bottom > scBottom)
			scissorHeight -= Math.abs(bottom - scBottom);
		
		//render living_entity/entity_name
		if(livingEntity != null && !livingEntity.isDead())
			GuiUtils.applyScissor(x + 1, scissorY + 1, width - 2, scissorHeight - 2, () ->
			{
				int centerX = this.x + (this.width / 2);
				int centerY = this.y + (this.height / 2);
				
				InventoryScreen.drawEntity(
						this.x + cache_mobOffset.x,
						this.y + cache_mobOffset.y,
						cache_mobSize,
						-rInt(mouseX, centerX), -rInt(mouseY, centerY),
						livingEntity);
			});
		else
			GuiUtils.applyScissor(x, scissorY, width, scissorHeight, () ->
			GuiUtils.drawCenteredTextLines(
					matrices,
					parent.getTextRenderer(),
					this.x + (this.width / 2),
					this.y + (this.height / 2),
					mobNameSplit,
					BSConfig.COLOR_STAT_GENERAL_TEXT));
	}
	
	private static int rInt(int input, int relativeTo) { return input - relativeTo; }
	// ==================================================
}