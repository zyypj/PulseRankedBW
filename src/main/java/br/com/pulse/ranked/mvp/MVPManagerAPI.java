package br.com.pulse.ranked.mvp;

import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.entity.Player;

public interface MVPManagerAPI {

    Player determineMVP(IArena arena);
}
