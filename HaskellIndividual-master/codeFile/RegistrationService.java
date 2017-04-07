package edu.tcd.tapserve.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.tcd.tapserve.bean.Administrator;
import edu.tcd.tapserve.bean.Credentials;
import edu.tcd.tapserve.bean.Role;
import edu.tcd.tapserve.bean.ServiceProvider;
import edu.tcd.tapserve.bean.ServiceToProviderMapper;
import edu.tcd.tapserve.bean.User;
import edu.tcd.tapserve.constants.Constants.RoleType;
import edu.tcd.tapserve.repository.AdministratorRepository;
import edu.tcd.tapserve.repository.CredentialsRepository;
import edu.tcd.tapserve.repository.RoleRepository;
import edu.tcd.tapserve.repository.ServiceProviderRepository;
import edu.tcd.tapserve.repository.ServiceRepository;
import edu.tcd.tapserve.repository.ServiceToProviderMapperRepository;
import edu.tcd.tapserve.repository.UserRepository;
import edu.tcd.tapserve.utils.PasswordEncryptionUtil;

@Service
public class RegistrationService {
	private final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

	@Autowired
	private CredentialsRepository credentialsRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private ServiceProviderRepository serviceProviderRepository;

	@Autowired
	private AdministratorRepository administratorRepository;

	@Autowired
	private ServiceRepository serviceRepository;

	@Autowired
	private ServiceToProviderMapperRepository mapperRepository;

	public String addCredentials(Credentials credentials) {
		if (credentials == null) {
			logger.warn("Input credentails object cannot be null.");
			return null;
		}
		if (credentials.getUsername() == null || credentials.getPassword() == null) {
			logger.warn("Username or passward of credentails cannot be null.");
			return null;
		}

		credentials.setId(UUID.randomUUID().toString());
		String actorId = UUID.randomUUID().toString();
		credentials.setActorId(actorId);
		logger.debug("Set Id and actorID to credential object.");

		String encryptedPassword = PasswordEncryptionUtil.encrypt(credentials.getPassword());
		credentials.setPassword(encryptedPassword);

		credentialsRepository.save(credentials);
		return actorId;
	}

	public User addUser(User user) {
		Role role = roleRepository.findByName(RoleType.USER.name());
		user.setRole(role);
		userRepository.save(user);
		return user;
	}

	public ServiceProvider addServiceProvider(ServiceProvider serviceProvider) {
		Role role = roleRepository.findByName(RoleType.SERVICE_PROVIDER.name());
		serviceProvider.setRole(role);
		serviceProviderRepository.save(serviceProvider);

		ServiceToProviderMapper mapper = new ServiceToProviderMapper();
		for (edu.tcd.tapserve.bean.Service service : serviceProvider.getServices()) {
			mapper.setId(UUID.randomUUID().toString());
			mapper.setService(service);
			mapper.setServiceProvider(serviceProvider);
			mapperRepository.save(mapper);
		}

		return serviceProvider;
	}

	public Administrator addAdministrator(Administrator administrator) {
		Role role = roleRepository.findByName(RoleType.ADMIN.name());
		administrator.setRole(role);
		administratorRepository.save(administrator);
		return administrator;
	}

	public List<edu.tcd.tapserve.bean.Service> getServices() {
		List<edu.tcd.tapserve.bean.Service> services = new ArrayList<edu.tcd.tapserve.bean.Service>();
		Iterable<edu.tcd.tapserve.bean.Service> servicesIterable = serviceRepository.findAll();
		for (edu.tcd.tapserve.bean.Service service : servicesIterable)
			services.add(service);
		return services;
	}

}
