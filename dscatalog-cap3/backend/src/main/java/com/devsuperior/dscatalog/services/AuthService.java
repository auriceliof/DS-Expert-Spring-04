package com.devsuperior.dscatalog.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.devsuperior.dscatalog.dto.EmailDTO;
import com.devsuperior.dscatalog.entities.PasswordRecover;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.PasswordRecoverRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
	
	@Value("${email.password-recover.uri}")
	private String recoverUri;

	@Value("${email.password-recover.token.minutes}")
	private Long tokenMinutes;
	
	@Autowired
	private PasswordRecoverRepository passwordRecoverRepository;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Transactional
	public void createRecoverToken(EmailDTO body) {
		
		User user = userRepository.findByEmail(body.getEmail());
		if (user == null) {
			throw new ResourceNotFoundException("Email não encontrado");
		}
		
		String token = UUID.randomUUID().toString();

		PasswordRecover entity = new PasswordRecover();
		entity.setToken(token);
		entity.setExpiration(Instant.now().plusSeconds(tokenMinutes * 60L));
		entity.setEmail(body.getEmail());
		passwordRecoverRepository.save(entity);

		String text = "Acesse o link para definir uma nova senha\n\n"
				+ recoverUri + token + ". Validade de " + tokenMinutes + " minutos";

		emailService.sendEmail(body.getEmail(), "Recuperação de senha", text);
	}
	
	protected User authenticated() {
		  try {
		    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		    Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
		    String username = jwtPrincipal.getClaim("username");
		    return userRepository.findByEmail(username);
		  }
		  catch (Exception e) {
		    throw new UsernameNotFoundException("Invalid user");
		  }
		}
}













