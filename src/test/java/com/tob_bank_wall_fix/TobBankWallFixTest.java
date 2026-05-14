package com.tob_bank_wall_fix;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TobBankWallFixTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TobBankWallFix.class);
		RuneLite.main(args);
	}
}