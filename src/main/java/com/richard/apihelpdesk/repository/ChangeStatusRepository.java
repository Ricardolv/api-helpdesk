package com.richard.apihelpdesk.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.richard.apihelpdesk.entity.ChangeStatus;

public interface ChangeStatusRepository extends MongoRepository<ChangeStatus, String> {
	
	Iterable<ChangeStatus> findByTicketIdOrderByDateChangeDesc(String ticketId);

}
