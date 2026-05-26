package com.clinica.clinicamvc.service;

import java.util.List;
import com.clinica.clinicamvc.form.MascotaForm;
import com.clinica.clinicamvc.model.Mascota;
import com.clinica.clinicamvc.repository.MascotaRepository;
import org.springframework.stereotype.Service;

@Service
public class MascotaService {
    private final MascotaRepository repository;
    public MascotaService(MascotaRepository repository) {
    this.repository = repository;
    }
    public List<Mascota> listar() {
    // TODO 12: delegar en repository.findAll().
       return repository.findAll();
    }
    public void registrar(MascotaForm form) {
        // TODO 13: crear un objeto Mascota nuevo.
        Mascota mascota = new Mascota();
        // TODO 14: copiar nombre, especie, propietario y telefono del form.
        mascota.setNombre(form.getNombre());
        mascota.setEspecie(form.getEspecie());
        mascota.setPropietario(form.getPropietario());
        mascota.setTelefono(form.getTelefono());
        // TODO 15: asignar estado = "PENDIENTE".
        mascota.setEstado("PENDIENTE");
        // TODO 16: guardar con repository.save(mascota).
        repository.save(mascota);
    }
    public Mascota buscarPorId(int id) {
    // TODO 17: delegar en repository.findById(id).
        return repository.findById(id);
    }
    public boolean atender(int id) {
    // TODO 18: buscar la mascota por id.
        Mascota mascota = repository.findById(id);
    // TODO 19: si su estado es "PENDIENTE", cambiarlo a "EN_CONSULTA".
    if (mascota != null && "PENDIENTE".equals(mascota.getEstado())) {
        mascota.setEstado("EN_CONSULTA");
        repository.save(mascota);
        return true;
    }
    return false;
    }   
}