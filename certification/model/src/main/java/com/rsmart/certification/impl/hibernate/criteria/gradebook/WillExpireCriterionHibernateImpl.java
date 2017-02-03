package com.rsmart.certification.impl.hibernate.criteria.gradebook;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriterionProgress;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;

public class WillExpireCriterionHibernateImpl extends GradebookItemCriterionHibernateImpl
{
	private final String MESSAGE_REPORT_TABLE_HEADER_EXPIRE = "report.table.header.expire";
	private final DateFormat REPORT_DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");
	
    public String getExpiryOffset()
    {
        return getVariableBindings().get(CriteriaFactory.KEY_EXPIRY_OFFSET);
    }

    public void setExpiryOffset(String expiryOffset)
    {
        getVariableBindings().put(CriteriaFactory.KEY_EXPIRY_OFFSET, expiryOffset);
    }

	@Override
	public List<String> getReportHeaders() 
	{
		List<String> reportHeaders = new ArrayList<String>();
		
		String header = getCertificateService().getString(MESSAGE_REPORT_TABLE_HEADER_EXPIRE);
		
		reportHeaders.add(header);
		return reportHeaders;
	}

	/**
	 * Must supply the issue date before calling this method
	 */
	@Override
	public List<CriterionProgress> getReportData(String userId, String siteId, Date issueDate, boolean useCaching) 
	{
		List<CriterionProgress> reportData = new ArrayList<CriterionProgress>();

		boolean met = false;
		try
		{
			met = getCriteriaFactory().isCriterionMet(this, userId, siteId, useCaching);
		}
		catch (UnknownCriterionTypeException e)
		{
			//impossible
		}
	
		String progress = "";
		
		Date expiryDate = getExpiryDate(issueDate);
		if (expiryDate != null)
		{
			progress = REPORT_DATE_FORMAT.format(expiryDate);
		}
		
		CriterionProgress datum = new CriterionProgress(progress, met);
		reportData.add(datum);
		return reportData;
	}
	
	/**
	 * Calculates the expiry date from the specified issue date
	 * @param issueDate
	 * @return
	 */
	public Date getExpiryDate(Date issueDate)
	{
		Date expiryDate = null;
		if (issueDate != null)
		{
			Integer expiryOffset = new Integer(getExpiryOffset());
			Calendar cal = Calendar.getInstance();
			cal.setTime(issueDate);
			cal.add(Calendar.MONTH, expiryOffset);
			expiryDate = cal.getTime();
		}
		
		return expiryDate;
	}
	
	@Override
	public Date getDateMet(String userId, String siteId, boolean useCaching)
	{
		//For this criterion, date met is undefined
		return null;
	}
	
	@Override
	public String getProgress(String userId, String siteId, boolean useCaching)
	{
		//For this criterion, progress is undefined
		return "";
	}
}
