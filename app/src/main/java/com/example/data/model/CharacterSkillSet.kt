package com.example.data.model

data class SkillItem(
    val name: String,
    val character: String,
    val type: String, // "Active" or "Passive"
    val description: String,
    val tag: String
)

data class LoadoutItem(
    val name: String,
    val effect: String,
    val tacticalUsage: String
)

data class PetItem(
    val name: String,
    val skillName: String,
    val effect: String,
    val synergy: String
)

data class CharacterCombination(
    val roleId: String,
    val roleName: String,
    val roleTitle: String,
    val roleDescription: String,
    val activeSkill: SkillItem,
    val passiveSkills: List<SkillItem>,
    val loadout: LoadoutItem,
    val pet: PetItem,
    val recommendedWeapons: List<String>,
    val tacticalTips: String
)

object CharacterSkillDatabase {
    val combinations: List<CharacterCombination> = listOf(
        CharacterCombination(
            roleId = "primary_rusher",
            roleName = "Primary Rusher",
            roleTitle = "Entry Frag & Close Quarters Domination",
            roleDescription = "Spearheads aggressive pushes, initiates head-on duels, and dismantles enemy defensive setups instantly.",
            activeSkill = SkillItem(
                name = "Rebel Rush",
                character = "Tatsuya",
                type = "Active",
                description = "Dashes forward at rapid speed for 0.3s. Can be stacked for 2 consecutive bursts with rapid cooldown.",
                tag = "Speed & Evasion"
            ),
            passiveSkills = listOf(
                SkillItem(
                    name = "Bushido",
                    character = "Hayato (Awakened)",
                    type = "Passive",
                    description = "When max HP drops by 13%, armor penetration increases by 10% and frontal damage taken is reduced.",
                    tag = "Armor Piercing"
                ),
                SkillItem(
                    name = "Dash",
                    character = "Kelly (Awakened)",
                    type = "Passive",
                    description = "Sprinting speed boosted by 6%. After 4s of sprinting, first shot deals 106% bonus damage.",
                    tag = "Agility Boost"
                ),
                SkillItem(
                    name = "Fight or Flight",
                    character = "Luna",
                    type = "Passive",
                    description = "Increases rate of fire by up to 10%. Surplus rate of fire converts dynamically into movement agility.",
                    tag = "Fire Rate"
                )
            ),
            loadout = LoadoutItem(
                name = "Pocket Market",
                effect = "Provides instant mini-store to buy medkits, ammo, and Gloo walls during intense engagements.",
                tacticalUsage = "Buy instant gloo walls or repair kits immediately after winning an entry duel."
            ),
            pet = PetItem(
                name = "Rockie",
                skillName = "Stay Chill",
                effect = "Reduces active skill cooldown time by 15%.",
                synergy = "Allows Tatsuya dashes to replenish almost instantly for repeated repositioning."
            ),
            recommendedWeapons = listOf("MP40", "M1887", "MAC10", "Trogon"),
            tacticalTips = "Use Tatsuya's double burst to break enemy crosshair lock and close the gap for point-blank shotgun blasts."
        ),
        CharacterCombination(
            roleId = "secondary_rusher",
            roleName = "Secondary Rusher",
            roleTitle = "Crossfire Support & Re-fragger",
            roleDescription = "Backs up the primary rusher, executes re-frags if entry falls, and lays continuous suppression fire.",
            activeSkill = SkillItem(
                name = "Senses Shockwave",
                character = "Homer",
                type = "Active",
                description = "Releases a drone that searches for the nearest enemy in 100m, triggering a 5m pulse explosion slowing movement by 60% and firing rate by 35%.",
                tag = "Debuff & Crowd Control"
            ),
            passiveSkills = listOf(
                SkillItem(
                    name = "Hacker's Eye",
                    character = "Moco (Awakened)",
                    type = "Passive",
                    description = "Tags shot enemies for up to 6.5s, sharing continuous position tracking with the entire squad.",
                    tag = "Target Tracking"
                ),
                SkillItem(
                    name = "Bullet Beats",
                    character = "D-Bee",
                    type = "Passive",
                    description = "When firing while moving, movement speed increases by 30% and shooting accuracy increases by 60%.",
                    tag = "Hip-Fire Accuracy"
                ),
                SkillItem(
                    name = "Bushido",
                    character = "Hayato",
                    type = "Passive",
                    description = "Increased armor penetration when HP decreases, ensuring high damage output during trades.",
                    tag = "Combat Damage"
                )
            ),
            loadout = LoadoutItem(
                name = "Bounty Token",
                effect = "First elimination grants 400 FF coins and powerful tier-3 weapons and vests.",
                tacticalUsage = "Allows fast economy ramp-up in the first minutes of battle royale."
            ),
            pet = PetItem(
                name = "Flash",
                skillName = "Steel Shell",
                effect = "Reduces damage taken from behind (from FF Knife/bullets) by up to 25%.",
                synergy = "Guards against third-party ambush while you clean up the primary rusher's battle."
            ),
            recommendedWeapons = listOf("UMP", "Groza", "MAG-7", "Bizon"),
            tacticalTips = "Release Homer's drone right as your point rusher pushes; enemies caught in the shockwave are defenseless."
        ),
        CharacterCombination(
            roleId = "rusher",
            roleName = "Rusher (All-Round)",
            roleTitle = "Solo Clutch & Aggressive Skirmisher",
            roleDescription = "Versatile aggressive playstyle blending shield defense, relentless assault, and fast retreat options.",
            activeSkill = SkillItem(
                name = "Drop the Beat",
                character = "Alok (Awakened)",
                type = "Active",
                description = "Creates a 5m aura that increases movement speed by 15% and restores 3 HP/s for 10s. Music notes buff teammates.",
                tag = "Speed & Regen"
            ),
            passiveSkills = listOf(
                SkillItem(
                    name = "Dash",
                    character = "Kelly",
                    type = "Passive",
                    description = "Flat 6% sprinting velocity boost for smooth jukes and rapid rotations.",
                    tag = "Mobility"
                ),
                SkillItem(
                    name = "Hat Trick",
                    character = "Luqueta",
                    type = "Passive",
                    description = "Every kill raises maximum permanent HP by 25 (up to +50 extra maximum HP buffer).",
                    tag = "Max Health Stacking"
                ),
                SkillItem(
                    name = "Bushido",
                    character = "Hayato",
                    type = "Passive",
                    description = "Increases armor penetration as HP drops, guaranteeing lethal counter-punches.",
                    tag = "Penetration"
                )
            ),
            loadout = LoadoutItem(
                name = "Armor Crate",
                effect = "Spawns with Level 2 Helmet and Vest, plus auto-repairing vest durability every round.",
                tacticalUsage = "Guarantees immediate early-game survivability against early rushers."
            ),
            pet = PetItem(
                name = "Mr. Waggor",
                skillName = "Smooth Gloo",
                effect = "Produces a free Gloo Wall grenade every 100 seconds when player has fewer than 2 walls.",
                synergy = "Ensures you never run out of vital cover during aggressive open-field skirmishes."
            ),
            recommendedWeapons = listOf("Thompson", "Charge Buster", "Woodpecker", "Vector"),
            tacticalTips = "Activate Alok right before pushing around Gloo walls to combine high strafe speed with continuous health recovery."
        ),
        CharacterCombination(
            roleId = "sniper",
            roleName = "Sniper",
            roleTitle = "Long-Range Lethality & Precision Scout",
            roleDescription = "Controls long-distance sightlines, eliminates enemies before they enter engagement range, and silences flanks.",
            activeSkill = SkillItem(
                name = "Camouflage",
                character = "Wukong",
                type = "Active",
                description = "Transforms into a bush for 15s with reduced speed. Taking down an enemy resets cooldown immediately.",
                tag = "Invisibility & Reset"
            ),
            passiveSkills = listOf(
                SkillItem(
                    name = "Dead Silent",
                    character = "Rafael",
                    type = "Passive",
                    description = "Silences firing sound of Sniper and Marksman Rifles. Downed enemies bleed out 85% faster.",
                    tag = "Silencer & Fast Bleed"
                ),
                SkillItem(
                    name = "Sharp Shooter",
                    character = "Laura",
                    type = "Passive",
                    description = "Accuracy increases by 50% while aiming through any scope.",
                    tag = "Scope Accuracy"
                ),
                SkillItem(
                    name = "Hacker's Eye",
                    character = "Moco",
                    type = "Passive",
                    description = "Tags scoped targets on hit, revealing their behind-cover trajectory for follow-up wallbangs.",
                    tag = "Vision Tracking"
                )
            ),
            loadout = LoadoutItem(
                name = "Secret Clue",
                effect = "Displays next safe zone location on the minimap and drops bonus tactical supplies.",
                tacticalUsage = "Enables early positioning on high-ground towers inside the upcoming safe zone."
            ),
            pet = PetItem(
                name = "Beaston",
                skillName = "Helping Hand",
                effect = "Throwing distance of grenades, Gloo walls, and flashbangs increased by 30%.",
                synergy = "Allows snipers to provide distant throwable support to teammates from extreme elevation."
            ),
            recommendedWeapons = listOf("AWM", "Barrett M82B", "SVD-Y", "Woodpecker"),
            tacticalTips = "Rafael's passive automatically silences all sniper shots—remain masked while picking off moving enemies."
        ),
        CharacterCombination(
            roleId = "zone_pusher",
            roleName = "Zone Pusher",
            roleTitle = "Zone Edge Dominance & High Durability Survivor",
            roleDescription = "Master of surviving outside the safe zone, holding edge choke points, and outlasting opponents in late zones.",
            activeSkill = SkillItem(
                name = "Master of All",
                character = "K (Captain Booyah)",
                type = "Active",
                description = "Max EP increased to 250. Jiu-jitsu mode converts EP to HP 500% faster. Psychology mode recovers 3 EP every 2s.",
                tag = "Infinite EP & Rapid Regen"
            ),
            passiveSkills = listOf(
                SkillItem(
                    name = "Gluttony",
                    character = "Maxim",
                    type = "Passive",
                    description = "Eating mushrooms and using medkits is 25% faster, allowing emergency in-zone heals.",
                    tag = "Fast Healing"
                ),
                SkillItem(
                    name = "Nutty Movement",
                    character = "Joseph",
                    type = "Passive",
                    description = "Immune to disruptive slowing effects and movement speed boosted by 10% for 5s upon taking damage.",
                    tag = "Zone Sprint"
                ),
                SkillItem(
                    name = "Silent Sentinel",
                    character = "J.Biebs",
                    type = "Passive",
                    description = "Deducts 12% damage using EP instead of HP, dramatically lowering zone tick damage.",
                    tag = "EP Damage Shield"
                )
            ),
            loadout = LoadoutItem(
                name = "Bonfire",
                effect = "Places a campfire that rapidly regenerates HP and EP for all teammates nearby.",
                tacticalUsage = "Drop the bonfire during phase 4 and phase 5 edge shifts to sustain through extreme tick damage."
            ),
            pet = PetItem(
                name = "Ottero",
                skillName = "Double Blubber",
                effect = "When using Treatment Gun or Medkit, recovers extra EP equal to 65% of the HP restored.",
                synergy = "Keeps Captain K's EP reserves completely full for nonstop automatic health conversion."
            ),
            recommendedWeapons = listOf("AC80", "SCAR-L", "UMP", "Charge Buster"),
            tacticalTips = "Keep K in Jiu-jitsu mode when fighting inside or near zone borders to out-heal zone tick damage in clutches."
        ),
        CharacterCombination(
            roleId = "support_healer",
            roleName = "Support / Healer",
            roleTitle = "Squad Lifeline & Resuscitation Specialist",
            roleDescription = "Keeps teammates alive through revives, sustained area healing, and team-wide shields.",
            activeSkill = SkillItem(
                name = "Healing Heartbeat",
                character = "Dimitri",
                type = "Active",
                description = "Creates a 3.5m healing zone: regenerates 10 HP/s for 12s. Downed teammates and yourself can self-revive inside.",
                tag = "Area Heal & Self Revive"
            ),
            passiveSkills = listOf(
                SkillItem(
                    name = "Healing Touch",
                    character = "Olivia",
                    type = "Passive",
                    description = "Increases single-target healing effects by 80% and grants nearby teammates 80% of healing received.",
                    tag = "Aura Healing Multiplier"
                ),
                SkillItem(
                    name = "Gangster's Spirit",
                    character = "Antonio",
                    type = "Passive",
                    description = "Gains 40 extra Shield Points when match starts or after surviving combat encounters.",
                    tag = "Shield Points"
                ),
                SkillItem(
                    name = "Parting Gift",
                    character = "Thiva",
                    type = "Passive",
                    description = "Rescue (revive) speed increased by 70%. Successfully reviving gives 60 HP to the rescued player.",
                    tag = "Instant Revive"
                )
            ),
            loadout = LoadoutItem(
                name = "Airdrop",
                effect = "Calls down private resupply crate packed with level 3 defensive gear and heavy weapons.",
                tacticalUsage = "Provides instant re-kit for recovered teammates during final circles."
            ),
            pet = PetItem(
                name = "Rockie",
                skillName = "Stay Chill",
                effect = "Reduces Dimitri's active healing aura cooldown by 15%.",
                synergy = "Enables reliable revive auras across back-to-back team fights."
            ),
            recommendedWeapons = listOf("Treatment Laser Gun", "M4A1-Z", "Bizon", "M1014"),
            tacticalTips = "Cast Dimitri behind a Gloo wall as soon as a teammate gets knocked; Thiva's passive will revive them in under 1 second!"
        ),
        CharacterCombination(
            roleId = "flanker",
            roleName = "Flanker / Solo vs Squad",
            roleTitle = "Stealth Hunter & Multi-Angle Eliminator",
            roleDescription = "Takes wide rotation angles to catch rotated teams off guard, breaking enemy battle lines from unexpected angles.",
            activeSkill = SkillItem(
                name = "Time Turner",
                character = "Chrono",
                type = "Active",
                description = "Creates an impenetrable force field that blocks 800 damage for 6s. Prevents enemies from shooting inside.",
                tag = "Bulletproof Dome"
            ),
            passiveSkills = listOf(
                SkillItem(
                    name = "Dash",
                    character = "Kelly",
                    type = "Passive",
                    description = "Constant 6% running speed boost for rapid perimeter movement.",
                    tag = "Flank Speed"
                ),
                SkillItem(
                    name = "Damage Delivered",
                    character = "Shirou",
                    type = "Passive",
                    description = "When hit by an enemy within 100m, attacker is marked for 6s. First hit on marked enemy has 100% armor pen.",
                    tag = "Counter-Armor Pen"
                ),
                SkillItem(
                    name = "Hacker's Eye",
                    character = "Moco",
                    type = "Passive",
                    description = "Keeps tagged targets visible through foliage and smoke for seamless angle tracking.",
                    tag = "Wall Tracking"
                )
            ),
            loadout = LoadoutItem(
                name = "Pocket Market",
                effect = "Instant medkits and utility refills without needing to loot exposed death boxes.",
                tacticalUsage = "Buy Gloo walls immediately when isolated during a long flank."
            ),
            pet = PetItem(
                name = "Falco",
                skillName = "Skyline Spree",
                effect = "Gliding speed increased by 50% and diving speed after parachute opens increased by 45%.",
                synergy = "Land first on flanking vantage points to claim compound control before other squads."
            ),
            recommendedWeapons = listOf("Groza", "MP5-III", "M1887", "Woodpecker"),
            tacticalTips = "Pop Chrono's dome in the open to absorb enemy fire safely while using medkits or repositioning."
        ),
        CharacterCombination(
            roleId = "igl_tactician",
            roleName = "IGL / Tactician",
            roleTitle = "In-Game Leader, Squad Vision & Macro Shot-Caller",
            roleDescription = "Directs team rotations, dictates engagement timing, predicts safe circles, and provides vision advantages.",
            activeSkill = SkillItem(
                name = "Enigma's Gift",
                character = "Iris / Steffie",
                type = "Active",
                description = "Attacking Gloo walls penetrates and damages enemies behind them, nullifying enemy defensive bastions.",
                tag = "Wall Penetration"
            ),
            passiveSkills = listOf(
                SkillItem(
                    name = "Hacker's Eye",
                    character = "Moco",
                    type = "Passive",
                    description = "Broadcasts exact locations of fleeing or repositioning opponents to the entire squad.",
                    tag = "Team Intel"
                ),
                SkillItem(
                    name = "Hat Trick",
                    character = "Luqueta",
                    type = "Passive",
                    description = "Stacks bonus 50 HP for extra health buffer during intense late-game calls.",
                    tag = "Survivability"
                ),
                SkillItem(
                    name = "Bushido",
                    character = "Hayato",
                    type = "Passive",
                    description = "Increases damage penetration during endgame 1v1 and 2v2 tactical standoffs.",
                    tag = "Clutch Armor Pen"
                )
            ),
            loadout = LoadoutItem(
                name = "Secret Clue",
                effect = "Reveals the next safe zone circle on the minimap.",
                tacticalUsage = "Critical for calling rotational commands and setting up ambushes ahead of the circle shift."
            ),
            pet = PetItem(
                name = "Dreki",
                skillName = "Dragon Glare",
                effect = "Detects up to 4 opponents using medkits within a 30m radius for 5 seconds.",
                synergy = "Calls exact locations of injured enemies healing behind Gloo walls for coordinated squad breaches."
            ),
            recommendedWeapons = listOf("SCAR", "AC80", "MAG-7", "M4A1"),
            tacticalTips = "Use Secret Clue to take prime defensive compounds inside the white circle before any opposing squad arrives."
        )
    )
}
