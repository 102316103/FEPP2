package com.syscom.fep.batch.job;

import org.quartz.Trigger;

public class BatchJobEntry {
	private BatchJobParameter parameter;
	private BatchJob job;
	private Trigger trigger;
	private String scheduleInfo;

	public BatchJobEntry(BatchJobParameter parameter, BatchJob job, Trigger trigger, String scheduleInfo) {
		this.job = job;
		this.trigger = trigger;
		this.scheduleInfo = scheduleInfo;
	}

	public BatchJobParameter getParameter() {
		return parameter;
	}

	public void setParameter(BatchJobParameter parameter) {
		this.parameter = parameter;
	}

	public BatchJob getJob() {
		return job;
	}

	public void setJob(BatchJob job) {
		this.job = job;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public String getScheduleInfo() {
		return scheduleInfo;
	}

	public void setScheduleInfo(String scheduleInfo) {
		this.scheduleInfo = scheduleInfo;
	}
}
