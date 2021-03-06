package xyz.acrylicstyle.tomeito_bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Collection;
import util.ReflectionHelper;
import util.promise.rewrite.Promise;
import xyz.acrylicstyle.mcutil.mojang.MojangAPI;
import xyz.acrylicstyle.mcutil.mojang.Property;
import xyz.acrylicstyle.tomeito_api.shared.ChannelConstants;
import xyz.acrylicstyle.tomeito_bungee.channel.PluginChannelListener;

import java.util.UUID;

public class TomeitoBungee extends Plugin implements Listener {
    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerListener(this, new PluginChannelListener());
        getProxy().registerChannel(ChannelConstants.PROTOCOL_VERSION);
        getProxy().registerChannel(ChannelConstants.SET_SKIN);
        getProxy().registerChannel(ChannelConstants.REFRESH_PLAYER);
        getLogger().info("Enabled TomeitoBungee");
    }

    public static final Collection<UUID, LoginResult> profiles = new Collection<>();

    @EventHandler
    public void onPostLogin(PostLoginEvent e) {
        LoginResult profile = ((InitialHandler) e.getPlayer().getPendingConnection()).getLoginProfile();
        profiles.add(e.getPlayer().getUniqueId(), new LoginResult(profile.getId(), profile.getName(), profile.getProperties().clone()));
    }

    @Contract(pure = true)
    @NotNull
    public static Promise<LoginResult.@Nullable Property> setSkin(ProxiedPlayer player, String nick) {
        InitialHandler handler = (InitialHandler) player.getPendingConnection();
        if (nick.length() == 0) {
            ReflectionHelper.setFieldWithoutException(InitialHandler.class, handler, "loginProfile", profiles.get(player.getUniqueId()));
            return Promise.resolve(profiles.get(player.getUniqueId()).getProperties()[0]);
        }
        return MojangAPI.getUniqueId(nick).then(uuid ->
                MojangAPI.getGameProfile(uuid).then(profile -> {
                    LoginResult.Property prop = new LoginResult.Property(profile.properties[0].name, profile.properties[0].value, profile.properties[0].signature);
                    ReflectionHelper.setFieldWithoutException(InitialHandler.class, handler, "loginProfile", new LoginResult(profile.id.toString(), profile.name, new LoginResult.Property[]{ prop }));
                    return prop;
                }).complete()
        );
    }

    @Contract
    public static void setProperty(@NotNull ProxiedPlayer player, @Nullable Property property) {
        InitialHandler handler = (InitialHandler) player.getPendingConnection();
        if (property == null) {
            ReflectionHelper.setFieldWithoutException(InitialHandler.class, handler, "loginProfile", profiles.get(player.getUniqueId()));
            return;
        }
        LoginResult.Property prop = new LoginResult.Property(property.name, property.value, property.signature);
        ReflectionHelper.setFieldWithoutException(InitialHandler.class, handler, "loginProfile", new LoginResult(player.getUniqueId().toString().replaceAll("-", ""), player.getName(), new LoginResult.Property[]{ prop }));
    }
}
