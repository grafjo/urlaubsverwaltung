<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<div class="tw-w-full tw-bg-amber-50 dark:tw-bg-amber-100 tw-border tw-border-amber-200 dark:tw-border-0 tw-p-4 tw-flex tw-flex-col sm:tw-flex-row dark:tw-rounded">
    <div class="tw-mb-4 sm:tw-mb-0 sm:tw-mr-4 lg:tw-mr-14 tw-flex tw-items-center sm:tw-items-start">
        <icon:information-circle
            className="tw-text-amber-300 dark:tw-text-yellow-500 tw-w-6 tw-h-6 tw-mr-2"
            solid="true"
        />
        <span class="tw-text-black tw-text-opacity-75 dark:tw-text-opacity-100 tw-font-bold tw-text-sm sm:tw-mt-px">
            <spring:message code="privacy-box.title" />
        </span>
    </div>
    <div class="tw-max-w-xl tw-flex-1 tw-text-sm lg:tw-text-base tw-text-black tw-text-opacity-80 dark:tw-text-opacity-100 dark:tw-font-medium">
        <jsp:doBody />
    </div>
</div>
