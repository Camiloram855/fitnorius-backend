package com.camilo.fitnorius.config;

import com.camilo.fitnorius.model.ScratchPrize;
import com.camilo.fitnorius.repository.ScratchPrizeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Carga premios por defecto en la base de datos la PRIMERA vez que arranca la app.
 * Si ya hay premios guardados, no hace nada (no sobreescribe).
 */
@Configuration
public class ScratchPrizeSeeder {

    @Bean
    public CommandLineRunner seedScratchPrizes(ScratchPrizeRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(prize("🎉", "10% de descuento",    "percent", 10,   30));
                repo.save(prize("🌟", "15% de descuento",    "percent", 15,   25));
                repo.save(prize("💰", "$5.000 de descuento", "fixed",   5000, 20));
                repo.save(prize("🛍️", "5% de descuento",     "percent", 5,    15));
                repo.save(prize("🍀", "¡Suerte la próxima!", "none",    0,    10));
                System.out.println("✅ Premios del Raspa y Gana cargados por defecto.");
            }
        };
    }

    private ScratchPrize prize(String emoji, String label, String type, double value, int weight) {
        ScratchPrize p = new ScratchPrize();
        p.setEmoji(emoji);
        p.setLabel(label);
        p.setType(type);
        p.setValue(value);
        p.setWeight(weight);
        p.setActive(true);
        return p;
    }
}
