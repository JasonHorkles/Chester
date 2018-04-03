package org.moss.discord.listeners;

import java.awt.Color;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.UncachedMessageUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.moss.discord.Constants;

public class StarboardListener implements ReactionAddListener {

    private DiscordApi api;

    // TODO: Store this permanently, obviously this isn't going to work in-memory
    private Set<Long> starredMessages = new HashSet<Long>();

    public StarboardListener(DiscordApi dApi) {
        api = dApi;
    }

	@Override
	public void onReactionAdd(ReactionAddEvent event) {
        if (starredMessages.contains(event.getMessageId())) return;
        //System.out.println("reaction add star first");

        Optional<Message> mOptional = event.getMessage();
        Message message = mOptional.orElseGet(() -> 
            api.getMessageById(event.getMessageId(), event.getChannel()).join());

        int totalStars = message.getReactions().stream()
            .filter(r -> r.getEmoji().isUnicodeEmoji())
            .mapToInt(Reaction::getCount)
            .sum();
        
        System.out.println("Total stars: " + totalStars);

        if (totalStars >= Constants.STARS_MINIMUM) {
            postStarboard(message);
        }
    }
    
    private void postStarboard(Message message) {
        Optional<ServerChannel> starboardChannel = message.getServer().get().getChannelById(Constants.CHANNEL_STARBOARD);
        if (!starboardChannel.isPresent()) return; // Starboard is disabled

        EmbedBuilder embed = new EmbedBuilder();

        String author = message.getAuthor().getName();
        String channel = message.getServerTextChannel().get().getName();
        String content = message.getContent();

        embed.setTitle(author + " in " + channel);
        embed.setAuthor(message.getAuthor());
        embed.setColor(Color.ORANGE);
        embed.setDescription(content);

        starboardChannel.get().asTextChannel().get().sendMessage(embed).join();
    }

}