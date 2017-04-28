package de.crd.rubybots;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class Engine {

	public static void prepareEngine() {
		try (InputStreamReader isr = new InputStreamReader(App.class.getResourceAsStream("/bot.rb"));
				BufferedReader br = new BufferedReader(isr);) {

			ScriptEngine jruby = new ScriptEngineManager().getEngineByName("jruby");
			if (jruby == null) {
				System.out.println("JRuby not found.");
				return;
			}
			// process a ruby file
			try {
				Bindings bindings = new SimpleBindings();
				bindings.put("message", "global variable");
				System.out.println("result: " + jruby.eval(br, bindings));

			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
