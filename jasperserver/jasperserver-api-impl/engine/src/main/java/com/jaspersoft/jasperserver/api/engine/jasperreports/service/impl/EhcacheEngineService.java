package com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl;


import java.io.Serializable;
import java.util.Set;

/**
 * Extracted from {@link EhcacheEngineServiceImpl }
 * Not only for EhCache. Didn't change name to keep changes under control.
 */
public interface EhcacheEngineService {
    final static String IC_REFRESH_KEY = "com.jaspersoft.cascade.refreshIC";
    final static String IC_CACHE_KEY = "com.jaspersoft.cascade.ICcacheKey";

    static final String DIAGNOSTIC_REPORT_URI = "diagnosticReportURI";
    static final String DIAGNOSTIC_STATE = "diagnosticState";

    static enum DiagnosticItemType {
        DATA_CACHE,
        INPUT_CONTROL_CACHE
    }
    Set<Serializable> getDiagnosticKeys(String uri, DiagnosticItemType type);
    Serializable getFromDiagnosticCache(Serializable key);
    void removeFromDiagnosticCache(Serializable key);
    void removeDiagnosticKeys(String uri);
    void putToDiagnocsticCache(Serializable key, Object value);
    void saveKeyToDiagnosticCache(String uri, Serializable key, DiagnosticItemType type);
    void clear();
}
