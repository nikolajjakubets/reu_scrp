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
package l2r.gameserver.scripts.ai.group_template;

import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ATTACK;

import java.util.List;

import l2r.Config;
import l2r.gameserver.datatables.NpcTable;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2RiftInvaderInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.util.Util;

/**
 * Overarching Superclass for all mob AI.
 * @author Fulminus
 */
public final class L2AttackableAIScript extends Quest
{
	private L2AttackableAIScript(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (caster == null)
		{
			return null;
		}
		if (!(npc instanceof L2Attackable))
		{
			return null;
		}
		
		L2Attackable attackable = (L2Attackable) npc;
		
		int skillAggroPoints = skill.getAggroPoints();
		
		if (caster.hasSummon())
		{
			if ((targets.length == 1) && Util.contains(targets, caster.getSummon()))
			{
				skillAggroPoints = 0;
			}
		}
		
		if (skillAggroPoints > 0)
		{
			if (attackable.hasAI() && (attackable.getAI().getIntention() == AI_INTENTION_ATTACK))
			{
				L2Object npcTarget = attackable.getTarget();
				for (L2Object skillTarget : targets)
				{
					if ((npcTarget == skillTarget) || (npc == skillTarget))
					{
						L2Character originalCaster = isSummon ? caster.getSummon() : caster;
						attackable.addDamageHate(originalCaster, 0, (skillAggroPoints * 150) / (attackable.getLevel() + 7));
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isSummon)
	{
		if (attacker == null)
		{
			return null;
		}
		
		L2Character originalAttackTarget = (isSummon ? attacker.getSummon() : attacker);
		if (attacker.isInParty() && attacker.getParty().isInDimensionalRift())
		{
			byte riftType = attacker.getParty().getDimensionalRift().getType();
			byte riftRoom = attacker.getParty().getDimensionalRift().getCurrentRoom();
			
			if ((caller instanceof L2RiftInvaderInstance) && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
			{
				return null;
			}
		}
		
		// By default, when a faction member calls for help, attack the caller's attacker.
		// Notify the AI with EVT_AGGRESSION
		try
		{
			npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);
		}
		catch (Exception e)
		{
			_log.warn("Logger: notifyEvent failed (L2AttackableAIScript 1) Report this to team. ");
		}
		
		return null;
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (player == null)
		{
			return null;
		}
		
		L2Character target = isSummon ? player.getSummon() : player;
		
		((L2Attackable) npc).addDamageHate(target, 0, 1);
		
		// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
		if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if ((attacker != null) && (npc instanceof L2Attackable))
		{
			L2Attackable attackable = (L2Attackable) npc;
			
			L2Character originalAttacker = isSummon ? attacker.getSummon() : attacker;
			try
			{
				attackable.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, originalAttacker);
			}
			catch (Exception e)
			{
				_log.warn("Logger: notifyEvent failed (L2AttackableAIScript 2) Report this to team. ");
			}
			attackable.addDamageHate(originalAttacker, damage, (damage * 100) / (attackable.getLevel() + 7));
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc instanceof L2MonsterInstance)
		{
			final L2MonsterInstance mob = (L2MonsterInstance) npc;
			if ((mob.getLeader() != null) && mob.getLeader().hasMinions())
			{
				final int respawnTime = Config.MINIONS_RESPAWN_TIME.containsKey(npc.getId()) ? Config.MINIONS_RESPAWN_TIME.get(mob.getId()) * 1000 : -1;
				mob.getLeader().getMinionList().onMinionDie(mob, respawnTime);
			}
			
			if (mob.hasMinions())
			{
				mob.getMinionList().onMasterDie(false);
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		L2AttackableAIScript ai = new L2AttackableAIScript(-1, L2AttackableAIScript.class.getSimpleName(), "ai");
		// register all mobs here...
		for (int level = 1; level < 100; level++)
		{
			final List<L2NpcTemplate> templates = NpcTable.getInstance().getAllOfLevel(level);
			for (L2NpcTemplate t : templates)
			{
				try
				{
					if (L2Attackable.class.isAssignableFrom(Class.forName("l2r.gameserver.model.actor.instance." + t.getType() + "Instance")))
					{
						ai.addEventId(Quest.QuestEventType.ON_ATTACK, t.getId());
						ai.addEventId(Quest.QuestEventType.ON_KILL, t.getId());
						ai.addEventId(Quest.QuestEventType.ON_SPAWN, t.getId());
						ai.addEventId(Quest.QuestEventType.ON_SKILL_SEE, t.getId());
						ai.addEventId(Quest.QuestEventType.ON_FACTION_CALL, t.getId());
						ai.addEventId(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER, t.getId());
					}
				}
				catch (ClassNotFoundException ex)
				{
					_log.info("Class not found " + t.getType() + "Instance");
				}
			}
		}
	}
}