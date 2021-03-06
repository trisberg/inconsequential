/* Copyright (C) 2010 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.datastore.web.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.datastore.core.Datastore;
import org.springframework.datastore.core.DatastoreUtils;
import org.springframework.datastore.core.Session;
import org.springframework.datastore.transactions.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import javax.persistence.FlushModeType;

/**
 * A {@link org.springframework.web.context.request.WebRequestInterceptor} instance that
 * handles opening a Datastore session within the scope of a request
 */
public class OpenSessionInViewInterceptor implements WebRequestInterceptor {

    private static final Log LOG = LogFactory.getLog(OpenSessionInViewInterceptor.class);

    Datastore datastore;
    FlushModeType flushMode = FlushModeType.AUTO;

    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public void preHandle(WebRequest webRequest) throws Exception {
        if(!hasSessionBound()) {
            // single session mode
            LOG.debug("Opening single Datastore Session in OpenSessionInViewInterceptor");

            Session session = DatastoreUtils.getSession(datastore, true);
            session.setFlushMode(flushMode);
            TransactionSynchronizationManager.bindResource(getDatastore(), new SessionHolder(session));            
        }
    }

    public void postHandle(WebRequest webRequest, ModelMap modelMap) throws Exception {
		// Only potentially flush in single session mode.
        if(hasSessionBound()) {

            SessionHolder sessionHolder =
                    (SessionHolder) TransactionSynchronizationManager.getResource(getDatastore());
            LOG.debug("Flushing single Datastore Session in OpenSessionInViewInterceptor");
            final Session session = sessionHolder.getSession();

            if(session.getFlushMode() == FlushModeType.AUTO) {
                session.flush();
            }
        }
    }

    private boolean hasSessionBound() {
        return TransactionSynchronizationManager.hasResource(getDatastore());
    }

    public void afterCompletion(WebRequest webRequest, Exception e) throws Exception {
        if(hasSessionBound()) {
            // single session mode
            SessionHolder sessionHolder =
                    (SessionHolder) TransactionSynchronizationManager.unbindResource(getDatastore());
            LOG.debug("Closing single Datastore Session in OpenSessionInViewInterceptor");
            DatastoreUtils.closeSession(sessionHolder.getSession());

        }
    }
}
