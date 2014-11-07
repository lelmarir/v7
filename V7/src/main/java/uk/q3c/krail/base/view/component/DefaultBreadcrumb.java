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
package uk.q3c.krail.base.view.component;

import com.google.inject.Inject;
import com.vaadin.ui.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.q3c.krail.base.guice.uiscope.UIScoped;
import uk.q3c.krail.base.navigate.Navigator;
import uk.q3c.krail.base.navigate.sitemap.UserSitemap;
import uk.q3c.krail.base.navigate.sitemap.UserSitemapNode;
import uk.q3c.krail.base.view.KrailViewChangeListener;
import uk.q3c.krail.i18n.CurrentLocale;

import java.util.List;

@UIScoped
public class DefaultBreadcrumb extends NavigationButtonPanel implements KrailViewChangeListener,
        Button.ClickListener, Breadcrumb {
    private static Logger log = LoggerFactory.getLogger(DefaultBreadcrumb.class);

    @Inject
    protected DefaultBreadcrumb(Navigator navigator, UserSitemap sitemap, CurrentLocale currentLocale) {
        super(navigator, sitemap, currentLocale);
    }

    @Override
    protected void build() {
        log.debug("building breadcrumb");
        List<UserSitemapNode> nodeChain = getSitemap().nodeChainFor(getNavigator().getCurrentNode());
        organiseButtons(nodeChain);
    }
}
