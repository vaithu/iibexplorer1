package com.myapp.iib.backend;

import com.myapp.iib.ui.util.UIUtils;
import com.myapp.iib.ui.util.css.lumo.BadgeColor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.time.LocalDate;

public class Payment {

	private final Status status;
	private final String from;
	private final String fromIBAN;
	private final String to;
	private final String toIBAN;
	private final Double amount;
	private final LocalDate date;

	public enum Status {
		PENDING(VaadinIcon.CLOCK, "Pending",
				"Payment created, not yet submitted.",
				BadgeColor.CONTRAST), SUBMITTED(VaadinIcon.QUESTION_CIRCLE,
				"Submitted", "Payment submitted for processing.",
				BadgeColor.NORMAL), CONFIRMED(VaadinIcon.CHECK,
				"Confirmed", "Payment sent successfully.",
				BadgeColor.SUCCESS), FAILED(VaadinIcon.WARNING,
				"Failed", "Payment failed.",
				BadgeColor.ERROR);

		private final VaadinIcon icon;
		private final String name;
		private final String desc;
		private final BadgeColor theme;

		Status(VaadinIcon icon, String name, String desc, BadgeColor theme) {
			this.icon = icon;
			this.name = name;
			this.desc = desc;
			this.theme = theme;
		}

		public Icon getIcon() {
			Icon icon;
			switch (this) {
				case PENDING:
					icon = UIUtils.createSecondaryIcon(this.icon);
					break;
				case SUBMITTED:
					icon = UIUtils.createPrimaryIcon(this.icon);
					break;
				case CONFIRMED:
					icon = UIUtils.createSuccessIcon(this.icon);
					break;
				default:
					icon = UIUtils.createErrorIcon(this.icon);
					break;
			}
			return icon;
		}

		public String getName() {
			return name;
		}

		public String getDesc() {
			return desc;
		}

		public BadgeColor getTheme() {
			return theme;
		}
	}

	public Payment(Status status, String from, String fromIBAN, String to,
	               String toIBAN, Double amount, LocalDate date) {
		this.status = status;
		this.from = from;
		this.fromIBAN = fromIBAN;
		this.to = to;
		this.toIBAN = toIBAN;
		this.amount = amount;
		this.date = date;
	}

	public Status getStatus() {
		return status;
	}

	public String getFrom() {
		return from;
	}

	public String getFromIBAN() {
		return fromIBAN;
	}

	public String getTo() {
		return to;
	}

	public String getToIBAN() {
		return toIBAN;
	}

	public Double getAmount() {
		return amount;
	}

	public LocalDate getDate() {
		return date;
	}
}
