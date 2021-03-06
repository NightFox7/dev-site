/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.site.markdown.toc;

import java.util.List;

import org.parboiled.common.StringUtils;

import com.google.gwt.site.markdown.Strings;
import com.google.gwt.site.markdown.fs.FolderConfig;
import com.google.gwt.site.markdown.fs.MDNode;
import com.google.gwt.site.markdown.fs.MDParent;

public class TocFromMdCreator implements TocCreator {
    private static final String FOLDER = "folder";
    private static final String FILE = "file";

    public String createTocForNode(MDParent root, MDNode node) {
        MDNode tmpNode = node;
        while (tmpNode.getParent() != null && tmpNode.getDepth() > 1) {
            tmpNode = tmpNode.getParent();
        }

        MDNode parentNode = tmpNode;

        StringBuffer buffer = new StringBuffer();
        buffer.append("  <ul>\n");
        render(parentNode, buffer, node);
        buffer.append("  </ul>\n");

        return buffer.toString();
    }

    private void render(MDNode node, StringBuffer buffer, MDNode tocNode) {
        MDNode tmpNode = tocNode;
        while (tmpNode.getParent() != null) {
            if (tmpNode.isExcludeFromToc())
                return;
            tmpNode = tmpNode.getParent();
        }

        tmpNode = node;
        while (tmpNode.getParent() != null) {
            if (tmpNode.isExcludeFromToc())
                return;
            tmpNode = tmpNode.getParent();
        }

        // Use 4 spaces to indent <li>'s, so as we have room for indenting <ul>'s
        String margin = StringUtils.repeat(' ', 4 * node.getDepth());

        if (node.isFolder()) {
            MDParent mdParent = node.asFolder();

            FolderConfig config = mdParent.getConfig();
            List<FolderConfig.Entry> entries = config.getFolderEntries();
            List<MDNode> children = mdParent.getChildren();

            if (children.size() >= countEntries(entries)) {
                writeFromNodes(node, buffer, tocNode, margin, children);
            } else {
                boolean hasChildren = node.getDepth() > 1;
                if (hasChildren) {
                    openNode("#", node.getDisplayName(), buffer, margin, hasChildren);
                }
                writeFromConfig(node, buffer, margin, entries);
                if (hasChildren) {
                    closeNode(buffer, margin, hasChildren);
                }
            }
        } else {
            StringBuilder relativeUrl = new StringBuilder();
            if (tocNode.getDepth() > 0) {
                for (int i = 1; i < tocNode.getDepth(); i++) {
                    relativeUrl.append("../");
                }
            }

            String htmlFileName = node.getParent().getHref() + ".html";
            String relativePath = node.getRelativePath().replace(htmlFileName, "");

            relativeUrl.append(relativePath);

            buffer.append(margin).append("<li class='" + FILE + "'>");
            // TODO escape HTML
            buffer.append(
                    "<a href='" + relativeUrl.toString() + "' title='" + node.getDescription() + "'>"
                            + node.getDisplayName() + "</a>");
            buffer.append("</li>\n");
        }
    }

    private int countEntries(List<FolderConfig.Entry> entries) {
        int count = 0;
        for (FolderConfig.Entry entry : entries) {
            count += countEntries(entry.getSubEntries());
            count++;
        }

        return count;
    }

    private void writeFromConfig(
            MDNode node,
            StringBuffer buffer,
            String margin,
            List<FolderConfig.Entry> entries) {

        for (FolderConfig.Entry entry : entries) {
            StringBuilder relativeUrl = new StringBuilder();
            if (node.getDepth() > 0) {
                for (int i = 1; i < node.getDepth(); i++) {
                    relativeUrl.append("../");
                }
            }

            if (entry.getSubEntries().isEmpty()) {
                StringBuilder absoluteUrl = new StringBuilder();
                absoluteUrl.append("/");
                absoluteUrl.append(node.getRelativePath()).append(entry.getName());

                relativeUrl.append(node.getRelativePath()).append(entry.getName());

                buffer.append(margin).append("<li class='file'>");

                buffer.append(
                        "<a href='" + relativeUrl.toString() + "' title='" + entry.getDescription() + "'>"
                                + entry.getDisplayName() + "</a>");
                buffer.append("</li>\n");
            } else {
                openNode("#", entry.getDisplayName(), buffer, margin, true);
                writeFromConfig(node, buffer, margin, entry.getSubEntries());
                closeNode(buffer, margin, true);
            }
        }
    }

    private void writeFromNodes(
            MDNode node,
            StringBuffer buffer,
            MDNode tocNode,
            String margin,
            List<MDNode> children) {

        boolean writeNode = node.getDepth() > 1 || node.getDepth() == 1 && children.size() == 1;
        boolean hasMoreThanOneChildren = children.size() > 1;

        if (writeNode) {
            if (hasMoreThanOneChildren) {
                openNode("#", node.getDisplayName(), buffer, margin, true);
            } else {
                openNode(node, buffer, margin);
            }
        }

        if (hasMoreThanOneChildren) {
            for (MDNode child : children) {
                render(child, buffer, tocNode);
            }
        }

        if (writeNode) {
            closeNode(buffer, margin, hasMoreThanOneChildren);
        }
    }

    private void closeNode(
            StringBuffer buffer,
            String margin,
            boolean hasChildren) {
        if (hasChildren) {
            buffer.append(margin).append("  </ul>\n");
        }

        buffer.append(margin).append("</li>\n");
    }

    private void openNode(MDNode node, StringBuffer buffer, String margin) {
        FolderConfig config = node.asFolder().getConfig();
        String displayName;
        if (config == null || Strings.isNullOrEmpty(config.getDisplayName())) {
            displayName = node.getDisplayName();
        } else {
            displayName = config.getDisplayName();
        }

        openNode(node.getRelativePath(), displayName, buffer, margin, node.asFolder().getChildren().size() > 1);
    }

    private void openNode(
            String relativePath,
            String displayName,
            StringBuffer buffer,
            String margin,
            boolean hasChildren) {
        buffer.append(margin).append("<li class='" + getStyle(hasChildren) + "'>");
        buffer.append("<a href='").append(relativePath).append("'>");
        buffer.append(displayName);
        buffer.append("</a>\n");

        if (hasChildren) {
            buffer.append(margin).append("  <ul>\n");
        }
    }

    private String getStyle(boolean hasChildren) {
        return hasChildren ? FOLDER : FILE;
    }
}
