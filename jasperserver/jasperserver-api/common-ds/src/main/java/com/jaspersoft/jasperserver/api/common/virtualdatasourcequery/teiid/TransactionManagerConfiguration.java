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

package com.jaspersoft.jasperserver.api.common.virtualdatasourcequery.teiid;

/**
 * Created by IntelliJ IDEA.
 * User: ichan
 * Date: 5/7/14
 * Time: 1:35 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TransactionManagerConfiguration {

    /**
     * ASCII ID that must uniquely identify this TM instance. It must not exceed 51 characters or it will be truncated.
     * <p>Property name:<br/><b>bitronix.tm.serverId -</b> <i>(defaults to server's IP address but that's unsafe for
     * production use)</i></p>
     * @return the unique ID of this TM instance.
     */
    public String getServerId();

    /**
     * Get the journal fragment file 1 name.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.logPart1Filename -</b> <i>(defaults to btm1.tlog)</i></p>
     * @return the journal fragment file 1 name.
     */
    public String getLogPart1Filename();

    /**
     * Get the journal fragment file 2 name.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.logPart2Filename -</b> <i>(defaults to btm2.tlog)</i></p>
     * @return the journal fragment file 2 name.
     */
    public String getLogPart2Filename();

    /**
     * return whether saving log files to default log directory
     * @return the state of saving log files to default log directory
     */
    public boolean isSaveLogFilesToLogDirectory();

    /**
     * Are logs forced to disk?  Do not set to false in production since without disk force, integrity is not
     * guaranteed.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.forcedWriteEnabled -</b> <i>(defaults to true)</i></p>
     * @return true if logs are forced to disk, false otherwise.
     */
    public boolean isForcedWriteEnabled();

    /**
     * Are disk forces batched? Disabling batching can seriously lower the transaction manager's throughput.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.forceBatchingEnabled -</b> <i>(defaults to true)</i></p>
     * @return true if disk forces are batched, false otherwise.
     */
    public boolean isForceBatchingEnabled();

    /**
     * Maximum size in megabytes of the journal fragments. Larger logs allow transactions to stay longer in-doubt but
     * the TM pauses longer when a fragment is full.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.maxLogSize -</b> <i>(defaults to 2)</i></p>
     * @return the maximum size in megabytes of the journal fragments.
     */
    public int getMaxLogSizeInMb();

    /**
     * Should only mandatory logs be written? Enabling this parameter lowers space usage of the fragments but makes
     * debugging more complex.
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.filterLogStatus -</b> <i>(defaults to false)</i></p>
     * @return true if only mandatory logs should be written.
     */
    public boolean isFilterLogStatus();

    /**
     * Should corrupted logs be skipped?
     * <p>Property name:<br/><b>bitronix.tm.journal.disk.skipCorruptedLogs -</b> <i>(defaults to false)</i></p>
     * @return true if corrupted logs should be skipped.
     */
    public boolean isSkipCorruptedLogs();

    /**
     * Should two phase commit be executed asynchronously? Asynchronous two phase commit can improve performance when
     * there are many resources enlisted in transactions but is more CPU intensive due to the dynamic thread spawning
     * requirements. It also makes debugging more complex.
     * <p>Property name:<br/><b>bitronix.tm.2pc.async -</b> <i>(defaults to false)</i></p>
     * @return true if two phase commit should be executed asynchronously.
     */
    public boolean isAsynchronous2Pc();

    /**
     * Should transactions executed without a single enlisted resource result in a warning or not? Most of the time
     * transactions executed with no enlisted resource reflect a bug or a mis-configuration somewhere.
     * <p>Property name:<br/><b>bitronix.tm.2pc.warnAboutZeroResourceTransactions -</b> <i>(defaults to true)</i></p>
     * @return true if transactions executed without a single enlisted resource should result in a warning.
     */
    public boolean isWarnAboutZeroResourceTransaction();

    /**
     * Should creation and commit call stacks of transactions executed without a single enlisted tracked and logged
     * or not?
     * <p>Property name:<br/><b>bitronix.tm.2pc.debugZeroResourceTransactions -</b> <i>(defaults to false)</i></p>
     * @return true if creation and commit call stacks of transactions executed without a single enlisted resource
     *         should be tracked and logged.
     */
    public boolean isDebugZeroResourceTransaction();

    /**
     * Default transaction timeout in seconds.
     * <p>Property name:<br/><b>bitronix.tm.timer.defaultTransactionTimeout -</b> <i>(defaults to 60)</i></p>
     * @return the default transaction timeout in seconds.
     */
    public int getDefaultTransactionTimeout();

    /**
     * Maximum amount of seconds the TM will wait for transactions to get done before aborting them at shutdown time.
     * <p>Property name:<br/><b>bitronix.tm.timer.gracefulShutdownInterval -</b> <i>(defaults to 60)</i></p>
     * @return the maximum amount of time in seconds.
     */
    public int getGracefulShutdownInterval();

    /**
     * Interval in seconds at which to run the recovery process in the background. Disabled when set to 0.
     * <p>Property name:<br/><b>bitronix.tm.timer.backgroundRecoveryIntervalSeconds -</b> <i>(defaults to 60)</i></p>
     * @return the interval in seconds.
     */
    public int getBackgroundRecoveryIntervalSeconds();

    /**
     * Should JMX Mbeans not be registered even if a JMX MBean server is detected?
     * <p>Property name:<br/><b>bitronix.tm.disableJmx -</b> <i>(defaults to false)</i></p>
     * @return true if JMX MBeans should never be registered.
     */
    public boolean isDisableJmx();
    /**
     * Get the name the {@link jakarta.transaction.UserTransaction} should be bound under in the
     * {@link bitronix.tm.jndi.BitronixContext}.
     * @return the name the {@link jakarta.transaction.UserTransaction} should
     *         be bound under in the {@link bitronix.tm.jndi.BitronixContext}.
     */
    public String getJndiUserTransactionName();

    /**
     * Get the name the {@link jakarta.transaction.TransactionSynchronizationRegistry} should be bound under in the
     * {@link bitronix.tm.jndi.BitronixContext}.
     * @return the name the {@link jakarta.transaction.TransactionSynchronizationRegistry} should
     *         be bound under in the {@link bitronix.tm.jndi.BitronixContext}.
     */
    public String getJndiTransactionSynchronizationRegistryName();

    /**
     * Get the journal implementation. Can be <code>disk</code>, <code>null</code> or a class name.
     * @return the journal name.
     */
    public String getJournal();

    /**
     * Get the exception analyzer implementation. Can be <code>null</code> for the default one or a class name.
     * @return the exception analyzer name.
     */
    public String getExceptionAnalyzer();

    /**
     * Should the recovery process <b>not</b> recover XIDs generated with another JVM unique ID? Setting this property to true
     * is useful in clustered environments where multiple instances of BTM are running on different nodes.
     * @see #getServerId() contains the value used as the JVM unique ID.
     * @return true if recovery should filter out recovered XIDs that do not contain this JVM's unique ID, false otherwise.
     */
    public boolean isCurrentNodeOnlyRecovery();

    /**
     * Should the transaction manager allow enlistment of multiple LRC resources in a single transaction?
     * This is highly unsafe but could be useful for testing.
     * @return true if the transaction manager should allow enlistment of multiple LRC resources in a single transaction, false otherwise.
     */
    public boolean isAllowMultipleLrc();

    /**
     * {@link bitronix.tm.resource.ResourceLoader} configuration file name. {@link bitronix.tm.resource.ResourceLoader}
     * will be disabled if this value is null.
     * <p>Property name:<br/><b>bitronix.tm.resource.configuration -</b> <i>(defaults to null)</i></p>
     * @return the filename of the resources configuration file or null if not configured.
     */
    public String getResourceConfigurationFilename();

    void setUpConfig();

}
