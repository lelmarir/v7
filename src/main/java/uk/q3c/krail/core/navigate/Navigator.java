package uk.q3c.krail.core.navigate;

import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.StandardViewKey;
import uk.q3c.krail.core.ui.ScopedUI;
import uk.q3c.krail.core.view.KrailView;
import uk.q3c.krail.core.view.KrailViewChangeNotifier;

import com.vaadin.server.Page.UriFragmentChangedListener;

/**
 * Uses the {@link UserSitemap} to control navigation from one 'page' to another, using a uri String, or a
 * {@link StandardViewKey} or a {@link UserSitemapNode} to identify a page.<br>
 * <br>
 * Even though {@link UserSitemapNode} should have already been verified for authorisation, all page navigation is
 * checked for authorisation. <br>
 * <br>
 * Looks up the view for the supplied URI, or {@link UserSitemapNode} and calls on {@link ScopedUI} to present that
 * view. Listeners are notified before and after a change of view occurs. The {@link #loginSuccessful()} method is
 * called after a successful user login - this allows the navigator to change views appropriately (according to the
 * implementation). Typically this would be to either return to the view where the user was before they went to the
 * login page, or perhaps to a specified landing page (Page here refers really to a KrailView - a "virtual page"). <br>
 * <br>
 * The navigator must also respond to a change in user status (logged in or out) - logging out just navigates to the
 * logout page, while logging in applies some logic, see {@link #userStatusChanged()}
 * 
 * @author David Sowerby 20 Jan 2013
 * 
 */
public interface Navigator extends UriFragmentChangedListener,
		KrailViewChangeNotifier {

	void navigateTo(String fragment) throws InvalidURIException;

	/**
	 * A convenience method to look up the URI fragment for the {@link StandardViewKey} and navigate to it
	 * 
	 * @param pageKey
	 */
	void navigateTo(StandardViewKey pageKey);

	/**
	 * Navigates to the location represented by {@code navigationState}, which may include parameters
	 * 
	 * @param navigationState
	 */
	void navigateTo(NavigationState navigationState);

	<T extends KrailView> void navigateTo(Class<T> viewClass);

	<T extends KrailView> void navigateTo(Class<T> viewClass,
			NavigationCallbackHandler<T> callbackHandler);

	/**
	 * Returns the NavigationState representing the current position of the
	 * navigator
	 * 
	 * @return
	 */
	NavigationState getCurrentNavigationState();

	/**
	 * Returns the NavigationState representing the previous position of the
	 * navigator
	 * 
	 * @return
	 */
	NavigationState getPreviousNavigationState();

	void navigateToErrorView(Throwable throwable);

	/**
	 * Update the visible fragment to the current 
	 */
	void updateUriFragment();

}