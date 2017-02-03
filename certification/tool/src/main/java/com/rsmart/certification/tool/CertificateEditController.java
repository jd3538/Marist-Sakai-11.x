package com.rsmart.certification.tool;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rsmart.certification.api.criteria.InvalidBindingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.InvalidCertificateDefinitionException;
import com.rsmart.certification.api.VariableResolver;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.hibernate.CertificateDefinitionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.WillExpireCriterionHibernateImpl;
import com.rsmart.certification.tool.utils.CertificateToolState;

/**
 * bbailla2
 * Understanding this class:
 * Clicking a button from the jsp is going to trigger a request mapping method.
 * certificateToolState.getSubmitValue() represents the button that was clicked
 * If the submit value is (null, I think?), then the page is loading.
 * 
 * User: duffy
 * Date: Jun 7, 2011
 * Time: 4:15:32 PM
 */
@Controller
@SessionAttributes(types = CertificateToolState.class)
public class CertificateEditController extends BaseCertificateController
{
    private static final Log LOG = LogFactory.getLog(CertificateEditController.class);

    public static final String MIME_TYPES = "mimeTypes";
    
    //jsp views
    private static final String VIEW_CREATE_CERTIFICATE_ONE = "createCertificateOne";
    private static final String VIEW_CREATE_CERTIFICATE_TWO = "createCertificateTwo";
    private static final String VIEW_CREATE_CERTIFICATE_THREE = "createCertificateThree";
    private static final String VIEW_CREATE_CERTIFICATE_FOUR = "createCertificateFour";
    
    //submit button values
    private static final String ACTION_CANCEL = "cancel";
    private static final String ACTION_SAVE = "save";
    private static final String ACTION_NEXT = "next";
    private static final String ACTION_BACK = "back";
    
    //http session attributes
    public static final String ATTR_TEMPLATE_FIELDS = "template.fields";
    public static final String ATTR_TEMPLATE_ID = "templateId";
    public static final String ATTR_CERT_ID = "certId";
    
    //request parameters
    public static final String REQUEST_PARAM_CERT_ID = "certId";
    
    private static final String ERROR_NOT_ADMIN = "error.not.admin";
    
    private static final String INVALID_NAME_LENGTH = "form.submit.error.name.too.long";
    private static final String INVALID_DESCRIPTION_LENGTH = "form.submit.error.description.too.long";
    
    private static final String TOO_MANY_EXPIRATION_CRITERIA = "**TooManyExpiry**";

    private Pattern varValuePattern = Pattern.compile ("variableValues\\[(.*)\\]");
    private Pattern variablePattern = Pattern.compile ("\\$\\{(.+)\\}");

    private ObjectMapper mapper = new ObjectMapper();
    
    private final int CONSTRAINT_DESCRIPTION_LENGTH = 500;
    private final int CONSTRAINT_NAME_LENGTH = 255;
    
    
    public final int ERROR_BAD_REQUEST = 400;
    

    /**
     * This allows other methods to use @ModelAttribute(MOD_ATTR). 
     * Using this model attribute adds the certId request param, and populates 
     * the certificateToolState if the certId is specified.
     * @param certId
     * @return
     * @throws Exception
     */
	@ModelAttribute(MOD_ATTR)
	public  CertificateToolState initializeModel(@RequestParam(value=REQUEST_PARAM_CERT_ID, required=false) String certId) 
			throws Exception
	{
		CertificateToolState certificateToolState = CertificateToolState.getState();
		if(certId != null && !"".equals(certId))
		{
			/* We are editing an existing certificate definition.
			 * It may already be stored in the tool state.
			 * If not, we'll need to grab it*/ 
			boolean grabCertificate = false;
			if (certificateToolState.getCertificateDefinition() == null)
			{
				grabCertificate = true;
			}	
			else if (!certId.equals(certificateToolState.getCertificateDefinition().getId()))
			{
				grabCertificate = true;
			}
			
			
			if (grabCertificate)
			{
				CertificateDefinition certificateDefinition = getCertificateService().getCertificateDefinition(certId);
				certificateToolState.setCertificateDefinition(certificateDefinition);
				certificateToolState.setNewDefinition(false);
			}
		}
		
		return certificateToolState;
	}


    @RequestMapping(value="/first.form")
    protected ModelAndView createCertHandlerFirst(	@ModelAttribute(MOD_ATTR) CertificateToolState certificateToolState,
    		 										BindingResult result, HttpServletRequest request, SessionStatus status) 
    		 throws Exception
    {
    	Map<String, Object> model = new HashMap<String, Object>();

    	String strRedirect = REDIRECT + CertificateListController.THIS_PAGE;
    	
		if (!isAdministrator())
		{
			CertificateToolState.clear();
			status.setComplete();
			
		    return new ModelAndView(strRedirect, ERROR_MESSAGE, ERROR_NOT_ADMIN);
		}
		 
    	if(ACTION_CANCEL.equals(certificateToolState.getSubmitValue()))
    	{
			CertificateToolState.clear();
			status.setComplete();
			return new ModelAndView(strRedirect);
		}


    	if(result.hasErrors())
    	{
    		return new ModelAndView(VIEW_CREATE_CERTIFICATE_ONE, STATUS_MESSAGE_KEY, FORM_ERR);
    	}
        else if(ACTION_NEXT.equals(certificateToolState.getSubmitValue()) && !result.hasErrors())
        {
            try
            {
            	certificateDefinitionValidator.validateFirst(certificateToolState, result, getCertificateService());
            	if(!result.hasErrors())
            	{
                    certificateToolState = persistFirstFormData(certificateToolState);
            		certificateToolState.setNewDefinition(false);
            	}
            	else
            	{
            		model.put(ERROR_MESSAGE, TEMPLATE_FIELD_ERR);
                    model.put(MOD_ATTR, certificateToolState);
                    return new ModelAndView(VIEW_CREATE_CERTIFICATE_ONE, model);
            	}
            }
            catch (InvalidCertificateDefinitionException icde)
            {
            	int field = icde.getInvalidField();
            	if (icde.getReason() == InvalidCertificateDefinitionException.REASON_TOO_LONG)
            	{
	            	if (field == CertificateDefinition.FIELD_NAME)
	            	{
	            		model.put(ERROR_MESSAGE, INVALID_NAME_LENGTH);
	            		model.put(ERROR_ARGUMENTS, CONSTRAINT_NAME_LENGTH);
	            	}
	            	else if (field == CertificateDefinition.FIELD_DESCRIPTION)
	            	{
	            		model.put(ERROR_MESSAGE, INVALID_DESCRIPTION_LENGTH);
	            		model.put(ERROR_ARGUMENTS, CONSTRAINT_DESCRIPTION_LENGTH);
	            	}
	            	else 
            		{
	            		model.put(STATUS_MESSAGE_KEY, TEMPLATE_FIELD_ERR);
	            	}
            	}
            	else
            	{
            		model.put(STATUS_MESSAGE_KEY, TEMPLATE_FIELD_ERR);
            	}
            	model.put(MOD_ATTR, certificateToolState);
            	return new ModelAndView(VIEW_CREATE_CERTIFICATE_ONE, model);
            }
            catch (IdUsedException iue)
            {
                logger.warn("CertificateEditController.createCertHandlerFirst.save", iue);

                model.put(ERROR_MESSAGE, DUPLICATE_NAME_ERR);
                model.put(MOD_ATTR, certificateToolState);

                return new ModelAndView(VIEW_CREATE_CERTIFICATE_ONE, model);
            }
            catch (Exception e)
            {
                logger.warn("CertificateEditController.createCertHandlerFirst.next", e);
                CertificateDefinition certificateDefinition = certificateToolState.getCertificateDefinition();
                if(certificateToolState.isNewDefinition() && certificateDefinition.getId() != null)
                {
	                try
	                {
	                  getCertificateService().deleteCertificateDefinition(certificateDefinition.getId());
	                }
	                catch(Exception e2)
	                {
                        logger.warn("", e);
	                }
                }
				model.put(ERROR_MESSAGE, TEMPLATE_FIELD_ERR);
                model.put(MOD_ATTR, certificateToolState);
                return new ModelAndView(VIEW_CREATE_CERTIFICATE_ONE, model);
            }

            certificateToolState.setSubmitValue(null);
            return createCertHandlerSecond(certificateToolState, result, request, status);
        }
    	else
    	{
    		/*
    		 	Rendering the first page. It requires all registered mime types. If we are editing an existing certificate, 
    		 	it will already be in the certificateToolState because of MOD_ATTR
    		 */
    		
    		
    		CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
    		if (certDef != null 
    				&& certDef.getId() != null 
    				&& !"".equals(certDef.getId()) 
    				&& certificateToolState.getNewTemplate() == null) 
    		{
    			/*It's an existing certificate and a new template was not provided, 
    			 * so we need to load the existing template from resources*/
    			
    			//we are editing an existing certificate definition
    			DocumentTemplate docTemp = certDef.getDocumentTemplate();
    			certificateToolState.setTemplateFilename(docTemp.getName());
    			certificateToolState.setTemplateMimeType(docTemp.getOutputMimeType());

    			//Get the document template's file's contents as a byte array
    			CertificateService certServ = getCertificateService();
    			InputStream resourceStream = certServ.getTemplateFileInputStream(docTemp.getResourceId());

    			//credit to Triton Man (http://stackoverflow.com/questions/6790485/save-inputstream-to-bytearray)
    			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    			byte [] tmp = new byte[4096];
    			int ret = 0;
    			while ((ret = resourceStream.read(tmp)) > 0)
    			{
    				byteStream.write(tmp, 0, ret);
    			}
    			
    			byte[] resourceContent = byteStream.toByteArray();
    			
    			certificateToolState.setTemplateByteArray(resourceContent);
    		}
    		
    		//clear the new template in case we are coming back to this page from page 2
    		certificateToolState.setNewTemplate(null);
    		
    		String delim = "";
	        StringBuffer mimeBuff = new StringBuffer();

	        for (String mimeType : getDocumentTemplateService().getRegisteredMimeTypes())
	        {
	            mimeBuff.append(delim).append(mimeType);
	            delim = ", ";
	        }
	        
	        certificateToolState.setMimeTypes(mimeBuff.toString());
    		return new ModelAndView(VIEW_CREATE_CERTIFICATE_ONE, MOD_ATTR, certificateToolState);
    	}
    }

    private CertificateToolState persistFirstFormData(CertificateToolState certificateToolState) throws Exception
    {
        CertificateService certificateService = getCertificateService();
    	CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
    	
        CommonsMultipartFile newTemplate = certificateToolState.getNewTemplate();
        
        if (certDef.getName().length() > CONSTRAINT_NAME_LENGTH)
        {
        	InvalidCertificateDefinitionException icde = new InvalidCertificateDefinitionException();
        	icde.setInvalidField(CertificateDefinition.FIELD_NAME);
        	icde.setReason(icde.REASON_TOO_LONG);
        	throw icde;
        }
        else if (certDef.getDescription().length() > CONSTRAINT_DESCRIPTION_LENGTH)
        {
        	InvalidCertificateDefinitionException icde = new InvalidCertificateDefinitionException();
        	icde.setInvalidField(CertificateDefinition.FIELD_DESCRIPTION);
        	icde.setReason(icde.REASON_TOO_LONG);
        	throw icde;
        	
        }
        

    	if(certDef.getId() == null || "".equals(certDef.getId()))
    	{

            CertificateDefinition existing = null;

            try
            {
                existing = getCertificateService().getCertificateDefinitionByName(siteId(), certDef.getName());
            }
            catch (IdUnusedException iue)
            {
                // this is good! not a duplicate
            }
            

            if (existing != null)
            {
                throw new IdUsedException (certDef.getName());
            }
            
            
            /*certDef = certificateService.createCertificateDefinition(certDef.getName(), certDef.getDescription(), 
            		siteId(), data.getOriginalFilename(), data.getContentType(), data.getInputStream());*/
            

    		//certDef = certificateService.getCertificateDefinition(certDef.getId());
			//DocumentTemplate dt = certDef.getDocumentTemplate();
            
            InputStream inputStream = certificateToolState.getTemplateInputStream();
            String mimeType = certificateToolState.getTemplateMimeType();
            
            if (newTemplate != null && newTemplate.getSize() > 0)
            {
            	//clear the field values
            	certDef.getFieldValues().clear();
            	if (certificateToolState.getTemplateFields() != null)
            	{
            		certificateToolState.getTemplateFields().clear();
            	}
            	
            	certificateToolState.setTemplateByteArray(newTemplate.getBytes());
            	certificateToolState.setTemplateFilename(newTemplate.getOriginalFilename());
            	inputStream = newTemplate.getInputStream();
            	mimeType = certificateService.getMimeType(newTemplate.getBytes());
            	certificateToolState.setTemplateMimeType(mimeType);
            }
            
            //String mimeType = certificateService.getMimeType(certificateToolState.getData().getBytes());
			Set<String> templateFields = getDocumentTemplateService().getTemplateFields(inputStream, mimeType);
			ToolSession session = SessionManager.getCurrentToolSession();
			session.setAttribute(ATTR_TEMPLATE_FIELDS, templateFields);
			certificateToolState.setTemplateFields(templateFields);
    	}
    	else
    	{
    		if (newTemplate != null && newTemplate.getSize() > 0)
    		{
    			if (certDef instanceof CertificateDefinitionHibernateImpl)
    			{
    				CertificateDefinitionHibernateImpl cdhi = (CertificateDefinitionHibernateImpl) certDef;
    				//document template will get created upon activation
    				//remove the document template if we intend to replace it
    				cdhi.setDocumentTemplate(null);
    			}
    			
				certificateToolState.setTemplateByteArray(newTemplate.getBytes());
				certificateToolState.setTemplateFilename(newTemplate.getOriginalFilename());
				
				InputStream inputStream = newTemplate.getInputStream();
				String mimeType = certificateService.getMimeType(newTemplate.getBytes());
				certificateToolState.setTemplateMimeType(mimeType);
				
				Set<String> templateFields = getDocumentTemplateService().getTemplateFields(inputStream, mimeType);
				ToolSession session = SessionManager.getCurrentToolSession();
				session.setAttribute(ATTR_TEMPLATE_FIELDS, templateFields);
				certificateToolState.setTemplateFields(templateFields);
				Map<String, String> fieldValues = new HashMap<String, String>();
				certDef.setFieldValues(fieldValues);
    		}
    		else
    		{
    			DocumentTemplateService dts = getDocumentTemplateService();
    			DocumentTemplate dt = certDef.getDocumentTemplate();
    			/* dt will be null if a new template was uploaded earlier 
    			 * and we are coming back to this page*/
    			if (dt != null)
    			{
	    			Set<String> templateFields = dts.getTemplateFields(certDef.getDocumentTemplate());
	    			
	    			ToolSession session = SessionManager.getCurrentToolSession();
	    			session.setAttribute(ATTR_TEMPLATE_FIELDS, templateFields);
	    			certificateToolState.setTemplateFields(templateFields);
    			}
    		}
    		
    		//As for the name and the description, they will be dealt with on the fourth page 
		}

		certificateToolState.setCertificateDefinition(certDef);
		return certificateToolState;
    }

	@RequestMapping(value="/second.form")
    protected ModelAndView createCertHandlerSecond(@ModelAttribute(MOD_ATTR) CertificateToolState certificateToolState,
                                                  BindingResult result, HttpServletRequest request, SessionStatus status)
        throws Exception
    {
        final String subVal = certificateToolState.getSubmitValue();
        CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
        
        CertificateService certSvc = getCertificateService();
        Map<String, Object> model = new HashMap<String, Object>();
        
        model.put(MODEL_KEY_TOOL_URL, getToolUrl());
        String viewName = null;

        String strRedirect = REDIRECT + CertificateListController.THIS_PAGE;
        
        if (!isAdministrator())
		{
			CertificateToolState.clear();
			status.setComplete();
		    return new ModelAndView(strRedirect, ERROR_MESSAGE, ERROR_NOT_ADMIN);
		}
        
        if(ACTION_CANCEL.equals(certificateToolState.getSubmitValue()))
		{
        	//The certificate definition's award criteria will be changed in memory. 
        	//Reloading will overwrite the changes
        	String id = certDef.getId();
        	if (id !=null && !"".equals(id))
        	{
        		certSvc.getCertificateDefinition(id);
        	}
        	
			CertificateToolState.clear();
			status.setComplete();
			return new ModelAndView(strRedirect);
		}
        else if(ACTION_BACK.equals(subVal))
    	{
            certificateToolState.setSubmitValue(null);
            return createCertHandlerFirst(certificateToolState, result, request, status);
    	}

        if(result.hasErrors())
        {
        	viewName=VIEW_CREATE_CERTIFICATE_TWO;
        }
    	else if(ACTION_NEXT.equals(subVal))
    	{
    		// bjones86 - can't have expiry date as the only criterion
    		if( certDef.getAwardCriteria().size() == 1 )
			{
				Criterion criterion = (Criterion) certDef.getAwardCriteria().iterator().next();
				if( criterion != null && criterion instanceof WillExpireCriterionHibernateImpl )
				{
					viewName = VIEW_CREATE_CERTIFICATE_TWO;
					model.put( ERROR_MESSAGE, EXPIRY_ONLY_CRITERION_ERROR_MSG_KEY );
					model.put( MOD_ATTR, certificateToolState );
					
			    	return new ModelAndView( viewName, model );
				}
			}
    		
    		certificateDefinitionValidator.validateSecond(certificateToolState, result);
    		if(!result.hasErrors())
			{
				//bbailla2 ----
				//Set<String> templateFields = getDocumentTemplateService().getTemplateFields(certDef.getDocumentTemplate());
    			//OWLTODO: could be the value set we're looking for, but I think this is right
    			Set<String> templateFields = certificateToolState.getTemplateFields().keySet();
				if (templateFields == null || templateFields.isEmpty())
				{
					ToolSession session = SessionManager.getCurrentToolSession();
					templateFields = (Set<String>) session.getAttribute(ATTR_TEMPLATE_FIELDS);
				}
				certificateToolState.setTemplateFields(templateFields);
				model.put(STATUS_MESSAGE_KEY, SUCCESS);
				//-------------
    		    certificateToolState.setSubmitValue(null);
    		    return createCertHandlerThird(certificateToolState, result, request, status);
        	}
    	    else
    	    {
    	    	/*This shouldn't happen (this page is always valid 
    	    	 * except in the cases that are already handled above)*/
				viewName=VIEW_CREATE_CERTIFICATE_TWO;
				model.put(STATUS_MESSAGE_KEY, FORM_ERR);
			}
    	}
    	else
    	{
    		/*
    		   add criteria templates to model
    		   in JSP loop through templates
    		   		add a template to the dropdown
    		   			CriteriaTemplate.getExpression()
    		   		call JS function to populate remainder of form
    		   			loop through CriteriaTemplateVariables
    		   				if multiChoice - add a dropdown to the form
    		   					(eg. pick a gradebook item)
    		   				else add a textbox to the form
    		   					(eg. enter minimum score)
    		 */
    		Set<CriteriaTemplate> criteriaTemplates = certSvc.getCriteriaTemplates();

            certificateToolState.setCriteriaTemplates(criteriaTemplates);

            viewName=VIEW_CREATE_CERTIFICATE_TWO;
    	}

        model.put(MOD_ATTR, certificateToolState);
    	return new ModelAndView (viewName, model);
    }
	
	@RequestMapping(value="/third.form")
    protected ModelAndView createCertHandlerThird(@ModelAttribute(MOD_ATTR) CertificateToolState certificateToolState,
   		                                           BindingResult result, HttpServletRequest request,
                                                   SessionStatus status)
	   throws Exception
    {
		Map<String, Object> model = new HashMap<String, Object>();

		String strRedirect = REDIRECT + CertificateListController.THIS_PAGE;
		
		if (!isAdministrator())
		{
			CertificateToolState.clear();
			status.setComplete();
		    return new ModelAndView(strRedirect, ERROR_MESSAGE, ERROR_NOT_ADMIN);
		}
		
    	if(ACTION_CANCEL.equals(certificateToolState.getSubmitValue()))
    	{
			CertificateToolState.clear();
			status.setComplete();
			return new ModelAndView(strRedirect);
		}
    	else if(ACTION_BACK.equals(certificateToolState.getSubmitValue()))
    	{
    		//bbailla2 - jsp strips the $'s out
    		CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
    		Map<String, String> templateFields = certDef.getFieldValues();
    		Map<String, String> newTemplateFields = new HashMap<String, String>();
    		Set<String> keys = templateFields.keySet();
    		for (String key : keys)
    		{
    			String originalValue = templateFields.get(key);
    			if (originalValue.charAt(0) == '{' && originalValue.endsWith("}"))
    			{
	    			StringBuilder value = new StringBuilder("$");
	    			value.append(templateFields.get(key));
	    			newTemplateFields.put(key, value.toString());
    			}
    			else
    			{
    				newTemplateFields.put(key, originalValue);
    			}
    		}
    		
    		certDef.setFieldValues(newTemplateFields);
    		
    		//bbailla2 - added this for consistency
    		certificateToolState.setSubmitValue(null);
    		return createCertHandlerSecond(certificateToolState, result, request, status);
    	}

    	if(result.hasErrors())
		{
			return new ModelAndView(VIEW_CREATE_CERTIFICATE_THREE,STATUS_MESSAGE_KEY,FORM_ERR);
		}
    	else if(ACTION_NEXT.equals(certificateToolState.getSubmitValue()))
    	{
    		try
    		{
	    		certificateDefinitionValidator.validateThird(certificateToolState, result);
	    		if(!result.hasErrors())
	    		{
	    			CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
	    			
	    			certDef.setFieldValues(certificateToolState.getTemplateFields());
		    		//getCertificateService().setFieldValues(certDef.getId(), certificateToolState.getTemplateFields());
		    		model.put(STATUS_MESSAGE_KEY, SUCCESS);
	    		}
	    		else
	    		{
	    			model.put(STATUS_MESSAGE_KEY, FORM_ERR);
	    			model.put(MOD_ATTR, certificateToolState);
	    			model.put(ERROR_MESSAGE, PREDEFINED_VAR_EXCEPTION);
	        		return new ModelAndView(VIEW_CREATE_CERTIFICATE_THREE, model);
	    		}
    		}
    		catch(Exception e)
    		{
    			logger.warn("CertificateEditController.createCertHandlerThird.next", e);
    			model.put(STATUS_MESSAGE_KEY, FORM_ERR);
    			model.put(MOD_ATTR, certificateToolState);
    			return new ModelAndView(VIEW_CREATE_CERTIFICATE_THREE, model);
    		}

            certificateToolState.setSubmitValue(null);
            return createCertHandlerFourth(certificateToolState, result, request, status);
    	}
    	else
    	{
            certificateToolState.setPredifinedFields(getCertificateService().getPredefinedTemplateVariables());
    		return new ModelAndView(VIEW_CREATE_CERTIFICATE_THREE, MOD_ATTR, certificateToolState);
    	}
    }

    @RequestMapping(value="/fourth.form", method=RequestMethod.POST)
    protected ModelAndView createCertHandlerFourth(@ModelAttribute(MOD_ATTR) CertificateToolState certificateToolState,
      		 BindingResult result, HttpServletRequest request, SessionStatus status)
		 throws Exception
    {
    	Map<String, Object> model = new HashMap<String, Object>();

    	String strRedirect = REDIRECT + CertificateListController.THIS_PAGE;
    	
    	if (!isAdministrator())
		{
			CertificateToolState.clear();
			status.setComplete();
		    return new ModelAndView(strRedirect, ERROR_MESSAGE, ERROR_NOT_ADMIN);
		}
    	
    	if(ACTION_CANCEL.equals(certificateToolState.getSubmitValue()))
    	{
			CertificateToolState.clear();
			status.setComplete();
			return new ModelAndView(strRedirect);
		}
    	if(ACTION_BACK.equals(certificateToolState.getSubmitValue()))
    	{
    		certificateToolState.setSubmitValue(null);
    		return createCertHandlerThird(certificateToolState, result, request, status);
    	}
    	else if(ACTION_SAVE.equals(certificateToolState.getSubmitValue()))
    	{
    		/*
    		 	call to certServ.activateCertificateDef
    		 	redirect to cert list page
    		 */
    		/*try
    		{
	    		CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
	    		getCertificateService().activateCertificateDefinition(certDef.getId(), true);
    		}
    		catch (Exception e)
    		{
    			model.put(STATUS_MESSAGE_KEY, FORM_ERR);
    			model.put(MOD_ATTR, certificateToolState);
    			return new ModelAndView(VIEW_CREATE_CERTIFICATE_FOUR,model);
    		}*/
    		
    		try
    		{
	    		CertificateService certificateService = getCertificateService();
	    		
	    		CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
	    		/* Every time the certificate definition is updated, we need to refresh it from the certificate service.
	    		 * However, once we refresh the certificate definition, our new awardCriteria will disappear.
	    		 * So, let's grab it now*/
	    		Set<Criterion> newAwardCriteria = certDef.getAwardCriteria();
	    		if (certDef.getId() == null || "".equals(certDef.getId()))
	    		{
	    			//create a hibernate impl
	    			certificateService.createCertificateDefinition(certDef.getName(), certDef.getDescription(),
	    					siteId(), certDef.getProgressHidden(), certificateToolState.getTemplateFilename(), certificateToolState.getTemplateMimeType(), certificateToolState.getTemplateInputStream());
	    			
	    			//gets the hibernateImpl
	    			certDef = certificateService.getCertificateDefinitionByName(siteId(), certDef.getName());
	    			
	    			//add the award criteria
	    			certificateService.setAwardCriteria(certDef.getId(), newAwardCriteria);
	    			
	    			//get the updated certificateDefinition
	    			certDef = certificateService.getCertificateDefinition(certDef.getId());
	    			
	    			//set the field values
	    			certificateService.setFieldValues(certDef.getId(), certificateToolState.getTemplateFields());
	    			
	    			//activate the certificate
	    			certificateService.activateCertificateDefinition(certDef.getId(), true);
	    			
	    		}
	    		else
	    		{
	    			//this will get replaced if we don't grab it now
	    			Set<Criterion> awardCriteria = certDef.getAwardCriteria();
	    			
	    			certDef = certificateService.updateCertificateDefinition(certDef);
	    			
	    			
	    			if (certificateToolState.getNewTemplate() != null)
	    			{
		    			//update the document template
		    			DocumentTemplate dt = certificateService.setDocumentTemplate(certDef.getId(), certificateToolState.getTemplateFilename(), certificateToolState.getTemplateMimeType(), certificateToolState.getTemplateInputStream());
	    			}
	    			
	    			certificateService.setAwardCriteria(certDef.getId(), awardCriteria);
	    			
	    			certDef = certificateService.getCertificateDefinition(certDef.getId());
	    			certificateService.setFieldValues(certDef.getId(), certificateToolState.getTemplateFields());
	    			
	    			certDef = certificateService.getCertificateDefinition(certDef.getId());
	    			certificateService.activateCertificateDefinition(certDef.getId(), true);
	    			
	    		}
	
	    		CertificateToolState.clear();
	    		status.setComplete();
	    		return new ModelAndView(strRedirect);
    		}
    		catch (Exception e)
    		{
    			model.put(STATUS_MESSAGE_KEY, FORM_ERR);
    			model.put(MOD_ATTR, certificateToolState);
    			return new ModelAndView(VIEW_CREATE_CERTIFICATE_FOUR, model);
    		}
    	}
    	else
    	{
    		/*
    		 send the entire CertDefinition to the JSP page through the certificateToolState
    		 */
    		return new ModelAndView(VIEW_CREATE_CERTIFICATE_FOUR,MOD_ATTR,certificateToolState);
    	}
    }

    @RequestMapping(value="/getTemplate.form")
    protected void getCriteriaTemplate(HttpServletRequest request, HttpServletResponse response)
        throws Exception
    {
        String templateId = request.getParameter(ATTR_TEMPLATE_ID);
        
        if (templateId == null)
        {
            response.sendError(ERROR_BAD_REQUEST);
            return;
        }
        
        CriteriaFactory critFact = getCertificateService().getCriteriaFactory(templateId);
        
        if (critFact == null)
        {
            response.sendError(ERROR_BAD_REQUEST);
            return;
        }
        
        CriteriaTemplate template = critFact.getCriteriaTemplate(templateId);
        
        if (template == null)
        {
            response.sendError(ERROR_BAD_REQUEST);
            return;
        }
        
        TemplateTransferObject tto = new TemplateTransferObject(template);
        
        mapper.writeValue(response.getOutputStream(), tto);
    }

    @RequestMapping(value="/addCriterion.form")
    protected void addCertCriteria(HttpServletRequest request, HttpServletResponse response)
    throws Exception
    {
    	
    	if (!isAdministrator())
		{
    		response.sendError(ERROR_BAD_REQUEST);
            return;
		}

        CertificateToolState state = CertificateToolState.getState();
        CertificateService cs = getCertificateService();

        // place to store parameters from HTTP request
        Map <String, String[]> params = request.getParameterMap();

        // variable bindings for the new Criterion
        HashMap<String,String> varMap = new HashMap<String, String>(0);

        // grab the parameters that we know
        String certId[] = params.get(ATTR_CERT_ID);
        String templateId[] = params.get(ATTR_TEMPLATE_ID);

        // loop through to find request parameters for setting variable values (from dynamic Criterion creation form)
        for (String key : params.keySet())
        {
            // should look like ${variable}
            Matcher matcher = varValuePattern.matcher(key);

            if (matcher.matches())
            {
                String mapKey = matcher.group(1);
                String value[] = params.get(key);
                
                varMap.put(mapKey, value[0]);
            }
        }

        /*
        // report protocol level errors for bad requests
        if (certId == null || certId.length == 0)
        {
            //error
            response.sendError(ERROR_BAD_REQUEST, messages.getString(ERROR_BAD_ID));
            return;
        }*/
        if (templateId == null || templateId.length == 0)
        {
            //error
            response.sendError(ERROR_BAD_REQUEST, messages.getString(ERROR_BAD_TEMPLATE_ID));
            return;
        }

        // preconditions are met for a valid request - do the actual work
        // get the certificate def.
        //CertificateDefinition cert = cs.getCertificateDefinition(certId[0]);
        CertificateDefinition cert = state.getCertificateDefinition();

        // get the CriteriaTemplate - first need to get the CriteriaFactory which holds the CriteraTemplate
        CriteriaFactory critFact = cs.getCriteriaFactory(templateId[0]);
        CriteriaTemplate template = critFact.getCriteriaTemplate(templateId[0]);

        Criterion newCriterion = null;

        // create the criterion based on the form contents
        try
        {
            newCriterion = critFact.createCriterion(template, varMap);
        }
        catch (InvalidBindingException ibe)
        {
            response.sendError(ERROR_BAD_REQUEST, ibe.getLocalizedMessage());
            return;
        }
        
        // bjones86 - multiple expiry date criterion check
        if( newCriterion != null && newCriterion instanceof WillExpireCriterionHibernateImpl )
        {
        	boolean alreadyHasExpiry = false;
        	if( cert.getAwardCriteria().size() > 0 )
        	{
        		Iterator<Criterion> itr = cert.getAwardCriteria().iterator();
        		while( itr.hasNext() )
        		{
        			Criterion criterion = (Criterion) itr.next();
        			if( criterion != null && criterion instanceof WillExpireCriterionHibernateImpl )
        			{
        				alreadyHasExpiry = true;
        				break;
        			}
        		}
        	}
        	
        	// If more than one expiry was found, return the flag to produce the proper UI error message
        	if( alreadyHasExpiry )
        	{
        		response.sendError( ERROR_BAD_REQUEST, TOO_MANY_EXPIRATION_CRITERIA );
        		return;
        	}
        }
       
        //cs.addAwardCriterion(certId[0], newCriterion);
        cert.addAwardCriterion(newCriterion);

        //update the toolstate's certificate definition so it has the new criterion
        //state.setCertificateDefinition(cs.getCertificateDefinition(certId[0]));
        mapper.writeValue(response.getOutputStream(), new CriterionTransferObject(template,newCriterion));
    }

    @RequestMapping(value="/removeCriterion.form")
    protected void removeCertCriteria(HttpServletRequest request, HttpServletResponse response)
		throws Exception
    {
    	CertificateDefinition certDef = CertificateToolState.getState().getCertificateDefinition();
    	
    	String paramCritId = "criterionId";
    	
    	if (!isAdministrator())
		{
    		response.sendError(ERROR_BAD_REQUEST);
            return;
		}
    	
    	Map <String, String[]> params = request.getParameterMap();
    	
    	String certId[] = params.get(ATTR_CERT_ID);
    	String criterionId[] = params.get(paramCritId);
    	//CertificateService cs = getCertificateService();
    	
    	//cs.removeAwardCriterion(certId[0], criterionId[0]);
	    
    	Set<Criterion> awardCriteria = certDef.getAwardCriteria();
    	Criterion match = null;
    	Iterator<Criterion> itAwardCriteria = awardCriteria.iterator();
    	while (itAwardCriteria.hasNext())
    	{
    		Criterion next = itAwardCriteria.next();
    		if (criterionId[0].equals(next.getId()))
    		{
    			match = next;
    		}
    	}
    	if (match != null)
    	{
    		if (match instanceof WillExpireCriterionHibernateImpl)
    		{
    			//expiry date field values are meaningless if the certificate will not expire.
    			//switch any expiry date field values to unassigned
    			Map<String, String> fieldValues = certDef.getFieldValues();
    			
    			String expireDate = "${" + VariableResolver.CERT_EXPIREDATE + "}";
    			List<String> keysToReplace = new ArrayList<String>();
    			
    			Set<String> keySet = fieldValues.keySet();
    			for (String key : keySet)
    			{
    				String value = fieldValues.get(key);
    				if ( expireDate.equals(value) )
    				{
    					keysToReplace.add(key);
    				}
    			}
    			
    			String unassigned = "${" + VariableResolver.UNASSIGNED + "}";
    			
    			for (String key : keysToReplace)
    			{
    				fieldValues.remove(key);
    				fieldValues.put(key, unassigned);
    			}
    		}
    		awardCriteria.remove(match);
    	}
	    //CertificateToolState.getState().setCertificateDefinition(cs.getCertificateDefinition(certId[0]));
	    mapper.writeValue(response.getOutputStream(), criterionId[0]);
	  
    }

    
    //****************************NESTED CLASSES****************************
    
    private class TemplateTransferObject
    {
        private String id;
        private String expression;
        private List<VariableTransferObject> variables = new ArrayList<VariableTransferObject>();

        private CriteriaTemplate template;
        
        TemplateTransferObject (CriteriaTemplate template)
        {
            id = template.getId();
            expression = template.getExpression();

            for (CriteriaTemplateVariable variable : template.getTemplateVariables())
            {
                variables.add(new VariableTransferObject(variable));
            }
            
            this.template = template;
        }

        public String getId()
        {
            return id;
        }

        public String getExpression()
        {
            return expression;
        }

        public List<VariableTransferObject> getTemplateVariables()
        {
            return variables;
        }
        
        public String getMessage()
        {
        	return template.getMessage();
        }
    }

    private class VariableTransferObject
    {
        private String key;
        private String label;
        private boolean multipleChoice;
        private HashMap<String, String> values = new HashMap<String, String>();

        public VariableTransferObject (CriteriaTemplateVariable variable)
        {
            key = variable.getVariableKey();
            label = variable.getVariableLabel();
            multipleChoice = variable.isMultipleChoice();

            if (multipleChoice)
            {
                values.putAll(variable.getValues());
            }
        }

        public String getVariableKey()
        {
            return key;
        }
        
        public String getVariableLabel()
        {
            return label;
        }

        public boolean isMultipleChoice()
        {
            return multipleChoice;
        }

        public Map<String, String> getValues()
        {
            return values;
        }
    }

    private class CriterionTransferObject
    {
        private String id;
        private String expression;

        public CriterionTransferObject (CriteriaTemplate template, Criterion criterion)
        {
            id = criterion.getId();
            expression = template.getExpression(criterion);
        }

        public String getId()
        {
            return id;
        }

        public String getExpression()
        {
            return expression;
        }
    }
}
