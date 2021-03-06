<div id="header">

	<div id="bannerArea1" class="banner">
		<c:if test="${pageContext.request.servletPath == '/portal/index.jsp'}">
			<!-- show a link to WISE2 if this is the main homepage -->
			<div class="announce">
				<spring:htmlEscape defaultHtmlEscape="false">
					<spring:escapeBody htmlEscape="false">
						<spring:message code="headermain.newUrl" />
					</spring:escapeBody>
				</spring:htmlEscape>
			</div>
			<img class="announceIcon"
				src="${contextPath}/<spring:theme code="marker"/>"
				alt="announcement" />
		</c:if>

		<a id="name" href="${contextPath}/index.html" title="WISE Homepage">WISE
		</a>

		<%@ include file="accountmenu.jsp"%>

	</div>
</div>