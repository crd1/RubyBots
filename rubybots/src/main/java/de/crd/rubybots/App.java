package de.crd.rubybots;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import de.crd.rubybots.battle.Battle;
import de.crd.rubybots.bots.BotClasspathConfig;
import de.crd.rubybots.bots.BotConfig;
import de.crd.rubybots.bots.BotFileConfig;
import de.crd.rubybots.engine.Engine;

public class App {

	private static final List<BotConfig> DEFAULT_BOTS = new ArrayList<>();
	private static final int DEFAULT_ROUNDS = 3;

	static {
		DEFAULT_BOTS.add(new BotClasspathConfig("bot.rb"));
		DEFAULT_BOTS.add(new BotClasspathConfig("bot.rb"));
	}

	public static void main(String[] args) {
		System.out.println("RubyBots\n\n");
		setExceptionHandler();
		if (!init(getBots(args))) {
			System.exit(-1);
		}
		Battle battle = getBattle();
		battle.execute();
		Engine.shutdown();
		System.exit(0);
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

	private static Battle getBattle() {
		// TODO
		return new Battle(DEFAULT_ROUNDS);
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
