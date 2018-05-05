package com.richard.apihelpdesk.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.richard.apihelpdesk.entity.Ticket;

public interface TicketRepository extends MongoRepository<Ticket, String> {
	
	Page<Ticket> findByUserIdOrderByDateDesc(Pageable pages, String userId);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusAndPriorityOrderByDateDesc(
			String title, String status, String priority, Pageable pages);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusAndPriorityAndUserIdOrderByDateDesc(
			String title, String status, String priority, String userId, Pageable pages);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusAndPriorityIgnoreCaseContainingAndAssigneUserOrderByDateDesc(
			String title, String status, String priority, String assigneUser, Pageable pages);
	
	Page<Ticket> findByNumber(Integer number, Pageable pages);

}