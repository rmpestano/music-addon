
package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.model.SongsFilter;
import com.github.forge.addon.music.util.AppCache;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;

import javax.inject.Inject;

/**
 * Created by pestano on 16/08/15.
 */
public class SearchCommand extends AbstractUICommand implements UIWizard {


	@Inject
	@WithAttributes(label = "Title", description = "Filter by song title", type = InputType.DEFAULT)
	private UIInput<String> title;

	@Inject
	@WithAttributes(label = "Artist", description = "Filter by song artist", type = InputType.DEFAULT)
	private UIInput<String> artist;

	@Inject
	@WithAttributes(label = "Album", description = "Filter by song album", type = InputType.DEFAULT)
	private UIInput<String> album;

	@Inject
	@WithAttributes(label = "Genre", description = "Filter by song genre")
	private UISelectOne<String> genre;

	@Inject
	private SongsFilter songsFilter;

	@Inject
	private AppCache appCache;



	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(SearchCommand.class).description("Search songs in any playlists")
				.name("Music: Search").category(Categories.create("Music"));
	}

	@Override
	public void initializeUI(UIBuilder uiBuilder) throws Exception {
		genre.setValueChoices(appCache.getGenres());
		uiBuilder.add(title).add(artist).add(album).add(genre);
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
		return null;

	}

	@Override
	public NavigationResult next(UINavigationContext context) throws Exception {
		songsFilter.filter(artist.getValue(),title.getValue(),album.getValue(),genre.getValue());
		return context.navigateTo(SearchCommandStep.class);
	}
}
