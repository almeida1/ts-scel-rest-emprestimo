package com.fatec.emprestimo.controller;

import java.util.*;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
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

		ResponseEntity<Object> response = null;
		if (result.hasErrors()) {
			logger.info(">>>>>> 1. controller entrada de dados invalida na pagina registrar emprestimo");
			response = new ResponseEntity<Object>("Dados invalidos", HttpStatus.OK);
		} else {
			// se o ra estivier em branco controller responde com o endpoint de consulta
			// todos verificar
			// retorna ok com body contendo inf de aluno e retorna ok com body ra nao
			// localizado (o recurso que presta o servico foi localizado)
			// https://reflectoring.io/spring-boot-exception-handling/
			String url1 = "https://ts-scel-rest-aluno.herokuapp.com/api/v1/alunos/" + emprestimo.getRa();
			String url2 = "https://ts-scel-rest.herokuapp.com/api/v1/livros/" + emprestimo.getIsbn();
			boolean alunoCadastrado = false;
			boolean livroCadastrado = false;
			ResponseEntity<String> resposta = null;
			RestTemplate restTemplate = new RestTemplate();
			List<Emprestimo> emprestimos = servico.findByIsbnRa(emprestimo.getIsbn(), emprestimo.getRa());
			boolean emprestimoEmAberto = false;
			for (Emprestimo umEmprestimo : emprestimos) {
				if (umEmprestimo.getDataDevolucao() == null) {
					emprestimoEmAberto = true;
				}
			}
			try {

				resposta = restTemplate.getForEntity(url1, String.class);
				// if (resposta.getStatusCode().equals(HttpStatus.OK)) {
				if (!resposta.getBody().contains("RA não localizado")) {
					logger.info(">>>>>> 1. controller aluno cadastrado ");
					alunoCadastrado = true;
				} else {
					logger.info(">>>>>> 2. controller aluno nao cadastrado");
					alunoCadastrado = false;
					response = ResponseEntity.badRequest().body("Aluno não cadastrado.");

				}
				
				resposta = restTemplate.getForEntity(url2, String.class);
				
				if (!resposta.getBody().contains("ISBN não localizado")) {
					logger.info(">>>>>> 3. controller livro cadastrado");
					livroCadastrado=true;
				} else {
					logger.info(">>>>>> 4. controller livro nao encontrado");
					livroCadastrado=false;
					response = ResponseEntity.badRequest().body("Livro não cadastrado.");

				}//400 bad request, A 404 error is often returned when pages have been moved or deleted
			} catch (Exception e) {
				logger.info(">>>>>> 1. controller erro nao esperado = " + e.getMessage());

				response = ResponseEntity.internalServerError().body("Erro não esperado comunique o administrador.");
			}
			if (livroCadastrado == true & alunoCadastrado == true & (emprestimoEmAberto == false || emprestimos.isEmpty())) {
				logger.info(" achou livro/aluno no db e nao existe emprestimo cadastrado");
				DateTime dataAtual = new DateTime();
				emprestimo.setDataEmprestimo(dataAtual);
				emprestimo.setDataDevolucaoPrevista();
				servico.save(emprestimo);
				response = ResponseEntity.ok("Emprestimo cadastrado.");
			} else {
				response = ResponseEntity.ok("Emprestimo não cadastrado - existem emprestimos em aberto.");
			}
		}
		return response;
	}
}
