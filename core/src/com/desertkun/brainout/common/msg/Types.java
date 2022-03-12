package com.desertkun.brainout.common.msg;

import com.desertkun.brainout.common.editor.*;
import com.desertkun.brainout.common.editor.props.get.EditorGetActivePropertiesMsg;
import com.desertkun.brainout.common.editor.props.get.EditorGetBlockPropertiesMsg;
import com.desertkun.brainout.common.editor.props.get.EditorGetMapPropertiesMsg;
import com.desertkun.brainout.common.editor.props.set.EditorSetActivePropertiesMsg;
import com.desertkun.brainout.common.editor.props.set.EditorSetActivePropertyMsg;
import com.desertkun.brainout.common.editor.props.set.EditorSetBlockPropertiesMsg;
import com.desertkun.brainout.common.editor.props.set.EditorSetMapPropertiesMsg;
import com.desertkun.brainout.common.enums.*;
import com.desertkun.brainout.common.enums.data.*;
import com.desertkun.brainout.common.msg.client.*;
import com.desertkun.brainout.common.msg.client.cards.*;
import com.desertkun.brainout.common.msg.client.editor.*;
import com.desertkun.brainout.common.msg.client.editor2.*;
import com.desertkun.brainout.common.msg.server.*;

import com.desertkun.brainout.common.msg.server.editor.MapListMsg;
import com.desertkun.brainout.common.msg.server.editor.MapSettingsUpdatedMsg;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.interfaces.ActiveLayer;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.esotericsoftware.kryo.Kryo;

import java.util.Date;

public class Types
{
    public static void initClasses(Kryo kryo, Class[] classes)
    {
        for (Class clazz: classes)
        {
            kryo.register(clazz);
        }
    }

    public static void init(Kryo registerTo)
    {
        initClasses(registerTo, new Class[]
        {
            // raw data
            byte[].class, short[].class,
            // date
            Date.class,
            // client -> server, hello
            HelloMsg.class, GameMode.ID.class, PlayState.ID.class,
            // server -> client, version mismatch error
            VersionMismatchMsg.class,
            // server -> client, packages info
            ServerInfo.class, ServerInfo.KeyValue[].class, ServerInfo.KeyValue.class,
            ServerInfo.Price[].class, ServerInfo.Price.class,
            // server -> client, other players info (including myself)
            RemoteClientsMsg.class, RemoteClientsMsg.RemotePlayer[].class, RemoteClientsMsg.RemotePlayer.class,
            // server -> client, new consumableContainer connected
            NewRemoteClientMsg.class,
            // server -> client, some consumableContainer disconnected
            RemoveRemoteClientMsg.class,
            // server -> client, chat message
            ChatMsg.class,
            // client -> server, map voted
            MapVotedMsg.class,
            // client -> server, simple 1-code message
            SimpleMsg.class, SimpleMsg.Code.class,
            // client -> server, send chat message
            ChatSendMsg.class, ChatSendMsg.Mode.class,
            // client -> server, update selection
            UpdateSelectionsMsg.class, UpdateSelectionsMsg.Item.class, UpdateSelectionsMsg.Item[].class,
                // client -> server, spawn
            SpawnMsg.class, NotSpawnMsg.class,
            // server -> client, some new object created
            NewActiveDataMsg.class,
            // server -> client, some object deleted
            DeleteActiveDataMsg.class,
            // client -> server, move the character
            PlayerMoveMsg.class,
            // server -> client, some character moved
            ServerActiveMoveMsg.class, ServerPlayerMoveMsg.class,
            // server -> client, ask client for ping
            PingMsg.class,
            // client -> server, ping success
            PongMsg.class,
            // server -> client, other users ping info
            ClientsInfo.class, ClientsInfo.PingInfo[].class, ClientsInfo.PingInfo.class,
            // client -> server, change my instrument (send id)
            SelectInstrumentMsg.class,
            // server -> client, you should change your instrument (send id)
            ServerSelectInstrumentMsg.class,
            // server -> client, some other consumableContainer selected instrument
            RemoteUpdateInstrumentMsg.class,
            // client -> server, aim msg
            PlayerAimMsg.class,
            // server -> client, some other consumableContainer aimed
            OtherPlayerAimMsg.class,
            // client -> server, consumableContainer fired a bullet
            BulletLaunchMsg.class, Bullet.BulletSlot.class,
            // server -> client, some other consumableContainer fired
            OtherPlayerBulletLaunch.class,
            // server -> client, some other consumableContainer reloaded his instrument
            OtherPlayerInstrumentActionMsg.class, Instrument.Action.class,
            // server -> client, some other consumableContainer reloaded his magazine
            OtherPlayerMagazineActionMsg.class, WeaponMagazineActionMsg.Action.class,
            // server -> client, update consumables
            ConsumablesUpdateMsg.class,
            // server -> client, update consumables reporting only changed things
            LoadAmmoMsg.class,
            // server -> client, some block destroyed
            BlockDestroyMsg.class,
            // server -> client, some user has been hit
            ActiveDamageMsg.class, HitConfirmMsg.class,
            // server -> client, some user killed someone
            KillMsg.class,
            // server -> client, user got notified
            NotifyMsg.class, NotifyReason.class, NotifyAward.class,
            NotifyMethod.class, NotifyData.class, KillND.class, ContentND.class,
            SkillsND.class, LevelND.class, EventRewardND.class, ContentPayloadND.class, RankND.class,
            BattlePassEventRewardND.class,
            DuelResultsND.class,
            // client -> server, client executed console command
            ConsoleCommand.class,
            // client -> server, client is trying to pick up an item
            PickUpItemMsg.class, TakeRecordFromItemMsg.class, PutRecordIntoItemMsg.class,
            // server -> client, server doesn't know about some active data
            UnknownActiveDataMsg.class,
            // client -> server, client wants to place a block
            PlaceBlockMsg.class,
            // server -> client, some block was placed
            BlockAddMsg.class,
            // client -> server, client makes some weapon actions
            WeaponActionMsg.class, WeaponActionMsg.Action.class,
            WeaponMagazineActionMsg.class, WeaponMagazineActionMsg.Action.class,
            // client -> server, client makes some simple instrument actions
            SimpleInstrumentActionMsg.class,
            // server -> client, update of weapon info (for MyWeaponComponent)
            WeaponInfoMsg.class, WeaponInfoMsg.MagazineInfo.class, WeaponInfoMsg.MagazineInfo[].class,
            // common, message that reliableUDP was delivered
            ReliableUdpMessage.class, ReliableReceivedMsg.class, ReliableBody.class,
            // server -> client, when client got some team
            TeamChanged.class,
            // client -> server, when consumableContainer would like to remove block
            RemoveBlockMsg.class,
            // server -> cleint, some consumableContainer launched some instrument
            OtherPlayerInstrumentLaunch.class,
            // server -> client, some active data changed
            UpdatedActiveDataMsg.class,
            // server -> client, some component of active data changed
            UpdatedComponentMsg.class,

            // [editor] client -> server, some editor changed a block
            EditorBlockMsg.class,
            // [editor] client -> server, editor added an active
            EditorActiveAddMsg.class,
            // [editor] client -> server, editor removed an active
            EditorActiveRemoveMsg.class,
            // [editor] client -> server, some editor action (e.g. save the map)
            EditorActionMsg.class, EditorActionMsg.ID.class,
            // [editor] client -> server, editor moved an active
            EditorActiveMoveMsg.class,
            // [editor] client -> server, editor cloned an active
            EditorActiveCloneMsg.class,
            // [editor] client -> server, editor updated inspectable
            EditorSetActivePropertiesMsg.class,
            EditorSetActivePropertyMsg.class,
            EditorSetBlockPropertiesMsg.class,
            EditorSetMapPropertiesMsg.class,
            // [editor] server -> client, editor requested inspectable properies
            EditorGetActivePropertiesMsg.class,
            EditorGetBlockPropertiesMsg.class,
            EditorGetMapPropertiesMsg.class,
            // [editor] client -> server, map resize request
            ResizeMapMsg.class,
            // [editor] client -> server, dimensions stuff
            NewDimensionMsg.class, DeleteDimensionMsg.class,

            // [editor] server -> client, server gives export
            MapSettingsUpdatedMsg.class,
            // [editor] client -> server, get map list
            GetMapListMsg.class,
            // [editor] server -> client, map list success
            MapListMsg.class,
            // [editor] client -> server, load map request
            LoadMapMsg.class,
            // [editor] client -> server, create map request
            CreateMapMsg.class,

            // server -> client, game mode updated
            ModeUpdatedMsg.class,
            // server -> client, play state changed (not updated)
            PlayStateChangedMsg.class,
            // server -> client, play state updated
            PlayStateUpdatedMsg.class,
            // client -> server, client decides to sit (or not)
            PlayerStateMsg.class, Player.State.class,
            // server -> client, other client sits (or not)
            OtherPlayerStateMsg.class,
            // client -> server, client wants to drop an item
            DropConsumableMsg.class,
            // server -> client, some client got consumables
            ActiveReceivedConsumableMsg.class,
            // server -> client, server delivered that client noticed another player
            ServerActiveVisibilityMsg.class,
            // server -> client, the time to respawn
            RespawnTimeMsg.class,
            // server -> client, some effect should be launcher
            LaunchEffectMsg.class,
            // client -> server, some throwable instrument launcher
            ThrowableLaunchMsg.class,
            // server -> client, client have been killed
            KilledByMsg.class,
            // client -> server, ask to update active info
            ActiveUpdateRequestMsg.class,
            // server -> client, game will soon finish
            ModeWillFinishInMsg.class,
            // client -> server, client activates some instrument (canBeActivated kind)
            ActivateInstrumentMsg.class,
            // server -> client, server asked client to spawn (and sent available items)
            SpawnRequestMsg.class,
            // server -> client, server wats the client to see the popup
            PopupMsg.class,
            // client -> server, some content action (like purchase)
            ContentActionMsg.class, ContentActionMsg.Action.class,
            // server -> client, user profile updated
            UpdateUserProfile.class,
            // server -> client, server have set world speed
            SetGameSpeedMsg.class,
            // client -> server, client forgives another clients kill
            ForgiveKillMsg.class,
            // client -> server, asks for a profile
            GetProfileMsg.class,
            // server -> client, got a profile
            UserProfileMsg.class,

            // server -> client, instrument should shoot custom effect
            InstrumentEffectMsg.class,
            // server -> client, slowmo should be applied
            SlowmoMsg.class,
            // client -> server, client would like to shoot some custom effect
            ClientInstrumentEffectMsg.class,
            // client -> server, client changed WatchPoint
            WatchPointMsg.class,
            // server -> client, case open result
            CaseOpenResultMsg.class, CaseOpenResultMsg.Result.class,
            // client -> server, notification client have read the badge
            BadgeReadMsg.class,
            // client -> server, client wants to disassemble
            DisassembleTrophyMsg.class,
            // client -> server, client entered a promocode
            PromoCodeMsg.class,
            // server -> client, promocode result
            PromoCodeResultMsg.class, PromoCodeResultMsg.Result.class,
            // server -> client, analytics
            AnalyticsEventMsg.class, AnalyticsEventMsg.Kind.class,
            AnalyticsResourceEventMsg.class,
            // server -> client, events updated
            OnlineEventsInfoMsg.class, OnlineEventUpdated.class,
            // client -> server, client claims an event reward
            ClaimOnlineEventRewardMsg.class, ClaimOnlineEventResultMsg.class,
            // server -> client
            ActivateOwnerComponentMsg.class,
            // client -> server, user would like to change a name
            ChangeNameMsg.class,
            // client -> server, create new order
            CreateNewOrderMsg.class, CreateNewOrderMsg.OrderEnvironmentItem[].class,
            CreateNewOrderMsg.OrderEnvironmentItem.class,
            // server -> client, server response for new order
            NewOrderResultMsg.class,
            // client -> server, process created order
            UpdateOrderMsg.class,
            // server -> client, server response for updated order
            UpdateOrderResultMsg.class,
            // sever -> client, some important stats are updated
            StatUpdatedMsg.class,
            // server -> client, some achievement is unlocked
            AchievementCompletedMsg.class,
            // client -> server, time check
            ClientSyncMsg.class,
            // server -> client, player rights was changed
            RightsUpdatedMsg.class,
            // client -> server, start a party
            StartPartyMsg.class,
            // server -> client(s), party is started
            PartyStartedMsg.class,
            // client -> server, kick
            KickPlayerMsg.class,
            // server -> party start result
            StartPartyResultMsg.class,
            // client -> server, friend list
            FriendListMsg.class,
            // client -> server, async request
            RequestMsg.class,
            // server -> client, async request response
            RequestSuccessMsg.class, RequestErrorMsg.class,
            // server -> client, message service message
            SocialMsg.class, SocialMsg[].class, SocialBatchMsg.class, SocialBatchMsg.LastReadMsg.class,
            SocialBatchMsg.LastReadMsg[].class,
            // server -> client, message service messages being updated or deleted
            SocialUpdatedMsg.class, SocialDeletedMsg.class,
            // client -> server, client activates some active
            ActivateActiveMsg.class, CancelPlayerProgressMsg.class,
            // server -> client, free play summary information
            FreePlaySummaryMsg.class,
            // client -> server, client activates some item
            ActivateItemMsg.class,
            // server -> client, some custom player animation on player actor
            CustomPlayerAnimationMsg.class,
            // client -> server, opening an item
            ItemActionMsg.class,
            // server -> client, player moved an item
            InventoryItemMovedMsg.class,
            // bidirectional, voice chat data
            VoiceChatMsg.class,
            // server -> client, server changed some animation
            UpdateActiveAnimationMsg.class,
            // server -> client, server forced some music
            PlayMusicMsg.class,
            // client -> server, player is ready to play
            ReadyMsg.class, SummaryReadyMsg.class,
            // client -> server, placer decided to change skin
            FreePlayChangeSkinMsg.class,
            // server -> client, shooting range related
            ShootingRangeWarmupMsg.class,
            ShootingRangeStartedMsg.class,
            ShootingRangeHitMsg.class,
            ShootingRangeCompletedMsg.class,
            // client -> server, changing categories and tags
            SelectCategoryMsg.class,
            SelectTagMsg.class,
            SelectFavoritesMsg.class,
            SetFavoriteMsg.class,
            // server -> client, server asks client weapon to pull the bolt
            PullRequiredMsg.class,
            // server -> client, player wounded update
            PlayerWoundedMsg.class,
            // server -> client, server notifies client that he is about to be spawned
            YouWillBeSpawnedWithinMsg.class,
            // server -> client, player has stepped somewhere
            StepMsg.class,
            // server -> client, updated spectator status
            SpectatorFlagMsg.class,
            // server -> client,
            WatchAnimationMsg.class,
            // server -> client
            DuelCompletedMsg.class,
            // client -> server or server -> client
            SetFriendlyStatusMsg.class,
            // server -> client
            MadeFriendsMsg.class,
            // server -> client
            QuestTaskProgress.class,
            // server -> client
            FreePlayRadioMsg.class,
            // client -> server
            FreePlayPlayAgain.class,
            // server -> client, informs the player that there is a short opportunity to enter the zone near his friend
            TeamLandingMsg.class,
            // server -> client, informs the player about entering to FreePlay war zone
            FreeEnterMsg.class,
            // player hops between maps
            ServerActiveChangeDimensionMsg.class,
            // client -> server, client doesn't have an active it needs
            PleaseSendActiveMsg.class,
            // server -> client, update about battle pass progress
            BattlePassTaskProgressUpdateMsg.class,
            // server wishes to unload a map
            FreeDimensionMsg.class,
            // other
            CurrentlyWatchingMsg.class, CardMessage.class, BlockMessage.class,
            ChangeFrequencyMsg.class, CreateClanMsg.class, MarkMessageReadMsg.class,
            SwitchShootModeMsg.class,

            // [editor2]
            BlockRectMsg.class, MultipleBlocksMsg.class, SingleBlockMsg.class, CreateObjectMsg.class,
            RemoveObjectMsg.class, MoveObjectsMsg.class, CopyObjectsMsg.class, SpawnEditor2Msg.class,
            Editor2ActiveRemoveMsg.class, Editor2ActiveAddMsg.class, CreateUserImageMsg.class,
            RemoveUserImageMsg.class,

            // cards
            GetTable.class,
            LeaveTable.class,
            CardsTable.class,
            ResetTable.class,
            CardsTable.CardOnTable.class,
            CardsTable.CardOnTable[].class,
            CardsTable.Participant.class,
            CardsTable.Participant[].class,
            DiscardAllCards.class,
            DiscardCard.class,
            FlipCard.class,
            GiveCardToPlayerFromDeck.class,
            JoinCards.class,
            MoveCardOnTable.class,
            PlaceCardOnTableFromHand.class,
            TakeCardOffDeckOntoTable.class,
            GiveCardToPlayerFromTable.class,

            // disconnecting
            ClientDisconnect.class, ServerDisconnect.class, DisconnectReason.class,

            // common
            String[].class,
            Integer[].class,
            int[].class, float[].class,
            ErrorMsg.class, ErrorMsg.Code.class,
            ActiveData.LastHitKind.class,
            EntityReceived.class, PlayerRights.class,

            // instrument info
            InstrumentInfoPart.class, InstrumentInfoPart.InstrumentUpgrade.class,
            InstrumentInfoPart.InstrumentUpgrade[].class,

            // inspectable
            EditorProperty.class, EditorProperty[].class, PropertyKind.class, PropertyValue.class,
            ActiveLayer.class, Weapon.ShootMode.class,

            UpdateGlobalContentIndex.class,
        });
    }
}
