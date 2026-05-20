package com.miapp.service;

import com.miapp.model.Contacto;
import com.miapp.repository.ContactoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactoService {
    
    @Autowired
    private ContactoRepository contactoRepository;
    
    public Contacto guardarContacto(Contacto contacto) {
        return contactoRepository.save(contacto);
    }
    
    public List<Contacto> obtenerTodosLosContactos() {
        return contactoRepository.findAll();
    }
    
    public Optional<Contacto> obtenerContactoPorId(int id) {
        return contactoRepository.findById(id);
    }
}