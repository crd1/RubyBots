package de.crd.rubybots;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import de.crd.rubybots.battle.Battle;
import de.crd.rubybots.battle.BattleStats;
import de.crd.rubybots.bots.BotClasspathConfig;
import de.crd.rubybots.bots.BotConfig;
import de.crd.rubybots.bots.BotFileConfig;
import de.crd.rubybots.engine.Engine;

public class App {

	private static final List<BotConfig> DEFAULT_BOTS = new ArrayList<>();
	private static final int DEFAULT_ROUNDS = 5;

	static {
		DEFAULT_BOTS.add(new BotClasspathConfig("bot.rb"));
		DEFAULT_BOTS.add(new BotClasspathConfig("bot.rb"));
	}

	public static void main(String[] args) {
		System.out.println("*************************\nRubyBots v0.1\nCreated by crd\n*************************\n\n");
		setExceptionHandler();
		List<BotConfig> botConfig = getBots(args);
		Battle battle = getBattle(botConfig.size());
		if (!startBattle(battle, botConfig)) {
			System.exit(-1);
		}
		System.exit(0);
	}

	public static boolean startBattle(Battle battle, List<BotConfig> botConfigs) {
		if (!init(botConfigs)) {
			return false;
		}
		battle.execute();
		Engine.shutdown();
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
		}
		printFinalStats(battle);
		return true;
	}

	private static void printFinalStats(Battle battle) {
		BattleStats finalStats = battle.getCurrentBattleStats();
		System.out.println("\n\n***************************");
		System.out.println("Time passed: " + finalStats.getTimestamp() + " ms.");
		System.out.println("Number of bots: " + finalStats.getNumberOfBots());
		System.out.println("Winner: " + (finalStats.getWinner() != null ? "Bot " + finalStats.getWinner() : "Nobody"));
		System.out.println("Rounds: " + finalStats.getRounds());
		System.out.println("History: " + finalStats.getHistory());
		System.out.println("Summed up history: " + finalStats.getSummedUpHistory());
		System.out.println("Final Battlefield: " + finalStats.getBattlefield());
		System.out.println("***************************");
	}

	private static void setExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				System.exit(-1);
			}
		});
	}

	private static Battle getBattle(int numberOfBots) {
		// TODO
		return new Battle(numberOfBots, DEFAULT_ROUNDS);
	}

	private static List<BotConfig> getBots(String[] args) {
		if (args == null || args.length == 0) {
			System.out.println("Using default bots.");
			return DEFAULT_BOTS;
		}
		// TODO
		return DEFAULT_BOTS;
	}

	private static boolean init(List<BotConfig> bots) {
		try {
			Engine.prepareEngine();
			Engine.loadBotsFromClasspath(getConfigsOfType(bots, BotClasspathConfig.class));
			Engine.loadBotsFromFiles(getConfigsOfType(bots, BotFileConfig.class));
		} catch (ScriptException e) {
			System.out.println("RubyBots could not be initialized.");
			return false;
		}
		return true;
	}

	private static <T extends BotConfig> List<T> getConfigsOfType(List<BotConfig> botConfigs, Class<T> configType) {
		return botConfigs.stream().filter(botConfig -> configType.isAssignableFrom(botConfig.getClass()))
				.map(botConfig -> configType.cast(botConfig)).collect(Collectors.toList());
	}

}
