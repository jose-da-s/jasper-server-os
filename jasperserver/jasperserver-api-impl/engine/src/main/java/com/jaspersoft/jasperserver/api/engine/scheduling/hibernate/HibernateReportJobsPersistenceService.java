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
package com.jaspersoft.jasperserver.api.engine.scheduling.hibernate;

import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import jakarta.annotation.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.common.user.UserPersistenceHandler;
import com.jaspersoft.jasperserver.api.engine.scheduling.ReportJobValidator;
import com.jaspersoft.jasperserver.api.engine.scheduling.ReportJobsInternalService;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobAlert;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobCalendarTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobIdHolder;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobMailNotification;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobRepositoryDestination;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSimpleTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSummary;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.FTPInfoModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobAlertModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobCalendarTriggerModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobMailNotificationModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobRepositoryDestinationModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobSimpleTriggerModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobSourceModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.DuplicateOutputLocationException;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportJobNotFoundException;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportJobRuntimeInfoException;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportJobsPersistenceService;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.TriggerTypeMismatchException;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.HibernateDaoImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.HibernateRepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoUser;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeService;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import jakarta.annotation.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class HibernateReportJobsPersistenceService extends HibernateDaoImpl
	implements ReportJobsPersistenceService, ReportJobsInternalService, ApplicationContextAware {
	
	protected static final Log log = LogFactory.getLog(HibernateReportJobsPersistenceService.class);

	private UserPersistenceHandler userHandler;

	private String referenceResolverBean;

	private HibernateRepositoryService referenceResolver;

    private ProfileAttributeService profileAttributeService;

	private ApplicationContext appContext;

    @Resource(name = "${bean.reportJobValidator}")
    private ReportJobValidator validator;

	public HibernateReportJobsPersistenceService() {
		super();
	}
	
	public UserPersistenceHandler getUserHandler() {
		return userHandler;
	}

	public void setUserHandler(UserPersistenceHandler userHandler) {
		this.userHandler = userHandler;
	}

    public ProfileAttributeService getProfileAttributeService() {
        return profileAttributeService;
    }

    public void setProfileAttributeService(ProfileAttributeService profileAttributeService) {
        this.profileAttributeService = profileAttributeService;
    }

	public String getReferenceResolverBean() {
		return referenceResolverBean;
	}

	public void setReferenceResolverBean(String referenceResolverBean) {
		this.referenceResolverBean = referenceResolverBean;
	}

    /*
     * lazy lookup of repository bean
     */
    public HibernateRepositoryService getReferenceResolver() {
    	if (referenceResolver == null && appContext != null) {
    		referenceResolver = (HibernateRepositoryService) appContext.getBean(referenceResolverBean);
    	}
		return referenceResolver;
	}

    public void setReferenceResolver(HibernateRepositoryService referenceResolver) {
		this.referenceResolver = referenceResolver;
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public ReportJob saveJob(ExecutionContext context, final ReportJob job) {
		return saveJob(context, job, true);
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public ReportJob saveJob(final ExecutionContext context, final ReportJob job, final boolean setContextUsername) {
		return (ReportJob) executeWriteCallback(new DaoCallback() {

			public Object execute() {
				HibernateTemplate hibernateTemplate = getHibernateTemplate();
				
				PersistentReportJob persistentJob = new PersistentReportJob();
				RepoUser owner;
				if (setContextUsername) {
					owner = userHandler.getPersistentUserFromContext();
				} else {
					owner = userHandler.getPersistentUserFromUsername(job.getUsername());
				}
				persistentJob.setOwner(owner);
                ArrayList unusedEntities = new ArrayList();
				persistentJob.copyFrom(job, hibernateTemplate, unusedEntities, getProfileAttributeService(), getReferenceResolver(), context);
				persistentJob.cascadeSave(hibernateTemplate);
				hibernateTemplate.save(persistentJob);
                hibernateTemplate.deleteAll(unusedEntities);
				
				hibernateTemplate.flush();//force job id generation
				ReportJob clientJob = toClient(persistentJob, context);
				
				if (log.isDebugEnabled()) {
					log.debug("Saved report job " + clientJob.getId() + " for report " + clientJob.getSource().getReportUnitURI());
				}
				
				return clientJob;
			}
		});
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public ReportJob updateJob(final ExecutionContext context, final ReportJob job) {
		return (ReportJob) executeWriteCallback(new DaoCallback() {
			public synchronized Object execute() {
				HibernateTemplate hibernateTemplate = getHibernateTemplate();
				PersistentReportJob persistentJob = findJob(job.getId(), true);
                ArrayList unusedEntities = new ArrayList();
				persistentJob.copyFrom(job, hibernateTemplate, unusedEntities, getProfileAttributeService(), getReferenceResolver(), context);

				persistentJob.cascadeSave(hibernateTemplate);
				hibernateTemplate.update(persistentJob);
                hibernateTemplate.deleteAll(unusedEntities);
				hibernateTemplate.flush();//force version updates

				ReportJob clientJob = toClient(persistentJob, context);
				
				if (log.isDebugEnabled()) {
					log.debug("Updated report job " + clientJob.getId());
				}
				
				return clientJob;
			}
		});
	}

	// update report jobs with a report job model
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public List<ReportJobIdHolder> updateJobsByID(final ExecutionContext context, final List<ReportJobIdHolder> reportJobHolders, final ReportJobModel jobModel, final boolean replaceTriggerIgnoreType)
            throws TriggerTypeMismatchException, ReportJobNotFoundException, DuplicateOutputLocationException {
        return (List<ReportJobIdHolder>) executeWriteCallback(new DaoCallback() {
            public Object execute() {
                if ((jobModel == null) || (reportJobHolders == null) || (reportJobHolders.size() == 0)) return new ArrayList<ReportJob>();
                List persistentJobs = getJobsByID(reportJobHolders);
                try {
                    foundInvalidID(reportJobHolders, persistentJobs);
                } catch (ReportJobNotFoundException ex) {
                    throw ex;
                }
                ArrayList<PersistentReportJob> resultList = new ArrayList<PersistentReportJob>();
                ArrayList<ReportJobIdHolder> idList = new ArrayList<ReportJobIdHolder>();
                try {
                    if ((jobModel.getTrigger() != null) && (!replaceTriggerIgnoreType)) verifyTriggerType(persistentJobs, jobModel.getTrigger());
                } catch (TriggerTypeMismatchException ex) {
                    throw ex;
                }
                try {
                    verifyOutputLocation(persistentJobs, jobModel, context);
                } catch (DuplicateOutputLocationException ex) {
                    throw ex;
                }
                HibernateTemplate hibernateTemplate = getHibernateTemplate();
                ArrayList newEntities = new ArrayList();
                ArrayList unusedEntities = new ArrayList();
                for (Object persistentJob : persistentJobs) {
                    PersistentReportJob result = updateJob((PersistentReportJob)persistentJob, jobModel, replaceTriggerIgnoreType, hibernateTemplate, newEntities, unusedEntities, context);
                    if (result != null) {
                        resultList.add(result);
                        idList.add(new ReportJobIdHolder(result.getId()));
                    }
                }
                for (Object entity : resultList) newEntities.add(entity);
                for (Iterator iterator = newEntities.iterator();iterator.hasNext();) {
                    hibernateTemplate.saveOrUpdate(iterator.next());
                }
                hibernateTemplate.deleteAll(unusedEntities);
                hibernateTemplate.flush();//force version updates
                 return idList;
            }
        });
    }

	// update report jobs with a report job model
    public List<ReportJob> updateJobs(final ExecutionContext context, final List<ReportJob> reportJobList, final ReportJobModel jobModel, final boolean replaceTriggerIgnoreType)
            throws TriggerTypeMismatchException, DuplicateOutputLocationException {
        return (List<ReportJob>) executeWriteCallback(new DaoCallback() {
            public Object execute() {
                if (jobModel == null) return new ArrayList<ReportJob>();
                List persistentJobs = getJobs(reportJobList);
                ArrayList<PersistentReportJob> resultList = new ArrayList<PersistentReportJob>();
                ArrayList<ReportJob> idList = new ArrayList<ReportJob>();
                try {
                    if ((jobModel.getTriggerModel() != null) && (!replaceTriggerIgnoreType)) verifyTriggerType(persistentJobs, jobModel.getTrigger());
                } catch (TriggerTypeMismatchException ex) {
                    throw ex;
                }
                try {
                    verifyOutputLocation(persistentJobs, jobModel, context);
                } catch (DuplicateOutputLocationException ex) {
                    throw ex;
                }
                HibernateTemplate hibernateTemplate = getHibernateTemplate();
                ArrayList newEntities = new ArrayList();
                ArrayList unusedEntities = new ArrayList();
                for (Object persistentJob : persistentJobs) {
                    PersistentReportJob result = updateJob((PersistentReportJob)persistentJob, jobModel, replaceTriggerIgnoreType, hibernateTemplate, newEntities, unusedEntities, context);
                    if (result != null) resultList.add(result);
                }
                for (Object entity : resultList) newEntities.add(entity);
                for (Iterator iterator = newEntities.iterator();iterator.hasNext();) {
                    hibernateTemplate.saveOrUpdate(iterator.next());
                }
                hibernateTemplate.deleteAll(unusedEntities);
                hibernateTemplate.flush();//force version updates
                for (PersistentReportJob result : resultList) {
                    idList.add(result.toClient(profileAttributeService, context));
                }
                return idList;
            }
        });
    }

    private void verifyOutputLocation(List persistentJobs, ReportJobModel jobModel, ExecutionContext context) throws DuplicateOutputLocationException {
        Boolean isSaveToRepository = null;
        if ((jobModel.getContentRepositoryDestinationModel() != null) && jobModel.getContentRepositoryDestinationModel().isSaveToRepositoryModified()) {
            isSaveToRepository = jobModel.getContentRepositoryDestinationModel().isSaveToRepository();
        }
        // nothing will get save to repository, skip verification
        if ((isSaveToRepository != null) && (!isSaveToRepository)) return;
        String baseOutputName = null;
        if ((jobModel.getContentRepositoryDestinationModel() != null) && jobModel.getContentRepositoryDestinationModel().isFolderURIModified()) {
            baseOutputName = jobModel.getBaseOutputFilename();
        }
        String folderURI = null;
        if ((jobModel.getContentRepositoryDestinationModel() != null) && jobModel.getContentRepositoryDestinationModel().isFolderURIModified()) {
            folderURI = jobModel.getContentRepositoryDestinationModel().getFolderURI();
        }
        Boolean isUsingDefaultFolderURI = null;
        if ((jobModel.getContentRepositoryDestinationModel() != null) && jobModel.getContentRepositoryDestinationModel().isUsingDefaultReportOutputFolderURIModified()) {
            isUsingDefaultFolderURI = jobModel.getContentRepositoryDestinationModel().isUsingDefaultReportOutputFolderURI();
        }
        String defaultFolderURI = null;
        if ((jobModel.getContentRepositoryDestinationModel() != null) && jobModel.getContentRepositoryDestinationModel().isDefaultReportOutputFolderURIModified()) {
            defaultFolderURI = jobModel.getContentRepositoryDestinationModel().getDefaultReportOutputFolderURI();
        }
        // return if not updating any fields
        if ((isSaveToRepository == null) && (folderURI == null) && (baseOutputName == null) && (isUsingDefaultFolderURI == null) && (defaultFolderURI == null)) return;
        HashSet<String> pathList = new HashSet<String>();
        for (Object persistentJob : persistentJobs) {
            ReportJob job = toClient(((PersistentReportJob) persistentJob), context);
             if (job.getContentRepositoryDestination() == null) continue;
            boolean isSaveToRepository_TMP = (isSaveToRepository != null? isSaveToRepository.booleanValue() : job.getContentRepositoryDestination().isSaveToRepository());
            // not save to repository
            if (!isSaveToRepository_TMP) continue;
            String baseOutputName_TMP = (baseOutputName != null? new String(baseOutputName) : job.getBaseOutputFilename());

            boolean isUsingDefaultFolderURI_TMP = (isUsingDefaultFolderURI != null? isUsingDefaultFolderURI.booleanValue() : job.getContentRepositoryDestination().isUsingDefaultReportOutputFolderURI());
            String folderURI_TMP;
            if (isUsingDefaultFolderURI_TMP) {
                folderURI_TMP = (defaultFolderURI != null? new String(defaultFolderURI) : job.getContentRepositoryDestination().getDefaultReportOutputFolderURI());
            } else {
                folderURI_TMP = (folderURI != null? new String(folderURI) : job.getContentRepositoryDestination().getFolderURI());
            }
            String path = folderURI_TMP + "/" + baseOutputName_TMP;
            if (pathList.contains(path)) {
               throw new DuplicateOutputLocationException(((PersistentReportJob)persistentJob).getId(), path);
            } else pathList.add(path);
        }
    }

    private void verifyTriggerType(List persistentJobs, ReportJobTrigger jobTrigger) throws TriggerTypeMismatchException {
        for (Object persistentJob : persistentJobs) {
                 if (!((PersistentReportJob)persistentJob).getTrigger().supports(jobTrigger.getClass())) {
                     throw new TriggerTypeMismatchException(((PersistentReportJob)persistentJob).getId(),
                             (jobTrigger instanceof ReportJobSimpleTriggerModel ? TriggerTypeMismatchException.TRIGGER_TYPE.SIMPLE_TRIGGER:
                             TriggerTypeMismatchException.TRIGGER_TYPE.CALENDAR_TRIGGER));
                 }
        }
    }

    private PersistentReportJob updateJob(PersistentReportJob persistentJob, ReportJobModel jobModel, boolean replaceTrigger,
            HibernateTemplate hibernateTemplate, List newEntities, List unusedEntities, ExecutionContext context) {
        persistentJob.copyFrom(jobModel, replaceTrigger, hibernateTemplate,  unusedEntities, getProfileAttributeService(), context);
        persistentJob.cascadeSave(newEntities);
        return persistentJob;
    }

    private void foundInvalidID(List<ReportJobIdHolder> expectedIDs, List actualIDs) throws ReportJobNotFoundException {
        if (actualIDs == null) throw new ReportJobNotFoundException(expectedIDs.get(0).getId());
        ArrayList<Long> foundIDList = new ArrayList<Long>();
        for (Object persistentJob : actualIDs) foundIDList.add(((PersistentReportJob)persistentJob).getId());
        for (ReportJobIdHolder expectedID : expectedIDs) {
            if (!foundIDList.contains(expectedID.getId())) throw new ReportJobNotFoundException(expectedID.getId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
	public ReportJob loadJob(final ExecutionContext context, ReportJobIdHolder jobIdHolder) {
		final long jobId = jobIdHolder.getId();
		return (ReportJob) executeCallback(new DaoCallback() {
			public Object execute() {
				PersistentReportJob persistentJob = findJob(jobId, false);
				ReportJob job;
				if (persistentJob == null) {
					job = null;
				} else {
					job = toClient(persistentJob, context);
				}
				return job;
			}
		});
	}

    @Transactional(propagation = Propagation.REQUIRED)
    public List<ReportJob> loadJobs(final ExecutionContext context, final List<ReportJobIdHolder> jobIDList) {
        return (List<ReportJob>) executeCallback(new DaoCallback() {
            public Object execute() {
                List persistentJobs = getJobsByID(jobIDList);
                if (persistentJobs == null) return null;
                List<ReportJob> newList = new ArrayList<ReportJob>();
                for (Object persistentJob: persistentJobs) {
                    newList.add(toClient((PersistentReportJob)persistentJob, context));
                }
                return newList;
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void deleteJob(ExecutionContext context, ReportJobIdHolder jobIdHolder) {
		deleteJob(jobIdHolder.getId());
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void deleteJob(final long jobId) {
		executeWriteCallback(new DaoCallback() {
			public Object execute() {
				PersistentReportJob job = findJob(jobId, false);
				if (job != null) {
					deleteJob(job);					
				} else {
					if (log.isInfoEnabled()) {
						log.info("Report job with id " + jobId + " not found for deletion");
					}
				}
				return null;
			}
		}, false);
	}

    @Transactional(propagation = Propagation.REQUIRED)
	public List listJobs(ExecutionContext context, final String reportUnitURI) {
		return (List) executeCallback(new DaoCallback() {
			public Object execute() {
				List persistentJobs = getReportUnitJobs(reportUnitURI);
                return toClientSummary(persistentJobs);
			}
		});
	}

	// list all the jobs that match the searching criterion
    public List<ReportJobSummary> listJobs(final ExecutionContext context, final ReportJobModel reportJobCriteria,
                                           final int startIndex, final int numberOfRows, final ReportJobModel.ReportJobSortType sortType,
                                           final boolean isAscending)
                                           throws ReportJobRuntimeInfoException {
        // require report scheduling service to display/ sort by runtime information
        if (sortType != null) {
            switch (sortType) {
                case SORTBY_STATUS:
                case SORTBY_LASTRUN:
                case SORTBY_NEXTRUN:
                    throw new ReportJobRuntimeInfoException(sortType);
            }
        }
        return (List) executeCallback(new DaoCallback() {
            public Object execute() {
                List persistentJobs = getReportUnitJobs(reportJobCriteria, context);
                List<ReportJobSummary> jobs = toClientSummary(persistentJobs);
                // apply sorting
                Comparator<ReportJobSummary> comparator = getComparator(context, sortType);
                if (comparator != null) {
                    if (!isAscending) comparator = Collections.reverseOrder(comparator);
                    Collections.sort(jobs, comparator);
                } else if (!isAscending) {
                    Collections.reverse(jobs);
                }
                // apply pagination
                int beginningIndex = 0;
                if (startIndex > 0) beginningIndex = startIndex;
                if ((beginningIndex == 0) && (numberOfRows == -1)) return jobs;
                List<ReportJobSummary> newList = new ArrayList<ReportJobSummary>();
                if (beginningIndex >= jobs.size()) return newList;
                int showRowCount = numberOfRows;
                if ((numberOfRows < 0) || (numberOfRows > (jobs.size() - startIndex))) showRowCount = jobs.size() - beginningIndex;
                for (int i = beginningIndex; i < (showRowCount + beginningIndex); i++) {
                    newList.add(jobs.get(i));
                }
                return newList;
            }
        });
    }


  @Transactional(propagation = Propagation.REQUIRED)
  public List listJobs(ExecutionContext context, final List<ReportJob> list) {
    return (List) executeCallback(new DaoCallback() {
      public Object execute() {
        List persistentJobs = getJobs(list);
        return toClientSummary(persistentJobs);
      }
    });
  }
  

    @Transactional(propagation = Propagation.REQUIRED)
	public List listJobs(ExecutionContext context) {
		return (List) executeCallback(new DaoCallback() {
			public Object execute() {
				List persistentJobs = getAllJobs();
                return toClientSummary(persistentJobs);
			}
		});
	}


  protected PersistentReportJob findJob(long jobId, boolean required) {
		PersistentReportJob job = getHibernateTemplate().get(PersistentReportJob.class, jobId);
		if (job == null && required) {
			throw new ReportJobNotFoundException(jobId);
		}
		return job;
	}

	public String getJobOwner(final long jobId) {
		return (String) executeCallback(new DaoCallback() {
            public Object execute() {
                PersistentReportJob persistentJob = findJob(jobId, true);
                return userHandler.getClientUsername(persistentJob.getOwner());
            }
        });
	}

    @Override
    public String getSourceUriByJobId(final long jobId) {
        return (String) executeCallback(new DaoCallback() {
            public Object execute() {
                PersistentReportJob persistentJob = findJob(jobId, true);
                return persistentJob.getSource().getReportUnitURI();
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long[] deleteReportUnitJobs(final String reportUnitURI) {
		return (long[]) executeWriteCallback(new DaoCallback() {
			public Object execute() {
				List jobs = getReportUnitJobs(reportUnitURI);
				return deletePersistentJobs(jobs);
			}
		}, false);
	}

	protected void deleteJob(PersistentReportJob job) {
		job.delete(getHibernateTemplate());
		
		if (log.isDebugEnabled()) {
			log.debug("Deleted job " + job.getId());
		}
	}

	protected List getReportUnitJobs(final String reportUnitURI) {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		DetachedCriteria crit = DetachedCriteria.forClass(PersistentReportJob.class);
		crit.add(Restrictions.eq("source.reportUnitURI", reportUnitURI));
        return hibernateTemplate.findByCriteria(crit);
	}

	// get all the jobs that match the searching criterion
    protected List getReportUnitJobs(final ReportJobModel criteriaReportJob, ExecutionContext context) {
        if (criteriaReportJob == null) return getAllJobs();
        boolean requireDetailSearch = false;
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		DetachedCriteria crit = DetachedCriteria.forClass(PersistentReportJob.class);
        // REPORT JOB (MAIN CLASS)
        if (criteriaReportJob.isBaseOutputFileNameModified()) addLikeRestriction(crit, "baseOutputFilename", criteriaReportJob.getBaseOutputFilename());
        if (criteriaReportJob.isCreationDateModified() && criteriaReportJob.getCreationDate() != null) requireDetailSearch = true;
        if (criteriaReportJob.isLabelModified()){
        	crit.createAlias("scheduledResource",  "scheduledResource");
        	addLikeRestriction(crit, new String[]{"label", "description", "scheduledResource.label"}, criteriaReportJob.getLabel());
        }
        if(criteriaReportJob.isJobIDModified())addEqualRestriction(crit, "id", criteriaReportJob.getId());
        if (criteriaReportJob.isDescriptionModified()) addLikeRestriction(crit, "description", criteriaReportJob.getDescription());
        if (criteriaReportJob.isJobLabelModified()) addLikeRestriction(crit, "label", criteriaReportJob.getJobLabel());
        if (criteriaReportJob.isResourceLabelModified()) {
            crit.createAlias("scheduledResource",  "scheduledResource");
            addLikeRestriction(crit, "scheduledResource.label", criteriaReportJob.getResourceLabel());
        }
        if (criteriaReportJob.isOutputLocaleModified()) addEqualRestriction(crit, "outputLocale", criteriaReportJob.getOutputLocale());
        // USER REPO
        final String username = criteriaReportJob.getUsername();
        if (criteriaReportJob.isUsernameModified() && username != null) {
            String[] nameTenant = username.split("\\|");
            crit.createAlias("owner", "owner");
            addEqualRestriction(crit, "owner.username", nameTenant[0]);
            crit.createAlias("owner.tenant", "tenant");
            addEqualRestriction(crit, "tenant.tenantId", nameTenant.length > 1 ? nameTenant[1] : TenantService.ORGANIZATIONS);
        }
        if (criteriaReportJob.isOutputFormatsModified() && criteriaReportJob.getOutputFormatsSet() != null) requireDetailSearch = true;
        // REPORT TRIGGER
        if (criteriaReportJob.isTriggerModified() && criteriaReportJob.getTriggerModel() != null) requireDetailSearch = true;
        // REPORT JOB SOURCE
        if (criteriaReportJob.isSourceModified() && criteriaReportJob.getSourceModel() != null) {
           ReportJobSourceModel des = criteriaReportJob.getSourceModel();
           if (des.isParametersMapModified()) addEqualRestriction(crit, "source.parametersMap", des.getParametersMap());
           if (des.isReportUnitURIModified()) addLikeRestriction(crit, "source.reportUnitURI", des.getReportUnitURI());
        }
        // REPORTJOBREPOSITORYDESTINATION
        if (criteriaReportJob.isContentRepositoryDestinationModified() && criteriaReportJob.getContentRepositoryDestinationModel() != null) {
            crit.createAlias("contentRepositoryDestination", "contentRepositoryDestination");
            ReportJobRepositoryDestinationModel des = criteriaReportJob.getContentRepositoryDestinationModel();
            if (des.isFolderURIModified()) addLikeRestriction(crit, "contentRepositoryDestination.folderURI", des.getFolderURI());
            if (des.isOutputDescriptionsModified()) addLikeRestriction(crit, "contentRepositoryDestination.outputDescription", des.getOutputDescription());
            if (des.isTimestampPatternModified()) addEqualRestriction(crit, "contentRepositoryDestination.timestampPattern", des.getTimestampPattern());
            if (des.isOverwriteFilesModified()) addEqualRestriction(crit, "contentRepositoryDestination.overwriteFiles", des.isOverwriteFiles());
            if (des.isSaveToRepositoryModified()) addEqualRestriction(crit, "contentRepositoryDestination.saveToRepository", des.isSaveToRepository());
            if (des.isSequentialFilenamesModified()) addEqualRestriction(crit, "contentRepositoryDestination.sequentialFilenames", des.isSequentialFilenames());
            if (des.isDefaultReportOutputFolderURIModified()) requireDetailSearch = true;
            if (des.isUsingDefaultReportOutputFolderURIModified()) {
                addEqualRestriction(crit, "contentRepositoryDestination.usingDefaultReportOutputFolderURI", des.isUsingDefaultReportOutputFolderURI());
            }
            if (des.isOutputLocalFolderModified()) addEqualRestriction(crit, "contentRepositoryDestination.outputLocalFolder", des.getOutputLocalFolder());

            // FTP INFO
            if (des.isOutputFTPInfoModified() && des.getOutputFTPInfoModel() != null) {
                FTPInfoModel ftpInfoModel = des.getOutputFTPInfoModel();
                if (ftpInfoModel.isUserNameModified()) addLikeRestriction(crit, "contentRepositoryDestination.outputFTPInfo.userName", ftpInfoModel.getUserName());
                if (ftpInfoModel.isPasswordModified()) addEqualRestriction(crit, "contentRepositoryDestination.outputFTPInfo.password", ftpInfoModel.getPassword());
                if (ftpInfoModel.isServerNameModified()) addLikeRestriction(crit, "contentRepositoryDestination.outputFTPInfo.server_name", ftpInfoModel.getUserName());
                if (ftpInfoModel.isFolderPathModified()) addLikeRestriction(crit, "contentRepositoryDestination.outputFTPInfo.folder_path", ftpInfoModel.getPassword());
                if (ftpInfoModel.isPropertiesMapModified()) addEqualRestriction(crit, "contentRepositoryDestination.outputFTPInfo.propertiesMap", ftpInfoModel.getPropertiesMap());
            }
        }
        // MAILNOTIFICATION
        if (criteriaReportJob.isMailNotificationModified() && criteriaReportJob.getMailNotificationModel()!= null) {
            crit.createAlias("mailNotification", "mailNotification");
            ReportJobMailNotificationModel des = criteriaReportJob.getMailNotificationModel();
            if (des.isSubjectModified()) addLikeRestriction(crit, "mailNotification.subject", des.getSubject());
            if (des.isMessageTextModified()) addLikeRestriction(crit, "mailNotification.messageText", des.getMessageText());
            if (des.isMessageTextWhenJobFailsModified()) addLikeRestriction(crit, "mailNotification.messageTextWhenJobFails", des.getMessageTextWhenJobFails());
            if (des.isIncludingStackTraceWhenJobFailsModified()) addEqualRestriction(crit, "mailNotification.includingStackTraceWhenJobFails", des.isIncludingStackTraceWhenJobFails());
            if (des.isSkipNotificationWhenJobFailsModified()) addEqualRestriction(crit, "mailNotification.skipNotificationWhenJobFails", des.isSkipNotificationWhenJobFails());
            if (des.isResultSendTypeModified()) addEqualRestriction(crit, "mailNotification.resultSendType", des.getResultSendTypeCode());
            if (des.isSkipEmptyReportsModified()) addEqualRestriction(crit, "mailNotification.skipEmptyReports", des.isSkipEmptyReports());
            if (((des.isToAddressesModified()) && (des.getToAddresses() != null) && (des.getToAddresses().size() > 0)) ||
                ((des.isCcAddressesModified()) && (des.getCcAddresses() != null) && (des.getCcAddresses().size() > 0)) ||
                ((des.isBccAddressesModified()) && (des.getBccAddresses() != null) && (des.getBccAddresses().size() > 0))) requireDetailSearch = true;
        }
        // ALERT
        if (criteriaReportJob.isAlertModified() && criteriaReportJob.getAlertModel()!= null) {
            crit.createAlias("alert", "alert");
            ReportJobAlertModel des = criteriaReportJob.getAlertModel();
            if (des.isMessageTextModified()) addLikeRestriction(crit, "alert.messageText", des.getMessageText());
            if (des.isMessageTextWhenJobFailsModified()) addLikeRestriction(crit, "alert.messageTextWhenJobFails", des.getMessageTextWhenJobFails());
            if (des.isSubjectModified()) addLikeRestriction(crit, "alert.subject", des.getSubject());
            if (des.isIncludingStackTraceModified()) addEqualRestriction(crit, "alert.includingStackTrace", des.isIncludingStackTrace());
            if (des.isIncludingReportJobInfoModified()) addEqualRestriction(crit, "alert.includingReportJobInfo", des.isIncludingReportJobInfo());
            if (des.isJobStateModified()) addEqualRestriction(crit, "alert.jobState", des.getJobState());
            if (des.isRecipientModified()) addEqualRestriction(crit, "alert.recipient", des.getRecipient());
            if (des.isToAddressesModified()) requireDetailSearch = true;
        }
		List persistentJobs = hibernateTemplate.findByCriteria(crit);
        if (requireDetailSearch && (persistentJobs != null)) {
            for (int i = persistentJobs.size()-1; i >= 0; i--) {
                if (!detailCriteriaMatch((PersistentReportJob)persistentJobs.get(i), criteriaReportJob, context)) {
                    persistentJobs.remove(i);
                }
            }
        }
		return persistentJobs;
	}

    private void addEqualRestriction(DetachedCriteria crit, String propertyName, Object value) {
        if (value != null) crit.add(Restrictions.eq(propertyName, value));
    }

    private void addLikeRestriction(DetachedCriteria crit, String propertyName, Object value) {
        if (value != null) {
            if (value instanceof String) {
                crit.add(Restrictions.ilike(propertyName, (String) value, MatchMode.ANYWHERE));
            } else {
                crit.add(Restrictions.ilike(propertyName, value));
            }
         }
    }

    private void addLikeRestriction(DetachedCriteria crit, String[] propertyName, Object value) {
        if (value != null && propertyName!=null && propertyName.length>0) {
        	Disjunction condition = Restrictions.disjunction();
        	for(String property: propertyName){
	            if (value instanceof String) {
	                condition.add(Restrictions.ilike(property, (String) value, MatchMode.ANYWHERE));
	            } else {
	                condition.add(Restrictions.ilike(property, value));
	            }
        	}
        	crit.add(condition);
        }
    }
    
    
    private boolean detailCriteriaMatch(PersistentReportJob reportJob, ReportJobModel jobModel, ExecutionContext context) {
        // report job output format
        if ((jobModel.isOutputFormatsModified() && jobModel.getOutputFormatsSet() != null) && (!jobModel.getOutputFormatsSet().isEmpty())) {
            if (reportJob.getOutputFormats() == null) return false;
            for (Byte outputFormat : jobModel.getOutputFormatsSet()) {
                if (!reportJob.getOutputFormats().contains(outputFormat)) return false;
            }
        }
        // report job creation date
        if (jobModel.isCreationDateModified() && jobModel.getCreationDate() != null) {
             if (!matchRestriction( jobModel.getCreationDate(), reportJob.getCreationDate())) return false;
        }
        // report trigger
        if (jobModel.isTriggerModified() && jobModel.getTriggerModel() != null) {
            if (reportJob.getTrigger() == null) return false;
            // report job calendar trigger
            if (jobModel.getTriggerModel() instanceof ReportJobCalendarTriggerModel) {
                ReportJobCalendarTriggerModel calendarTriggerModel = (ReportJobCalendarTriggerModel) jobModel.getTriggerModel();
                if (calendarTriggerModel.isModified()) {
                    ReportJobTrigger jobTrigger = reportJob.getTrigger().toClient();
                    if (!(jobTrigger instanceof ReportJobCalendarTrigger)) return false;
                    ReportJobCalendarTrigger calendarTrigger = (ReportJobCalendarTrigger) jobTrigger;
                    if (calendarTriggerModel.isStartDateModified() && !matchRestriction(calendarTriggerModel.getStartDate(), calendarTrigger.getStartDate())) return false;
                    if (calendarTriggerModel.isEndDateModified() && !matchRestriction(calendarTriggerModel.getEndDate(), calendarTrigger.getEndDate())) return false;
                    if (calendarTriggerModel.isStartTypeModified() && !matchRestriction(calendarTriggerModel.getStartType(), calendarTrigger.getStartType())) return false;
                    if (calendarTriggerModel.isTimezoneModified() && !matchRestriction(calendarTriggerModel.getTimezone(), calendarTrigger.getTimezone())) return false;
                    if (calendarTriggerModel.isCalendarNameModified() && !matchRestriction(calendarTriggerModel.getCalendarName(), calendarTrigger.getCalendarName())) return false;
                    if (calendarTriggerModel.isDaysTypeModified() && !matchRestriction(calendarTriggerModel.getDaysTypeCode(), calendarTrigger.getDaysTypeCode())) return false;
                    if (calendarTriggerModel.isHoursModified() && !matchRestriction(calendarTriggerModel.getHours(), calendarTrigger.getHours())) return false;
                    if (calendarTriggerModel.isMinutesModified() && !matchRestriction(calendarTriggerModel.getMinutes(), calendarTrigger.getMinutes())) return false;
                    if (calendarTriggerModel.isMonthDaysModified() && !matchRestriction(calendarTriggerModel.getMonthDays(), calendarTrigger.getMonthDays())) return false;
                    if (calendarTriggerModel.isMonthsModified() && (calendarTriggerModel.getMonths() != null) &&
                        (!matchRestriction(PersistentReportJobCalendarTrigger.toEnumerationString(calendarTriggerModel.getMonths()),
                                PersistentReportJobCalendarTrigger.toEnumerationString(calendarTrigger.getMonths())))) return false;
                    if (calendarTriggerModel.isWeekDaysModified() && !matchRestriction(calendarTriggerModel.getWeekDays(), calendarTrigger.getWeekDays())) return false;
                }
            }
            // report job simple trigger
            if (jobModel.getTriggerModel() instanceof ReportJobSimpleTriggerModel) {
                ReportJobSimpleTriggerModel simpleTriggerModel = (ReportJobSimpleTriggerModel) jobModel.getTriggerModel();
                if (simpleTriggerModel.isModified()) {
                     ReportJobTrigger jobTrigger = reportJob.getTrigger().toClient();
                    if (!(jobTrigger instanceof ReportJobSimpleTrigger)) return false;
                    ReportJobSimpleTrigger simpleTrigger = (ReportJobSimpleTrigger) jobTrigger;
                    if (simpleTriggerModel.isStartDateModified() && !matchRestriction(simpleTriggerModel.getStartDate(), simpleTrigger.getStartDate())) return false;
                    if (simpleTriggerModel.isEndDateModified() && !matchRestriction(simpleTriggerModel.getEndDate(), simpleTrigger.getEndDate())) return false;
                    if (simpleTriggerModel.isStartTypeModified() && !matchRestriction(simpleTriggerModel.getStartType(), simpleTrigger.getStartType())) return false;
                    if (simpleTriggerModel.isTimezoneModified() && !matchRestriction(simpleTriggerModel.getTimezone(), simpleTrigger.getTimezone())) return false;
                    if (simpleTriggerModel.isCalendarNameModified() && !matchRestriction(simpleTriggerModel.getCalendarName(), simpleTriggerModel.getCalendarName())) return false;
                    if (simpleTriggerModel.isOccurrenceCountModified() && !matchRestriction(simpleTriggerModel.getOccurrenceCount(), simpleTrigger.getOccurrenceCount())) return false;
                    if (simpleTriggerModel.isRecurrenceIntervalModified() && !matchRestriction(simpleTriggerModel.getRecurrenceInterval(), simpleTrigger.getRecurrenceInterval())) return false;
                    if (simpleTriggerModel.isRecurrenceIntervalUnitModified() && !matchRestriction(simpleTriggerModel.getRecurrenceIntervalUnit(), simpleTrigger.getRecurrenceIntervalUnit())) return false;
                }
            }
        }
        // REPORT JOB REPOSITORY DESTINATION
        if (jobModel.isContentRepositoryDestinationModified() && jobModel.getContentRepositoryDestinationModel() != null) {
            if (reportJob.getMailNotification() == null) return false;
            ReportJobRepositoryDestinationModel destinationModel = jobModel.getContentRepositoryDestinationModel();
            if (destinationModel.isDefaultReportOutputFolderURIModified() && destinationModel.getDefaultReportOutputFolderURI() != null) {
                ReportJobRepositoryDestination destination = reportJob.getContentRepositoryDestination().toClient(getProfileAttributeService(), reportJob.getOwner(), context);
                if (!matchRestriction(destinationModel.getDefaultReportOutputFolderURI(), destination.getDefaultReportOutputFolderURI())) return false;
            }
        }
        // mailNotification recipients
        if (jobModel.isMailNotificationModified() && jobModel.getMailNotificationModel() != null) {
            if (reportJob.getMailNotification() == null) return false;
            ReportJobMailNotification mailNotification = reportJob.getMailNotification().toClient();
            ReportJobMailNotificationModel mailNotificationModel = jobModel.getMailNotificationModel();
            if (mailNotificationModel.isToAddressesModified() && !matchRestrictionList(mailNotificationModel.getToAddresses(), mailNotification.getToAddresses())) return false;
            if (mailNotificationModel.isCcAddressesModified() && !matchRestrictionList(mailNotificationModel.getCcAddresses(), mailNotification.getCcAddresses())) return false;
            if (mailNotificationModel.isBccAddressesModified() && !matchRestrictionList(mailNotificationModel.getBccAddresses(), mailNotification.getBccAddresses())) return false;
        }
        // alert
        if (jobModel.isAlertModified() && jobModel.getAlertModel() != null) {
            if (reportJob.getAlert() == null) return false;
            ReportJobAlert alert = reportJob.getAlert().toClient();
            ReportJobAlertModel alertModel = jobModel.getAlertModel();
            if (alertModel.isToAddressesModified() && !matchRestrictionList(alertModel.getToAddresses(), alert.getToAddresses())) return false;
        }
        return true;

    }

    private boolean matchRestriction(Object model, Object value) {
        try {
            if (model == null) return true;
            if (model instanceof Date) {
                Calendar calendar1  = new GregorianCalendar();
                calendar1.setTime((Date)model);
                calendar1.set(Calendar.SECOND, 0);
                calendar1.set(Calendar.MILLISECOND, 0);
                Calendar calendar2  = new GregorianCalendar();
                calendar2.setTime((Date)value);
                calendar2.set(Calendar.SECOND, 0);
                calendar2.set(Calendar.MILLISECOND, 0);
                return (calendar1.compareTo(calendar2) == 0);
            }
            else if (model instanceof String) return  ((String) model).equalsIgnoreCase((String)value);
            else return model.equals(value);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean matchRestrictionList(List modelList, List valueList) {
        if ((modelList == null) || (modelList.size() == 0)) return true;
        if (valueList == null) return false;
        for (Object item : modelList) if (!valueList.contains(item)) return false;
        return true;
    }

	protected List getAllJobs() {
		return getHibernateTemplate().loadAll(PersistentReportJob.class);
	}

	// return persistent report jobs by ID
    protected List getJobsByID(List<ReportJobIdHolder> jobIDList)  {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		DetachedCriteria crit = DetachedCriteria.forClass(PersistentReportJob.class);
        Disjunction criterion = Restrictions.disjunction();
        for (ReportJobIdHolder idHolder: jobIDList) criterion.add(Restrictions.eq("id", idHolder.getId()));
        crit.add(criterion);
        List persistentJobs = hibernateTemplate.findByCriteria(crit);
		return persistentJobs;
	}



    // return persistent report jobs by report job list
    protected List getJobs(List<ReportJob> jobIDList)  {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		DetachedCriteria crit = DetachedCriteria.forClass(PersistentReportJob.class);
        Disjunction criterion = Restrictions.disjunction();
        for (ReportJob idHolder: jobIDList) criterion.add(Restrictions.eq("id", idHolder.getId()));
        crit.add(criterion);
        List persistentJobs = hibernateTemplate.findByCriteria(crit);
		return persistentJobs;
	}

	public List<ReportJobSummary> toClientSummary(List persistentJobs) {
		List<ReportJobSummary> jobs = new ArrayList(persistentJobs.size());
		for (Iterator it = persistentJobs.iterator(); it.hasNext();) {
			PersistentReportJob persistentJob = (PersistentReportJob) it.next();
			jobs.add(toClientSummary(persistentJob));
		}
		return jobs;
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long[] deletePersistentJobs(List jobs) {
		long[] jobIds;
		if (jobs == null || jobs.isEmpty()) {
			jobIds = null;
		} else {
			jobIds = new long[jobs.size()];
			int c = 0;
			for (Iterator it = jobs.iterator(); it.hasNext(); ++c) {
				PersistentReportJob job = (PersistentReportJob) it.next();
				jobIds[c] = job.getId();
				deleteJob(job);
			}
		}
		return jobIds;
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long[] updateReportUnitURI(final String oldURI, final String newURI) {
		return (long[]) executeWriteCallback(new DaoCallback() {
			public Object execute() {
				List jobs = getReportUnitJobs(oldURI);
				return updateReportURI(jobs, newURI);
			}
		}, false);
	}

	protected Object updateReportURI(List jobs, final String newURI) {
		long[] jobIds;
		if (jobs == null || jobs.isEmpty()) {
			jobIds = null;
		} else {
			jobIds = new long[jobs.size()];
			int c = 0;
			for (Iterator it = jobs.iterator(); it.hasNext(); ++c) {
				PersistentReportJob job = (PersistentReportJob) it.next();
				jobIds[c] = job.getId();
				
				job.getSource().setReportUnitURI(newURI);
				getHibernateTemplate().update(job);
				
				if (log.isDebugEnabled()) {
					log.debug("Updated report URI of job " + job.getId() + " to " + newURI);
				}
			}
		}
		return jobIds;
	}

	protected ReportJob toClient(PersistentReportJob persistentJob, ExecutionContext context) {
		ReportJob clientJob = persistentJob.toClient(profileAttributeService, context);
		clientJob.setUsername(userHandler.getClientUsername(persistentJob.getOwner()));
		return clientJob;
	}

	protected ReportJobSummary toClientSummary(PersistentReportJob persistentJob) {
		ReportJobSummary clientSummary = persistentJob.toSummary();
		clientSummary.setUsername(userHandler.getClientUsername(persistentJob.getOwner()));
		return clientSummary;
	}





    private Comparator<ReportJobSummary> getComparator(final ExecutionContext context, final ReportJobModel.ReportJobSortType sortBy) {
        if ((sortBy == null) || (sortBy == ReportJobModel.ReportJobSortType.NONE)) return null;
        return new Comparator<ReportJobSummary>() {
            public int compare(ReportJobSummary o1, ReportJobSummary o2) {
                switch (sortBy) {
                    case SORTBY_JOBID:
                        return (int) (o1.getId() - o2.getId());
                    case SORTBY_JOBNAME:
                        return compareObject(o1.getLabel(), o2.getLabel());
                    case SORTBY_REPORTURI:
                        return compareObject(o1.getReportUnitURI(), o2.getReportUnitURI());
                    case SORTBY_RESOURCELABEL:
                        return compareObject(o1.getReportLabel(), o2.getReportLabel());
                    case SORTBY_REPORTNAME:
                    case SORTBY_REPORTFOLDER:
                       return compareURI(
                               (o1.getReportUnitURI() != null ? o1.getReportUnitURI() : null),
                               (o2.getReportUnitURI() != null ? o2.getReportUnitURI() : null), sortBy);
                    case SORTBY_OWNER:
                        return compareObject(o1.getUsername(), o2.getUsername());
                    default:
                        return 0;
                }
            }
        };
    }

    private static Integer checkForNull(Object str1, Object str2) {
        if ((str1 == null) && (str2 != null)) return -1;
        if ((str1 != null) && (str2 == null)) return 1;
        if ((str1 == null) && (str2 == null)) return 0;
        return null;
    }

    private String getName(String uri, ReportJobModel.ReportJobSortType sorttype) {
        String folder = uri.replace('\\', '/');
        folder = uri.substring(0, folder.lastIndexOf("/"));
        if (sorttype == ReportJobModel.ReportJobSortType.SORTBY_REPORTFOLDER) return folder;
        else return uri.substring(folder.length() + 1);
    }

    private int compareURI(String str1, String str2, ReportJobModel.ReportJobSortType sorttype) {
        Integer isNull = checkForNull(str1,  str2);
        if (isNull != null) return isNull;
        return compareObject(getName(str1, sorttype), getName(str2, sorttype));
    }

    public static int compareObject(Object str1, Object str2) {
        Integer isNull = checkForNull(str1,  str2);
        if (isNull != null) return isNull;
        if (str1 instanceof String) return (((String)str1).toUpperCase()).compareTo(((String)str2).toUpperCase());
        if (str1 instanceof Date) return ((Date)str1).compareTo((Date)str2);
        return 0;
    }


  @Override
  public void setApplicationContext(ApplicationContext appContext) throws BeansException {
          this.appContext = appContext;
  }

}

