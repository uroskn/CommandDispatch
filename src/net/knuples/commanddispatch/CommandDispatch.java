package net.knuples.commanddispatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandDispatch extends JavaPlugin
implements Listener
{
  protected String dispatcher;
  protected List<String> commands;
  protected List<String> passthrucommands;

  @Override
  public void onEnable()
  {
    this.saveDefaultConfig();

    this.dispatcher       = this.getConfig().getString("dispatcher");
    this.commands         = this.getConfig().getStringList("commands-list");
    this.passthrucommands = this.getConfig().getStringList("passthru-commands-list");
    this.getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  public void onCommandPreprocess(PlayerCommandPreprocessEvent event)
  {
    if (this.dispatchCommand(event.getPlayer().getName(), event.getMessage().substring(1), false)) event.setCancelled(true);
  }

  @EventHandler
  public void onConsoleCommandPreprocess(ServerCommandEvent event)
  {
    this.dispatchCommand("", event.getCommand(), true);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event)
  {
    this.dispatchProgram("player-join", new String[]{event.getPlayer().getName()});
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event)
  {
    this.dispatchProgram("player-quit", new String[]{event.getPlayer().getName()});
  }

  protected boolean sendCommand(String[] args, String sender, String command, String cmd, boolean isconsole)
  {
    if (args[0].equalsIgnoreCase(cmd))
    {
      if (!isconsole)
      {
        List<String> str = new ArrayList<String>(Arrays.asList(args));
        str.add(0, sender);
        this.dispatchProgram("command", str.toArray(new String[str.size()]));
      }
      else this.dispatchProgram("console-command", args);
      return true;
    }
    return false;
  }

  protected boolean dispatchCommand(String sender, String command, boolean isconsole)
  {
    String[] args = command.split(" ");
    for (String cmd : this.commands) { if (sendCommand(args, sender, command, cmd, isconsole)) return true; }
    for (String cmd : this.passthrucommands) { sendCommand(args, sender, command, cmd, isconsole); }
    return false;
  }

  protected void dispatchProgram(String type, String[] arguments)
  {
    String str = this.dispatcher + " " + type;
    for (int i = 0; i < arguments.length; i++) str = str + " " + arguments[i];
    try
    {
      Runtime.getRuntime().exec(str);
    }
    catch (IOException e)
    {
      this.getLogger().warning("Failed executing external program: " + str);
      e.printStackTrace();
    }
  }
}
