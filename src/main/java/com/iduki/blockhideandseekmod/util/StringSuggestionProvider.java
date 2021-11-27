package com.iduki.blockhideandseekmod.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class StringSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    private final Set<String> suggests;

    public StringSuggestionProvider(String... suggests) {
        this.suggests = Set.of(suggests);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        suggests.forEach(builder::suggest);
        return builder.buildFuture();
    }
}
