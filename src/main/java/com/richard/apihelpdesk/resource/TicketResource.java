package com.richard.apihelpdesk.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.richard.apihelpdesk.entity.ChangeStatus;
import com.richard.apihelpdesk.entity.Ticket;
import com.richard.apihelpdesk.entity.User;
import com.richard.apihelpdesk.entity.enums.StatusEnum;
import com.richard.apihelpdesk.resource.dto.Summary;
import com.richard.apihelpdesk.resource.response.Response;
import com.richard.apihelpdesk.security.jwt.JwtTokenUtil;
import com.richard.apihelpdesk.service.TicketService;
import com.richard.apihelpdesk.service.UserService;

@RestController
@RequestMapping("/api/ticket")
@CrossOrigin(origins = "*")
public class TicketResource {
	
	@Autowired
	private TicketService ticketService;
	
	@Autowired
	protected JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private UserService userService;
	
	@PostMapping
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<Ticket>> create(HttpServletRequest request, @RequestBody Ticket ticket, BindingResult result) {
		Response<Ticket> response = new Response<>();
		
		try {
			validateCreateTicket(ticket, result);
			
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			ticket.setStatus(StatusEnum.getStatus("New"));
			ticket.setUser(userFromRequest(request));
			ticket.setDate(new Date());
			ticket.setNumber(generateNumber());
			
			Ticket ticketPersisted = ticketService.createOrUpdate(ticket);
			response.setData(ticketPersisted);
			
		} catch (Exception e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@PutMapping
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<Ticket>> update(HttpServletRequest request, @RequestBody Ticket ticket, BindingResult result) {
		Response<Ticket> response = new Response<>();
		
		try {
			validateUpdateTicket(ticket, result);
			
			Optional<Ticket> ticketCurrentOptional = ticketService.findById(ticket.getId());
			
			if (!ticketCurrentOptional.isPresent()) {
				result.addError(new ObjectError("Ticket", "Register not found id: " + ticket.getId()));
			}
			
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			Ticket ticketCurrent = ticketCurrentOptional.get();
			ticket.setStatus(ticketCurrent.getStatus());
			ticket.setUser(ticketCurrent.getUser());
			ticket.setDate(ticketCurrent.getDate());
			ticket.setNumber(ticketCurrent.getNumber());
			
			if (null != ticketCurrent.getAssigneUser()) {
				ticket.setAssigneUser(ticketCurrent.getAssigneUser());
			}
			
			Ticket ticketPersisted = ticketService.createOrUpdate(ticket);
			response.setData(ticketPersisted);
			
		} catch (Exception e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Ticket>> findById(@PathVariable("id") String id) {
		Response<Ticket> response = new Response<>();
		Optional<Ticket> ticketOptional = ticketService.findById(id);
		
		if (!ticketOptional.isPresent()) {
			response.getErrors().add("Register not found id: " + id);
			return ResponseEntity.badRequest().body(response);
		}
		
		Ticket ticketPesisted = ticketOptional.get();
		
		List<ChangeStatus> changes = new ArrayList<>();
		Iterable<ChangeStatus> changesCurrent = ticketService.listChangeStatus(ticketPesisted.getId());
		
		for (Iterator<ChangeStatus> iterator = changesCurrent.iterator(); iterator.hasNext();) {
			ChangeStatus changeStatus = (ChangeStatus) iterator.next();
			changeStatus.setTicket(null);
			changes.add(changeStatus);
		}
		
		ticketPesisted.setChanges(changes);
		response.setData(ticketPesisted);
		
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping(value = "/{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<String>> deleteById(@PathVariable("id") String id) {
		Response<String> response = new Response<>();
		Optional<Ticket> ticketOptional = ticketService.findById(id);
		
		if (!ticketOptional.isPresent()) {
			response.getErrors().add("Register not found id: " + id);
			return ResponseEntity.badRequest().body(response);
		}
		
		ticketService.delete(id);
		return ResponseEntity.ok(new Response<String>());
	
	}
	
	@GetMapping(value = "/{page}/{count}")
	@PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
	public ResponseEntity<Response<Page<Ticket>>> findAll(HttpServletRequest request, @PathVariable("page") int page, @PathVariable("count") int count) {
		Response<Page<Ticket>> response = new Response<>();
		Page<Ticket> tickets = null;
		
		User userRequest = userFromRequest(request);
		
		if (userRequest.getProfile().isTechnician()) {
			tickets = ticketService.listTicket(page, count);
		} else if (userRequest.getProfile().isCustomer()) {
			tickets = ticketService.findByCurrentUser(page, count, userRequest.getId());
		}
		
		response.setData(tickets);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/{page}/{count}/{number}/{title}/{status}/{priority}/{assigned}")
	@PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
	public ResponseEntity<Response<Page<Ticket>>> findByParams(HttpServletRequest request, @PathVariable("page") int page, 
															   @PathVariable("count") int count, @PathVariable("number") Integer number, 
															   @PathVariable("title") String title, @PathVariable("status") String status, 
															   @PathVariable("priority") String priority, @PathVariable("assigned") boolean assigned ) {
		title = title.equals("uninformed") ? "" : title;
		status = status.equals("uninformed") ? "" : status;
		priority = priority.equals("uninformed") ? "" : priority;
		
		Response<Page<Ticket>> response = new Response<>();
		Page<Ticket> tickets = null;
		
		if (number > 0) {
			tickets = ticketService.findByNumber(page, count, number);
		} else {
			User userRequest = userFromRequest(request);
			if (userRequest.getProfile().isTechnician()) {
				
				if (assigned) {
					tickets = ticketService.findByParameterAndAssignedUser(page, count, title, status, priority, userRequest.getId());
				} else {
					tickets = ticketService.findByParameters(page, count, title, status, priority);
				}
				
			} else if (userRequest.getProfile().isCustomer()) {
				tickets = ticketService.findByParametersAndCurrentUser(page, count, title, status, priority, userRequest.getId());
			}
		}
		
		response.setData(tickets);
		return ResponseEntity.ok(response);
	}
	
	@PutMapping(value = "{id}/{status}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Ticket>> changeStatus(HttpServletRequest request, 
														 @PathVariable("id") String id, 
														 @PathVariable("status") String status, 
														 @RequestBody Ticket ticket,
														 BindingResult result) {
		Response<Ticket> response = new Response<>();
		
		try {
			validateChangeStatus(id, status, result);
			Optional<Ticket> ticketOptional = ticketService.findById(id);
			
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			if (!ticketOptional.isPresent()) {
				response.getErrors().add("Register not found id: " + id);
				return ResponseEntity.badRequest().body(response);
			}
			
			Ticket ticketCurrent = ticketOptional.get();
			ticketCurrent.setStatus(StatusEnum.getStatus(status));
			
			User userRequest = userFromRequest(request);
			
			if (ticketCurrent.getStatus().isAssigned()) {
				ticketCurrent.setAssigneUser(userRequest);
			}
			
			Ticket ticketPersisted = ticketService.createOrUpdate(ticketCurrent);
			
			ChangeStatus changeStatus = new ChangeStatus();
			changeStatus.setUserChange(userRequest);
			changeStatus.setDateChange(new Date());
			changeStatus.setStatus(StatusEnum.getStatus(status));
			changeStatus.setTicket(ticketPersisted);
			
			ticketService.createChangeStatus(changeStatus);
			response.setData(ticketPersisted);
			
		} catch (Exception e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/summary")
	@PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
	public ResponseEntity<Response<Summary>> findSummary() {
		Response<Summary> response = new Response<>();
		Summary summary = new Summary();
		
		int amountNew = 0;
		int amountResolved = 0;
		int amountApproved = 0;
		int amountDisapproved = 0;
		int amountAssigned = 0;
		int amountClosed = 0;
		
		Iterable<Ticket> tickets = ticketService.findAll();
		
		if (null != tickets) {
			for (Iterator<Ticket> iterator = tickets.iterator(); iterator.hasNext();) {
				Ticket ticket = (Ticket) iterator.next();
				
				switch (ticket.getStatus()) {
				case New:
					amountNew++;
					break;
				case Approved:
					amountApproved++;
					break;
				case Assigned:
					amountAssigned++;	
					break;
				case Closed:
					amountClosed++;
					break;
				case Disapproved:
					amountDisapproved++;
					break;
				case Resolved:
					amountResolved++;
					break;

				}
			}
		}
		
		summary.setAmountNew(amountNew);
		summary.setAmountApproved(amountApproved);
		summary.setAmountAssigned(amountAssigned);
		summary.setAmountClosed(amountClosed);
		summary.setAmountDisapproved(amountDisapproved);
		summary.setAmountResolved(amountResolved);
		
		response.setData(summary);
		return ResponseEntity.ok(response);
	}

	private void validateCreateTicket(Ticket ticket, BindingResult result) {
		
		if (StringUtils.isBlank(ticket.getTitle())) {
			result.addError(new ObjectError("Ticket", "Title no information"));
		}
		
	}
	
	private User userFromRequest(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		String email = jwtTokenUtil.getUserNameFromToken(token);
		return userService.findByEmail(email).get();
	}
	
	private Integer generateNumber() {
		Random random = new Random();
		return random.nextInt(9999);
	}
	
	private void validateUpdateTicket(Ticket ticket, BindingResult result) {
		
		if (StringUtils.isBlank(ticket.getId())) {
			result.addError(new ObjectError("Ticket", "Id no information"));
		}
		
		if (StringUtils.isBlank(ticket.getTitle())) {
			result.addError(new ObjectError("Ticket", "Title no information"));
		}
		
	}
	
	private void validateChangeStatus(String id, String status, BindingResult result) {
		
		if (StringUtils.isBlank(id)) {
			result.addError(new ObjectError("Ticket", "Id no information"));
		}
		
		if (StringUtils.isBlank(status)) {
			result.addError(new ObjectError("Ticket", "Status no information"));
		}
		
	}

}
