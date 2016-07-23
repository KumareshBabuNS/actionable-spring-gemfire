/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app.service;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import example.app.model.Contact;

/**
 * The ContactsService class is a Spring {@link Service @Service} class used to manage {@link Contact} information
 * for people.
 *
 * @author John Blum
 * @see org.springframework.stereotype.Service
 * @see example.app.model.Address
 * @see example.app.model.Contact
 * @see example.app.model.Person
 * @see example.app.model.PhoneNumber
 * @since 1.0.0
 */
@Service("contactsService")
@SuppressWarnings("unused")
public class ContactsService {

	protected static final Pattern EMAIL_PATTERN =
		Pattern.compile("[a-zA-Z0-9_]+@[a-zA-Z0-9_]+(\\.com|\\.net|\\.org|\\.edu)");

	@Autowired
	private example.app.repo.gemfire.ContactRepository gemfireContactRepository;

	@Autowired
	private example.app.repo.jpa.ContactRepository jpaContactRepository;

	@Transactional
	public Contact save(Contact contact) {
		Assert.notNull(contact, "Contact cannot be null");

		return gemfireContactRepository.save(jpaContactRepository.save(
			validatePhoneNumber(validateEmail(validateAddress(contact)))));
	}

	@Transactional
	public void remove(Contact contact) {
		jpaContactRepository.delete(contact);
		gemfireContactRepository.delete(contact);
	}

	protected Contact validateAddress(Contact contact) {
		return contact;
	}

	protected Contact validateEmail(Contact contact) {
		if (contact.hasEmail() && !EMAIL_PATTERN.matcher(contact.getEmail()).find()) {
			throw new IllegalArgumentException(String.format("Email [%1$s] in Contact for Person [%2$s] is not valid",
				contact.getEmail(), contact.getPerson()));
		}

		return contact;
	}

	protected Contact validatePhoneNumber(Contact contact) {
		if (contact.hasPhoneNumber() && "555".equals(contact.getPhoneNumber().getPrefix())) {
			throw new IllegalArgumentException(String.format(
				"Phone Number [%1$s] in Contact for Person [%2$s] is not valid; '555' is not valid phone number exchange",
					contact.getPhoneNumber(), contact.getPerson()));
		}

		return contact;
	}
}
