package io.github.cellrepair.service.impl;

import io.github.cellrepair.dto.ClienteDto;
import io.github.cellrepair.exception.ConflitoException;
import io.github.cellrepair.exception.NenhumResultadoException;
import io.github.cellrepair.mapper.ClienteMapper;
import io.github.cellrepair.model.entity.Cliente;
import io.github.cellrepair.repository.ClienteRepository;
import io.github.cellrepair.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Transactional(readOnly = true)
    @Override
    public ClienteDto findById(Long id) {
        var cliente =  clienteRepository.findById(id)
                .orElseThrow(() -> new NenhumResultadoException("Cliente não encontrado."));
        return clienteMapper.toDto(cliente);
    }
    @Transactional(readOnly = true)
    @Override
    public Page<ClienteDto> findAll(Pageable pageable) {
        return clienteRepository.findAll(pageable)
                .map(clienteMapper::toDto);
    }

    @Override
    @Transactional
    public ClienteDto save(ClienteDto clienteDto) {
        if (clienteRepository.existsByCpfCnpj(clienteDto.getCpfCnpj())){
            throw new ConflitoException("Já existe Cliente com esse CNPJ ou CPF.");
        }
        if (clienteRepository.existsByEmail(clienteDto.getEmail())){
            throw new ConflitoException("Já existe Cliente com esse e-mail.");
        }

        Cliente cliente = clienteMapper.toEntity(clienteDto);
        Cliente clienteSalvo = clienteRepository.save(cliente);
        return clienteMapper.toDto(clienteSalvo);

    }

    @Override
    @Transactional
    public ClienteDto update(ClienteDto clienteDto, Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NenhumResultadoException("Cliente não encontrado."));

        if (clienteRepository.existsByCpfCnpjAndIdNot(clienteDto.getCpfCnpj(), id)){
            throw new ConflitoException("Já existe outro Cliente com esse CNPJ ou CPF.");
        }
        if (clienteRepository.existsByEmailAndIdNot(clienteDto.getEmail(), id)){
            throw new ConflitoException("Já existe outro Cliente com esse e-mail.");
        }

        clienteMapper.updateEntityFromDto(clienteDto, cliente);
        var clienteAtualizado = clienteRepository.save(cliente);
        return clienteMapper.toDto(clienteAtualizado);
    }
}
