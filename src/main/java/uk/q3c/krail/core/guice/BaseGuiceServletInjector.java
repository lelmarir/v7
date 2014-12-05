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
package uk.q3c.krail.core.guice;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.krail.core.config.ApplicationConfigurationModule;
import uk.q3c.krail.core.guice.threadscope.ThreadScopeModule;
import uk.q3c.krail.core.guice.uiscope.UIScopeModule;
import uk.q3c.krail.core.guice.vsscope.VaadinSessionScopeModule;
import uk.q3c.krail.core.navigate.sitemap.SitemapModule;
import uk.q3c.krail.core.services.ServicesMonitor;
import uk.q3c.krail.core.services.ServicesMonitorModule;
import uk.q3c.krail.core.shiro.ShiroVaadinModule;
import uk.q3c.krail.core.shiro.StandardShiroModule;
import uk.q3c.krail.core.user.UserModule;
import uk.q3c.krail.core.user.opt.UserOptionModule;
import uk.q3c.krail.core.view.ViewModule;
import uk.q3c.krail.i18n.I18NModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public abstract class BaseGuiceServletInjector extends GuiceServletContextListener {
	private static Logger log = LoggerFactory.getLogger(BaseGuiceServletInjector.class);

    protected static Injector injector;

    protected BaseGuiceServletInjector() {
        super();
    }

	public Injector getInjector(boolean create) {
		if (injector == null && create) {
			createInjector();
        }
		return injector;
    }

    /**
     * Module instances for the base should be added in {@link #getModules()}. Module instance for the app using Krail
     * should be added to {@link AppModules#appModules()}
     *
     * @see com.google.inject.servlet.GuiceServletContextListener#getInjector()
     */
    @Override
	protected Injector getInjector() {
		return getInjector(true);
    }

    protected void createInjector() {
        injector = Guice.createInjector(getModules());
        log.debug("injector created");

        // By default Shiro provides a binding to DefaultSecurityManager, but that is replaced by a binding to
        // KrailSecurityManager in DefaultShiroModule#bindSecurityManager (or potentially to another security manager if
        // the developer overrides that method)
        SecurityManager securityManager = injector.getInstance(SecurityManager.class);
        SecurityUtils.setSecurityManager(securityManager);

    }

    private List<Module> getModules() {
        List<Module> baseModules = new ArrayList<>();

        baseModules.add(i18NModule());
        baseModules.add(applicationConfigurationModule());
		baseModules.add(sitemapModule());

        baseModules.add(new ThreadScopeModule());
        baseModules.add(new UIScopeModule());
        baseModules.add(new VaadinSessionScopeModule());

        baseModules.add(new ServicesMonitorModule());

        baseModules.add(shiroModule());
        baseModules.add(shiroVaadinModule());
        baseModules.add(new ShiroAopModule());

        baseModules.add(servletModule());

        baseModules.add(viewModule());

        baseModules.add(userModule());

        baseModules.add(userOptionModule());

        addAppModules(baseModules);
        addSitemapModules(baseModules);
        return baseModules;
    }

    /**
     * Override this if you have provided your own {@link UserOptionModule}
     *
     * @return
     */
    protected Module userOptionModule() {
        return new UserOptionModule();
    }

    protected Module i18NModule() {
        return new I18NModule();
    }

    protected Module applicationConfigurationModule() {
        return new ApplicationConfigurationModule();
    }

    /**
	 * Modules used in the creation of the {@link MasterSitemap} do not actually need to be separated, this just makes a
     * convenient way of seeing them as a group
     *
     * @param baseModules
     */
    protected void addSitemapModules(List<Module> baseModules) {
    }

    /**
     * Override this if you have provided your own {@link ServletModule}
     *
     * @return
     */
    protected Module servletModule() {
        return new BaseServletModule();
    }

    /**
     * Override this method if you have sub-classed {@link ShiroVaadinModule} to provide your own bindings for Shiro
     * related exceptions.
     *
     * @return
     */
    protected Module shiroVaadinModule() {
        return new ShiroVaadinModule();
    }

	private Module sitemapModule() {
		return new SitemapModule();
    }

    /**
     * Override this if you have sub-classed {@link ViewModule} to provide bindings to your own standard page views
     */
    protected Module viewModule() {
        return new ViewModule();
    }

    /**
     * Override this method if you have sub-classed {@link StandardShiroModule} to provide bindings to your Shiro
     * related implementations (for example, {@link Realm} and {@link CredentialsMatcher}
     *
     * @param servletContext
     * @param ini
     * @return
     */

    protected Module shiroModule() {
        return new StandardShiroModule();
    }

    /**
	 * Override this if you have sub-classed {@link UserModule} to provide bindings to your user related implementations
     */
    private Module userModule() {
        return new UserModule();
    }

    /**
     * Add as many application specific Guice modules as you wish by overriding this method.
     *
     * @param baseModules
     * @param ini
     */
    protected abstract void addAppModules(List<Module> baseModules);

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		log.info("Stopping services");
		try {
			Injector injector = getInjector(false);
			if(injector != null) {
				injector.getInstance(ServicesMonitor.class).stopAllServices();
			}
		} catch (Exception e) {
			log.error("Exception while stopping services", e);
		}
		super.contextDestroyed(servletContextEvent);
	}
}