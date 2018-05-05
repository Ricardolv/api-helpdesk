package com.richard.apihelpdesk.entity.enums;

public enum ProfileEnum {
	
	ROLE_ADMIN,
	ROLE_CUSTOMER,
	ROLE_TECHNICIAN;

	public boolean isTechnician() {
		return this.equals(ROLE_TECHNICIAN);
	}

	public boolean isCustomer() {
		return this.equals(ROLE_CUSTOMER);
	}

}
