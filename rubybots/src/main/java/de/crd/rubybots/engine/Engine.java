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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import de.crd.rubybots.RubyBots;
import de.crd.rubybots.RubyBots.BattleStatsUpdateListener;
import de.crd.rubybots.battle.BattleStats;
import de.crd.rubybots.battle.Context;
import de.crd.rubybots.bots.BotClasspathConfig;
import de.crd.rubybots.bots.BotFileConfig;

public class Engine {
	private static final Logger LOGGER = Logger.getLogger(Engine.class.getSimpleName());
	private volatile ScriptEngine jruby;
	private final List<String> bots = new ArrayList<>();
	private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
	private volatile boolean preparingEngine;
	private final BlockingQueue<BattleStats> battleStatsUpdateQueue = new LinkedBlockingQueue<>();
	private final BattleStatsUpdateListener battleStatsUpdateListener;

	public Engine(BattleStatsUpdateListener listener) {
		this.battleStatsUpdateListener = listener;
	}

	public void prepareEngine() throws ScriptException {
		System.out.print("Preparing engine.");
		preparingEngine = true;
		backgroundExecutor.execute(new ProgressTask());
		jruby = new ScriptEngineManager().getEngineByName("jruby");
		if (jruby == null) {
			throw new IllegalStateException("JRuby not found.");
		}
		jruby.eval("puts \".\""); // init engine
		preparingEngine = false;
		LOGGER.log(Level.FINE, "Done preparing engine.");
	}

	public void loadBotsFromClasspath(List<BotClasspathConfig> botConfigs) {
		LOGGER.log(Level.FINE, "Loading bots from classpath.");
		int i = getNumberOfBots();
		for (BotClasspathConfig botConfig : botConfigs) {
			LOGGER.log(Level.FINE, "Loading bot nr " + i + " from " + botConfig);
			try (InputStreamReader isr = new InputStreamReader(
					RubyBots.class.getResourceAsStream("/" + botConfig.getClasspathReference()));
					BufferedReader br = new BufferedReader(isr);) {
				String bot = br.lines().collect(Collectors.joining(System.getProperty("line.separator")));
				LOGGER.log(Level.FINE, "Found bot:\n" + bot);
				bots.add(bot);
			} catch (Exception e) {
				throw new IllegalStateException("Loading bot from classpath failed.");
			}
			i++;
		}
		LOGGER.log(Level.FINE, "Done loading bots from classpath.");
	}

	public int getNumberOfBots() {
		return bots.size();
	}

	public void callBot(Context context) throws ScriptException {
		LOGGER.log(Level.FINE, "Calling bot " + context.getBotNumber());
		Bindings bindings = new SimpleBindings();
		bindings.put("context", context);
		jruby.eval(bots.get(context.getBotNumber()), bindings);
		LOGGER.log(Level.FINE, "Bot call " + context + " returned.");
	}

	private class ProgressTask implements Runnable {

		@Override
		public void run() {
			while (preparingEngine) {
				System.out.print(".");
				try {
					Thread.sleep(500L);
				} catch (InterruptedException e) {
				}
			}
			System.out.println("\n\n");
			// engine prepared, start showing battleStats
			while (!Thread.currentThread().isInterrupted()) {
				try {
					BattleStats battleStatsUpdate = battleStatsUpdateQueue.take();
					battleStatsUpdateListener.onBattleStatsUpdate(battleStatsUpdate);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public void shutdown() {
		LOGGER.log(Level.FINE, "Shutting down engine.");
		backgroundExecutor.shutdownNow();
	}

	public void loadBotsFromFiles(List<BotFileConfig> botConfigs) {
		LOGGER.log(Level.FINE, "Loading bots from files.");
		int i = getNumberOfBots();
		for (BotFileConfig botConfig : botConfigs) {
			LOGGER.log(Level.FINE, "Loading bot nr " + i + " from " + botConfig);
			try (FileInputStream fis = new FileInputStream(botConfig.getBotFile());
					InputStreamReader isr = new InputStreamReader(fis);
					BufferedReader br = new BufferedReader(isr);) {
				String bot = br.lines().collect(Collectors.joining(System.getProperty("line.separator")));
				LOGGER.log(Level.FINE, "Found bot:\n" + bot);
				bots.add(bot);
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}
		LOGGER.log(Level.FINE, "Done loading bots from files.");
	}

	public Queue<BattleStats> getBattleStatsUpdateQueue() {
		return battleStatsUpdateQueue;
	}
}
