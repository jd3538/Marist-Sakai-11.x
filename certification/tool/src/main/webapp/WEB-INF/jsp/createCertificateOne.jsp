<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
	<form:form id="createCertFormOne" modelAttribute="certificateToolState" action="first.form" enctype="multipart/form-data">
		<div>
			<h3><spring:message code="form.text.instruction"/></h3>
			<p><spring:message code="form.text.create.description"/></p>
		</div>
		<div id="submitError" class="alertMessage" style="display:none"></div>
		<c:if test="${statusMessageKey != null}" >
			<div id="statusMessageKey" class="alertMessage" >
				<spring:message code="${statusMessageKey}"/>
			</div>
		</c:if>
		<c:if test="${errorMessage != null}" >
			<div id="errorMessage" class="alertMessage" >
				<spring:message code="${errorMessage}" arguments="${errorArguments}"/>
			</div>
		</c:if>
		<div style="position:relative; margin-left:20px">
		<table>
			<tbody>
				<tr>
					<td><form:label path="certificateDefinition.name"><B><spring:message code="form.label.name" /></B><span class="reqStarInline">*</span></form:label></td>
					<td><form:input id="name" path="certificateDefinition.name"/></td>
				</tr>
				<tr>
					<td><form:label path="certificateDefinition.description"><B><spring:message code="form.label.description" /></B></form:label></td>
					<td><form:textarea cssStyle="resize:none; width:350px; height:100px" path="certificateDefinition.description"/></td>
				</tr>
				
				<tr>
					<td><form:label path="certificateDefinition.documentTemplate"><B><spring:message code="form.label.templatefile" /><B><span class="reqStarInline">*</span></form:label></td>
                    <td>
                        <c:if test="${certificateToolState.templateFilename != null}">
                                <spring:message code="form.label.currentFile"/>
                                <c:out value="${certificateToolState.templateFilename}"/><br/>
                                <form:hidden id="currentTemplate" path="templateFilename" />
                        </c:if>
                        <input id="templateFile" type="file" name="newTemplate" accept="application/pdf"/>
                        <span style="font-size : xx-small;"><spring:message code="form.label.mimeTypes" arguments="${certificateToolState.mimeTypes}"/></span>
                    </td>
				</tr>
			</tbody>
			
		</table>
		</div>
		<div style="margin:5px">
			<input id="back" type="button" disabled="disabled" value="<spring:message code="form.submit.back" />" />&nbsp;
			<!-- bbailla2 <input id="save" type="button" value="<spring:message code="form.submit.saveProgress"/>"/>&nbsp;-->
			<input id="next" type="button" value="<spring:message code="form.submit.next"/>"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<input id="cancel" type="button" value="<spring:message code="form.submit.cancel"/>"/>
			<form:hidden path="submitValue" />
		</div>
	</form:form>	
<script type="text/javascript">

	$(document).ready(function() {

		loaded();
		
		/* bbailla2 $("#save").click(function() {
			save();
		});*/
	
		$("#next").click(function() {
			next();
		});
		
		$("#cancel").click(function() {
			cancel();
		});
		
		$("textarea").resize(function() {
			loaded();
		});
	});
	
	/* bbailla2 function save() {
		if(validateForm()) {
			$("#submitValue").val("save");
			$("#createCertFormOne").submit();
		}
	}*/
	
	function cancel() {
		$("#submitValue").val("cancel");
		$("#createCertFormOne").submit();
	}
	
	function next() {
		if(validateForm()) {
			$("#submitValue").val("next");
			$("#createCertFormOne").submit();
		}
	}
	
	function validateForm() {
		$(".alertMessage").hide();
		var error = false;
		var errHtml = "";
		
		if(!$("#name").val()) {
			errHtml = errHtml + "<spring:message code="form.error.namefield"/>" + "</br>" ;
			error = true;
		}
		
		if(!$("input:file").val() && !$("#currentTemplate").val()) {
			errHtml = errHtml + "<spring:message code="form.error.templateField"/>" + "</br>" ;
			error = true;
		}
		
		if(error)
		{
			$("#submitError").html(errHtml).show();
			resetHeight();
			return false;
		}
		else
		{
			return true;
		}
	}

	String.prototype.endsWith = function(suffix) 
	{
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};

	$("#templateFile").change( function() 
	{
		if ( !$(this).val().toLowerCase().endsWith(".pdf") )
		{
			if ($("#errorMessage").length == 0)
			{
				var templateMessage = "<div id=\"submitError\" class=\"alertMessage\"> \n " +
							"<spring:message code="form.error.templateField"/> \n" +
						"</div>";
				$("#submitError").replaceWith( templateMessage );
			}
		}
	});

</script>
<%@include file="/WEB-INF/jsp/footer.jsp" %>
