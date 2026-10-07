package io.github.mabals.pocketrand.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.mabals.pocketrand.dto.TransactionRequest;
import io.github.mabals.pocketrand.dto.TransactionResponse;
import io.github.mabals.pocketrand.exception.TransactionNotFoundException;
import io.github.mabals.pocketrand.model.Category;
import io.github.mabals.pocketrand.model.CategorySource;
import io.github.mabals.pocketrand.model.Transaction;
import io.github.mabals.pocketrand.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<TransactionResponse> findAll() {
        return repository.findAllByOrderByDateDescIdDesc().stream()
                .map(TransactionResponse::from)
                .toList();
    }

    public TransactionResponse findById(Long id) {
        return TransactionResponse.from(getOrThrow(id));
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        validateAmount(request.amount());

        // Until automatic categorisation exists (Milestone 4), uncategorised
        // transactions fall back to OTHER.
        Category category = request.category() != null ? request.category() : Category.OTHER;
        CategorySource source = request.category() != null ? CategorySource.USER : CategorySource.RULE;

        Transaction transaction = new Transaction(
                request.date(), request.description().trim(), request.amount(), category, source);

        return TransactionResponse.from(repository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        validateAmount(request.amount());
        Transaction transaction = getOrThrow(id);
        transaction.updateDetails(request.date(), request.description().trim(), request.amount());
        if (request.category() != null) {
            transaction.changeCategory(request.category(), CategorySource.USER);
        }
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private Transaction getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.signum() == 0) {
            throw new IllegalArgumentException("Amount cannot be zero");
        }
    }
}