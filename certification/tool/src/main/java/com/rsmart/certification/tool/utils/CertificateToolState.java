/*
 * Copyright 2011 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): duffy
 */

package com.rsmart.certification.tool.utils;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rsmart.certification.api.BaseCertificateDefinition;
import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.DocumentTemplateService;

import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.api.VariableResolver;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.WillExpireCriterionHibernateImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.util.Set;

public class CertificateToolState
{
    private static final Log LOG = LogFactory.getLog(CertificateToolState.class);
    public static final String CERTIFICATE_TOOL_STATE = CertificateToolState.class.getName();
    private CertificateDefinition certificateDefinition = null;
    private String newDocumentTemplateName;
    private String submitValue;
    private String selectedCert;
    private String mimeTypes;
    private byte[] templateByteArray;
    private String templateFilename;
    private String templateMimeType;
    private CommonsMultipartFile newTemplate;
    private Set<CriteriaTemplate> criteriaTemplates;
    private Set<Criterion> awardCriteria;
    private CriteriaTemplate selectedCriteriaTemplate;
    private Map<String, String> templateFields = null;
    private Map<String, String> predifinedFields = null;
    private boolean newDefinition;
    
    public CriteriaTemplate getSelectedCriteriaTemplate()
    {
        return selectedCriteriaTemplate;
    }

    public void setSelectedCriteriaTemplate(CriteriaTemplate selectedCriteriaTemplate)
    {
        this.selectedCriteriaTemplate = selectedCriteriaTemplate;
    }
    
	public CertificateDefinition getCertificateDefinition() 
	{
		return certificateDefinition;
	}

	public void setCertificateDefinition(CertificateDefinition certificateDefinition) 
	{
		this.certificateDefinition = certificateDefinition;
	}

	public String getNewDocumentTemplateName() 
	{
		return newDocumentTemplateName;
	}

	public void setNewDocumentTemplateName(String newDocumentTemplateName) 
	{
		this.newDocumentTemplateName = newDocumentTemplateName;
	}

	public boolean isNewDefinition() 
	{
		return newDefinition;
	}

	public void setNewDefinition(boolean newDefinition) 
	{
		this.newDefinition = newDefinition;
	}

	public String getTemplateFilename() 
	{
		return templateFilename;
	}

	public void setTemplateFilename(String templateFilename)
	{
		this.templateFilename = templateFilename;
	}
	
	public byte[] getTemplateByteArray()
	{
		return templateByteArray;
	}
	
	public void setTemplateByteArray(byte[] templateByteArray)
	{
		this.templateByteArray = templateByteArray;
	}
	
	public InputStream getTemplateInputStream()
	{
		if (templateByteArray == null)
		{
			return null;
		}
		return new ByteArrayInputStream(templateByteArray);
	}
	
	public String getTemplateMimeType()
	{
		return templateMimeType;
	}
	
	public void setTemplateMimeType(String templateMimeType)
	{
		this.templateMimeType = templateMimeType;
	}
	
	public CommonsMultipartFile getNewTemplate()
	{
		return newTemplate;
	}
	
	public void setNewTemplate(CommonsMultipartFile newTemplate)
	{
		this.newTemplate = newTemplate;
	}

	public String getSubmitValue() 
	{
		return submitValue;
	}

	public void setSubmitValue(String submitValue) 
	{
		this.submitValue = submitValue;
	}

	public String getSelectedCert() 
	{
		return selectedCert;
	}

	public void setSelectedCert(String selectedCert) 
	{
		this.selectedCert = selectedCert;
	}

    public void setCriteriaTemplates(Set<CriteriaTemplate> criteriaTemplates)
    {
        this.criteriaTemplates = criteriaTemplates;
    }

    public Set<CriteriaTemplate> getCriteriaTemplates()
    {
        return criteriaTemplates;
    }
    
    public void addCriterion(Criterion criterion)
    {
    	awardCriteria.add(criterion);
    }
    
    public Set<Criterion> getAwardCriteria()
    {
    	return awardCriteria;
    }

   public Map<String, String> getTemplateFields() 
   {
	   return templateFields;
   }

	public void setTemplateFields(Map<String, String> templateFields) 
	{
		this.templateFields = templateFields;
	}
	
	public Map<String, String> getFieldToDescription()
	{
		HashMap<String, String> fieldToDesc = new HashMap<String, String>();
		for (String key : getTemplateFields().keySet())
		{
			String repulsiveNotation = getTemplateFields().get(key);
			String description = getPredifinedFields().get(repulsiveNotation);
			fieldToDesc.put(key, description);
		}
		
		return fieldToDesc;
	}

	/**
	 * 
	 * @return a map of ${} format to description
	 */
	public Map<String, String> getPredifinedFields() 
	{
		return predifinedFields;
	}
	
	public List<String[]> getOrderedEscapedPredifinedFields()
	{
		ArrayList<String []> retVal = new ArrayList<String []>();
		Map<String, String> predefFields = getPredifinedFields();
		if (predefFields==null || predefFields.isEmpty())
		{
			//TODO: log it
			return retVal;
		}
		
		Iterator<String> itPredefFields = predefFields.keySet().iterator();
		while (itPredefFields.hasNext())
		{
			String key = itPredefFields.next();
			
			//passing something of the form ${} makes jsp treat it like a variable
			//soln: remove the $ here and append it back in the jsp code
			if ("${unassigned}".equals(key))
			{
				retVal.add(0, new String[] { key.substring(1), predefFields.get(key) });
			}
			else
			{
				retVal.add(new String[] { key.substring(1), predefFields.get(key) });
			}
		}
		
		
		return retVal;
	}
	
	public Map<String, String> getEscapedPredifinedFields() 
	{
		Map<String, String> retVal = new HashMap<String, String>();
		Map<String, String> predefFields = getPredifinedFields();
		if (predefFields==null || predefFields.isEmpty())
		{
			//TODO: log it
			return predefFields;
		}
		
		Iterator<String> itPredefFields = predefFields.keySet().iterator();
		while (itPredefFields.hasNext())
		{
			String key = itPredefFields.next();
			//passing something of the form ${} makes jsp treat it like a variable
			//soln: remove the $ here and append it back in the jsp code
			retVal.put(key.substring(1), predefFields.get(key));
		}
		
		return retVal;
	}

    public String getMimeTypes() 
    {
        return mimeTypes;
    }

    public void setMimeTypes(String mimeTypes) 
    {
        this.mimeTypes = mimeTypes;
    }    

	public void setPredifinedFields(Map<String, String> predifinedFields) 
	{
		boolean criteriaContainWechi = false;
		
		CertificateDefinition certDef = getCertificateDefinition();
		Set<Criterion> awardCriteria = certDef.getAwardCriteria();
		for (Criterion criterion : awardCriteria)
		{
			if (criterion instanceof WillExpireCriterionHibernateImpl)
			{
				criteriaContainWechi = true;
			}
		}
		
		
		//hate to hard code this. I'd grab it from GradebookVariableResolver, but we can't access impl
		String expireDate = "${cert.expiredate}";
		boolean fieldValuesContainWechi = certDef.getFieldValues().values().contains(expireDate);
		
		
		Map<String, String> temp = null;
		if(predifinedFields != null)
		{
			temp = new HashMap<String, String>();
			for(String key : predifinedFields.keySet())
			{
				String newKey = key;
				if(!(key.startsWith("${") && key.endsWith("}")))
				{
					newKey = "${"+key+"}";
				}
				//if the award criteria don't contain a wechi and the key is the expiry date, skip it
				
				/* Anything other than the expiry date gets added.
				 * If the key is the expiry date, we only add it if the award criteria contain wechi
				 * or if the field values contain wechi (for whatever reason).
				 * So we add it if 
				 * key != expiry date OR criteriacontainswechi OR fieldvaluescontainwechi*/ 
				if (!expireDate.equals(newKey) || criteriaContainWechi || fieldValuesContainWechi)
				{
					temp.put(newKey, predifinedFields.get(key));
				}
			}
		}
		this.predifinedFields = temp;
	}
	
	/**
	 * @return a map from the PDF's fields to their selected values' descriptions
	 */
	public Map <String, String> getTemplateFieldsToDescriptions()
	{
		Map<String, String> retVal = new HashMap<String, String>();
		Map<String, String> preDefFields = getPredifinedFields();
		
		if (preDefFields == null || preDefFields.isEmpty())
		{
			LOG.error("preDefFields is null or empty!");
			return null;
		}
		
		CertificateDefinition certDef = getCertificateDefinition();
		if (certDef == null)
		{
			LOG.error("certDef is null!");
			return null;
		}
		Set<String> keys = certDef.getFieldValues().keySet();
		if (keys == null || keys.isEmpty())
		{
			//this is fine - just means it's a new cert def
			return null;
		}
		
		
		Iterator<String> itKeys = keys.iterator();
		while (itKeys.hasNext())
		{
			String key = itKeys.next();
			retVal.put(key, preDefFields.get(certDef.getFieldValues().get(key)));
		}
		return retVal;
	}
	
	/**
	 * 
	 * @return a map of PDF field names to ${} format
	 */
	public Map<String, String> getEscapedFieldValues()
	{
		Map<String, String> retVal = null;
		CertificateDefinition certDef = getCertificateDefinition();
		if (certDef == null)
		{
			LOG.error("certDef is null");
			return retVal;
		}
		Map<String, String> fieldValues = certDef.getFieldValues();
		if (fieldValues == null || fieldValues.isEmpty())
		{
			//this is fine, just means it's a new cert def
			return getTemplateFields();
		}
		retVal = new HashMap<String, String>();
		Iterator<String> itKeys = fieldValues.keySet().iterator();
		while (itKeys.hasNext())
		{
			String key = itKeys.next();
			//passing something of the form ${} makes jsp treat it like a variable
			//soln: remove the $ here and append it back in the jsp code
			String value = fieldValues.get(key).substring(1);
			retVal.put(key, value);
		}
		
		return retVal;
	}
	
	public String getUnassignedValue()
	{
		return "{" + VariableResolver.UNASSIGNED + "}";
	}

	public CertificateToolState()
    {
        reset();
    }

    public void reset()
    {
    	certificateDefinition = new BaseCertificateDefinition();
    	newDocumentTemplateName = null;
    	submitValue = null;
    	selectedCert = null;
    	templateFields = null;
    	predifinedFields = null;
        criteriaTemplates = null;
        awardCriteria = new HashSet<Criterion>();
        selectedCriteriaTemplate = null;
        newDefinition = true;
        templateFilename = null;
        templateByteArray = null;
        templateMimeType = null;
    }

   public void setTemplateFields(Set<String> templateFields)
   {
	   Map<String, String> newTemplateField = null;
	   if(templateFields != null)
	   {
		   newTemplateField = new HashMap<String, String>();
		   for(String val : templateFields)
		   {
			   newTemplateField.put(val, val);
		   }
	   }
	   setTemplateFields(newTemplateField);
   }
   
    private static final ToolSession session()
    {
        final ToolSession session = SessionManager.getCurrentToolSession();
        if (session == null)
        {
            LOG.fatal("No tool session found; cannot manage CertificateToolState object");
        }

        return session;
    }

    public static final CertificateToolState getState()
    {
        final ToolSession session = session();
        if (session == null)
        {
            return null;
        }

        CertificateToolState state = (CertificateToolState) session.getAttribute(CERTIFICATE_TOOL_STATE);
        if (state == null)
        {
            state = new CertificateToolState();
            session.setAttribute(CERTIFICATE_TOOL_STATE, state);
        }

        return state;
    }

    public static final void clear()
    {
        final ToolSession session = session();

        if (session != null)
        {
            session.removeAttribute(CERTIFICATE_TOOL_STATE);
        }
    }
}
