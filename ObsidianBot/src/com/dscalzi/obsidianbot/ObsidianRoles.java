package com.dscalzi.obsidianbot;

import net.dv8tion.jda.entities.Role;

public enum ObsidianRoles {

	ADMIN(211525795837902848L),
	DEVELOPER(211525353016000513L),
	BOT(236743037831741440L),
	SEMI_ADMIN(231846021011996672L),
	MODERATOR(211525891719823360L),
	STEWARD(230160357170216960L),
	HOLY_EMINENCE(232628498110480385L),
	EMPEROR(237716301567492106L),
	OVERLORD(237716255140872202L),
	KING(237716082268438528L),
	CEO(237716042212704257L),
	EXECUTIVE(237716012143869953L),
	SUPPORTER(237715976383234048L),
	PREMIUM(237715920800317440L),
	VIP(237715898289356802L),
	PATRON(237720078899085323L),
	EVERYONE(211524927831015424L);
	
	private long id;
	
	ObsidianRoles(long id){
		this.id = id;
	}
	
	public long getId(){
		return this.id;
	}
	
	public String getIdString(){
		return Long.toString(this.id);
	}
	
	public Role getRole(){
		if(ObsidianBot.getStatus() == BotStatus.CONNECTED){
			return ObsidianBot.getInstance().getJDA().getGuildById(Long.toString(ObsidianBot.ocId)).getRoleById(getIdString());
		}
		return null;
	}
	
}
