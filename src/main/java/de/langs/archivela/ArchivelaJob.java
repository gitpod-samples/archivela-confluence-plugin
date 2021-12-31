package de.langs.archivela;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.user.AuthenticatedUserImpersonator;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The Archivela scheduled job triggered by Confluence. It does the actual
 * processing of labels and moves the identified pages to the archive location.
 */
@Slf4j
@Component
public class ArchivelaJob implements JobRunner {
	public static final String JOB_NAME = "Archivela Job";

	// Auto wired components.
	@Setter
	@Autowired
	private GlobalState globalState;

	private final PageManager pageManager;

	private final TransactionTemplate transactionTemplate;

	@Autowired
	public ArchivelaJob(@ComponentImport PageManager pageManager,
			@ComponentImport TransactionTemplate transactionTemplate) {
		this.pageManager = pageManager;
		this.transactionTemplate = transactionTemplate;
	}

	/**
	 * The main method of this job. Called by Confluence.
	 * 
	 * @request execution request
	 */
	public JobRunnerResponse runJob(JobRunnerRequest request) {
		if (request.isCancellationRequested()) {
			return JobRunnerResponse.aborted(JOB_NAME + " cancelled");
		}

		try {
			log.debug(Constants.PLUGIN_KEY + " Executing " + JOB_NAME);

			Configuration configuration = globalState.getConfiguration();

			for (Rule rule : configuration.getRules()) {
				String user = rule.getJobUser();

				ConfluenceUser confluenceUser = StaticAccessor.getUserAccessor().getUserByName(user);

				Callable<Integer> callableAction = () -> {
					transactionTemplate.execute(new TransactionCallback<Object>() {
						@Override
						public Void doInTransaction() {
							movePages(rule.getSourceParentPage(), rule.getTargetParentPage(), rule.getPrefix(),
									rule.getDateFormat(), rule.getRecursive());
							return null;
						}
					});

					return 0;
				};
				AuthenticatedUserImpersonator.REQUEST_AGNOSTIC.asUser(callableAction, confluenceUser);
			}
		} catch (Throwable e) {
			log.error(Constants.PLUGIN_KEY + " Error while running " + JOB_NAME + ": " + e.toString(), e);
			return JobRunnerResponse.failed(e);
		}

		return JobRunnerResponse.success(JOB_NAME + " successful");
	}

	/**
	 * Identify which pages to move and move them.
	 * 
	 * @param sourceParentPageName source parent page name
	 * @param targetParentPageName target parent page name where the pages will be
	 *                             moved to
	 * @param prefix               label prefix
	 * @param dateFormat           label date format
	 * @param recursive            whether to recurse into the source parent page
	 *                             hierarchy or not
	 */
	private void movePages(String sourceParentPageName, String targetParentPageName, String prefix, String dateFormat,
			String recursive) {
		Page sourceParentPage = PageUtils.getPage(sourceParentPageName);
		Page targetParentPage = PageUtils.getPage(targetParentPageName);

		List<Page> pagesToArchive = new ArrayList<Page>();

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		// Enforce format
		sdf.setLenient(false);
		try {
			Date todayDate = sdf.parse(sdf.format(new Date()));

			List<Page> childPages = new ArrayList<>();

			if (recursive.equals(RuleRecursive.Yes)) {
				childPages = sourceParentPage.getDescendants();
			} else {
				childPages = sourceParentPage.getChildren();
			}

			// For each child page, get the labels
			for (Page childPage : childPages) {
				for (Label label : childPage.getLabels()) {
					try {
						String labelForParsing = label.getName();
						if (!prefix.isEmpty() && !labelForParsing.startsWith(prefix)) {
							continue;
						}

						labelForParsing = labelForParsing.substring(prefix.length());

						// Try to parse labels to identify which ones to move
						Date labelDate = sdf.parse(labelForParsing);

						// Move page to new target parent page if expired
						if (todayDate.after(labelDate)) {
							log.info(JOB_NAME + " found a label '" + label.getName()
									+ "' that indicates archiving is required for: " + childPage.getTitle());
							pagesToArchive.add(childPage);
						}
						// We found a date label, continue with next page
						break;

					} catch (ParseException e) {
						// Continue with next label, this cannot be parsed as a date
					}
				}
			}

		} catch (ParseException e1) {
			// Should not happen
			log.error(Constants.PLUGIN_KEY + " Parsing current date failed", e1);
		}

		// Move pages
		for (Page page : pagesToArchive) {
			log.info(JOB_NAME + " is moving page '" + page.getTitle() + "' to new parent '"
					+ targetParentPage.getTitle() + "'");
			pageManager.movePageAsChild(page, targetParentPage);
			pageManager.updatePageInAncestorCollections(page, targetParentPage);
		}
	}

}
