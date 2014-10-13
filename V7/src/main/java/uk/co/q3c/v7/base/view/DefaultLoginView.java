/*
 * Copyright (c) 2014 David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package uk.co.q3c.v7.base.view;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ChameleonTheme;

import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;

import uk.co.q3c.util.ID;
import uk.co.q3c.v7.base.shiro.LoginExceptionHandler;
import uk.co.q3c.v7.base.shiro.SubjectProvider;
import uk.co.q3c.v7.i18n.*;

public class DefaultLoginView extends GridViewBase implements LoginView,
		ClickListener {
	private final LoginExceptionHandler loginExceptionHandler;
	private final Provider<Subject> subjectProvider;
	private final Translate translate;
	private Label demoInfoLabel;
	private Label demoInfoLabel2;
	@I18NValue(value = LabelKey.Authentication)
	private Label label;
	private PasswordField passwordBox;
	private Label statusMsgLabel;
	private Button submitButton;
	@I18N(caption = LabelKey.User_Name, description = DescriptionKey.Enter_your_user_name)
	private TextField usernameBox;

	@Inject
	protected DefaultLoginView(LoginExceptionHandler loginExceptionHandler,
			SubjectProvider subjectProvider, Translate translate) {
		super();
		this.loginExceptionHandler = loginExceptionHandler;
		this.subjectProvider = subjectProvider;
		this.translate = translate;

		getRootComponent().setColumns(3);
		getRootComponent().setRows(3);
		getRootComponent().setSizeFull();
		Panel centrePanel = new Panel("Log in"); // TODO i18N
		centrePanel.addStyleName(ChameleonTheme.PANEL_BUBBLE);
		centrePanel.setSizeUndefined();
		VerticalLayout vl = new VerticalLayout();
		centrePanel.setContent(vl);
		vl.setSpacing(true);
		vl.setSizeUndefined();
		label = new Label();
		usernameBox = new TextField();
		passwordBox = new PasswordField("password");

		demoInfoLabel = new Label(
				"for this demo, enter any user name, and a password of 'password'");
		demoInfoLabel2 = new Label(
				"In a real application your Shiro Realm implementation defines how to authenticate");

		submitButton = new Button("submit");
		submitButton.addClickListener(this);

		statusMsgLabel = new Label("Please enter your username and password");

		vl.addComponent(label);
		vl.addComponent(demoInfoLabel);
		vl.addComponent(demoInfoLabel2);
		vl.addComponent(usernameBox);
		vl.addComponent(passwordBox);
		vl.addComponent(submitButton);
		vl.addComponent(statusMsgLabel);

		getRootComponent().addComponent(centrePanel, 1, 1);
		getRootComponent().setColumnExpandRatio(0, 1);
		getRootComponent().setColumnExpandRatio(2, 1);

		getRootComponent().setRowExpandRatio(0, 1);
		getRootComponent().setRowExpandRatio(2, 1);
	}

	@Override
	protected void setIds() {
		super.setIds();
		submitButton.setId(ID.getId(this, submitButton));
		usernameBox.setId(ID.getId("username", this, usernameBox));
		passwordBox.setId(ID.getId("password", this, passwordBox));
		statusMsgLabel.setId(ID.getId("status", this, statusMsgLabel));
	}

	@Override
	public void buttonClick(ClickEvent event) {
		UsernamePasswordToken token = new UsernamePasswordToken(
				usernameBox.getValue(), passwordBox.getValue());
		try {
			subjectProvider.get().login(token);
		} catch (UnknownAccountException uae) {
			loginExceptionHandler.unknownAccount(this, token, uae);
		} catch (IncorrectCredentialsException ice) {
			loginExceptionHandler.incorrectCredentials(this, token, ice);
		} catch (ExpiredCredentialsException ece) {
			loginExceptionHandler.expiredCredentials(this, token, ece);
		} catch (LockedAccountException lae) {
			loginExceptionHandler.accountLocked(this, token, lae);
		} catch (ExcessiveAttemptsException excess) {
			loginExceptionHandler.excessiveAttempts(this, token, excess);
		} catch (DisabledAccountException dae) {
			loginExceptionHandler.disabledAccount(this, token, dae);
		} catch (ConcurrentAccessException cae) {
			loginExceptionHandler.concurrentAccess(this, token, cae);
		} catch (AuthenticationException ae) {
			loginExceptionHandler.genericException(this, token, ae);
		}
		// unexpected condition - error?
		// an exception would be raised if login failed
	}

	@Override
	public void setUsername(String username) {
		usernameBox.setValue(username);
	}

	@Override
	public void setPassword(String password) {
		passwordBox.setValue(password);
	}

	@Override
	public Button getSubmitButton() {
		return submitButton;
	}

	@Override
	public String getStatusMessage() {
		return statusMsgLabel.getValue();
	}

	@Override
	public void setStatusMessage(I18NKey<?> messageKey) {
		setStatusMessage(translate.from(messageKey));
	}

	@Override
	public void setStatusMessage(String msg) {
		statusMsgLabel.setValue(msg);
	}

	public TextField getUsernameBox() {
		return usernameBox;
	}

	public PasswordField getPasswordBox() {
		return passwordBox;
	}

	@Override
	public String viewName() {

		return getClass().getSimpleName();
	}

}
