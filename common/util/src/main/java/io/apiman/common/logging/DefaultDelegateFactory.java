package io.apiman.common.logging;

import io.apiman.common.logging.impl.SystemOutLogger;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultDelegateFactory implements IDelegateFactory {
    @Override
    public IApimanLogger createLogger(String name) {
        return new SystemOutLogger();
    }

    @Override
    public IApimanLogger createLogger(Class<?> klazz) {
        return new SystemOutLogger();
    }
}
