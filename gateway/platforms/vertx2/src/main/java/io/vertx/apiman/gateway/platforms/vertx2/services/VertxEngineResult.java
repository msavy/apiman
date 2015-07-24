package io.vertx.apiman.gateway.platforms.vertx2.services;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

@DataObject
public class VertxEngineResult {
    private VertxPolicyFailure policyFailure = null;

    public VertxEngineResult(VertxEngineResult copy) {
        this.policyFailure = copy.getPolicyFailure();
    }

    public VertxEngineResult(JsonObject json) {
        this(Json.decodeValue(json.toString(), VertxEngineResult.class));
    }

    // TODO hack until clean way of going POJO <-> JsonObject
    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
    }

    public VertxEngineResult() {
    }

    public VertxEngineResult(PolicyFailure policyFailure) {
        this.policyFailure = new VertxPolicyFailure(policyFailure);
    }

    public boolean isSuccess() {
        return policyFailure == null;
    }

    public void setPolicyFailure(VertxPolicyFailure policyFailure) {
        this.policyFailure = policyFailure;
    }

    public VertxPolicyFailure getPolicyFailure() {
        return policyFailure;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "VertxEngineResult [policyFailure=" + policyFailure + ", getClass()=" + getClass()
                + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
    }
}
