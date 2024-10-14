package community.solace.ep.wrapper;

import community.solace.ep.client.model.Address;
import community.solace.ep.client.model.AddressLevel;
import community.solace.ep.client.model.DeliveryDescriptor;

public class TopicUtils {
	

	public static String buildTopicHtml(DeliveryDescriptor dd) {
		char levelSeparator = '/'; // default
		if ("kafka".equals(dd.getBrokerType()))
			levelSeparator = '.'; // could be null
		StringBuilder sb = new StringBuilder("<html><nobr>");
		Address a = dd.getAddress();
		if (a == null)
			return ""; // no address yet defined
		for (AddressLevel level : a.getAddressLevels()) {
			if (level.getAddressLevelType() == AddressLevel.AddressLevelTypeEnum.LITERAL) {
				sb.append(level.getName());
			} else {
				sb.append("<i>{").append(level.getName()).append("}</i>");
			}
			sb.append(levelSeparator);
		}
		return sb.deleteCharAt(sb.length() - 1).append("</nobr></html>").toString();
	}

	
	public static String buildTopic(DeliveryDescriptor dd) {
		char levelSeparator = '/'; // default
		if ("kafka".equals(dd.getBrokerType()))
			levelSeparator = '.'; // could be null
		StringBuilder sb = new StringBuilder();
		Address a = dd.getAddress();
		if (a == null)
			return ""; // no address yet defined
		for (AddressLevel level : a.getAddressLevels()) {
			if (level.getAddressLevelType() == AddressLevel.AddressLevelTypeEnum.LITERAL) {
				sb.append(level.getName());
			} else {
				sb.append("{").append(level.getName()).append("}");
			}
			sb.append(levelSeparator);
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

}
