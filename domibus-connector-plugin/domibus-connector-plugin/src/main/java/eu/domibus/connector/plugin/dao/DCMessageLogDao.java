package eu.domibus.connector.plugin.dao;

import eu.domibus.connector.plugin.config.DCPluginConfiguration;
import eu.domibus.connector.plugin.config.IsPullPluginCondition;
import eu.domibus.connector.plugin.entity.DCMessageLogEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.dao.WSMessageLogDao;
import eu.domibus.plugin.webService.entity.WSMessageLogEntity;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Conditional(IsPullPluginCondition.class)
@Repository
public class DCMessageLogDao extends DCBasicDao<DCMessageLogEntity> {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSMessageLogDao.class);

    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String MESSAGE_IDS = "MESSAGE_IDS";
    private static final String FINAL_RECIPIENT= "FINAL_RECIPIENT";

    public DCMessageLogDao() {
        super(DCMessageLogEntity.class);
    }

    /**
     * Find the entry based on a given MessageId.
     *
     * @param messageId the id of the message.
     */
    public WSMessageLogEntity findByMessageId(String messageId) {
        TypedQuery<WSMessageLogEntity> query = em.createNamedQuery("DCMessageLogEntity.findByMessageId", WSMessageLogEntity.class);
        query.setParameter(MESSAGE_ID, messageId);
        WSMessageLogEntity wsMessageLogEntity;
        try {
            wsMessageLogEntity = query.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
        return wsMessageLogEntity;
    }

    /**
     * Find all entries in the plugin table limited to maxCount. When maxCount is 0, return all.
     */
    public List<WSMessageLogEntity> findAll(int maxCount) {
        TypedQuery<WSMessageLogEntity> query = em.createNamedQuery("DCMessageLogEntity.findAll", WSMessageLogEntity.class);
        if(maxCount > 0) {
            return query.setMaxResults(maxCount).getResultList();
        }
        return query.getResultList();
    }

    /**
     * Find all entries in the plugin table.
     */
    public List<WSMessageLogEntity> findAll() {
        TypedQuery<WSMessageLogEntity> query = em.createNamedQuery("DCMessageLogEntity.findAll", WSMessageLogEntity.class);
        return query.getResultList();
    }

    /**
     * Delete the entry related to a given MessageId.
     *
     * @param messageId the id of the message.
     */
    public void deleteByMessageId(final String messageId) {
        Query query = em.createNamedQuery("DCMessageLogEntity.deleteByMessageId");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();
    }

    /**
     * Delete the entries related to a given MessageIds.
     *
     * @param messageIds the ids of the messages that should be deleted.
     */
    public void deleteByMessageIds(final List<String> messageIds) {
        Query query = em.createNamedQuery("DCMessageLogEntity.deleteByMessageIds");
        query.setParameter(MESSAGE_IDS, messageIds);
        query.executeUpdate();
    }

}
