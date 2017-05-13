package de.crd.rubybots;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import de.crd.rubybots.battle.Battle;
import de.crd.rubybots.battle.BattleStats;
import de.crd.rubybots.bots.BotClasspathConfig;
import de.crd.rubybots.bots.BotConfig;
import de.crd.rubybots.bots.BotFileConfig;
import de.crd.rubybots.engine.Engine;

public class RubyBots {

	private static final Logger LOGGER = Logger.getLogger(RubyBots.class.getSimpleName());
	private static final List<BotConfig> DEFAULT_BOTS = new ArrayList<>();
	private static final int DEFAULT_ROUNDS = 5;

	private final List<BotConfig> botConfigs;
	private final Engine mEngine;
	private boolean initialized;

	static {
		// honor the simple minds
		DEFAULT_BOTS.add(new BotClasspathConfig("hunter.rb")); // the hunter...
		DEFAULT_BOTS.add(new BotClasspathConfig("hunted.rb")); // ...and the hunted
	}

	public static void main(String[] args) throws IOException {
		System.out.println(
				"\n\n\n*************************\nRubyBots v0.1\nCreated by crd\n*************************\n\n");
		setExceptionHandler();
		List<BotConfig> botConfig = getBotsFromArgs(args);
		RubyBots rubyBots = new RubyBots(getDefaultBattleStatsUpdateListener(), botConfig);
		Battle battle = null;
		if ((battle = rubyBots.startBattle(null)) == null) {
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

	private static BattleStatsUpdateListener getDefaultBattleStatsUpdateListener() {
		return new BattleStatsUpdateListener() {

			@Override
			public void onBattleStatsUpdate(BattleStats battleStats) {
				displayBattleStatsUpdate(battleStats);
			}
		};
	}

	private static void displayBattleStatsUpdate(BattleStats battleStatsUpdate) {
		System.out.print("\rround: " + battleStatsUpdate.getBattlefield().getCurrentRound() + " | "
				+ battleStatsUpdate.getBattlefield().getFieldRepresentation());
	}

	public RubyBots(BattleStatsUpdateListener listener, List<BotConfig> botConfigs) {
		this.mEngine = new Engine(listener);
		this.botConfigs = botConfigs;
	}

	public void shutdown() {
		mEngine.shutdown();
	}

	/**
	 * API ENTRY POINT
	 */
	public Battle startBattle(Integer numberOfRounds) {
		if (!init()) {
			return null;
		}
		Battle battle = new Battle(numberOfRounds, botConfigs);
		battle.execute(mEngine);
		return battle;
	}

	private static void printFinalStats(Battle battle) {
		BattleStats finalStats = battle.getCurrentBattleStats();
		System.out.println(finalStats.getComprehensiveStats());
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
			if (!argFile.isDirectory()) {
				botConfigs.add(new BotFileConfig(argFile));
			} else {
				addFromDirectory(argFile, botConfigs);
			}
		}
		return botConfigs;
	}

	private static void addFromDirectory(File argFile, List<BotConfig> botConfigs) {
		File[] filesInDirectory = argFile.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return !pathname.isDirectory() && pathname.getName().endsWith(".rb");
			}
		});
		for (File file : filesInDirectory) {
			botConfigs.add(new BotFileConfig(file));
		}
	}

	private boolean init() {
		if (initialized) {
			return true;
		}
		try {
			mEngine.prepareEngine();
			mEngine.loadBotsFromClasspath(getConfigsOfType(botConfigs, BotClasspathConfig.class));
			mEngine.loadBotsFromFiles(getConfigsOfType(botConfigs, BotFileConfig.class));
		} catch (ScriptException | IllegalStateException e) {
			LOGGER.log(Level.SEVERE, "RubyBots could not be initialized: " + e.getMessage());
			return false;
		}
		initialized = true;
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
