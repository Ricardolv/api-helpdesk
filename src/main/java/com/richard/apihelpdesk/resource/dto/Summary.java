package com.richard.apihelpdesk.resource.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class Summary implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer amountNew;
	private Integer amountResolved;
	private Integer amountApproved;
	private Integer amountDisapproved;
	private Integer amountAssigned;
	private Integer amountClosed;
	

}
