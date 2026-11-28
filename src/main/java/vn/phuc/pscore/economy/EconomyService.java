
package vn.phuc.pscore.economy;

import vn.phuc.pscore.database.DatabaseManager;

import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class EconomyService {

    private final DatabaseManager database;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public EconomyService(DatabaseManager database) {
        this.database = database;
        this.accounts = new SqliteAccountRepository();
        this.transactions = new SqliteTransactionRepository();
    }

    public AccountSnapshot getOrCreateAccount(
            UUID playerId,
            String playerName
    ) {
        long now = Instant.now().getEpochSecond();

        return database.getTransactions().execute(connection -> {
            accounts.createIfAbsent(connection, playerId, playerName, now);

            return accounts.find(connection, playerId)
                    .orElseThrow(() -> new AccountNotFoundException(playerId));
        });
    }

    public Money getBalance(UUID playerId) {
        try (Connection connection = database.getConnection()) {
            return accounts.find(connection, playerId)
                    .orElseThrow(() -> new AccountNotFoundException(playerId))
                    .getBalance();
        } catch (java.sql.SQLException exception) {
            throw new EconomyException("Could not read player balance.", exception);
        }
    }

    public EconomyOperationResult transfer(
            UUID sender,
            String senderName,
            UUID receiver,
            String receiverName,
            Money amount
    ) {
        validateAmount(amount);

        if (sender.equals(receiver)) {
            throw new EconomyException("You cannot transfer money to yourself.");
        }

        long now = Instant.now().getEpochSecond();
        UUID transactionId = UUID.randomUUID();

        return database.getTransactions().execute(connection -> {
            accounts.createIfAbsent(connection, sender, senderName, now);
            accounts.createIfAbsent(connection, receiver, receiverName, now);

            AccountSnapshot senderAccount = accounts.find(connection, sender)
                    .orElseThrow(() -> new AccountNotFoundException(sender));

            if (!accounts.debit(connection, sender, amount, now)) {
                throw new InsufficientFundsException(
                        senderAccount.getBalance(),
                        amount
                );
            }

            accounts.credit(connection, receiver, amount, now);

            EconomyTransaction transaction = new EconomyTransaction(
                    transactionId,
                    sender,
                    receiver,
                    amount,
                    TransactionType.PLAYER_TRANSFER,
                    TransactionStatus.PENDING,
                    "Player transfer",
                    Instant.ofEpochSecond(now)
            );

            transactions.insert(connection, transaction);
            transactions.updateStatus(
                    connection,
                    transactionId,
                    TransactionStatus.COMPLETED,
                    now
            );

            Money senderBalance = accounts.find(connection, sender)
                    .orElseThrow(() -> new AccountNotFoundException(sender))
                    .getBalance();

            Money receiverBalance = accounts.find(connection, receiver)
                    .orElseThrow(() -> new AccountNotFoundException(receiver))
                    .getBalance();

            return new EconomyOperationResult(
                    transactionId,
                    amount,
                    senderBalance,
                    receiverBalance
            );
        });
    }

    public EconomyOperationResult deposit(
            UUID playerId,
            String playerName,
            Money amount,
            TransactionType type,
            String description
    ) {
        validateAmount(amount);

        long now = Instant.now().getEpochSecond();
        UUID transactionId = UUID.randomUUID();

        return database.getTransactions().execute(connection -> {
            accounts.createIfAbsent(connection, playerId, playerName, now);
            accounts.credit(connection, playerId, amount, now);

            transactions.insert(connection, new EconomyTransaction(
                    transactionId,
                    null,
                    playerId,
                    amount,
                    type,
                    TransactionStatus.PENDING,
                    description,
                    Instant.ofEpochSecond(now)
            ));

            transactions.updateStatus(
                    connection,
                    transactionId,
                    TransactionStatus.COMPLETED,
                    now
            );

            Money balance = accounts.find(connection, playerId)
                    .orElseThrow(() -> new AccountNotFoundException(playerId))
                    .getBalance();

            return new EconomyOperationResult(
                    transactionId,
                    amount,
                    null,
                    balance
            );
        });
    }

    public EconomyOperationResult withdraw(
            UUID playerId,
            String playerName,
            Money amount,
            TransactionType type,
            String description
    ) {
        validateAmount(amount);

        long now = Instant.now().getEpochSecond();
        UUID transactionId = UUID.randomUUID();

        return database.getTransactions().execute(connection -> {
            accounts.createIfAbsent(connection, playerId, playerName, now);

            AccountSnapshot account = accounts.find(connection, playerId)
                    .orElseThrow(() -> new AccountNotFoundException(playerId));

            if (!accounts.debit(connection, playerId, amount, now)) {
                throw new InsufficientFundsException(
                        account.getBalance(),
                        amount
                );
            }

            transactions.insert(connection, new EconomyTransaction(
                    transactionId,
                    playerId,
                    null,
                    amount,
                    type,
                    TransactionStatus.PENDING,
                    description,
                    Instant.ofEpochSecond(now)
            ));

            transactions.updateStatus(
                    connection,
                    transactionId,
                    TransactionStatus.COMPLETED,
                    now
            );

            Money balance = accounts.find(connection, playerId)
                    .orElseThrow(() -> new AccountNotFoundException(playerId))
                    .getBalance();

            return new EconomyOperationResult(
                    transactionId,
                    amount,
                    balance,
                    null
            );
        });
    }

    public List<BalanceTopEntry> getTopBalances(int limit) {
        try (Connection connection = database.getConnection()) {
            return accounts.findTopBalances(connection, limit);
        } catch (java.sql.SQLException exception) {
            throw new EconomyException("Could not load balance leaderboard.", exception);
        }
    }

    private void validateAmount(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
    }
}
