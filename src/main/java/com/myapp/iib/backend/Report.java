package com.myapp.iib.backend;

import java.time.LocalDate;

public class Report {

	private final Long id;
	private final String source;
	private final String name;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final Double balance;

	public Report(Long id, String source, String name, LocalDate startDate,
	              LocalDate endDate, Double balance) {
		this.id = id;
		this.source = source;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.balance = balance;
	}

	public Long getId() {
		return id;
	}

	public String getSource() {
		return source;
	}

	public String getName() {
		return name;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public Double getBalance() {
		return balance;
	}

}
