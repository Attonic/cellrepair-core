package io.github.cellrepair.service.impl;

import io.github.cellrepair.dto.AnexoOsDto;
import io.github.cellrepair.dto.ItemOsDto;
import io.github.cellrepair.dto.OrdemServicoDto;
import io.github.cellrepair.exception.NenhumResultadoException;
import io.github.cellrepair.mapper.ItemOsMapper;
import io.github.cellrepair.mapper.AnexoOsMapper;
import io.github.cellrepair.mapper.OrdemServicoMapper;
import io.github.cellrepair.model.entity.AnexoOs;
import io.github.cellrepair.model.entity.ItemOs;
import io.github.cellrepair.model.entity.OrdemServico;
import io.github.cellrepair.model.entity.Usuario;
import io.github.cellrepair.repository.*;
import io.github.cellrepair.service.OrdemServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdemServicoImpl implements OrdemServicoService {

    private final AnexoOsMapper anexoOsMapper;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrdemServicoMapper ordemServicoMapper;
    private final UserRepository userRepository;
    private final AparelhoRepository aparelhoRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ClienteRepository clienteRepository;
    private final PecaRepository pecaRepository;
    private final ItemOsMapper itemOsMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<OrdemServicoDto> findAll(Pageable pageable) {
        return ordemServicoRepository.findAll(pageable)
                .map(ordemServicoMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public OrdemServicoDto findById(Long id) {
        var ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new NenhumResultadoException("Ordem de Serviço não encontrada."));

        return ordemServicoMapper.toDto(ordemServico);
    }

    @Transactional
    @Override
    public OrdemServicoDto save(OrdemServicoDto ordemServicoDto) {

        var aparelho = aparelhoRepository.findById(ordemServicoDto.getAparelhoId())
                .orElseThrow(() -> new NenhumResultadoException("Aparelho não encontrado."));

        var tencnico = tecnicoRepository.findById(ordemServicoDto.getTecnicoId())
                .orElseThrow(() -> new NenhumResultadoException("Técnico não encontrado."));

        var cliente = clienteRepository.findById(ordemServicoDto.getClienteId())
                .orElseThrow(() -> new NenhumResultadoException("Cliente não encontrado."));

        String loginUsuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        var userDetails = userRepository.findByNomeUsuario(loginUsuarioLogado);
        if (userDetails == null) {
            throw new NenhumResultadoException("Usuário não encontrado.");
        }

        Usuario usuarioLogado = (Usuario) userDetails;

        OrdemServico ordemServico = ordemServicoMapper.toEntity(ordemServicoDto);
        ordemServico.setUsuario(usuarioLogado);
        ordemServico.setAparelho(aparelho);
        ordemServico.setCliente(cliente);
        ordemServico.setTecnico(tencnico);

        List<ItemOsDto> itensDto = ordemServicoDto.getItensOs();
        ordemServico.getItensOs().clear();

        if (itensDto != null && !itensDto.isEmpty()) {
            for (ItemOsDto itemDto : itensDto) {

                var peca = pecaRepository.findById(itemDto.getPecaId())
                        .orElseThrow(() -> new NenhumResultadoException("Peça não encontrada."));

                ItemOs novoItem = itemOsMapper.toEntity(itemDto);

                novoItem.setPeca(peca);
                novoItem.setValorUnitario(peca.getPrecoVenda());

                ordemServico.adicionarItem(novoItem);
            }
        }

        List<AnexoOsDto> anexosDto = ordemServicoDto.getAnexosOs();
        ordemServico.getAnexosOs().clear();

        if (anexosDto != null && !anexosDto.isEmpty()) {
            for (AnexoOsDto anexoDto : anexosDto) {

                AnexoOs novoAnexo = anexoOsMapper.toEntity(anexoDto);
                novoAnexo.setUsuarioCriacao(usuarioLogado);
                novoAnexo.setCaminhoArquivo(anexoDto.getCaminhoArquivo());

                ordemServico.adicionarAnexo(novoAnexo);
            }
        }

        OrdemServico ordemServicoSalva = ordemServicoRepository.save(ordemServico);
        return ordemServicoMapper.toDto(ordemServicoSalva);
    }

    @Override
    @Transactional
    public OrdemServicoDto update(OrdemServicoDto ordemServicoDto, Long id) {

        OrdemServico ordemServicoExistente = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new NenhumResultadoException("Ordem de serviço não encontrada."));

        var aparelho = aparelhoRepository.findById(ordemServicoDto.getAparelhoId())
                .orElseThrow(() -> new NenhumResultadoException("Aparelho não encontrado."));

        var tecnico = tecnicoRepository.findById(ordemServicoDto.getTecnicoId())
                .orElseThrow(() -> new NenhumResultadoException("Técnico não encontrado."));

        var cliente = clienteRepository.findById(ordemServicoDto.getClienteId())
                .orElseThrow(() -> new NenhumResultadoException("Cliente não encontrado."));

        ordemServicoMapper.updateEntityFromDto(ordemServicoDto, ordemServicoExistente);

        ordemServicoExistente.setAparelho(aparelho);
        ordemServicoExistente.setCliente(cliente);
        ordemServicoExistente.setTecnico(tecnico);

        if (ordemServicoDto.getItensOs() != null) {
            ordemServicoExistente.getItensOs().clear();

            for (ItemOsDto itemDto : ordemServicoDto.getItensOs()) {
                ItemOs novoItem = new ItemOs();
                novoItem.setQuantidade(itemDto.getQuantidade());
                novoItem.setValorUnitario(itemDto.getValorUnitario());

                var peca = pecaRepository.findById(itemDto.getPecaId())
                        .orElseThrow(() -> new NenhumResultadoException("Peça não encontrada."));

                novoItem.setPeca(peca);
                novoItem.setValorUnitario(peca.getPrecoVenda());

                novoItem.setOrdemServico(ordemServicoExistente);

                ordemServicoExistente.getItensOs().add(novoItem);
            }
        }

        if (ordemServicoDto.getAnexosOs() != null) {
            ordemServicoExistente.getAnexosOs().clear();

            for (AnexoOsDto anexoDto : ordemServicoDto.getAnexosOs()) {
                AnexoOs novoAnexo = new AnexoOs();

                var userDetails = userRepository.findByNomeUsuario(SecurityContextHolder.getContext().getAuthentication().getName());
                if (userDetails == null) {
                    throw new NenhumResultadoException("Usuário não encontrado.");
                }
                Usuario usuarioLogado = (Usuario) userDetails;
                novoAnexo.setUsuarioCriacao(usuarioLogado);

                novoAnexo.setNomeArquivo(anexoDto.getNomeArquivo());
                novoAnexo.setTipoArquivo(anexoDto.getTipoArquivo());
                novoAnexo.setCaminhoArquivo(anexoDto.getCaminhoArquivo());
                novoAnexo.setObservacao(anexoDto.getObservacao());
                novoAnexo.setOrdemServico(ordemServicoExistente);
                ordemServicoExistente.getAnexosOs().add(novoAnexo);
            }
        }

        return ordemServicoMapper.toDto(ordemServicoRepository.save(ordemServicoExistente));
    }

    @Override
    @Transactional
    public OrdemServicoDto updateItemOs(OrdemServicoDto ordemServicoDto, Long id, Long idItemOs) {
        var ordemServico = ordemServicoRepository.findById(id).get();

        var itemOs = ordemServico.getItensOs().stream()
                .filter(item -> item.getId().equals(idItemOs))
                .findFirst()
                .orElseThrow(() -> new NenhumResultadoException("Item da ordem de serviço não encontrado."));

        return ordemServicoMapper.toDto(ordemServicoRepository.save(ordemServico));
    }
}
