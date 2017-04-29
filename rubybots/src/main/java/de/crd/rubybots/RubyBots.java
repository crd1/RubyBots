package de.crd.rubybots;

import java.io.File;
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

public class RubyBots {

	private static final List<BotConfig> DEFAULT_BOTS = new ArrayList<>();
	private static final int DEFAULT_ROUNDS = 5;

	private final Engine mEngine;

	static {
		DEFAULT_BOTS.add(new BotClasspathConfig("bot.rb"));
		DEFAULT_BOTS.add(new BotClasspathConfig("bot.rb"));
	}

	public static void main(String[] args) {
		System.out.println("*************************\nRubyBots v0.1\nCreated by crd\n*************************\n\n");
		setExceptionHandler();
		List<BotConfig> botConfig = getBotsFromArgs(args);
		Battle battle = getDefaultBattle(botConfig.size());
		RubyBots rubyBots = new RubyBots(getBattleStatsUpdateListener());
		if (!rubyBots.startBattle(battle, botConfig)) {
			System.exit(-1);
		}
		rubyBots.shutdown();
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
		}
		printFinalStats(battle);
		System.exit(0);
	}

	private static BattleStatsUpdateListener getBattleStatsUpdateListener() {
		return new BattleStatsUpdateListener() {

			@Override
			public void onBattleStatsUpdate(BattleStats battleStats) {
				displayBattleStatsUpdate(battleStats);
			}
		};
	}

	private static void displayBattleStatsUpdate(BattleStats battleStatsUpdate) {
		System.out.println("New BattleStats: " + battleStatsUpdate);
	}

	public RubyBots(BattleStatsUpdateListener listener) {
		this.mEngine = new Engine(listener);
	}

	public void shutdown() {
		mEngine.shutdown();
	}

	/**
	 * API ENTRY POINT
	 */
	public boolean startBattle(Battle battle, List<BotConfig> botConfigs) {
		if (!init(botConfigs)) {
			return false;
		}
		battle.execute(mEngine);
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

	private static Battle getDefaultBattle(int numberOfBots) {
		return new Battle(numberOfBots, DEFAULT_ROUNDS);
	}

	private static List<BotConfig> getBotsFromArgs(String[] args) {
		if (args == null || args.length == 0) {
			System.out.println("Using default bots.");
			return DEFAULT_BOTS;
		}
		return getBotFileConfigs(args);
	}

	private static List<BotConfig> getBotFileConfigs(String[] args) {
		List<BotConfig> botConfigs = new ArrayList<>();
		for (String arg : args) {
			File argFile = new File(arg);
			if (!argFile.exists()) {
				throw new IllegalArgumentException("File not found.");
			}
			botConfigs.add(new BotFileConfig(argFile));
		}
		return botConfigs;
	}

	private boolean init(List<BotConfig> bots) {
		try {
			mEngine.prepareEngine();
			mEngine.loadBotsFromClasspath(getConfigsOfType(bots, BotClasspathConfig.class));
			mEngine.loadBotsFromFiles(getConfigsOfType(bots, BotFileConfig.class));
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

	public interface BattleStatsUpdateListener {
		void onBattleStatsUpdate(BattleStats battleStats);
	}

}
