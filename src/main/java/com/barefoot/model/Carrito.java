package com.barefoot.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Carrito {
    // Esta clase NO es una entidad (@Entity), es solo un modelo de ayuda (DTO)

    private List<ItemCarrito> items = new ArrayList<>();
    private Double total = 0.0;

    public void agregarItem(ItemCarrito item) {
        this.items.add(item);
        calcularTotal();
    }

    public void calcularTotal() {
        this.total = items.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();
    }
}