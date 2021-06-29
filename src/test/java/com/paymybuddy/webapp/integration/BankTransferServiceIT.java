package com.paymybuddy.webapp.integration;


import com.paymybuddy.webapp.constants.BankTransferTypes;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.BankAccount;
import com.paymybuddy.webapp.model.BankTransfer;
import com.paymybuddy.webapp.model.DTO.BankTransferDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.BankAccountRepository;
import com.paymybuddy.webapp.repository.BankTransferRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.contract.IBankTransferService;
import com.paymybuddy.webapp.testconstants.BankAccountTestConstants;
import com.paymybuddy.webapp.testconstants.BankTransferTestConstants;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import com.paymybuddy.webapp.util.DateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
public class BankTransferServiceIT {
    @Autowired
    IBankTransferService bankTransferService;

    @Autowired
    BankTransferRepository bankTransferRepository;

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    UserRepository userRepository;

    private static final DateUtil dateUtil = new DateUtil();

    private User existingUser;
    private BankAccount existingBankAccount;
    private BankTransferDTO bankTransferDTOToCreate;


    @BeforeEach
    private void setUpPerTest() {
        //initialisation avec un user et un compte bancaire associé en base
        existingUser = new User();
        existingUser.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
        existingUser.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
        existingUser.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
        existingUser.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
        existingUser.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
        existingUser = userRepository.save(existingUser);

        existingBankAccount = new BankAccount();
        existingBankAccount.setIban(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_IBAN);
        existingBankAccount.setName(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_NAME);
        existingBankAccount.setUser(existingUser);
        existingBankAccount = bankAccountRepository.save(existingBankAccount);

        //initialisation de l objet bankTransferDTOToCreate
        bankTransferDTOToCreate = new BankTransferDTO();
        bankTransferDTOToCreate.setDescription(BankTransferTestConstants.NEW_BANK_TRANSFER_DESCRIPTION);
        bankTransferDTOToCreate.setAmount(BankTransferTestConstants.NEW_BANK_TRANSFER_AMOUNT);
        bankTransferDTOToCreate.setType(BankTransferTypes.CREDIT);
        bankTransferDTOToCreate.setBankAccountId(existingBankAccount.getBankAccountId());
    }

    @AfterEach
    private void tearDownPerTest() {
        //nettoyage la DB en fin de test en supprimant le user créé à l initialisation
        userRepository.deleteById(existingUser.getUserId());
    }


    @Nested
    @DisplayName("transferWithBankAccount IT")
    class TransferWithBankAccountIT {

        @Test
        @DisplayName("WHEN creating a new bank transfer from bank (ie CREDIT) " +
                "with correct informations for an existing bank account " +
                "THEN the returned value is the added bank transfer, " +
                "AND the bank transfer is added in DB " +
                "AND the user's balance is increased by the bank transfer amount")
        public void transferWithBankAccountIT_CreditWithSuccess() throws Exception {

            Optional<BankTransferDTO> bankTransferDTOCreated
                    = bankTransferService.transferWithBankAccount(bankTransferDTOToCreate);
            assertThat(bankTransferDTOCreated).isPresent();
            assertNotNull(bankTransferDTOCreated.get().getBankTransferId());

            Optional<BankTransfer> bankTransferCreated = bankTransferRepository
                    .findById(bankTransferDTOCreated.get().getBankTransferId());
            assertThat(bankTransferCreated).isPresent();
            assertEquals(bankTransferDTOToCreate.getDescription(), bankTransferCreated.get().getDescription());
            assertNotNull(bankTransferDTOCreated.get().getDate());

            Optional<User> userUpdated = userRepository.findById(existingUser.getUserId());
            assertThat(userUpdated).isPresent();
            assertEquals(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE
                    .add(bankTransferDTOToCreate.getAmount()), userUpdated.get().getBalance());

            //nettoyage de la DB en fin de test en supprimant le compte bancaire créé à l'initialisation
            // et le transfert créé par le test
            bankTransferRepository.deleteById(bankTransferCreated.get().getBankTransferId());
            bankAccountRepository.deleteById(existingBankAccount.getBankAccountId());
        }


        @Test
        @DisplayName("WHEN creating a new bank transfer from bank (ie CREDIT)" +
                "with correct informations for a non-existing bank account " +
                "THEN an PMBException is thrown AND the bank transfer is not added in DB " +
                "AND no user balance is updated")
        public void transferWithBankAccountIT_WithNotExistingBankAccount() {

            bankTransferDTOToCreate.setBankAccountId(BankAccountTestConstants.UNKNOWN_BANK_ACCOUNT_ID);

            Exception exception = assertThrows(PMBException.class,
                    () -> bankTransferService.transferWithBankAccount(bankTransferDTOToCreate));
            assertEquals(PMBExceptionConstants.DOES_NOT_EXISTS_BANK_ACCOUNT, exception.getMessage());

            Optional<BankTransfer> bankTransferCreated = bankTransferRepository
                    .findByDateAndBankAccount_BankAccountId(bankTransferDTOToCreate.getDate(),
                            bankTransferDTOToCreate.getBankAccountId());
            assertThat(bankTransferCreated).isNotPresent();

            //nettoyage de la DB en fin de test en supprimant le compte bancaire créé à l'initialisation
            bankAccountRepository.deleteById(existingBankAccount.getBankAccountId());
        }
    }


    @Test
    @DisplayName("WHEN getting the list of bank transfers for an existing user " +
            "THEN the list of bank transfers in DB is returned")
    public void getAllBankTransfersForUser_WithData() throws PMBException {

        //initialisation du test avec un transfert bancaire en base
        BankTransfer existingBankTransfer = new BankTransfer();
        existingBankTransfer.setDate(dateUtil.getCurrentLocalDateTime());
        existingBankTransfer.setDescription(BankTransferTestConstants.EXISTING_BANK_TRANSFER_DESCRIPTION);
        existingBankTransfer.setAmount(BankTransferTestConstants.EXISTING_BANK_TRANSFER_AMOUNT);
        existingBankTransfer.setType(BankTransferTypes.CREDIT);
        existingBankTransfer.setBankAccount(existingBankAccount);
        existingBankTransfer = bankTransferRepository.save(existingBankTransfer);

        //test
        List<BankTransferDTO> bankTransferDTOList = bankTransferService.getAllBankTransfersForUser(existingUser.getUserId());

        assertThat(bankTransferDTOList).isNotEmpty();
        assertEquals(existingBankTransfer.getBankTransferId(), bankTransferDTOList.get(0).getBankTransferId());

        //nettoyage de la DB en fin de test en supprimant le compte bancaire
        // et le transfert créés à l'initialisation
        bankTransferRepository.deleteById(existingBankTransfer.getBankTransferId());
        bankAccountRepository.deleteById(existingBankAccount.getBankAccountId());
    }
}
