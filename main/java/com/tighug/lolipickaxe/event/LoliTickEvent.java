package com.tighug.lolipickaxe.event;

import com.google.common.collect.Queues;
import com.tighug.lolipickaxe.Lolipickaxe;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Queue;
import java.util.TimerTask;

@Mod.EventBusSubscriber()
public class LoliTickEvent implements Lolipickaxe.LoliEvent {

	private static final Queue<TimerTask> tickStartTasks = Queues.newArrayDeque();
	private static final Queue<TimerTask> tickEndTasks = Queues.newArrayDeque();

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			synchronized (tickStartTasks) {
				while (!tickStartTasks.isEmpty()) {
					tickStartTasks.poll().run();
				}
			}
		}
		else {
			synchronized (tickEndTasks) {
				while (!tickEndTasks.isEmpty()) {
					tickEndTasks.poll().run();
				}
			}
		}
	}

	public static void addTask(TimerTask task, TickEvent.Phase phase) {
		if (phase == TickEvent.Phase.START) {
			synchronized (tickStartTasks) {
				tickStartTasks.add(task);
			}
		}
		else {
			synchronized (tickEndTasks) {
				tickEndTasks.add(task);
			}
		}
	}

	@FunctionalInterface
	public interface TickFun {

		void invok();

	}

	public static class TickStartTask extends TimerTask {

		private int tick;
		private final TickFun fun;

		public TickStartTask(int tick, TickFun fun) {
			this.tick = tick;
			this.fun = fun;
		}

		@Override
		public void run() {
			if (--tick > 0) {
				addTask(new TickEndTask(this), TickEvent.Phase.END);
			} else {
				fun.invok();
			}
		}

	}

	public static class TickEndTask extends TimerTask {

		private final TimerTask nextTask;

		public TickEndTask(TimerTask nextTask) {
			this.nextTask = nextTask;
		}

		@Override
		public void run() {
			addTask(nextTask, TickEvent.Phase.START);
		}

	}

}
