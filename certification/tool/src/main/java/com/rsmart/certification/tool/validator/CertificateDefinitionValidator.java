package com.rsmart.certification.tool.validator;

import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplateException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.rsmart.certification.tool.utils.CertificateToolState;


public class CertificateDefinitionValidator
{
	private Pattern variablePattern = Pattern.compile ("\\$\\{(.+)\\}");

	public void validateFirst(CertificateToolState certificateToolState, Errors errors, CertificateService service)
	{
		CommonsMultipartFile newTemplate = certificateToolState.getNewTemplate();
		
		if (newTemplate != null && newTemplate.getSize() > 0)
		{
			if(certificateToolState.getMimeTypes().indexOf(newTemplate.getContentType()) < 0)
			{
				// could be browser misreporting (ie. Firefox), so get a second opinion
				// OWL-990  --plukasew
				try
				{
					String mimeType = service.getMimeType(newTemplate.getBytes());
					if (!certificateToolState.getMimeTypes().contains(mimeType))
					{
						errors.rejectValue("newTemplate", "mimeType", "invalid mimeType");
					}
				}
				catch (DocumentTemplateException e)
				{
					errors.rejectValue("newTemplate", "mimeType", "invalid mimeType");
				}
			}
		}
	}
	
	public void validateSecond(CertificateToolState certificateToolState, Errors errors)
	{
		// The only invalid case is when the expiry date is your only criterion.
		// This case is handled in CertificateEditController
	}
	
	public void validateThird(CertificateToolState certificateToolState, Errors errors)
	{
		Map<String, String> currentFields = certificateToolState.getTemplateFields();
		//Add the $'s back in (they were removed in CertificateToolState.getEscapedPredifinedFields())
		Set<String> keys = new HashSet<String>();
		keys.addAll(currentFields.keySet());
		for (String key : keys)
		{
			String value = "$" + currentFields.get(key);
			currentFields.remove(key);
			currentFields.put(key, value);
		}
		certificateToolState.setTemplateFields(currentFields);
		Map<String, String> preDefFields = certificateToolState.getPredifinedFields();
		Set<String> keySet = preDefFields.keySet();
		for(String val : currentFields.values())
		{
	        Matcher variableMatcher = variablePattern.matcher(val);
	
	        if (variableMatcher.matches() && !keySet.contains(val))
	        {
	            errors.rejectValue("templateFields","not valid","not valid");
	        }
		}
	}
}




