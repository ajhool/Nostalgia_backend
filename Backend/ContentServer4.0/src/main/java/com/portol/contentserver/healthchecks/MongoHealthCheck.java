package com.portol.contentserver.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.portol.contentserver.manager.MongoManaged;

public class MongoHealthCheck extends HealthCheck {

        private MongoManaged mongo;

        public MongoHealthCheck(MongoManaged mongoManaged) {
            this.mongo = mongoManaged;
        }

        @Override
        protected Result check() throws Exception {
            mongo.getDB();
            return Result.healthy();
        }

}