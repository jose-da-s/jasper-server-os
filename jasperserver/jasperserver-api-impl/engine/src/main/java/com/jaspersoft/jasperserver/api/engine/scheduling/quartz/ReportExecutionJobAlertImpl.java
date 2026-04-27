/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.api.engine.scheduling.quartz;

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobAlert;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * @author Ivan Chan (ichan@jaspersoft.com)
 * @version $Id$
 */
public class ReportExecutionJobAlertImpl implements ReportExecutionJobAlert {

    private static final Log log = LogFactory.getLog(ReportExecutionJobAlertImpl.class);

    public void sendAlertMail(Job job, ReportJob jobDetails, List<ErrorDescriptor> exceptions, JavaMailSender mailSender, String fromAddress,
                              String[] toAddresses, String characterEncoding) throws JobExecutionException {
        ReportJobAlert alert = jobDetails.getAlert();
        boolean isSucceed = exceptions.isEmpty();
        switch (alert.getJobState()) {
            case FAIL_ONLY:
                if (isSucceed) return;
                break;
            case SUCCESS_ONLY:
                if (!isSucceed) return;
                break;
            case NONE:
                return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, characterEncoding);
            messageHelper.setFrom(fromAddress);
            String subject = alert.getSubject();
            if ((subject == null) && (job instanceof ReportExecutionJob))
                subject = ((ReportExecutionJob) job).getMessage("report.scheduling.job.default.alert.subject", null);
            messageHelper.setSubject(subject);

            StringBuffer messageText = new StringBuffer();

            String text = (isSucceed ? alert.getMessageText() : alert.getMessageTextWhenJobFails());
            if (text != null) {
                messageText.append(text);
            }
            messageHelper.setTo(toAddresses);

            if (alert.isIncludingReportJobInfo()) {
                messageText.append("\n");
                messageText.append("Scheduled Job Info:").append("\n");
                messageText.append("Label = ").append(jobDetails.getLabel()).append("\n");
                messageText.append("ID = ").append(jobDetails.getId()).append("\n");
                messageText.append("Description = ").append(jobDetails.getDescription()).append("\n");
                messageText.append("Status = ").append(exceptions.isEmpty() ? "PASS" : "FAIL").append("\n");
            }

            if (alert.isIncludingStackTrace()) {
                for (ErrorDescriptor ed : exceptions) {
                    messageText.append("\n");
                    messageText.append(ed.getMessage());
                    attachException(messageHelper, ed);
                }
            }
            messageHelper.setText(messageText.toString());
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Error while sending report job alert notification", e);
            throw new JSExceptionWrapper(e);
        }
    }

    protected static void attachException(MimeMessageHelper messageHelper, ErrorDescriptor errorDescriptor) throws MessagingException {
        String[] stackTrace = errorDescriptor.getParameters();
        Throwable exception = errorDescriptor.getException();
        if (stackTrace == null || stackTrace.length == 0) {
            return;
        }

        ByteArrayOutputStream bufOut = new ByteArrayOutputStream();
        PrintStream printOut = new PrintStream(bufOut);
        for (String stack : stackTrace)
            printOut.append(stack);
        printOut.flush();

        String attachmentName = "exception_" + System.identityHashCode(exception) + ".txt";
        messageHelper.addAttachment(attachmentName, new ByteArrayResource(bufOut.toByteArray()));
    }
}
