/*
 * Copyright (C) 2013 David Sowerby
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.co.q3c.v7.base.navigate.sitemap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import uk.co.q3c.util.BasicForest;
import uk.co.q3c.v7.base.navigate.StandardPageKey;

import com.google.common.collect.ImmutableMap;

/**
 * Encapsulates the site layout. Individual "virtual pages" are represented by {@link SitemapNode} instances. This map
 * is built by one or more implementations of {@link SitemapLoader}, and is one of the fundamental building blocks of
 * the application, as it maps out pages, URIs and Views.
 * <p>
 * <p>
 * Because of it use as such a fundamental building block, an instance of this class has to be created early in the
 * application start up process. To avoid complex dependencies within modules, the building of the {@link Sitemap} is
 * managed by the {@link SitemapService}
 * <p>
 * Simple URI redirects can be added using {@link #addRedirect(String, String)}
 * 
 * @see SitemapURIConverter
 * 
 * @author David Sowerby 19 May 2013
 * 
 */
@Singleton
public class Sitemap {

	private String publicRoot = "public";
	private String privateRoot = "private";
	private int nextNodeId = 0;
	private int errors = 0;
	private final Map<StandardPageKey, String> standardPages = new HashMap<>();
	private String report;
	// Uses LinkedHashMap to retain insertion order
	private final Map<String, String> redirects = new LinkedHashMap<>();
	private SitemapNode privateRootNode;
	private SitemapNode publicRootNode;
	private final BasicForest<SitemapNode> forest;

	@Inject
	public Sitemap() {
		super();
		forest = new BasicForest<>();
	}

	/**
	 * Returns the full URI for {@code node}
	 * 
	 * @param node
	 * @return
	 */
	public String uri(SitemapNode node) {
		StringBuilder buf = new StringBuilder(node.getUriSegment());
		prependParent(node, buf);
		return buf.toString();
	}

	/**
	 * Recursively prepends the parent URI segment of {@code node}, until the full URI has been built
	 */
	private void prependParent(SitemapNode node, StringBuilder buf) {
		SitemapNode parentNode = forest.getParent(node);
		if (parentNode != null) {
			buf.insert(0, "/");
			buf.insert(0, parentNode.getUriSegment());
			prependParent(parentNode, buf);
		}
	}

	/**
	 * creates a SiteMapNode and appends it to the map according to the {@code uri} given, then returns it. If a node
	 * already exists at that location it is returned. If there are gaps in the structure, nodes are created to fill
	 * them (the same idea as forcing directory creation on a file path). An empty (not null) URI is allowed. This
	 * represents the site base URI without any further qualification.
	 * 
	 * @param uri
	 * @return
	 */
	public SitemapNode append(String uri) {

		if (uri.equals("")) {
			SitemapNode node = new SitemapNode();
			node.setUriSegment(uri);
			addNode(node);
			return node;
		}
		SitemapNode node = null;
		String[] segments = StringUtils.split(uri, "/");
		List<SitemapNode> nodes = forest.getRoots();
		SitemapNode parentNode = null;
		for (int i = 0; i < segments.length; i++) {
			node = findNodeBySegment(nodes, segments[i], true);
			addChild(parentNode, node);
			nodes = forest.getChildren(node);
			parentNode = node;
		}

		return node;
	}

	private SitemapNode findNodeBySegment(List<SitemapNode> nodes, String segment, boolean createIfAbsent) {
		SitemapNode foundNode = null;
		for (SitemapNode node : nodes) {
			if (node.getUriSegment().equals(segment)) {
				foundNode = node;
				break;
			}
		}

		if ((foundNode == null) && (createIfAbsent)) {
			foundNode = new SitemapNode();
			foundNode.setUriSegment(segment);

		}
		return foundNode;
	}

	public void addNode(SitemapNode node) {
		if (node.getId() == 0) {
			node.setId(nextNodeId());
		}
		forest.addNode(node);
	}

	public void addChild(SitemapNode parentNode, SitemapNode childNode) {
		// super allows null parent
		if (parentNode != null) {
			if (parentNode.getId() == 0) {
				parentNode.setId(nextNodeId());
			}
		}
		if (childNode.getId() == 0) {
			childNode.setId(nextNodeId());
		}
		forest.addChild(parentNode, childNode);
	}

	public String standardPageURI(StandardPageKey pageKey) {
		return standardPages.get(pageKey);
	}

	private int nextNodeId() {
		nextNodeId++;
		return nextNodeId;
	}

	public Map<StandardPageKey, String> getStandardPages() {
		return standardPages;
	}

	public boolean hasErrors() {
		return errors > 0;
	}

	public int getErrors() {
		return errors;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getReport() {
		return report;
	}

	/**
	 * If the {@code page} has been redirected, return the page it has been redirected to, otherwise, just return
	 * {@code page}
	 * 
	 * @param page
	 * @return
	 */
	public String getRedirectFor(String page) {
		String p = redirects.get(page);
		if (p == null) {
			return page;
		}
		return p;
	}

	/**
	 * Safe copy of redirects
	 * 
	 * @return
	 */
	public ImmutableMap<String, String> getRedirects() {
		return ImmutableMap.copyOf(redirects);

	}

	public Sitemap addRedirect(String fromPage, String toPage) {
		redirects.put(fromPage, toPage);
		return this;
	}

	/**
	 * Returns a list of {@link SitemapNode} matching the {@code segments} provided. If there is an incomplete match (a
	 * segment cannot be found) then:
	 * <ol>
	 * <li>if {@code allowPartialPath} is true a list of nodes is returned correct to the longest path possible.
	 * <li>if {@code allowPartialPath} is false an empty list is returned
	 * 
	 * @param segments
	 * @return
	 */

	public List<SitemapNode> nodeChainForSegments(List<String> segments, boolean allowPartialPath) {
		List<SitemapNode> nodeChain = new ArrayList<>();
		int i = 0;
		String currentSegment = null;
		List<SitemapNode> nodes = forest.getRoots();
		boolean segmentNotFound = false;
		SitemapNode node = null;
		while ((i < segments.size()) && (!segmentNotFound)) {
			currentSegment = segments.get(i);
			node = findNodeBySegment(nodes, currentSegment, false);
			if (node != null) {
				nodeChain.add(node);
				nodes = forest.getChildren(node);
				i++;
			} else {
				segmentNotFound = true;
			}

		}
		if (segmentNotFound && !allowPartialPath) {
			nodeChain.clear();
		}
		return nodeChain;
	}

	/**
	 * Returns a list of all the URIs contained in the sitemap. This is a fairly expensive call, as each URI has to be
	 * built from the node structure.
	 * 
	 * @return
	 */
	public List<String> uris() {
		List<String> list = new ArrayList<>();
		for (SitemapNode node : forest.getAllNodes()) {
			list.add(uri(node));
		}
		return list;
	}

	/**
	 * Returns true if the sitemap contains {@code uri}. This is a fairly expensive call, as each URI has to be built
	 * from the node structure, before this method can be evaluated
	 * 
	 * @param uri
	 * @return
	 */
	public boolean hasUri(String uri) {
		List<String> list = uris();
		return list.contains(uri);
	}

	public void setErrors(int errorSum) {
		errors = errorSum;

	}

	public String getPublicRoot() {
		return publicRoot;
	}

	public void setPublicRoot(String publicRoot) {
		this.publicRoot = publicRoot;
	}

	public String getPrivateRoot() {
		return privateRoot;
	}

	public void setPrivateRoot(String privateRoot) {
		this.privateRoot = privateRoot;
	}

	public SitemapNode getPrivateRootNode() {
		if (this.privateRootNode == null) {
			privateRootNode = findNodeBySegment(forest.getRoots(), privateRoot, false);
		}
		return privateRootNode;
	}

	public SitemapNode getPublicRootNode() {
		if (this.publicRootNode == null) {
			publicRootNode = findNodeBySegment(forest.getRoots(), publicRoot, false);
		}
		return publicRootNode;
	}

	/**
	 * If a duplicate URI is received in any of the add methods:
	 * <p>
	 * If {@code overwrite} is true, the existing node is replaced in its entirety by a the new one. If
	 * {@code overwrite} is false, then the new node is ignored and the current one left as it is.
	 * 
	 * @param overwrite
	 */
	public void duplicateURIoverwrites(boolean overwrite) {

	}

	public int getNodeCount() {
		return forest.getNodeCount();
	}

	/**
	 * Returns the parent of {@code node}. Will be null if {@code node} has no parent (that is, it is a root node)
	 * 
	 * @param node
	 * @return
	 */
	public SitemapNode getParent(SitemapNode node) {
		return forest.getParent(node);
	}

	/**
	 * Delegates to {@link BasicForest#getRoots()}
	 * 
	 * @return
	 */
	public List<SitemapNode> getRoots() {
		return forest.getRoots();
	}

	/**
	 * Delegates to {@link BasicForest#getRootFor(Object)}
	 * 
	 * @param node
	 * @return
	 */
	public SitemapNode getRootFor(SitemapNode node) {
		return forest.getRootFor(node);
	}

	/**
	 * Delegates to {@link BasicForest#getChildCount(Object)}
	 * 
	 * @param node
	 * @return
	 */

	public int getChildCount(SitemapNode node) {
		return forest.getChildCount(node);
	}

	/**
	 * Delegates to {@link BasicForest#getAllNodes()}
	 * 
	 * @return
	 */
	public List<SitemapNode> getAllNodes() {
		return forest.getAllNodes();
	}

	/**
	 * Delegates to {@link BasicForest#getChildren(Object)}
	 * 
	 * @param newParentNode
	 * @return
	 */
	public List<SitemapNode> getChildren(SitemapNode parentNode) {
		return forest.getChildren(parentNode);

	}

}
