package com.rsmart.certification.api;

import com.rsmart.certification.api.criteria.CriterionCreationException;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.api.ReportRow;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This service manages the creation, update, and retrieval of CertificateDefinitions as well as the award of
 * certificates to users.
 *
 * The CertificateService depends on two other services to complete its work:
 *
 *  ConditionService is an implementation of an interface from the org.sakaiproject.condition.api from the
 *  sakai-kernel-api and is used to establish and check conditions for certificate award.
 *
 *  DocumentTemplateService is used to manage DocumentTemplate objects and to use those objects to render
 *  printable certificates.
 *
 * User: duffy
 * Date: Jun 7, 2011
 * Time: 4:40:50 PM
 */
public interface CertificateService
{
	/**
	 * Inserts the specified certificate definition into the database as UNPUBLISHED
	 * @param cd
	 * @return
	 * @throws IdUsedException
	 */
    public CertificateDefinition createCertificateDefinition (CertificateDefinition cd) throws IdUsedException;

    /**
     * Updates the specified certification definition in the database
     * @param cd
     * @return
     * @throws IdUnusedException
     */
    public CertificateDefinition updateCertificateDefinition (CertificateDefinition cd) throws IdUnusedException;
    
    public void setDocumentTemplateService (DocumentTemplateService dts);

    public DocumentTemplateService getDocumentTemplateService();

    public ContentHostingService getContentHostingService();
    
    /**
     * @author bbailla2
     * 
     * For i18n
     * @param key
     * @return internationalised string for the given key
     */
    public String getString(String key);
    
    /**
     * @author bbailla2
     * 
     * For i18n
     * @param key
     * @param values
     * @return internationalised string for the given key and the substituted values
     */
    public String getFormattedMessage(String key, Object[] values);

    /**
     * Deletes the certificate definition as well as their associated document templates
     * OWLTODO: make this also delete the criteria and the field values 
     * 
     * @param certificateDefinitionId
     * @throws IdUnusedException
     * @throws DocumentTemplateException
     */
    public void deleteCertificateDefinition (String certificateDefinitionId)
        throws IdUnusedException, DocumentTemplateException;

    /**
     * Creates a new certificate definition
     * @param name the name of the certificate
     * @param description a description of the certificate
     * @param siteId the containing site
     * @param progressHidden specifies whether site members can view their progress towards earning this certificate
     * @param fileName the filename associated with the template file
     * @param mimeType the mimetype for the template file
     * @param template an input stream containing the contents of the template file
     * @return
     * @throws IdUsedException
     * @throws UnsupportedTemplateTypeException
     * @throws DocumentTemplateException
     */
    public CertificateDefinition createCertificateDefinition (String name, String description, String siteId,
                                                              Boolean progressHidden, String fileName, 
                                                              String mimeType, InputStream template)
        throws IdUsedException, UnsupportedTemplateTypeException, DocumentTemplateException;
    
    /**
     * Populates the DocumentTemplate object for this CertificateDefinition.
     *
     * @param certificateDefinitionId
     * @param mimeType
     * @param template
     *
     * @return the new DocumentTemplate object
     */
    public DocumentTemplate setDocumentTemplate (String certificateDefinitionId, String name, String mimeType,
                                                 InputStream template)
        throws IdUnusedException, UnsupportedTemplateTypeException, DocumentTemplateException;

    /**
     * Populates the DocumentTemplate object for this CertificateDefinition. An attempt is made to determine the mime
     * type from the input stream.
     *
     * @param certificateDefinitionId
     * @param template
     *
     * @return the new DocumentTemplate object
     */
    public DocumentTemplate setDocumentTemplate (String certificateDefinitionId, String name, InputStream template)
        throws IdUnusedException, UnsupportedTemplateTypeException, DocumentTemplateException;

    /**
     * Gets inputstream for the DocumentTemplate set in content resource using the resourceId
     * @param resourceId
     * @return
     * @throws TemplateReadException
     */
    public InputStream getTemplateFileInputStream(String resourceId)
		throws TemplateReadException;
	
    /**
     * Sets the values to be used when populating the fields for the template during rendering of the printable
     * certificate.
     * 
     * @param certificateDefinitionId
     * @param fieldValues
     * @throws IdUnusedException
     */
    public void setFieldValues (String certificateDefinitionId, Map<String, String> fieldValues)
        throws IdUnusedException;

    /**
     * This sets the CertificateDefinitionStatus to ACTIVE or INACTIVE depending on the value of the boolean 'active'
     * parameter passed in. This method will validate whether the CertificateDefinition is complete before setting its
     * status. If the DocumentTemplate or AwardCriteria are null an IncompleteCertificateDefinitionException will be 
     * thrown.
     *
     * @param certificateDefinitionId
     * @param active
     *
     * throws IncompleteCertificateDefinitionException
     */
    public void activateCertificateDefinition (String certificateDefinitionId, boolean active)
            throws IncompleteCertificateDefinitionException, IdUnusedException;

    public CertificateDefinition getCertificateDefinitionByName (String siteId, String name)
        throws IdUnusedException;
    
    public CertificateDefinition getCertificateDefinition (String id)
        throws IdUnusedException;

    /**
     * @return All CertificateDefinition objects.
     */
    public Set<CertificateDefinition> getCertificateDefinitions ();

    /**
     * @param siteId
     * @return All CertificateDefinition objects for the given siteId.
     */
    public Set<CertificateDefinition> getCertificateDefinitionsForSite (String siteId);

    /**
     * @param siteId
     * @param statuses
     * @return All CertificateDefinition objects for the given siteId filtered by the supplied statuses.
     */
    public Set<CertificateDefinition> getCertificateDefinitionsForSite (String siteId, CertificateDefinitionStatus statuses[]);

    /**
     * This sets the AwardCriteria for the identified CertificateDefinition by using the supplied conditions. The
     * conditions Set can be populated with Conditions created by the ConditionService available from the
     * getConditionService() method.
     *
     * @param certificateDefinitionId
     * @param conditions
     * @return the new AwardCriteria object
     */
    public void setAwardCriteria (String certificateDefinitionId, Set<Criterion> conditions)
            throws IdUnusedException, UnmodifiableCertificateDefinitionException;

    public Criterion addAwardCriterion (String certificateDefinitionId, Criterion criterion)
            throws IdUnusedException, UnmodifiableCertificateDefinitionException;

    public void removeAwardCriterion (String certificateDefinitionId, String criterionId)
    		throws IdUnusedException, UnmodifiableCertificateDefinitionException;
    /**
     * This checks the current user's progress on AwardCriteria for a CertificateDefinition
     *
     * @param certificateDefinitionId
     * @return the Conditions which the current user has not met for the supplied CertificateDefinition ID.
     */
    public Set<Criterion> getUnmetAwardConditions (String certificateDefinitionId, boolean useCaching)
            throws IdUnusedException, UnknownCriterionTypeException;

    /**
     * This checks the identified user's progress on AwardCriteria for a CertificateDefinition
     *
     * @param certificateDefinitionId
     * @return the Conditions which the current user has not met for the supplied CertificateDefinition ID.
     */
    public Set<Criterion> getUnmetAwardConditionsForUser (String certificateDefinitionId, String userId, boolean useCaching)
            throws IdUnusedException, UnknownCriterionTypeException;

    /**
     * Returns a Map whose key values are variable names that can be used to fill in template fields. The values in the
     * map contain text describing what the variable will be populated by during rendering.
     *
     * @return
     */
    public Map<String, String> getPredefinedTemplateVariables ();

    /**
     * Registers a new CriteriaFactory which can then be used to create (and manage) new criteria
     * @param cFact
     */
    public void registerCriteriaFactory (CriteriaFactory cFact);

    public Set<CriteriaTemplate> getCriteriaTemplates();

    public CriteriaFactory getCriteriaFactory (String criteriaTemplateId);

    public int getCategoryType(final String gradebookId);
    
    public Map<Long, Double> getCategoryWeights(final String gradebookId);
    
	public Map<Long,Double> getAssignmentWeights(final String gradebookId);
	
	public Map<Long,Double> getAssignmentPoints(final String gradebookId);
	
	public Map<Long, Double> getCatOnlyAssignmentPoints(final String gradebookId);
	
	public Map<Long,Double> getAssignmentScores(final String gradebookId, final String studentId);
	
	public Map<Long,Date> getAssignmentDatesRecorded(final String gradebookId, final String studentId);
	
	/**
	 * @author bbailla2
	 * 
	 * Returns a list of map entries where each key is a requirement and each value is the user's progress towards 
	 * the requirement (both as human readable strings for the UI)
	 * @param certId the certificate definition id from which we are pulling the requirements
	 * @param userId the user whose progress we are checking
	 * @param siteId the certificate's containing siteId
	 * @return
	 */
	public List<Map.Entry<String, String>> getCertificateRequirementsForUser(String certId, String userId, String siteId, boolean useCaching) 
			throws IdUnusedException;
	
	/**
	 * @author bbailla2
	 * 
	 * Gets all users who have earned grades in the site - regardless of whether they are still enrolled 
	 * (for historical purposes)
	 * 
	 * @param siteId
	 * @return
	 */
	public Collection<String> getGradedUserIds(String siteId);

	public String getMimeType(byte[] toCheck) throws DocumentTemplateException;
	
	/**
	 * Gets the report rows for the users specified in userIds and the certificate definition specified by definition, 
	 * and filters by the filter type (all, awarded, unawarded), the filter date type (issue date, expiry date), 
	 * between the start date and the end date, and the progress of the criteria will be ordered according to orderedCriteria
	 **/
	public List<ReportRow> getReportRows(List<String> userIds, CertificateDefinition definition, String filterType, String filterDateType, Date startDate, Date endDate, List<Criterion> orderedCriteria);
}
