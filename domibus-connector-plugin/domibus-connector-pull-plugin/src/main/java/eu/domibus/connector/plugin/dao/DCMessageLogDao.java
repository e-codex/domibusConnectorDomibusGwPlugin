package eu.domibus.connector.plugin.dao;

import eu.domibus.connector.plugin.entity.DCMessageLogEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

//@Conditional(IsPullPluginCondition.class)
@Repository
public class DCMessageLogDao extends DCBasicDao<DCMessageLogEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DCMessageLogDao.class);

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
    public DCMessageLogEntity findByMessageId(String messageId) {
        TypedQuery<DCMessageLogEntity> query = em.createNamedQuery("DCMessageLogEntity.findByMessageId", DCMessageLogEntity.class);
        query.setParameter(MESSAGE_ID, messageId);
        DCMessageLogEntity wsMessageLogEntity;
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
    public List<DCMessageLogEntity> findAll(int maxCount) {
        TypedQuery<DCMessageLogEntity> query = em.createNamedQuery("DCMessageLogEntity.findAll", DCMessageLogEntity.class);
        if(maxCount > 0) {
            return query.setMaxResults(maxCount).getResultList();
        }
        return query.getResultList();
    }

    /**
     * Find all entries in the plugin table.
     */
    public List<DCMessageLogEntity> findAll() {
        TypedQuery<DCMessageLogEntity> query = em.createNamedQuery("DCMessageLogEntity.findAll", DCMessageLogEntity.class);
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
