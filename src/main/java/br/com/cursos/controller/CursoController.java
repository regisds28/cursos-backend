package br.com.cursos.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.cursos.controller.request.CursoRequest;
import br.com.cursos.controller.response.CursoResponse;
import br.com.cursos.model.Arquivo;
import br.com.cursos.model.Curso;
import br.com.cursos.service.CursoService;

@RestController
@RequestMapping("/curso")
@Validated
public class CursoController {

	@Autowired
	private CursoService cursoService;
	
	@Autowired
	private ModelMapper mapper;
	
	@GetMapping
	public List<CursoResponse> todos(CursoRequest cursoRequest) {
		
		Curso curso = mapper.map(cursoRequest, Curso.class);
		
		List<Curso> cursos = cursoService.filtraPor(curso);
		
		List<CursoResponse> cursosResponse = cursos
			.stream()
			.map((c) -> {
				return new CursoResponse(c);
			}).collect(Collectors.toList());
		
		return cursosResponse;
		
	}
	
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public CursoResponse salvar(
			@RequestPart("curso") @Valid CursoRequest cursoRequest,
			@RequestPart(value = "file", required = false) MultipartFile arquivoRequest) {
		
		Arquivo arquivo = getArquivo(arquivoRequest);
		
		Curso curso = mapper.map(cursoRequest, Curso.class);
		curso.setArquivo(arquivo);
		
		Curso cursoSalvo = cursoService.salvar(curso);
		
		return new CursoResponse(cursoSalvo);
	}
	
	@PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CursoResponse alterar(
			@PathVariable Integer id, 
			@RequestPart("curso") CursoRequest cursoRequest,
			@RequestPart(value = "file", required = false) MultipartFile arquivoRequest) {
		
		Arquivo arquivo = getArquivo(arquivoRequest);
		
		Curso curso = mapper.map(cursoRequest, Curso.class);
		
		if (arquivo != null) {
			curso.setArquivo(arquivo);
		}
		
		Curso cursoAlterado = cursoService.alterar(id, curso);
		return new CursoResponse(cursoAlterado);
	}
	
	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Integer id) {
		cursoService.excluir(id);
	}

	private Arquivo getArquivo(MultipartFile arquivoRequest) {
		Arquivo arquivo = null;
		
		try {
			String nomeArquivo = arquivoRequest.getOriginalFilename();
			String tipo = arquivoRequest.getContentType();
			byte[] conteudo = arquivoRequest.getBytes();
			
			arquivo = new Arquivo(nomeArquivo, tipo, conteudo);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arquivo;
	}
	
}
