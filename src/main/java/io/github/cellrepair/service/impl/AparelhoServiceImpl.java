package io.github.cellrepair.service.impl;

import io.github.cellrepair.dto.AparelhoDto;
import io.github.cellrepair.exception.NenhumResultadoException;
import io.github.cellrepair.mapper.AparelhoMapper;
import io.github.cellrepair.model.entity.Aparelho;
import io.github.cellrepair.repository.AparelhoRepository;
import io.github.cellrepair.service.AparelhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AparelhoServiceImpl implements AparelhoService {

    private final AparelhoRepository aparelhoRepository;
    private final AparelhoMapper aparelhoMapper;

    @Override
    @Transactional(readOnly = true)
    public AparelhoDto findById(Long id) {
        var aparelho = aparelhoRepository.findById(id)
                .orElseThrow(() -> new NenhumResultadoException("Aparelho não encontrado"));
        return aparelhoMapper.toDto(aparelho);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AparelhoDto> findAll(Pageable pageable) {
        return aparelhoRepository.findAll(pageable)
                .map(aparelhoMapper::toDto);
    }

    @Override
    @Transactional
    public AparelhoDto save(AparelhoDto aparelhoDto) {
        Aparelho aparelho = aparelhoMapper.toEntity(aparelhoDto);
        Aparelho aparelhoSalvo = aparelhoRepository.save(aparelho);
        return aparelhoMapper.toDto(aparelhoSalvo);
    }

    @Override
    @Transactional
    public AparelhoDto update(AparelhoDto aparelhoDto, Long id) {

        Aparelho aparelho = aparelhoRepository.findById(id)
                        .orElseThrow(() -> new NenhumResultadoException("Aparelho não encontrado"));

        aparelhoMapper.updateEntityFromDto(aparelhoDto, aparelho);
        var aparelhoAtualizado = aparelhoRepository.save(aparelho);
        return aparelhoMapper.toDto(aparelhoAtualizado);

    }
}
