package com.camilo.fitnorius.config;

import com.camilo.fitnorius.model.ScratchConfig;
import com.camilo.fitnorius.repository.ScratchConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScratchConfigSeeder {

    @Bean
    public CommandLineRunner seedScratchConfig(ScratchConfigRepository repo) {
        return args -> {
            if (repo.findById(1L).isEmpty()) {
                ScratchConfig config = new ScratchConfig();
                config.setVisible(true);
                repo.save(config);
            }
        };
    }
}
