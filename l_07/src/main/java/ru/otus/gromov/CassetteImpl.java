package ru.otus.gromov;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.gromov.exception.NotEnoughBillException;
import ru.otus.gromov.exception.OverloadCassetteException;

public class CassetteImpl implements Cassette {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int CASSETTE_CAPACITY = Integer.MAX_VALUE;
    private final Bill billDenomination;
    private int counter;


    public CassetteImpl(Bill billDenomination, int count) {
        this.billDenomination = billDenomination;
        counter = count;
    }

    @Override
    public void deposit(int count) {
        log.info("Deposit to cassette {}, bills count {}", billDenomination.denomination, count);
        if ((counter + count) > CASSETTE_CAPACITY)
            throw new OverloadCassetteException(billDenomination.toString());
        counter += count;

    }

    @Override
    public void withdraw(int count) {
        log.info("withdraw from cassette {}, bills count {}", billDenomination.denomination, count);
        if (count >= counter)
            throw new NotEnoughBillException(billDenomination.toString());
        counter -= count;
    }

    @Override
    public int getBalance() {
        int balance = counter * billDenomination.getDenomination();
        log.info("get balance from cassette {}, balance: {}", billDenomination.denomination, balance);
        return balance;
    }

    @Override
    public int getBillsCount() {
        log.info("get bills count from cassette {}, balance: {}", counter);
        return counter;
    }
}
