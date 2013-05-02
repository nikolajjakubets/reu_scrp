/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripts.ai.individual;

import l2r.gameserver.instancemanager.HellboundManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

/**
 * Manages Naia's cast on the Hellbound Core
 * @author GKR
 */
public class HellboundCore extends AbstractNpcAI
{
	private static final int NAIA = 18484;
	private static final int HELLBOUND_CORE = 32331;
	
	private static SkillHolder BEAM = new SkillHolder(5493, 1);
	
	private HellboundCore(String name, String descr)
	{
		super(name, descr);
		addSpawnId(HELLBOUND_CORE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("cast") && (HellboundManager.getInstance().getLevel() <= 6))
		{
			for (L2Character naia : npc.getKnownList().getKnownCharactersInRadius(900))
			{
				if ((naia != null) && naia.isMonster() && (((L2MonsterInstance) naia).getNpcId() == NAIA) && !naia.isDead())
				{
					naia.setTarget(npc);
					naia.doSimultaneousCast(BEAM.getSkill());
				}
			}
			startQuestTimer("cast", 10000, npc, null);
		}
		return null;
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		startQuestTimer("cast", 10000, npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new HellboundCore(HellboundCore.class.getSimpleName(), "ai");
	}
}
