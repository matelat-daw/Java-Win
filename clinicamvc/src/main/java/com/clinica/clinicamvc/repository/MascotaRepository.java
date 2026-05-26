package com.clinica.clinicamvc.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.clinica.clinicamvc.model.Mascota;

@Repository
public class MascotaRepository {
    private final List<Mascota> mascotas = new ArrayList<>();
private int siguienteId = 1;
public List<Mascota> findAll() {
// TODO 9: devolver la lista de mascotas.
    return mascotas;
}
public void save(Mascota mascota) {
// TODO 10: asignar siguienteId a mascota.setId(...)
// aumentar siguienteId en 1.
// añadir la mascota a la lista.
    if (mascota.getId() == 0) {
        mascota.setId(siguienteId);
        siguienteId++;
        mascotas.add(mascota);
        return;
    }

    for (int i = 0; i < mascotas.size(); i++) {
        if (mascotas.get(i).getId() == mascota.getId()) {
            mascotas.set(i, mascota);
            return;
        }
    }

    mascotas.add(mascota);
}
public Mascota findById(int id) {
// TODO 11: recorrer la lista y devolver la mascota cuyo id coincida.
// Si no existe, devolver null.
    for (Mascota mascota : mascotas) {
            if (mascota.getId() == id) {
                return mascota;
            }
        }
        return null;
    }
}