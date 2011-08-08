package org.mashimaru.web.page;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.mashimaru.web.service.JcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class HomePage extends WebPage {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(HomePage.class);

	@SpringBean
	private JcrService jcrService;

	public HomePage(final PageParameters pageParameters)
			throws RepositoryException, IOException {
		add(new Label("greet", "Welcome"));

		try {
			jcrService.create();
		} catch (Exception e) {
			e.printStackTrace();
		}

		TreeModel treeModel = new DefaultTreeModel(jcrService.getWebNode());
		add(new Tree("tree", treeModel));
	}
}
