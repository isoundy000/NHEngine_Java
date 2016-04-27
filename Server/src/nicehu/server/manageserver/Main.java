package nicehu.server.manageserver;

import java.net.InetSocketAddress;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nicehu.nhsdk.candy.log.LogBackMgr;
import nicehu.nhsdk.candy.str.ParseU;
import nicehu.nhsdk.candy.time.TimeZoneU;
import nicehu.nhsdk.core.data.AreaData;
import nicehu.nhsdk.core.data.SD;
import nicehu.nhsdk.core.data.ServerInfo;
import nicehu.nhsdk.core.db.DBMgr;
import nicehu.nhsdk.core.type.ServerType;
import nicehu.server.common.handler.ShutdownReqHandler;
import nicehu.server.manageserver.config.ConfigMgr;
import nicehu.server.manageserver.config.core.ConfigPath;
import nicehu.server.manageserver.config.serverconfig.ServerConfig;
import nicehu.server.manageserver.config.serverconfig.ServerConfigMgr;
import nicehu.server.manageserver.core.MSD;
import nicehu.server.manageserver.core.ManageHandlerRegister;
import nicehu.server.manageserver.logic.freeze.data.FreezeMgr;
import sun.misc.Signal;

public class Main
{
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static int serverType = ServerType.MANAGE;
	private static String serverName = "ManageServer";

	public static void main(String[] args)
	{
		LogBackMgr.init();
		SD.init(serverType, serverName);
		logger.warn("ServerName: ", SD.serverName);

		ConfigMgr.loadFromFile();

		ServerConfig serverConfig = ServerConfigMgr.instance.getServerConfig(serverName);
		SD.initServerConfig(serverName);

		TimeZoneU.setTimezone(ParseU.pInt(serverConfig.getAttr("TimeZone"), 0));
		AreaData.setAreaId(-1);

		DBMgr.init(serverType);
		ConfigMgr.loadFromDB();
		FreezeMgr.instance.init();

		SD.socketServerForS.initialize(16);
		ManageHandlerRegister.init();
		try
		{
			int portForServer = ParseU.pInt(serverConfig.getAttr("PortForServer"), 0);
			logger.warn("ManageServer openPort portForServer={}", portForServer);
			boolean portForServerOpen = SD.socketServerForS.openPort(new InetSocketAddress(portForServer));

			if (portForServerOpen)
			{
				logger.warn("Manage Start Success!!!___________________________________________________________________________________Manage!!!");
			}
		}
		catch (Exception e)
		{
			logger.error("Bind port failed !!! {}", ExceptionUtils.getStackTrace(e));
			System.exit(0);
		}

		ServerInfo serverInfo = new ServerInfo(serverConfig);
		MSD.serverInfoMgr.addServer(serverInfo);

		Signal.handle(new Signal("TERM"), new ShutdownReqHandler());
	}
}