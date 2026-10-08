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
import io.github.mabals.pocketrand.repository.UserRepository;
import io.github.mabals.pocketrand.model.User;


@Service
public class TransactionService {

        private final TransactionRepository repository;
        private final UserRepository userRepository;
        private final KeywordCategoriser categoriser;

        public TransactionService(TransactionRepository repository, UserRepository userRepository, KeywordCategoriser categoriser) {
            this.repository = repository;
            this.userRepository = userRepository;
            this.categoriser = categoriser;
        }

        public List<TransactionResponse> findAll(Long userId) {
            return repository.findAllByUserIdOrderByDateDescIdDesc(userId).stream()
                    .map(TransactionResponse::from)
                    .toList();
        }

        public TransactionResponse findById(Long id, Long userId) {
            return TransactionResponse.from(getOrThrow(id, userId));
        }

        @Transactional
        public TransactionResponse create(TransactionRequest request, Long userId) {
            validateAmount(request.amount());

            // Until automatic categorisation exists (Milestone 4), uncategorised
            Category category;
            CategorySource source;
            if (request.category() != null) {
                category = request.category();
                source = CategorySource.USER;
            } else {
                category = categoriser.categorise(request.description(), request.amount()).orElse(Category.OTHER);
                source = CategorySource.RULE;
            }

            User owner = userRepository.getReferenceById(userId);
            Transaction transaction = new Transaction(
                    owner, request.date(), request.description().trim(), request.amount(), category, source);

            return TransactionResponse.from(repository.save(transaction));
        }

        @Transactional
        public TransactionResponse update(Long id, TransactionRequest request, Long userId) {
            validateAmount(request.amount());
            Transaction transaction = getOrThrow(id, userId);
            transaction.updateDetails(request.date(), request.description().trim(), request.amount());
            if (request.category() != null) {
                transaction.changeCategory(request.category(), CategorySource.USER);
            }
            return TransactionResponse.from(transaction);
        }

        @Transactional
        public void delete(Long id, Long userId) {
            repository.delete(getOrThrow(id, userId));
        }

        private Transaction getOrThrow(Long id, Long userId) {
            return repository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new TransactionNotFoundException(id));
        }

        private void validateAmount(BigDecimal amount) {
            if (amount.signum() == 0) {
                throw new IllegalArgumentException("Amount cannot be zero");
            }
        }
}