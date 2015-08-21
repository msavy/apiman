package io.apiman.gateway.engine.components.http;

import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.io.ISignalWriteStream;

public interface IHttpServerResponse extends ISignalWriteStream {
    void setHead(ServiceResponse response);
}
