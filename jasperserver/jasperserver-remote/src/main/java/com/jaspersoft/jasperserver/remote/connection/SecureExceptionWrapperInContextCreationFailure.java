package com.jaspersoft.jasperserver.remote.connection;

import com.jaspersoft.jasperserver.api.common.error.handling.ExceptionOutputManager;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Arrays;
@Component
public class SecureExceptionWrapperInContextCreationFailure {

    @Resource
    private ExceptionOutputManager exceptionOutputManager;

    public ErrorDescriptor handleException (ErrorDescriptor errorDescriptor) {

        String[] values = errorDescriptor.getParameters()!=null ? Arrays.copyOf(errorDescriptor.getParameters(), 4):new String[4];
        if (!exceptionOutputManager.isExceptionMessageAllowed()) {
            errorDescriptor.setMessage(values[2]);
        }
        if(!exceptionOutputManager.isStackTraceAllowed()) {
            values[3] = null;
        }
        errorDescriptor.setParameters(values);
        return errorDescriptor;
    }
}