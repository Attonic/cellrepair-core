package io.github.cellrepair.service.impl;

import io.github.cellrepair.dto.AnexoOsDto;
import io.github.cellrepair.exception.NenhumResultadoException;
import io.github.cellrepair.mapper.AnexoOsMapper;
import io.github.cellrepair.model.entity.AnexoOs;
import io.github.cellrepair.repository.AnexoOsRepository;
import io.github.cellrepair.service.AnexoOsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnexoOsServiceImpl implements AnexoOsService {

    private final AnexoOsRepository anexoOsRepository;
    private final AnexoOsMapper anexoOsMapper;

    @Transactional(readOnly = true)
    @Override
    public AnexoOsDto findById(Long id) {
        var anexoOs = anexoOsRepository.findById(id)
                .orElseThrow(() -> new NenhumResultadoException("Anexo não encontrado"));
        return anexoOsMapper.toDto(anexoOs);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AnexoOs> findByOrdemServicoId(Long ordemServicoId) {
        List<AnexoOs> anexoOs = anexoOsRepository.findByOrdemServicoId(ordemServicoId);
        if (anexoOs.isEmpty()) {
            throw new NenhumResultadoException("Nenhum anexo encontrado para a ordem de serviço");
        }
        return anexoOs;
    }
}
