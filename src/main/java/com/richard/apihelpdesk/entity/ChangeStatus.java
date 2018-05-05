package com.richard.apihelpdesk.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.richard.apihelpdesk.entity.enums.StatusEnum;

import lombok.Data;

@Data
@Document
public class ChangeStatus {
	
	@Id
	private String id;
	
	@DBRef
	private Ticket ticket;
	
	@DBRef
	private User userChange;

	private Date dateChange;
	
	private StatusEnum status;
}
