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
package l2r.gameserver.scripts.transformations;

import l2r.gameserver.datatables.SkillTable;
import l2r.gameserver.instancemanager.TransformationManager;
import l2r.gameserver.model.L2Transformation;

public class DoomWraith extends L2Transformation
{
	private static final int[] SKILLS =
	{
		586,
		587,
		588,
		589,
		5491,
		619
	};
	
	public DoomWraith()
	{
		// id, colRadius, colHeight
		super(2, 13, 25);
	}
	
	@Override
	public void onTransform()
	{
		if ((getPlayer().getTransformationId() != 2) || getPlayer().isCursedWeaponEquipped())
		{
			return;
		}
		
		transformedSkills();
	}
	
	@Override
	public void onUntransform()
	{
		removeSkills();
	}
	
	public void removeSkills()
	{
		// Rolling Attack (up to 2 levels)
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(586, 2), false);
		// Earth Storm (up to 2 levels)
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(587, 2), false);
		// Curse of Darkness (up to 2 levels)
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(588, 2), false);
		// Darkness Energy Drain (up to 2 levels)
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(589, 2), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public void transformedSkills()
	{
		// Rolling Attack (up to 2 levels)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(586, 2), false);
		// Earth Storm (up to 2 levels)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(587, 2), false);
		// Curse of Darkness (up to 2 levels)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(588, 2), false);
		// Darkness Energy Drain (up to 2 levels)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(589, 2), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(SKILLS);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DoomWraith());
	}
}
