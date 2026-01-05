
package com.example.cotizador.service;

import com.example.cotizador.entity.Producto;
import com.example.cotizador.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository repo;

    public ProductoService(ProductoRepository repo) {
        this.repo = repo;
    }

    public List<Producto> listar() {
        return repo.findAll();
    }

    public void guardar(Producto p) {
        repo.save(p);
    }
}
