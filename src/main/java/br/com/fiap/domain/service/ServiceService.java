package br.com.fiap.domain.service;

import br.com.fiap.domain.repository.ServiceRepository;

import java.util.List;

public class ServiceService implements Service<Service,Long>{
    private ServiceRepository repo = ServiceRepository.build();

    @Override
    public List<Service> findAll() {
        return repo.findAll();
    }

    @Override
    public Service findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Service persiste(Service service) {
        return repo.persiste(service);
    }
}
