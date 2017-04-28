package de.crd.rubybots;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import de.crd.rubybots.battle.Context;
import de.crd.rubybots.battle.MoveResult;

public class Engine {

	private static ScriptEngine jruby;
	private static final List<String> bots = new ArrayList<>();

	public static void prepareEngine() throws ScriptException {
		System.out.println("Preparing engine.");
		jruby = new ScriptEngineManager().getEngineByName("jruby");
		if (jruby == null) {
			throw new IllegalStateException("JRuby not found.");
		}
		jruby.eval("puts \"Engine ready.\"");
		System.out.println("Done preparing engine.");
	}

	public static void loadBots(String... botfiles) {
		System.out.println("Loading bots.");
		int i = getNumberOfBots();
		for (String botfile : botfiles) {
			System.out.println("Loading bot nr " + i + " from " + botfile);
			try (InputStreamReader isr = new InputStreamReader(App.class.getResourceAsStream("/" + botfile));
					BufferedReader br = new BufferedReader(isr);) {
				String bot = br.lines().collect(Collectors.joining(System.getProperty("line.separator")));
				System.out.println("Found bot:\n" + bot);
				bots.add(bot);
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}
		System.out.println("Done loading bots.");
	}

	public static int getNumberOfBots() {
		return bots.size();
	}

	public static MoveResult callBot(Context context) {
		System.out.println("Calling bot " + context.getBotNumber());
		MoveResult result = new MoveResult();
		try {
			Bindings bindings = new SimpleBindings();
			bindings.put("botNumber", context.getBotNumber());
			bindings.put("round", context.getRound());
			bindings.put("result", result);
			jruby.eval(bots.get(context.getBotNumber()), bindings);
		} catch (ScriptException e) {
			throw new IllegalStateException("Scripting failed", e);
		}
		System.out.println("Bot call " + context + " returned.");
		return result;
	}
}
