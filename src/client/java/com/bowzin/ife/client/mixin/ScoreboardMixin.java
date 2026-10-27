package com.bowzin.ife.client.mixin;

import net.minecraft.world.scores.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.bowzin.ife.client.events.IFEEvents.SCOREBOARD_UPDATE_EVENT;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
	// TODO(bug/HIGH, verify against the server): onObjectiveChanged only fires when the objective's display name, render
	//  type or number format changes. It does NOT fire for score changes (onScoreChanged), team prefix/suffix updates
	//  (onTeamChanged, which is how most sidebars that use formatNameForTeam push line text), or when the sidebar is first
	//  assigned (setDisplayObjective). Coins/keys/players/multiplier and the improper-dismount check only update when the
	//  server happens to touch the title. Also hook those methods, and debounce to once per tick (see the ScoreboardData TODO).
	@Inject(at = @At("TAIL"), method = "onObjectiveChanged")
	private void ife$onObjectiveChanged(Objective objective, CallbackInfo ci) {
		if(objective.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR)==objective) {
			SCOREBOARD_UPDATE_EVENT.invoker().onUpdate(objective);
		}
	}
}