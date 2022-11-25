<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="actions" fragment="true" required="false" %>
<%@attribute name="below" fragment="true" required="false" %>

<div class="section-heading row tw-mb-4 lg:tw-mb-6">
    <div class="col-xs-12">
        <div class="tw-flex tw-items-baseline tw-pb-2 tw-border-b-2 dark:tw-border-b dark:tw-border-zinc-600">
            <div class="tw-flex-1 tw-text-2xl tw-leading-5 tw-flex tw-items-baseline tw-flex-wrap tw-space-x-1">
                <jsp:doBody />
            </div>
            <c:if test="${not empty actions}">
            <div class="section-heading-actions print:tw-hidden tw-flex">
                <jsp:invoke fragment="actions" />
            </div>
            </c:if>
        </div>
        <c:if test="${not empty below}">
        <div class="tw-mt-2">
            <jsp:invoke fragment="below" />
        </div>
        </c:if>
    </div>
</div>
