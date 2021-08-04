package com.fatec.emprestimo.servico;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fatec.emprestimo.controller.EmprestimoController;
import com.fatec.emprestimo.model.Emprestimo;
import com.fatec.emprestimo.model.EmprestimoRepository;

@Service
public class EmprestimoServicoI implements EmprestimoServico {
	@Autowired
	private EmprestimoRepository emprestimoRepository;
	Logger logger = LogManager.getLogger(EmprestimoServicoI.class);

	public void save(Emprestimo emprestimo) {
		emprestimoRepository.save(emprestimo);
	}

	public Iterable<Emprestimo> findAll() {
		return emprestimoRepository.findAll();
	}

	public void deleteById(Long id) {
		emprestimoRepository.deleteById(id);
	}

	public List findByIsbnRa(String isbn, String ra) {
		return emprestimoRepository.findByIsbnRa(isbn, ra);
	}
	
	public Optional<Emprestimo> findById(Long id) {
		return emprestimoRepository.findById(id);
	}
	
}
