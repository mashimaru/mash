package org.mashimaru.web.service;

import java.io.IOException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.mashimaru.jcr.service.JcrCallback;
import org.mashimaru.jcr.service.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JcrService {
	@Autowired
	private Template template;

	public void create() throws RepositoryException, IOException {
		template.execute(new JcrCallback() {
			@Override
			public Object doInJcr(Session session) throws IOException,
					RepositoryException {
				Node jcrRootNode = session.getRootNode();

				if (!jcrRootNode.hasNode("cms/web")) {
					Node cmsNode = jcrRootNode.addNode("cms", "nt:folder");
					Node webNode = cmsNode.addNode("web", "nt:folder");

					// For testing
					Node cssNode = webNode.addNode("css", "nt:folder");

					Node styleNode = cssNode.addNode("style.css", "nt:file");
					Node styleContentNode = styleNode.addNode("jcr:content",
							"nt:resource");
					styleContentNode.setProperty("jcr:lastModified",
							Calendar.getInstance());
					styleContentNode.setProperty("jcr:data", "");
					styleContentNode.setProperty("jcr:mimeType", "text/css");

					Node indexNode = webNode.addNode("index.html", "nt:file");
					Node indexContentNode = indexNode.addNode("jcr:content",
							"nt:resource");
					indexContentNode.setProperty("jcr:lastModified",
							Calendar.getInstance());
					indexContentNode.setProperty("jcr:data", "");
					indexContentNode.setProperty("jcr:mimeType", "text/html");

					session.save();
				}

				return null;
			}
		});
	}

	public MutableTreeNode getWebNode() throws RepositoryException, IOException {
		return (MutableTreeNode) template.execute(new JcrCallback() {
			@Override
			public Object doInJcr(Session session) throws IOException,
					RepositoryException {
				Node root = session.getRootNode();
				Node jcrRoot = root.getNode("cms/web");

				DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(
						jcrRoot.getName());
				add(treeRoot, jcrRoot);

				return treeRoot;
			}

			private void add(DefaultMutableTreeNode treeParent, Node jcrParent)
					throws RepositoryException {
				for (NodeIterator iterator = jcrParent.getNodes(); iterator
						.hasNext();) {
					Node jcrChild = (Node) iterator.next();

					String name = jcrChild.isNodeType("nt:folder") ? jcrChild
							.getName() : jcrChild.getName()
							+ " ["
							+ jcrChild.getNode("jcr:content")
									.getProperty("jcr:mimeType").getString()
							+ "]";

					DefaultMutableTreeNode treeChild = new DefaultMutableTreeNode(
							name);
					treeParent.add(treeChild);

					if (jcrChild.isNodeType("nt:folder")) {
						add(treeChild, jcrChild);
					}
				}
			}
		});
	}
}
