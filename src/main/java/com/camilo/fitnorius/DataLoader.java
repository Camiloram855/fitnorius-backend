package com.camilo.fitnorius;

import com.camilo.fitnorius.model.Producto;
import com.camilo.fitnorius.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {
    private final ProductoRepository productoRepository;
    public DataLoader(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productoRepository.count() == 0) {
            productoRepository.saveAll(List.of(
                    Producto.builder()
                            .nombre("Set de bandas de resistencia (heavy, medium, light)")
                            .precio(new BigDecimal("89900"))
                            .imagenUrl("./img/Bandas-3.png")
                            .descripcion(null)
                            .build(),
                    Producto.builder()
                            .nombre("Set de bandas super combo")
                            .precio(new BigDecimal("99900"))
                            .imagenUrl("./img/bandas-4.png")
                            .descripcion("Más músculo y menos grasa en menos tiempo. Déjalo en manos de profesionales.")
                            .build(),
                    Producto.builder()
                            .nombre("Set de bandas mega combo")
                            .precio(new BigDecimal("99900"))
                            .imagenUrl("./img/bandas-5.png")
                            .descripcion("Más músculo y menos grasa en menos tiempo. Déjalo en manos de profesionales.")
                            .build()
            ));
            System.out.println("Productos iniciales cargados.");
        }
    }
}
