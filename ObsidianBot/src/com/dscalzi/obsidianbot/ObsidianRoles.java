package com.dscalzi.obsidianbot;

import net.dv8tion.jda.entities.Role;

public enum ObsidianRoles {

	ADMIN("211525795837902848"),
	DEVELOPER("211525353016000513"),
	BOT("236743037831741440"),
	SEMI_ADMIN("231846021011996672"),
	MODERATOR("211525891719823360"),
	STEWARD("230160357170216960"),
	HOLY_EMINENCE("232628498110480385"),
	EMPEROR("237716301567492106"),
	OVERLORD("237716255140872202"),
	KING("237716082268438528"),
	CEO("237716042212704257"),
	EXECUTIVE("237716012143869953"),
	SUPPORTER("237715976383234048"),
	PREMIUM("237715920800317440"),
	VIP("237715898289356802"),
	PATRON("237720078899085323"),
	EVERYONE("211524927831015424");
	
	private String id;
	
	ObsidianRoles(String id){
		this.id = id;
	}
	
	public String getId(){
		return this.id;
	}
	
	public Role getRole(){
		if(ObsidianBot.getStatus() == BotStatus.CONNECTED){
			return ObsidianBot.getInstance().getGuild().getRoleById(id);
		}
		return null;
	}
	
}
