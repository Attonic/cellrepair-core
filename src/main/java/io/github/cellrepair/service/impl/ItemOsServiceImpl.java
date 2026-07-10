package io.github.cellrepair.service.impl;

import io.github.cellrepair.dto.ItemOsDto;
import io.github.cellrepair.exception.NenhumResultadoException;
import io.github.cellrepair.mapper.ItemOsMapper;
import io.github.cellrepair.model.entity.ItemOs;
import io.github.cellrepair.repository.ItemOsRepository;
import io.github.cellrepair.service.ItemOsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemOsServiceImpl implements ItemOsService {

    private final ItemOsRepository itemOsRepository;
    private final ItemOsMapper itemOsMapper;

    @Transactional(readOnly = true)
    @Override
    public ItemOsDto findById(Long id) {
        var itemOs = itemOsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de Ordem de Serviço não encontrado."));
        return itemOsMapper.toDto(itemOs);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemOs> findByOrdemServicoId(Long ordemServicoId) {
        List<ItemOs> itensOs = itemOsRepository.findByOrdemServicoId(ordemServicoId);
        if (itensOs.isEmpty()) {
            throw new NenhumResultadoException("Nenhum item de Ordem de Serviço encontrado para a Ordem de Serviço com ID.");
        }
        return itensOs;
    }

}
