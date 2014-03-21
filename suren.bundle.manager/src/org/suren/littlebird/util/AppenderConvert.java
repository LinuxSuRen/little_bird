 package org.suren.littlebird.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.varia.LevelMatchFilter;
import org.apache.log4j.varia.LevelRangeFilter;
import org.apache.log4j.varia.StringMatchFilter;
import org.suren.littlebird.po.DataEntry;

public class AppenderConvert
{
	public static List<Entry<String, String>> toList(Appender appender)
	{
		if(appender == null)
		{
			return null;
		}

		List<Entry<String, String>> data = new ArrayList<Entry<String, String>>();

		add("name", appender.getName(), data);

		Layout layout = appender.getLayout();
		if(layout != null)
		{
			add("type", appender.getClass().getName(), data);

			if(layout instanceof PatternLayout)
			{
				PatternLayout patternLayout = (PatternLayout) layout;

				add("pattern", patternLayout.getConversionPattern(), data);
			}
		}

		Filter filter = appender.getFilter();
		int index = 0;
		while(filter != null)
		{
			add("filter_type_" + index, filter.getClass().getName(), data);

			if(filter instanceof StringMatchFilter)
			{
				StringMatchFilter strMatchFilter = (StringMatchFilter) filter;

				add("filter_accept_" + index, String.valueOf(strMatchFilter.getAcceptOnMatch()), data);
				add("filter_match_" + index, strMatchFilter.getStringToMatch(), data);
			}
			else if(filter instanceof LevelRangeFilter)
			{
				LevelRangeFilter levelRangeFilter = (LevelRangeFilter) filter;

				add("filter_accept_" + index, String.valueOf(levelRangeFilter.getAcceptOnMatch()), data);
				add("filter_max_" + index, levelRangeFilter.getLevelMax().toString(), data);
				add("filter_min_" + index, levelRangeFilter.getLevelMin().toString(), data);
			}
			else if(filter instanceof LevelMatchFilter)
			{
				LevelMatchFilter levelFilter = (LevelMatchFilter) filter;

				add("filter_accept_" + index, String.valueOf(levelFilter.getAcceptOnMatch()), data);
				add("filter_level_" + index, levelFilter.getLevelToMatch(), data);
			}

			index++;
			filter = filter.getNext();
		}

		add("appender_type", appender.getClass().getName(), data);
		if(appender instanceof FileAppender)
		{
			FileAppender fileAppender = (FileAppender) appender;

			add("file", fileAppender.getFile(), data);
			add("buffer", String.valueOf(fileAppender.getBufferSize()), data);
			add("encoding", fileAppender.getEncoding(), data);

			if(appender instanceof RollingFileAppender)
			{
				RollingFileAppender rollingFileAppender = (RollingFileAppender) fileAppender;

				add("max_backup_index", String.valueOf(rollingFileAppender.getMaxBackupIndex()), data);
				add("max_file_size", String.valueOf(rollingFileAppender.getMaximumFileSize()), data);
			}
		}

		return data;
	}

	public static List<Entry<String, String>> toList(Logger logger)
	{
		if(logger == null)
		{
			return null;
		}

		List<Entry<String, String>> entryList = new ArrayList<Entry<String, String>>();

		int bridgesCount = 0;
		@SuppressWarnings("unchecked")
		Enumeration<Appender> appenders = logger.getAllAppenders();
		while(appenders.hasMoreElements())
		{
			if(appenders.nextElement() != null)
			{
				bridgesCount++;
			}
		}

		add("name", logger.getName(), entryList);
		add("level", logger.getEffectiveLevel().toString(), entryList);
		add("bridges_count", String.valueOf(bridgesCount), entryList);

		return entryList;
	}

	private static void add(String key, String value, List<Entry<String, String>> list)
	{
		if(list == null)
		{
			return;
		}

		list.add(new DataEntry<String, String>(key, value));
	}
}
