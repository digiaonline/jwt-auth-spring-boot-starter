package com.starcut.auth.jwt.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.starcut.auth.common.db.DummyUserRepository;
import com.starcut.auth.common.db.UserI;
import com.starcut.auth.common.db.UserRepositoryI;

@Configuration
@ComponentScan
@EntityScan("com.starcut.auth.jwt.db")
@EnableJpaRepositories("com.starcut.auth.jwt.db")
public class JwtAuthAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public UserRepositoryI<? extends UserI> getUserRepository() {
		return new DummyUserRepository();
	}

}
