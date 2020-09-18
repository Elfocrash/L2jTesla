trap finish 2

configure() {

# Loginserver
LSDBHOST="localhost"
LSDB="l2jtesla"
LSUSER="root"
LSPASS=""

# Gameserver
GSDBHOST="localhost"
GSDB="l2jtesla"
GSUSER="root"
GSPASS=""

echo "                        L2jTesla database installation"
echo "                        __________________________"
echo ""

MYSQLDUMPPATH=`which mysqldump 2>/dev/null`
MYSQLPATH=`which mysql 2>/dev/null`
if [ $? -ne 0 ]; then
echo "We were unable to find MySQL binaries on your path"
while :
 do
  echo -ne "\nPlease enter MySQL binaries directory (no trailing slash): "
  read MYSQLBINPATH
    if [ -e "$MYSQLBINPATH" ] && [ -d "$MYSQLBINPATH" ] && [ -e "$MYSQLBINPATH/mysqldump" ] && [ -e "$MYSQLBINPATH/mysql" ]; then
       MYSQLDUMPPATH="$MYSQLBINPATH/mysqldump"
       MYSQLPATH="$MYSQLBINPATH/mysql"
       break
    else
       echo "The data you entered is invalid. Please verify and try again."
       exit 1
    fi
 done
fi
MYL="$MYSQLPATH -h $LSDBHOST -u $LSUSER --password=$LSPASS -D $LSDB"
MYG="$MYSQLPATH -h $GSDBHOST -u $GSUSER --password=$GSPASS -D $GSDB"

echo "OPTIONS : (f) full install, it will destroy all."
echo "          (s) skip characters data, it will install only static server tables."
echo ""
echo -ne "Installation type: (f) full, (s) skip or (q) quit? "

read PROMPT
case "$PROMPT" in
	"f"|"F") fullinstall; upgradeinstall I;;
	"s"|"S") skip; upgradeinstall U;;
	"q"|"Q") finish;;
	*) configure;;
esac
}

fullinstall(){
echo "Deleting all tables for new content."
$MYG < gs_install.sql &> /dev/null
$MYG < full_install.sql &> /dev/null
$MYL < full_install.sql &> /dev/null
}

skip(){
echo "Deleting all gameserver tables for new content."
$MYG < gs_install.sql &> /dev/null
}

upgradeinstall(){
if [ "$1" == "I" ]; then 
echo "Installling new content."
else
echo "Upgrading gameserver content"
fi

$MYL < ../sql/accounts.sql &> /dev/null
$MYL < ../sql/gameservers.sql &> /dev/null

$MYG < ../sql/auction_bid.sql &> /dev/null
$MYG < ../sql/augmentations.sql &> /dev/null
$MYG < ../sql/bookmarks.sql &> /dev/null
$MYG < ../sql/buffer_schemes.sql &> /dev/null
$MYG < ../sql/buylists.sql &> /dev/null
$MYG < ../sql/castle.sql &> /dev/null
$MYG < ../sql/castle_doorupgrade.sql &> /dev/null
$MYG < ../sql/castle_manor_procure.sql &> /dev/null
$MYG < ../sql/castle_manor_production.sql &> /dev/null
$MYG < ../sql/castle_trapupgrade.sql &> /dev/null
$MYG < ../sql/character_friends.sql &> /dev/null
$MYG < ../sql/character_hennas.sql &> /dev/null
$MYG < ../sql/character_macroses.sql &> /dev/null
$MYG < ../sql/character_mail.sql &> /dev/null
$MYG < ../sql/character_memo.sql &> /dev/null
$MYG < ../sql/character_quests.sql &> /dev/null
$MYG < ../sql/character_raid_points.sql &> /dev/null
$MYG < ../sql/character_recipebook.sql &> /dev/null
$MYG < ../sql/character_recommends.sql &> /dev/null
$MYG < ../sql/character_shortcuts.sql &> /dev/null
$MYG < ../sql/character_skills.sql &> /dev/null
$MYG < ../sql/character_skills_save.sql &> /dev/null
$MYG < ../sql/character_subclasses.sql &> /dev/null
$MYG < ../sql/characters.sql &> /dev/null
$MYG < ../sql/clan_data.sql &> /dev/null
$MYG < ../sql/clan_privs.sql &> /dev/null
$MYG < ../sql/clan_skills.sql &> /dev/null
$MYG < ../sql/clan_subpledges.sql &> /dev/null
$MYG < ../sql/clan_wars.sql &> /dev/null
$MYG < ../sql/clanhall.sql &> /dev/null
$MYG < ../sql/clanhall_functions.sql &> /dev/null
$MYG < ../sql/cursed_weapons.sql &> /dev/null
$MYG < ../sql/fishing_championship.sql &> /dev/null
$MYG < ../sql/forums.sql &> /dev/null
$MYG < ../sql/games.sql &> /dev/null
$MYG < ../sql/grandboss_data.sql &> /dev/null
$MYG < ../sql/grandboss_list.sql &> /dev/null
$MYG < ../sql/heroes_diary.sql &> /dev/null
$MYG < ../sql/heroes.sql &> /dev/null
$MYG < ../sql/items.sql &> /dev/null
$MYG < ../sql/items_on_ground.sql &> /dev/null
$MYG < ../sql/mdt_bets.sql &> /dev/null
$MYG < ../sql/mdt_history.sql &> /dev/null
$MYG < ../sql/mods_wedding.sql &> /dev/null
$MYG < ../sql/olympiad_data.sql&> /dev/null
$MYG < ../sql/olympiad_fights.sql&> /dev/null
$MYG < ../sql/olympiad_nobles_eom.sql&> /dev/null
$MYG < ../sql/olympiad_nobles.sql&> /dev/null
$MYG < ../sql/pets.sql &> /dev/null
$MYG < ../sql/posts.sql &> /dev/null
$MYG < ../sql/raidboss_spawnlist.sql &> /dev/null
$MYG < ../sql/random_spawn.sql &> /dev/null
$MYG < ../sql/random_spawn_loc.sql &> /dev/null
$MYG < ../sql/server_memo.sql &> /dev/null
$MYG < ../sql/seven_signs.sql &> /dev/null
$MYG < ../sql/seven_signs_festival.sql &> /dev/null
$MYG < ../sql/seven_signs_status.sql &> /dev/null
$MYG < ../sql/siege_clans.sql &> /dev/null
$MYG < ../sql/spawnlist_4s.sql &> /dev/null
$MYG < ../sql/spawnlist.sql &> /dev/null
$MYG < ../sql/topic.sql &> /dev/null
echo ""
echo "Was fast, isn't it ?"
}

finish(){
echo ""
echo "Script execution finished."
exit 0
}

clear
configure