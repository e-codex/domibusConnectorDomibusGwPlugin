package eu.domibus.connector.plugin.entity;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "DC_PLUGIN_TB_MESSAGE_LOG")
@NamedQueries({
        @NamedQuery(name = "DCMessageLogEntity.findByMessageId",
                query = "select DCMessageLogEntity from DCMessageLogEntity DCMessageLogEntity where DCMessageLogEntity.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "DCMessageLogEntity.findAll",
                query = "select DCMessageLogEntity from DCMessageLogEntity DCMessageLogEntity order by DCMessageLogEntity.received asc"),
        @NamedQuery(name = "DCMessageLogEntity.deleteByMessageId",
                query = "DELETE FROM DCMessageLogEntity DCMessageLogEntity where DCMessageLogEntity.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "DCMessageLogEntity.deleteByMessageIds",
                query = "DELETE FROM DCMessageLogEntity DCMessageLogEntity where DCMessageLogEntity.messageId in :MESSAGE_IDS")
})
public class DCMessageLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PK")
    private long entityId;

    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Column(name = "RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;

    public DCMessageLogEntity() {
    }

    public DCMessageLogEntity(String messageId, Date received) {
        this.messageId = messageId;
        this.received = new Date();
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

}
