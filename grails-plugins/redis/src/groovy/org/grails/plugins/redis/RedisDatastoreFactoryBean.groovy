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
package org.grails.plugins.redis

import org.springframework.beans.factory.FactoryBean
import org.springframework.datastore.redis.RedisDatastore
import org.springframework.datastore.mapping.MappingContext
import org.grails.datastore.gorm.redis.RedisGormEnhancer
import org.springframework.transaction.PlatformTransactionManager
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.springframework.datastore.reflect.ClassPropertyFetcher
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.gorm.GormInstanceApi
import org.grails.datastore.gorm.redis.RedisGormStaticApi
import org.grails.datastore.gorm.events.AutoTimestampInterceptor
import org.grails.datastore.gorm.events.DomainEventInterceptor

/**
 * Constructs a RedisDatastore instance
 *
 * @author Graeme Rocher
 * @since 1.0
 */
class RedisDatastoreFactoryBean implements FactoryBean<RedisDatastore>{

  Map<String, String> config
  MappingContext mappingContext
  GrailsPluginManager pluginManager

  RedisDatastore getObject() {
    def datastore = new RedisDatastore(mappingContext, config)
    datastore.addEntityInterceptor(new DomainEventInterceptor())
    datastore.addEntityInterceptor(new AutoTimestampInterceptor())

    return datastore
  }

  Class<?> getObjectType() { RedisDatastore }

  boolean isSingleton() { true }
}
class InstanceProxy {
    def instance
    def target
    def invokeMethod(String name, args) {
      target."$name"(instance, *args)
    }
}