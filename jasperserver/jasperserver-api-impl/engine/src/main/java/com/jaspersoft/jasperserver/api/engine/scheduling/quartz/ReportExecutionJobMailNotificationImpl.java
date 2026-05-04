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
import com.jaspersoft.jasperserver.api.engine.common.service.impl.ContentResourceURIResolver;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobMailNotification;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.MemoryDataContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.DataContainerResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.DataContainerStreamUtil;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Ivan Chan (ichan@jaspersoft.com)
 * @version $Id$
 */
public class ReportExecutionJobMailNotificationImpl implements ReportExecutionJobMailNotification {

    private static final Log log = LogFactory.getLog(ReportExecutionJobMailNotificationImpl.class);
    private static final String CHARSET_UTF_8 = "UTF-8";

    public void sendMailNotification(ReportExecutionJob job, ReportJob jobDetails, List reportOutputs) throws JobExecutionException {
    	ReportJobMailNotification mailNotification = jobDetails.getMailNotification();

		if (mailNotification != null) {
			try {
                // skip mail notification when job fails
                if (mailNotification.isSkipNotificationWhenJobFails() && (!job.exceptions.isEmpty())) {
                    return;
                }
                JavaMailSender mailSender = job.getMailSender();
	            String fromAddress = job.getFromAddress();
				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, job.getCharacterEncoding());
				messageHelper.setFrom(fromAddress);
				messageHelper.setSubject(mailNotification.getSubject());

				StringBuffer messageText = new StringBuffer();
				addMailRecipients(mailNotification, messageHelper);

                boolean isEmailBodyOccupied = false;

				if (reportOutputs != null && !reportOutputs.isEmpty()) {
                    byte resultSendType = jobDetails.getMailNotification().getResultSendType();

					if ((resultSendType == ReportJobMailNotification.RESULT_SEND_EMBED) || (resultSendType == ReportJobMailNotification.RESULT_SEND_EMBED_ZIP_ALL_OTHERS)) {
                        if (resultSendType == ReportJobMailNotification.RESULT_SEND_EMBED_ZIP_ALL_OTHERS) {
                            // put all the attachments in 1 zip file
                            attachOutputs(job, messageHelper, reportOutputs);
                        }
                        for (Iterator it = reportOutputs.iterator(); it.hasNext();) {
							ReportOutput output = (ReportOutput) it.next();
                            boolean isOutputFileTypeHTML = output.getFileType().equals(ContentResource.TYPE_HTML);
                            if (resultSendType == ReportJobMailNotification.RESULT_SEND_EMBED) {
                                if (!isOutputFileTypeHTML || output.getChildren().isEmpty()){
                                    // save the output as attachments
                                    attachOutput(job, messageHelper, output, false);
                                } else {
                                    // put the html output in 1 zip file if it includes images
                                    attachZippedOutput(job, messageHelper, output);
                                }
                            }
                            if ((!isEmailBodyOccupied) && isOutputFileTypeHTML && (job.exceptions.isEmpty())) {
                                // embed html output
                                embedOutput(messageHelper, output);
                                isEmailBodyOccupied = true;
                            }
						}
                    } else if (resultSendType == ReportJobMailNotification.RESULT_SEND_ATTACHMENT ||
                        resultSendType == ReportJobMailNotification.RESULT_SEND_ATTACHMENT_NOZIP) {
						for (Iterator it = reportOutputs.iterator(); it.hasNext();) {
							ReportOutput output = (ReportOutput) it.next();
							attachOutput(job, messageHelper, output, (resultSendType == ReportJobMailNotification.RESULT_SEND_ATTACHMENT));
						}
					} else if (resultSendType == ReportJobMailNotification.RESULT_SEND_ATTACHMENT_ZIP_ALL) {
                           // put all the attachments in 1 zip file
                            attachOutputs(job, messageHelper, reportOutputs);
                    } else {
						appendRepositoryLinks(job, messageText, reportOutputs);
					}
				}
                if (mailNotification.isIncludingStackTraceWhenJobFails()) {
					for (ErrorDescriptor ed : job.exceptions) {
						messageText.append("\n");
						messageText.append(ed.getMessage());

						final String errorUid = ed.getErrorUid();
						if (errorUid != null && !errorUid.isEmpty())
							messageText.append(" Error Uid: " + errorUid);

						attachException(messageHelper, ed);
                    }
                }
                String text = mailNotification.getMessageText();
                if (!job.exceptions.isEmpty()) {
                    if (mailNotification.getMessageTextWhenJobFails() != null) text = mailNotification.getMessageTextWhenJobFails();
                    else text = job.getMessage("report.scheduling.job.default.mail.notification.message.on.fail", null);
                }
				if (!isEmailBodyOccupied) messageHelper.setText(text + "\n" + messageText.toString());
				mailSender.send(message);
			} catch (MessagingException e) {
				log.error("Error while sending report job result mail", e);
				throw new JSExceptionWrapper(e);
			}
		}
	}


    protected void attachZippedOutput(ReportExecutionJob job, MimeMessageHelper messageHelper, ReportOutput output)
            throws MessagingException, JobExecutionException {
        String attachmentName;
        DataContainer attachmentData;

        attachmentName = output.getFilename();
        List<ReportOutput> outputs = new ArrayList<ReportOutput>();
        outputs.add(output);

        try {
            attachmentName = MimeUtility.encodeWord(attachmentName, job.getCharacterEncoding(), null);
        } catch (UnsupportedEncodingException e) {
            throw new JSExceptionWrapper(e);
        }

        for (Iterator it = output.getChildren().iterator(); it.hasNext();) {
            ReportOutput child = (ReportOutput) it.next();
            outputs.add(child);
        }

        attachmentData = job.createDataContainer();
        createZipOutput(new ZipOutputStream(attachmentData.getOutputStream()), outputs);
        attachmentName = output.getFilename() + ".zip";
        messageHelper.addAttachment(attachmentName, new DataContainerResource(attachmentData));
    }


    protected void createZipOutput(ZipOutputStream zipOut, List<ReportOutput> outputs) {
        if (null == outputs || null == zipOut) return;

        boolean close = true;
        try {
            zipOutput(outputs, zipOut);
            zipOut.finish();
            zipOut.flush();
            close = false;
            zipOut.close();
        } catch (IOException e) {
            throw new JSExceptionWrapper(e);
        } finally {
            if (close) {
                try {
                    zipOut.close();
                } catch (IOException e) {
                    log.error("Error closing zip output stream", e);
                }
            }
        }
    }


    protected void appendRepositoryLinks(ReportExecutionJob job, StringBuffer notificationMessage, List reportOutputs) {
		if (reportOutputs != null && !reportOutputs.isEmpty()) {
			String preamble = job.getMessage("report.scheduling.notification.repository.links.preamble", null);
			notificationMessage.append(preamble);
			String linkDescription = job.getRepositoryLinkDescription();
			ContentResourceURIResolver contentResourceURIResolver = job.getContentResourceURIResolver();
			for (Iterator it = reportOutputs.iterator(); it.hasNext();) {
				ReportOutput output = (ReportOutput) it.next();
				String resourcePath = output.getRepositoryPath();
				if (resourcePath != null) {
					String resourceURI = contentResourceURIResolver.resolveURI(resourcePath);
					String line = job.getMessage("report.scheduling.notification.repository.link.line",
                            new Object[]{linkDescription, resourceURI});
					notificationMessage.append(line);
				}
			}
		}
	}

    protected void embedOutput(MimeMessageHelper messageHelper, ReportOutput output) throws MessagingException, JobExecutionException {
        /***
         try {
         BufferedReader in = new BufferedReader(new FileReader("C:/Users/ichan/Desktop/employee.html"));
         StringBuffer strBuffer = new StringBuffer();
         String strLine;
         while ((strLine = in.readLine()) != null) {
         strBuffer = strBuffer.append(strLine + "\n");
         }
         content = strBuffer.toString();
         } catch (Exception ex) {
         ex.printStackTrace();
         }
         System.out.println("CONTENT = " + content);
         ****/

        // modify content to use inline images
        StringBuffer primaryPage = null;
        try {
            primaryPage = new StringBuffer(new String(output.getData().getData(), CHARSET_UTF_8));
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding", e);
        }
        for (Iterator it = output.getChildren().iterator(); it.hasNext();) {
            ReportOutput child = (ReportOutput) it.next();
            String childName = child.getFilename();

            // NOTE:  add the ".dat" extension to all image resources
            // email client will automatically append ".dat" extension to all the files with no extension
            // should do it in JasperReport side
            if (output.getFileType().equals(ContentResource.TYPE_HTML)) {
                if (primaryPage == null) try {
                    primaryPage = new StringBuffer(new String(output.getData().getData(), CHARSET_UTF_8));
                } catch (UnsupportedEncodingException e) {
                    log.error("Unsupported encoding", e);
                }
                int fromIndex = 0;
                boolean findChild = false;
                while ((fromIndex = primaryPage.indexOf("src=\""+ childName + "\"", fromIndex)) > 0) {
                    primaryPage.insert(fromIndex + 5, "cid:");
                    findChild = true;
                }
                // for zipped attachment & embed resources - with repo folder hierarchy, long child file name
                if (!findChild) cutFolderPathFromSrc(output.getFilename(), childName, primaryPage);
            }
        }

        messageHelper.setText(primaryPage.toString(), true);
        // add inline images after setting the text in the main body
        for (Iterator it = output.getChildren().iterator(); it.hasNext();) {
            ReportOutput child = (ReportOutput) it.next();
            String childName = child.getFilename();
            DataContainerResource dataContainerResource = new DataContainerResource(child.getData());
            dataContainerResource.setFilename(childName);
            messageHelper.addInline(childName, dataContainerResource);
        }
    }

    private void cutFolderPathFromSrc(String folderName, String childName, StringBuffer primaryPage) {
        int fromIndex = 0;
        final String HTML_SRC = "src=\"";
        final String HTML_CID = "cid:";
        final String FOLDER_SUFFIX = "_files";

        String pattern = folderName + FOLDER_SUFFIX + Folder.SEPARATOR;
        while ((fromIndex = primaryPage.indexOf(HTML_SRC + pattern + childName, fromIndex)) > 0) {
            int insertIndex = fromIndex + HTML_SRC.length();
            int endIndex = insertIndex + pattern.length();
            primaryPage.replace(insertIndex, endIndex, HTML_CID);
            fromIndex = insertIndex;
        }
    }

    protected void attachOutputs(ReportExecutionJob job, MimeMessageHelper messageHelper, List reportOutputs) throws MessagingException, JobExecutionException {
        String attachmentName = null;
        DataContainer attachmentData = job.createDataContainer();
        boolean close = true;
        ZipOutputStream zipOut = new ZipOutputStream(attachmentData.getOutputStream());
        try {
            for (Iterator it = reportOutputs.iterator(); it.hasNext();) {
                ReportOutput output = (ReportOutput) it.next();
                if (attachmentName == null) attachmentName = removeExtension(output.getFilename()) + ".zip";
                zipOutput(job, output, zipOut);
            }
            zipOut.finish();
            zipOut.flush();
            close = false;
            zipOut.close();
        } catch (IOException e) {
            throw new JSExceptionWrapper(e);
        } finally {
            if (close) {
                try {
                    zipOut.close();
                } catch (IOException e) {
                    log.error("Error closing stream", e);
                }
            }
        }
		try {
			attachmentName = MimeUtility.encodeWord(attachmentName, job.getCharacterEncoding(), null);
		} catch (UnsupportedEncodingException e) {
			throw new JSExceptionWrapper(e);
		}
		messageHelper.addAttachment(attachmentName, new DataContainerResource(attachmentData));
    }

    protected void zipOutput(ReportExecutionJob job, ReportOutput output, ZipOutputStream zipOut) throws IOException {
        zipOut.putNextEntry(new ZipEntry(output.getFilename()));
        DataContainerStreamUtil.pipeDataAndCloseInput(output.getData().getInputStream(), zipOut);
        zipOut.closeEntry();

        for (Iterator it = output.getChildren().iterator(); it.hasNext();) {
            ReportOutput child = (ReportOutput) it.next();
            String childName = getChildrenFolderName(job, output.getFilename()) + '/' + child.getFilename();
            zipOut.putNextEntry(new ZipEntry(childName));
            DataContainerStreamUtil.pipeDataAndCloseInput(child.getData().getInputStream(), zipOut);
            zipOut.closeEntry();
        }
    }

    protected void zipOutput(List<ReportOutput> outputs, ZipOutputStream zipOut) throws IOException {
        for (int i = 0; i < outputs.size(); i++) {
            ReportOutput output = outputs.get(i);

            zipOut.putNextEntry(new ZipEntry(output.getFilename()));
            DataContainerStreamUtil.pipeDataAndCloseInput(output.getData().getInputStream(), zipOut);
            zipOut.closeEntry();
        }
    }

    protected void attachOutput(ReportExecutionJob job, MimeMessageHelper messageHelper, ReportOutput output, boolean useZipFormat) throws MessagingException, JobExecutionException {
		String attachmentName;
		DataContainer attachmentData;
		if (output.getChildren().isEmpty()) {
			attachmentName = output.getFilename();
			attachmentData = output.getData();
		} else if (useZipFormat) {  // use zip format
			attachmentData = job.createDataContainer();
			boolean close = true;
			ZipOutputStream zipOut = new ZipOutputStream(attachmentData.getOutputStream());
			try {
                zipOutput(job, output, zipOut);
   				zipOut.finish();
				zipOut.flush();
				close = false;
				zipOut.close();
			} catch (IOException e) {
				throw new JSExceptionWrapper(e);
			} finally {
				if (close) {
					try {
						zipOut.close();
					} catch (IOException e) {
						log.error("Error closing stream", e);
					}
				}
			}

			attachmentName = output.getFilename() + ".zip";
		} else {         // NO ZIP FORMAT
            attachmentName = output.getFilename();
            try {
			    attachmentName = MimeUtility.encodeWord(attachmentName, job.getCharacterEncoding(), null);
		    } catch (UnsupportedEncodingException e) {
			    throw new JSExceptionWrapper(e);
		    }
            StringBuffer primaryPage = null;
            for (Iterator it = output.getChildren().iterator(); it.hasNext();) {
					ReportOutput child = (ReportOutput) it.next();
                    String childName = child.getFilename();

                    // NOTE:  add the ".dat" extension to all image resources
                    // email client will automatically append ".dat" extension to all the files with no extension
                    // should do it in JasperReport side
                    if (output.getFileType().equals(ContentResource.TYPE_HTML)) {
                        if (primaryPage == null) primaryPage = new StringBuffer(new String(output.getData().getData()));
                        int fromIndex = 0;
                        while ((fromIndex = primaryPage.indexOf("src=\""+ childName + "\"", fromIndex)) > 0) {
                            primaryPage.insert(fromIndex + 5 + childName.length(), ".dat");
                        }
                        childName = childName + ".dat";
                    }

                    try {
			            childName = MimeUtility.encodeWord(childName, job.getCharacterEncoding(), null);
		            } catch (UnsupportedEncodingException e) {
			            throw new JSExceptionWrapper(e);
		            }
		            messageHelper.addAttachment(childName, new DataContainerResource(child.getData()));
		    }
            if (primaryPage == null) {
                messageHelper.addAttachment(attachmentName, new DataContainerResource(output.getData()));
            } else {
                messageHelper.addAttachment(attachmentName, new DataContainerResource(new MemoryDataContainer(primaryPage.toString().getBytes())));
            }
            return;
        }
		try {
			attachmentName = MimeUtility.encodeWord(attachmentName, job.getCharacterEncoding(), null);
		} catch (UnsupportedEncodingException e) {
			throw new JSExceptionWrapper(e);
		}
		messageHelper.addAttachment(attachmentName, new DataContainerResource(attachmentData));
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

    protected String getChildrenFolderName(ReportExecutionJob job, String resourceName) {
		return job.getRepository().getChildrenFolderName(resourceName);
	}

    protected void addMailRecipients(ReportJobMailNotification mailNotification, MimeMessageHelper messageHelper) throws MessagingException {
		List toAddresses = mailNotification.getToAddresses();
		if (toAddresses != null && !toAddresses.isEmpty()) {
			String[] addressArray = new String[toAddresses.size()];
			toAddresses.toArray(addressArray);
			messageHelper.setTo(addressArray);
		}

		List ccAddresses = mailNotification.getCcAddresses();
		if (ccAddresses != null && !ccAddresses.isEmpty()) {
			String[] addressArray = new String[ccAddresses.size()];
			ccAddresses.toArray(addressArray);
			messageHelper.setCc(addressArray);
        }
		List bccAddresses = mailNotification.getBccAddresses();
		if (bccAddresses != null && !bccAddresses.isEmpty()) {
			String[] addressArray = new String[bccAddresses.size()];
			bccAddresses.toArray(addressArray);
			messageHelper.setBcc(addressArray);
		}
	}

    private String removeExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex > 0) return fileName.substring(0, lastIndex);
        else return fileName;
    }
}
