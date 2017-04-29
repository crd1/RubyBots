package de.crd.rubybots.engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import de.crd.rubybots.App;
import de.crd.rubybots.battle.BattleStats;
import de.crd.rubybots.battle.Context;
import de.crd.rubybots.battle.MoveResult;
import de.crd.rubybots.bots.BotClasspathConfig;
import de.crd.rubybots.bots.BotFileConfig;

public class Engine {

	private static volatile ScriptEngine jruby;
	private static final List<String> bots = new ArrayList<>();
	private static final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
	private static volatile boolean preparingEngine;
	private static final BlockingQueue<BattleStats> battleStatsUpdateQueue = new LinkedBlockingQueue<>();

	public static void prepareEngine() throws ScriptException {
		System.out.print("Preparing engine.");
		preparingEngine = true;
		backgroundExecutor.execute(new ProgressTask());
		jruby = new ScriptEngineManager().getEngineByName("jruby");
		if (jruby == null) {
			throw new IllegalStateException("JRuby not found.");
		}
		jruby.eval("puts \".\"");
		preparingEngine = false;
		System.out.println("Done preparing engine.");
	}

	public static void loadBotsFromClasspath(List<BotClasspathConfig> botConfigs) {
		System.out.println("Loading bots from classpath.");
		int i = getNumberOfBots();
		for (BotClasspathConfig botConfig : botConfigs) {
			System.out.println("Loading bot nr " + i + " from " + botConfig);
			try (InputStreamReader isr = new InputStreamReader(
					App.class.getResourceAsStream("/" + botConfig.getClasspathReference()));
					BufferedReader br = new BufferedReader(isr);) {
				String bot = br.lines().collect(Collectors.joining(System.getProperty("line.separator")));
				System.out.println("Found bot:\n" + bot);
				bots.add(bot);
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}
		System.out.println("Done loading bots from classpath.");
	}

	public static int getNumberOfBots() {
		return bots.size();
	}

	public static void callBot(Context context) {
		System.out.println("Calling bot " + context.getBotNumber());
		try {
			Bindings bindings = new SimpleBindings();
			bindings.put("context", context);
			jruby.eval(bots.get(context.getBotNumber()), bindings);
		} catch (ScriptException e) {
			throw new IllegalStateException("Scripting failed", e);
		}
		System.out.println("Bot call " + context + " returned.");
	}

	private static void displayBattleStatsUpdate(BattleStats battleStatsUpdate) {
		// TODO
		System.out.println("New BattleStats: " + battleStatsUpdate);
	}

	private static class ProgressTask implements Runnable {

		@Override
		public void run() {
			while (preparingEngine) {
				System.out.print(".");
				try {
					Thread.sleep(500L);
				} catch (InterruptedException e) {
				}
			}
			// engine prepared, start showing battleStats
			while (!Thread.currentThread().isInterrupted()) {
				try {
					BattleStats battleStatsUpdate = battleStatsUpdateQueue.take();
					displayBattleStatsUpdate(battleStatsUpdate);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public static void shutdown() {
		backgroundExecutor.shutdownNow();
	}

	public static void loadBotsFromFiles(List<BotFileConfig> botConfigs) {
		System.out.println("Loading bots from files.");
		int i = getNumberOfBots();
		for (BotFileConfig botConfig : botConfigs) {
			System.out.println("Loading bot nr " + i + " from " + botConfig);
			try (FileInputStream fis = new FileInputStream(botConfig.getBotFile());
					InputStreamReader isr = new InputStreamReader(fis);
					BufferedReader br = new BufferedReader(isr);) {
				String bot = br.lines().collect(Collectors.joining(System.getProperty("line.separator")));
				System.out.println("Found bot:\n" + bot);
				bots.add(bot);
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}
		System.out.println("Done loading bots from files.");
	}

	public static Queue<BattleStats> getBattleStatsUpdateQueue() {
		return battleStatsUpdateQueue;
	}
}
