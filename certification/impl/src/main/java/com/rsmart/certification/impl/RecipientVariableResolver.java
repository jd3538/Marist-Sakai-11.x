package com.rsmart.certification.impl;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.VariableResolutionException;
import com.rsmart.certification.api.VariableResolver;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import java.util.HashMap;
import java.util.Set;

/**
 * User: duffy
 * Date: Jul 7, 2011
 * Time: 7:52:26 AM
 */
public class RecipientVariableResolver extends AbstractVariableResolver
{
    private UserDirectoryService uds = null;

    private static final String MESSAGE_FULLNAME = "variable.fullname";
    private static final String MESSAGE_FIRSTNAME = "variable.firstname";
    private static final String MESSAGE_LASTNAME = "variable.lastname";

    public RecipientVariableResolver()
    {
    	// bbailla2
        String fullName = getMessages().getString(MESSAGE_FULLNAME);
        String firstName = getMessages().getString(MESSAGE_FIRSTNAME);
        String lastName = getMessages().getString(MESSAGE_LASTNAME);
	
        addVariable (FULL_NAME, fullName);
        addVariable (FIRST_NAME, firstName);
        addVariable (LAST_NAME, lastName);
    }

    public void setUserDirectoryService(UserDirectoryService uds)
    {
        this.uds = uds;
    }

    public UserDirectoryService getUserDirectoryService()
    {
        return uds;
    }
    
    // bbailla2
    public String getValue(CertificateDefinition certDef, String key, String userId, boolean useCaching)
        throws VariableResolutionException
    {
        User user = null;
        try
        {
            user = getUserDirectoryService().getUser(userId);
        }
        catch (UserNotDefinedException e)
        {
            throw new VariableResolutionException("could not resolve variable \"" + key + "\" due to UserNotDefinedException. userId: " + userId, e);
        }

        if (FULL_NAME.equals(key))
        {
        	return user.getDisplayName();
            //return user.getFirstName() + " " + user.getLastName();
        }
        else if (FIRST_NAME.equals(key))
        {
            return user.getFirstName();
        }
        else if (LAST_NAME.equals(key))
        {
            return user.getLastName();
        }

        throw new VariableResolutionException ("key \"" + key + "\" has not been resolved");
    }
}
