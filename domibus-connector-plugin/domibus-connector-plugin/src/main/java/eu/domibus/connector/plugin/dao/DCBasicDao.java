package eu.domibus.connector.plugin.dao;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;


/**
 * A basic DAO implementation providing the standard CRUD operations
 *
 * for more information {@link eu.domibus.plugin.webService.dao.WSBasicDao}
 *
 */
public class DCBasicDao<T> {


    protected final Class<T> typeOfT;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager em;

    /**
     * @param typeOfT The entity class this DAO provides access to
     */
    public DCBasicDao(final Class<T> typeOfT) {
        this.typeOfT = typeOfT;
    }

    public <T> T findById(Class<T> typeOfT, String id) {
        return em.find(typeOfT, id);
    }

    @Transactional
    public void create(final T entity) {
        em.persist(entity);
    }

    @Transactional
    public void delete(final T entity) {
        em.remove(em.merge(entity));
    }

    public T read(final long id) {
        return em.find(this.typeOfT, id);
    }

    @Transactional
    public void updateAll(final Collection<T> update) {
        for (final T t : update) {
            this.update(t);
        }
    }

    @Transactional
    public void deleteAll(final Collection<T> delete) {
        for (final T t : delete) {
            this.delete(t);
        }
    }

    @Transactional
    public void update(final T entity) {
        em.merge(entity);
    }

}
