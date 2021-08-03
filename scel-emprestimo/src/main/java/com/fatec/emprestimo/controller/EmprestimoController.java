package com.fatec.emprestimo.controller;

import java.util.*;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fatec.emprestimo.model.*;

import com.fatec.emprestimo.servico.EmprestimoServico;

@RestController
@RequestMapping("/api")
public class EmprestimoController {
	Logger logger = LogManager.getLogger(EmprestimoController.class);
	@Autowired
	EmprestimoServico servico;

	@GetMapping("/v1/emprestimos")
	public ResponseEntity<Iterable<Emprestimo>> consultaTodos() {
		return ResponseEntity.ok().body(servico.findAll());
	}

	@PostMapping("/v1/emprestimos")
	public ResponseEntity<Object> save(@RequestBody @Valid Emprestimo emprestimo, BindingResult result) {
		logger.info(">>>>>> 1. controler metodo save chamado");
		ResponseEntity<Object> response = null;
		if (result.hasErrors()) {
			logger.info("entrada de dados invalida na pagina registrar emprestimo");
			response = new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
		}

		String url = "https://ts-scel-rest-aluno.herokuapp.com/api/v1/alunos/" + emprestimo.getRa();
		RestTemplate restTemplate = new RestTemplate();
		try {
			
			ResponseEntity<Object> resposta = restTemplate.exchange(url, HttpMethod.GET, null, Object.class, emprestimo.getRa());
			if (resposta.getStatusCode().equals(HttpStatus.OK)) {

				response = ResponseEntity.ok().body(resposta.getBody());
			}
			else {
				logger.info(">>>>>> aluno nao encontrado");
				response = ResponseEntity.badRequest().body("Aluno não cadastrado.");
				
			}

		} catch (Exception e) {
			logger.info(">>>>>> erro nao esperado=" + e.getMessage());
			
			response = ResponseEntity.badRequest().body("Aluno não cadastrado.");
		}
		return response;
	}
}
