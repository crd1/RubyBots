package de.crd.rubybots;

import javax.script.ScriptException;

import de.crd.rubybots.battle.Battle;

public class App {
	public static void main(String[] args) {
		System.out.println("RubyBots");
		if (!init("bot.rb", "bot.rb")) {
			System.exit(-1);
		}
		Battle battle = new Battle(3);
		battle.execute();
	}

	private static boolean init(String... bots) {
		try {
			Engine.prepareEngine();
			Engine.loadBotsFromClasspath(bots);
		} catch (ScriptException e) {
			System.out.println("RubyBots could not be initialized.");
			return false;
		}
		return true;
	}

}
