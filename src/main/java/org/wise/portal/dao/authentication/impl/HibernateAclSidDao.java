/**
 * Copyright (c) 2007 Encore Research Group, University of Toronto
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.wise.portal.dao.authentication.impl;


import org.springframework.dao.support.DataAccessUtils;
import org.wise.portal.dao.authentication.AclSidDao;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.domain.authentication.MutableAclSid;
import org.wise.portal.domain.authentication.impl.PersistentAclSid;

/**
 * This class is not being used. Tried to implement Hibernate versions of the acl
 * services and became bogged down, so went back to jdbc versions. Keeping this
 * class around in case we want to try again later.
 * 
 * @author Cynick Young
 * 
 * @version $Id$
 */
public class HibernateAclSidDao extends AbstractHibernateDao<MutableAclSid>
        implements AclSidDao<MutableAclSid> {

    private static final String FIND_ALL_QUERY = "from PersistentAclSid";

    // /**
    // * @see
    // net.sf.sail.webapp.dao.impl.AbstractHibernateDao#save(java.lang.Object)
    // */
    // @Override
    // public void save(MutableAclSid object) {
    // this.g
    // }
    //
    /**
     * @see org.wise.portal.dao.impl.AbstractHibernateDao#getDataObjectClass()
     */
    @Override
    protected Class<PersistentAclSid> getDataObjectClass() {
        return PersistentAclSid.class;
    }

    /**
     * @see org.wise.portal.dao.impl.AbstractHibernateDao#getFindAllQuery()
     */
    @Override
    protected String getFindAllQuery() {
        return FIND_ALL_QUERY;
    }

    /**
     * @see org.wise.portal.dao.authentication.AclSidDao#retrieveBySidName(java.lang.String)
     */
    public MutableAclSid retrieveBySidName(String sidName) {
        return (MutableAclSid) DataAccessUtils
                .uniqueResult(this
                        .getHibernateTemplate()
                        .findByNamedParam(
                                "from PersistentAclSid as sid where sid.sidName = :sidName",
                                "sidName", sidName));
    }
}