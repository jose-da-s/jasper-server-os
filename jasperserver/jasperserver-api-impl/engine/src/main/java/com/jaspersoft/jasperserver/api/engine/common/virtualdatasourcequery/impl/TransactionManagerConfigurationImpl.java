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

package com.jaspersoft.jasperserver.api.engine.common.virtualdatasourcequery.impl;

import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;
import com.jaspersoft.jasperserver.api.common.virtualdatasourcequery.teiid.TransactionManagerConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;

/**
 * User: ichan
 * To change this template use File | Settings | File Templates.
 */
public class TransactionManagerConfigurationImpl implements TransactionManagerConfiguration, InitializingBean {

    private static final Log log = LogFactory.getLog(TransactionManagerConfigurationImpl.class);

    private volatile String serverId = "TeiidEmbeddedServer-TransactionManager";
    private volatile String logPart1Filename = "transactionBtm1.tlog";
    private volatile String logPart2Filename = "transactionBtm2.tlog";
    private volatile boolean saveLogFilesToLogDirectory = false;
    private volatile boolean forcedWriteEnabled = true;
    private volatile boolean forceBatchingEnabled = true;
    private volatile int maxLogSizeInMb = 2;
    private volatile boolean filterLogStatus = false;
    private volatile boolean skipCorruptedLogs = true;
    private volatile boolean asynchronous2Pc = false;
    private volatile boolean warnAboutZeroResourceTransaction = true;
    private volatile boolean debugZeroResourceTransaction = false;
    private volatile int defaultTransactionTimeout = 60;
    private volatile int gracefulShutdownInterval = 60;
    private volatile int backgroundRecoveryIntervalSeconds = 60;
    private volatile boolean disableJmx = false;
    private volatile String jndiUserTransactionName = null;
    private volatile String jndiTransactionSynchronizationRegistryName = null;
    private volatile String journal = null;
    private volatile String exceptionAnalyzer = null;
    private volatile boolean currentNodeOnlyRecovery = true;
    private volatile boolean allowMultipleLrc = false;
    private volatile String resourceConfigurationFilename = null;

    private File logDirectory = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        setUpConfig();
    }

    /**
     * ASCII ID that must uniquely identify this TM instance. It must not exceed 51 characters or it will be truncated.
     * <p>Property name:<br/><b>bitronix.tm.serverId -</b> <i>(defaults to server's IP address but that's unsafe for
     * production use)</i></p>
     * @return the unique ID of this TM instance.
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Set the ASCII ID that must uniquely identify this TM instance. It must not exceed 51 characters or it will be
     * truncated.
     * @see #getServerId()
     * @param serverId the unique ID of this TM instance.
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Get the journal fragment file 1 name.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.logPart1Filename -</b> <i>(defaults to btm1.tlog)</i></p>
     * @return the journal fragment file 1 name.
     */
    public String getLogPart1Filename() {
        return logPart1Filename;
    }

    /**
     * Set the journal fragment file 1 name.
     * @see #getLogPart1Filename()
     * @param logPart1Filename the journal fragment file 1 name.
     */
    public void setLogPart1Filename(String logPart1Filename) {
        this.logPart1Filename = logPart1Filename;
    }

    /**
     * Get the journal fragment file 2 name.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.logPart2Filename -</b> <i>(defaults to btm2.tlog)</i></p>
     * @return the journal fragment file 2 name.
     */
    public String getLogPart2Filename() {
        return logPart2Filename;
    }

    /**
     * Set the journal fragment file 2 name.
     * @see #getLogPart2Filename()
     * @param logPart2Filename the journal fragment file 2 name.
     */
    public void setLogPart2Filename(String logPart2Filename) {
        this.logPart2Filename = logPart2Filename;
    }

    /**
     * return whether saving log files to default log directory
     * @return the state of saving log files to default log directory
     */
    public boolean isSaveLogFilesToLogDirectory() {
        return saveLogFilesToLogDirectory;
    }

    /**
     * set saving log files to default log directory
     * @param saveLogFilesToLogDirectory the state of saving log files to default log directory
     */
    public void setSaveLogFilesToLogDirectory(boolean saveLogFilesToLogDirectory) {
        this.saveLogFilesToLogDirectory = saveLogFilesToLogDirectory;
    }

    /**
     * Are logs forced to disk?  Do not set to false in production since without disk force, integrity is not
     * guaranteed.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.forcedWriteEnabled -</b> <i>(defaults to true)</i></p>
     * @return true if logs are forced to disk, false otherwise.
     */
    public boolean isForcedWriteEnabled() {
        return forcedWriteEnabled;
    }

    /**
     * Set if logs are forced to disk.  Do not set to false in production since without disk force, integrity is not
     * guaranteed.
     * @see #isForcedWriteEnabled()
     * @param forcedWriteEnabled true if logs should be forced to disk, false otherwise.
     */
    public void setForcedWriteEnabled(boolean forcedWriteEnabled) {
        this.forcedWriteEnabled = forcedWriteEnabled;
    }

    /**
     * Are disk forces batched? Disabling batching can seriously lower the transaction manager's throughput.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.forceBatchingEnabled -</b> <i>(defaults to true)</i></p>
     * @return true if disk forces are batched, false otherwise.
     */
    public boolean isForceBatchingEnabled() {
        return forceBatchingEnabled;
    }

    /**
     * Set if disk forces are batched. Disabling batching can seriously lower the transaction manager's throughput.
     * @see #isForceBatchingEnabled()
     * @param forceBatchingEnabled true if disk forces are batched, false otherwise.
     */
    public void setForceBatchingEnabled(boolean forceBatchingEnabled) {
        this.forceBatchingEnabled = forceBatchingEnabled;
    }

    /**
     * Maximum size in megabytes of the journal fragments. Larger logs allow transactions to stay longer in-doubt but
     * the TM pauses longer when a fragment is full.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.maxLogSize -</b> <i>(defaults to 2)</i></p>
     * @return the maximum size in megabytes of the journal fragments.
     */
    public int getMaxLogSizeInMb() {
        return maxLogSizeInMb;
    }

    /**
     * Set the Maximum size in megabytes of the journal fragments. Larger logs allow transactions to stay longer
     * in-doubt but the TM pauses longer when a fragment is full.
     * @see #getMaxLogSizeInMb()
     * @param maxLogSizeInMb the maximum size in megabytes of the journal fragments.
     */
    public void setMaxLogSizeInMb(int maxLogSizeInMb) {
        this.maxLogSizeInMb = maxLogSizeInMb;
    }

    /**
     * Should only mandatory logs be written? Enabling this parameter lowers space usage of the fragments but makes
     * debugging more complex.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.filterLogStatus -</b> <i>(defaults to false)</i></p>
     * @return true if only mandatory logs should be written.
     */
    public boolean isFilterLogStatus() {
        return filterLogStatus;
    }

    /**
     * Set if only mandatory logs should be written. Enabling this parameter lowers space usage of the fragments but
     * makes debugging more complex.
     * @see #isFilterLogStatus()
     * @param filterLogStatus true if only mandatory logs should be written.
     */
    public void setFilterLogStatus(boolean filterLogStatus) {
        this.filterLogStatus = filterLogStatus;
    }

    /**
     * Should corrupted logs be skipped?
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.skipCorruptedLogs -</b> <i>(defaults to false)</i></p>
     * @return true if corrupted logs should be skipped.
     */
    public boolean isSkipCorruptedLogs() {
        return skipCorruptedLogs;
    }

    /**
     * Set if corrupted logs should be skipped.
     * @see #isSkipCorruptedLogs()
     * @param skipCorruptedLogs true if corrupted logs should be skipped.
     */
    public void setSkipCorruptedLogs(boolean skipCorruptedLogs) {
        this.skipCorruptedLogs = skipCorruptedLogs;
    }

    /**
     * Should two phase commit be executed asynchronously? Asynchronous two phase commit can improve performance when
     * there are many resources enlisted in transactions but is more CPU intensive due to the dynamic thread spawning
     * requirements. It also makes debugging more complex.
     * <p>Property name:<br/><b>bitronix.tm.2pc.async -</b> <i>(defaults to false)</i></p>
     * @return true if two phase commit should be executed asynchronously.
     */
    public boolean isAsynchronous2Pc() {
        return asynchronous2Pc;
    }

    /**
     * Set if two phase commit should be executed asynchronously. Asynchronous two phase commit can improve performance
     * when there are many resources enlisted in transactions but is more CPU intensive due to the dynamic thread
     * spawning requirements. It also makes debugging more complex.
     * @see #isAsynchronous2Pc()
     * @param asynchronous2Pc true if two phase commit should be executed asynchronously.
     */
    public void setAsynchronous2Pc(boolean asynchronous2Pc) {
        this.asynchronous2Pc = asynchronous2Pc;
    }

    /**
     * Should transactions executed without a single enlisted resource result in a warning or not? Most of the time
     * transactions executed with no enlisted resource reflect a bug or a mis-configuration somewhere.
     * <p>Property name:<br/><b>bitronix.tm.2pc.warnAboutZeroResourceTransactions -</b> <i>(defaults to true)</i></p>
     * @return true if transactions executed without a single enlisted resource should result in a warning.
     */
    public boolean isWarnAboutZeroResourceTransaction() {
        return warnAboutZeroResourceTransaction;
    }

    /**
     * Set if transactions executed without a single enlisted resource should result in a warning or not. Most of the
     * time transactions executed with no enlisted resource reflect a bug or a mis-configuration somewhere.
     * @see #isWarnAboutZeroResourceTransaction()
     * @param warnAboutZeroResourceTransaction true if transactions executed without a single enlisted resource should
     *        result in a warning.
     */
    public void setWarnAboutZeroResourceTransaction(boolean warnAboutZeroResourceTransaction) {
        this.warnAboutZeroResourceTransaction = warnAboutZeroResourceTransaction;
    }

    /**
     * Should creation and commit call stacks of transactions executed without a single enlisted tracked and logged
     * or not?
     * <p>Property name:<br/><b>bitronix.tm.2pc.debugZeroResourceTransactions -</b> <i>(defaults to false)</i></p>
     * @return true if creation and commit call stacks of transactions executed without a single enlisted resource
     *         should be tracked and logged.
     */
    public boolean isDebugZeroResourceTransaction() {
        return debugZeroResourceTransaction;
    }

    /**
     * Set if creation and commit call stacks of transactions executed without a single enlisted resource should be
     * tracked and logged.
     * @see #isDebugZeroResourceTransaction()
     * @see #isWarnAboutZeroResourceTransaction()
     * @param debugZeroResourceTransaction true if the creation and commit call stacks of transaction executed without
     *        a single enlisted resource should be tracked and logged.
     * @return this.
     */
    public void setDebugZeroResourceTransaction(boolean debugZeroResourceTransaction) {
        this.debugZeroResourceTransaction = debugZeroResourceTransaction;
    }

    /**
     * Default transaction timeout in seconds.
     * <p>Property name:<br/><b>bitronix.tm.timer.defaultTransactionTimeout -</b> <i>(defaults to 60)</i></p>
     * @return the default transaction timeout in seconds.
     */
    public int getDefaultTransactionTimeout() {
        return defaultTransactionTimeout;
    }

    /**
     * Set the default transaction timeout in seconds.
     * @see #getDefaultTransactionTimeout()
     * @param defaultTransactionTimeout the default transaction timeout in seconds.
     */
    public void setDefaultTransactionTimeout(int defaultTransactionTimeout) {
        this.defaultTransactionTimeout = defaultTransactionTimeout;
    }

    /**
     * Maximum amount of seconds the TM will wait for transactions to get done before aborting them at shutdown time.
     * <p>Property name:<br/><b>bitronix.tm.timer.gracefulShutdownInterval -</b> <i>(defaults to 60)</i></p>
     * @return the maximum amount of time in seconds.
     */
    public int getGracefulShutdownInterval() {
        return gracefulShutdownInterval;
    }

    /**
     * Set the maximum amount of seconds the TM will wait for transactions to get done before aborting them at shutdown
     * time.
     * @see #getGracefulShutdownInterval()
     * @param gracefulShutdownInterval the maximum amount of time in seconds..
     */
    public void setGracefulShutdownInterval(int gracefulShutdownInterval) {
        this.gracefulShutdownInterval = gracefulShutdownInterval;
    }

    /**
     * Interval in seconds at which to run the recovery process in the background. Disabled when set to 0.
     * <p>Property name:<br/><b>bitronix.tm.timer.backgroundRecoveryIntervalSeconds -</b> <i>(defaults to 60)</i></p>
     * @return the interval in seconds.
     */
    public int getBackgroundRecoveryIntervalSeconds() {
        return backgroundRecoveryIntervalSeconds;
    }

    /**
     * Set the interval in seconds at which to run the recovery process in the background. Disabled when set to 0.
     * @see #getBackgroundRecoveryIntervalSeconds()
     * @param backgroundRecoveryIntervalSeconds the interval in minutes.
     */
    public void setBackgroundRecoveryIntervalSeconds(int backgroundRecoveryIntervalSeconds) {
        this.backgroundRecoveryIntervalSeconds = backgroundRecoveryIntervalSeconds;
    }

    /**
     * Should JMX Mbeans not be registered even if a JMX MBean server is detected?
     * <p>Property name:<br/><b>bitronix.tm.disableJmx -</b> <i>(defaults to false)</i></p>
     * @return true if JMX MBeans should never be registered.
     */
    public boolean isDisableJmx() {
        return disableJmx;
    }

    /**
     * Set to true if JMX Mbeans should not be registered even if a JMX MBean server is detected.
     * @see #isDisableJmx()
     * @param disableJmx true if JMX MBeans should never be registered.
     */
    public void setDisableJmx(boolean disableJmx) {
        this.disableJmx = disableJmx;
    }

    /**
     * Get the name the {@link jakarta.transaction.UserTransaction} should be bound under in the
     * {@link bitronix.tm.jndi.BitronixContext}.
     * @return the name the {@link jakarta.transaction.UserTransaction} should
     *         be bound under in the {@link bitronix.tm.jndi.BitronixContext}.
     */
    public String getJndiUserTransactionName() {
        return jndiUserTransactionName;
    }

    /**
     * Set the name the {@link jakarta.transaction.UserTransaction} should be bound under in the
     * {@link bitronix.tm.jndi.BitronixContext}.
     * @see #getJndiUserTransactionName()
     * @param jndiUserTransactionName the name the {@link jakarta.transaction.UserTransaction} should
     *        be bound under in the {@link bitronix.tm.jndi.BitronixContext}.
     */
    public void setJndiUserTransactionName(String jndiUserTransactionName) {
        this.jndiUserTransactionName = jndiUserTransactionName;
    }

    /**
     * Get the name the {@link jakarta.transaction.TransactionSynchronizationRegistry} should be bound under in the
     * {@link bitronix.tm.jndi.BitronixContext}.
     * @return the name the {@link jakarta.transaction.TransactionSynchronizationRegistry} should
     *         be bound under in the {@link bitronix.tm.jndi.BitronixContext}.
     */
    public String getJndiTransactionSynchronizationRegistryName() {
        return jndiTransactionSynchronizationRegistryName;
    }

    /**
     * Set the name the {@link jakarta.transaction.TransactionSynchronizationRegistry} should be bound under in the
     * {@link bitronix.tm.jndi.BitronixContext}.
     * @see #getJndiUserTransactionName()
     * @param jndiTransactionSynchronizationRegistryName the name the {@link jakarta.transaction.TransactionSynchronizationRegistry} should
     *        be bound under in the {@link bitronix.tm.jndi.BitronixContext}.
     */
    public void setJndiTransactionSynchronizationRegistryName(String jndiTransactionSynchronizationRegistryName) {
        this.jndiTransactionSynchronizationRegistryName = jndiTransactionSynchronizationRegistryName;
    }

    /**
     * Get the journal implementation. Can be <code>disk</code>, <code>null</code> or a class name.
     * @return the journal name.
     */
    public String getJournal() {
        return journal;
    }

    /**
     * Set the journal name. Can be <code>disk</code>, <code>null</code> or a class name.
     * @see #getJournal()
     * @param journal the journal name.
     */
    public void setJournal(String journal) {
        this.journal = journal;
    }

    /**
     * Get the exception analyzer implementation. Can be <code>null</code> for the default one or a class name.
     * @return the exception analyzer name.
     */
    public String getExceptionAnalyzer() {
        return exceptionAnalyzer;
    }

    /**
     * Set the exception analyzer implementation. Can be <code>null</code> for the default one or a class name.
     * @see #getExceptionAnalyzer()
     * @param exceptionAnalyzer the exception analyzer name.
     */
    public void setExceptionAnalyzer(String exceptionAnalyzer) {
        this.exceptionAnalyzer = exceptionAnalyzer;
    }

    /**
     * Should the recovery process <b>not</b> recover XIDs generated with another JVM unique ID? Setting this property to true
     * is useful in clustered environments where multiple instances of BTM are running on different nodes.
     * @see #getServerId() contains the value used as the JVM unique ID.
     * @return true if recovery should filter out recovered XIDs that do not contain this JVM's unique ID, false otherwise.
     */
    public boolean isCurrentNodeOnlyRecovery() {
        return currentNodeOnlyRecovery;
    }

    /**
     * Set to true if recovery should filter out recovered XIDs that do not contain this JVM's unique ID, false otherwise.
     * @see #isCurrentNodeOnlyRecovery()
     * @param currentNodeOnlyRecovery true if recovery should filter out recovered XIDs that do not contain this JVM's unique ID, false otherwise.
     */
    public void setCurrentNodeOnlyRecovery(boolean currentNodeOnlyRecovery) {
        this.currentNodeOnlyRecovery = currentNodeOnlyRecovery;
    }

    /**
     * Should the transaction manager allow enlistment of multiple LRC resources in a single transaction?
     * This is highly unsafe but could be useful for testing.
     * @return true if the transaction manager should allow enlistment of multiple LRC resources in a single transaction, false otherwise.
     */
    public boolean isAllowMultipleLrc() {
        return allowMultipleLrc;
    }

    /**
     * Set to true if the transaction manager should allow enlistment of multiple LRC resources in a single transaction.
     * @param allowMultipleLrc true if the transaction manager should allow enlistment of multiple LRC resources in a single transaction, false otherwise.
     */
    public void setAllowMultipleLrc(boolean allowMultipleLrc) {
        this.allowMultipleLrc = allowMultipleLrc;
    }

    /**
     * {@link bitronix.tm.resource.ResourceLoader} configuration file name. {@link bitronix.tm.resource.ResourceLoader}
     * will be disabled if this value is null.
     * <p>Property name:<br/><b>bitronix.tm.resource.configuration -</b> <i>(defaults to null)</i></p>
     * @return the filename of the resources configuration file or null if not configured.
     */
    public String getResourceConfigurationFilename() {
        return resourceConfigurationFilename;
    }

    /**
     * Set the {@link bitronix.tm.resource.ResourceLoader} configuration file name.
     * @see #getResourceConfigurationFilename()
     * @param resourceConfigurationFilename the filename of the resources configuration file or null you do not want to
     *        use the {@link bitronix.tm.resource.ResourceLoader}.
     */
    public void setResourceConfigurationFilename(String resourceConfigurationFilename) {
        this.resourceConfigurationFilename = resourceConfigurationFilename;
    }

    @Override
    public void setUpConfig() {
        if (getLogDirectory() == null) {
            if ("disk".equals(getJournal())) {
                log.warn("BTM transaction logs are disabled!");
            }
            setJournal("null");
        }

        Configuration tmConfig = TransactionManagerServices.getConfiguration();
        if (!TransactionManagerServices.isTransactionManagerRunning()) {
            if (getServerId() != null) tmConfig.setServerId(getServerId());
            if (getLogPart1Filename() != null) tmConfig.setLogPart1Filename(getLogPart1Filename());
            if (getLogPart2Filename() != null) tmConfig.setLogPart2Filename(getLogPart2Filename());

            tmConfig.setLogPart1Filename(getTransactionLogFileName(isSaveLogFilesToLogDirectory(), tmConfig.getLogPart1Filename()));
            tmConfig.setLogPart2Filename(getTransactionLogFileName(isSaveLogFilesToLogDirectory(), tmConfig.getLogPart2Filename()));
            log.debug("Transaction Manager Log Part 1 - " + tmConfig.getLogPart1Filename());
            log.debug("Transaction Manager Log Part 2 - " + tmConfig.getLogPart2Filename());

            tmConfig.setForcedWriteEnabled(isForcedWriteEnabled());
            tmConfig.setForceBatchingEnabled(isForceBatchingEnabled());
            tmConfig.setMaxLogSizeInMb(getMaxLogSizeInMb());
            tmConfig.setFilterLogStatus(isFilterLogStatus());
            tmConfig.setSkipCorruptedLogs(isSkipCorruptedLogs());
            tmConfig.setAsynchronous2Pc(isAsynchronous2Pc());
            tmConfig.setWarnAboutZeroResourceTransaction(isWarnAboutZeroResourceTransaction());
            tmConfig.setDebugZeroResourceTransaction(isDebugZeroResourceTransaction());
            tmConfig.setDefaultTransactionTimeout(getDefaultTransactionTimeout());
            tmConfig.setGracefulShutdownInterval(getGracefulShutdownInterval());
            tmConfig.setBackgroundRecoveryIntervalSeconds(getBackgroundRecoveryIntervalSeconds());
            tmConfig.setDisableJmx(isDisableJmx());
            if (getJndiUserTransactionName() != null)
                tmConfig.setJndiUserTransactionName(getJndiUserTransactionName());
            if (getJndiTransactionSynchronizationRegistryName() != null)
                tmConfig.setJndiTransactionSynchronizationRegistryName(getJndiTransactionSynchronizationRegistryName());
            if (getJournal() != null) tmConfig.setJournal(getJournal());
            if (getExceptionAnalyzer() != null) tmConfig.setExceptionAnalyzer(getExceptionAnalyzer());
            tmConfig.setCurrentNodeOnlyRecovery(isCurrentNodeOnlyRecovery());
            tmConfig.setAllowMultipleLrc(isAllowMultipleLrc());
            if (getResourceConfigurationFilename() != null)
                tmConfig.setResourceConfigurationFilename(getResourceConfigurationFilename());
        }
    }

    private String getTransactionLogFileName(boolean isSaveToLogDirectory, String logFileName) {
        File logDirectory = null;
        if (isSaveToLogDirectory) logDirectory = getLogDirectory();
        if (logDirectory != null) {
            File logFile = new File(logDirectory, logFileName);
            return logFile.getPath();
        }
        return logFileName;
    }

    private File getLogDirectory() {
        if (logDirectory != null) {
            return logDirectory;
        }
        try {
            org.apache.logging.log4j.core.Logger loggerImpl = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(TransactionManagerConfigurationImpl.class);
            Appender appender = loggerImpl.getAppenders().get("fileout");
            String fileName = ((RollingFileAppender) appender).getFileName();
            logDirectory = new File(fileName).getParentFile();
            return logDirectory;
        } catch (Exception ex) {
            log.debug("Fail to detect log directory!");
        }
        return null;
    }
}
