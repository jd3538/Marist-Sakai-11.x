package com.rsmart.certification.api;

import com.rsmart.certification.api.criteria.CriterionProgress;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents information pertaining to one user in the reporting interface
 */
public class ReportRow
{
	/**
	 * The user's name (formatted as "lastname, firstname")
	 */
	private String name = "";

	/**
	 * The user's display id
	 */
	private String userId = "";

	/**
	 * The user's site role
	 */
	private String role = "";

	/**
	 * Any extra properties to be displayed (coming from the user's properties, selected in sakai.properties - 
	 * certification.extraUserProperties.keys, and enabled by certification.extraUserProperties.enable)
	 **/
	private List<String> extraProps = new ArrayList<String>();

	/**
	 * The date the user was issued the certificate
	 */
	private String issueDate = "";

	/**
	 * Cells representing the user's progress toward the criteria
	 **/
	private List<CriterionProgress> criterionCells = new ArrayList<CriterionProgress>();

	/**
	 * Whether the user was awarded or not (ie. yes/no)
	 **/
	private String awarded = "";

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

	public String getRole()
	{
		return role;
	}

	public void setExtraProps(List<String> extraProps)
	{
		this.extraProps = extraProps;
	}

	public List<String> getExtraProps()
	{
		return extraProps;
	}

	public void setIssueDate(String issueDate)
	{
		this.issueDate = issueDate;
	}

	public String getIssueDate()
	{
		return issueDate;
	}

	public void setCriterionCells(List<CriterionProgress> criterionCells)
	{
		this.criterionCells = criterionCells;
	}

	public List<CriterionProgress> getCriterionCells()
	{
		return criterionCells;
	}

	public void setAwarded(String awarded)
	{
		this.awarded = awarded;
	}

	public String getAwarded()
	{
		return awarded;
	}
}

