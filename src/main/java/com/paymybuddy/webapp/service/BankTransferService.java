package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.BankTransferTypes;
import com.paymybuddy.webapp.constants.LogConstants;
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
import com.paymybuddy.webapp.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class BankTransferService implements IBankTransferService {

    private final BankTransferRepository bankTransferRepository;

    private final BankAccountRepository bankAccountRepository;

    private final UserRepository userRepository;

    @Autowired
    public BankTransferService(BankTransferRepository bankTransferRepository,
                               BankAccountRepository bankAccountRepository, UserRepository userRepository) {
        this.bankTransferRepository = bankTransferRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    private static final DateUtil dateUtil = new DateUtil();

    /**
     * opère un transfert entre le compte utilisateur et le compte bancaire indiqués
     * selon le sens mentionné dans l'attribut 'type'
     * NB : on ne se soucie pas de l'accord bancaire lors du transfert banque -> utilisateur (hypothèse d'une validation en amont)
     *
     * @param bankTransferDTOToCreate contient les informations sur le mouvement à opérer
     * @return objet bankTransferDTO contenant le transfert bancaire créé
     * @throws PMBException si le compte bancaire indiqué n existe pas
     *                      ou que le compte utilisateur n'est pas suffisamment créditeur (dans le cas d'un DEBIT)
     *                      ou que des données sont manquantes
     */
    @Override
    public Optional<BankTransferDTO> transferWithBankAccount(BankTransferDTO bankTransferDTOToCreate) throws PMBException {

        Optional<BankTransferDTO> createdBankTransferDTO = Optional.empty();

        Optional<BankAccount> bankAccount = bankAccountRepository.findById(bankTransferDTOToCreate.getBankAccountId());

        if (checksBeforeCreatingBankTransfer(bankTransferDTOToCreate, bankAccount)) {

            User user = bankAccount.get().getUser();

            switch (bankTransferDTOToCreate.getType()) {
                case CREDIT: {
                    //augmente la balance du compte utilisateur du montant du transfert bancaire
                    user.setBalance(user.getBalance().add(bankTransferDTOToCreate.getAmount()));
                    break;
                }
                case DEBIT: {
                    //diminue la balance du user du montant du transfert bancaire
                    user.setBalance(user.getBalance().subtract(bankTransferDTOToCreate.getAmount()));
                    break;
                }
            }

            //mappe le DTO dans le DAO et associe le compte bancaire au transfert
            ModelMapper modelMapper = new ModelMapper();
            BankTransfer bankTransferToCreate = modelMapper.map(bankTransferDTOToCreate, BankTransfer.class);
            bankTransferToCreate.setBankAccount(bankAccount.get());
            bankTransferToCreate.setDate(dateUtil.getCurrentLocalDateTime());

            // puis le compte client et le nouveau transfert bancaire sont sauvegardés en base
            BankTransfer createdBankTransfer;
            try {
                userRepository.save(user);
                createdBankTransfer = bankTransferRepository.save(bankTransferToCreate);

            } catch (Exception exception) {
                log.error(LogConstants.CREATE_BANK_TRANSFER_ERROR + bankTransferDTOToCreate.getBankAccountId()
                        + " // " + bankTransferDTOToCreate.getDate()
                        + " // " + bankTransferDTOToCreate.getDescription());
                throw exception;
            }

            // avant mappage inverse du DAO dans le DTO
            createdBankTransferDTO =
                    Optional.ofNullable(modelMapper.map(createdBankTransfer, BankTransferDTO.class));
            log.info(LogConstants.CREATE_BANK_TRANSFER_OK + createdBankTransferDTO.orElse(null).getBankTransferId());
        }

        return createdBankTransferDTO;
    }


    /**
     * récupération de la liste de tous les transferts bancaires d un utilisateur donné
     *
     * @param userId id de l utilisateur dont on souhaite à récupérer la liste des transferts bancaires
     * @return la liste des transferts bancaires (DTO)
     */
    @Override
    public List<BankTransferDTO> getAllBankTransfersForUser(Long userId) throws PMBException {
        List<BankTransferDTO> bankTransferDTOList = new ArrayList<>();

        if (checksBeforeGettingBankTransfers(userId)) {
            List<BankTransfer> bankTransferList = bankTransferRepository.findAllByBankAccount_User_UserId(userId);
            ModelMapper modelMapper = new ModelMapper();
            bankTransferList.forEach(bankTransfer ->
                    bankTransferDTOList
                            .add(modelMapper.map(bankTransfer, BankTransferDTO.class)));
            log.info(LogConstants.LIST_BANK_TRANSFER_OK + bankTransferDTOList.size());
        }

        return bankTransferDTOList;
    }


    /**
     * vérifie que les informations transmises sont correctes en amont de la création du transfert bancaire
     *
     * @param bankTransferDTOToCreate contient les informations sur le transfert bancaire à créer
     * @param bankAccount             compte bancaire en lien avec le transfert
     * @return true si tout est correct
     * @throws PMBException si des données sont manquantes
     *                      ou que le type de transfert est invalide
     *                      ou que le compte bancaire n'existe pas
     *                      ou que le compte utilisateur n'est pas suffisamment créditeur en cas de transfert de type DEBIT
     */
    private boolean checksBeforeCreatingBankTransfer(BankTransferDTO bankTransferDTOToCreate,
                                                     Optional<BankAccount> bankAccount) throws PMBException {

        //vérifie qu il ne manque pas d informations
        if (!bankTransferDTOToCreate.isValid()) {
            log.error(LogConstants.CREATE_BANK_TRANSFER_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_TRANSFER
                    + " for: " + bankTransferDTOToCreate.getBankAccountId()
                    + " // " + bankTransferDTOToCreate.getDescription());
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_TRANSFER);

        }

        //vérifie que le type de transfert est valide
        if (!bankTransferDTOToCreate.typeIsValid()) {
            log.error(LogConstants.CREATE_BANK_TRANSFER_ERROR
                    + PMBExceptionConstants.INVALID_BANK_TRANSFER_TYPE + " for: " + bankTransferDTOToCreate.getBankAccountId());
            throw new PMBException(PMBExceptionConstants.INVALID_BANK_TRANSFER_TYPE);
        }

        //vérifie que le compte bancaire associé existe bien
        if (!bankAccount.isPresent()) {
            log.error(LogConstants.CREATE_BANK_TRANSFER_ERROR
                    + PMBExceptionConstants.DOES_NOT_EXISTS_BANK_ACCOUNT + " for: " + bankTransferDTOToCreate.getBankAccountId());
            throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_BANK_ACCOUNT);

        }

        //vérifie que le compteur utilisateur est suffisamment créditeur si le type de transfert est un débit
        if (bankTransferDTOToCreate.getType() == BankTransferTypes.DEBIT
                && bankAccount.get().getUser().getBalance().compareTo(bankTransferDTOToCreate.getAmount()) < 0) {
            log.error(LogConstants.CREATE_BANK_TRANSFER_ERROR
                    + PMBExceptionConstants.INSUFFICIENT_BALANCE + "for : " + bankAccount.get().getUser().getUserId());
            throw new PMBException(PMBExceptionConstants.INSUFFICIENT_BALANCE);
        }

        return true;
    }


    /**
     * vérifie que les informations transmises sont correctes en amont de la récupération de la liste des transferts bancaires
     *
     * @param userId identifiant de l'utilisateur pour lequel on souhaite récupérer la liste des transferts bancaires
     * @return true si tout est correct
     * @throws PMBException si l'identifiant transmis est nul
     *                      ou que l'utilisateur n'existe pas
     */
    private boolean checksBeforeGettingBankTransfers(Long userId) throws PMBException {
        //vérifie qu il ne manque pas d informations
        if (userId == null) {
            log.error(LogConstants.LIST_BANK_TRANSFER_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_LIST_BANK_TRANSFER);
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_LIST_BANK_TRANSFER);
        }
        Optional<User> user = userRepository.findById(userId);

        //vérifie que l'utilisateur existe
        if (!user.isPresent()) {
            log.error(LogConstants.LIST_BANK_TRANSFER_ERROR
                    + PMBExceptionConstants.DOES_NOT_EXISTS_USER + " for: " + userId);
            throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER);
        }
        return true;
    }
}
