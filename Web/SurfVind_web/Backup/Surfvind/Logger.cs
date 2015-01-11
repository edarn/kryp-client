using System;
using System.Web;
using System.IO;
using System.Web.Configuration;
using System.Configuration;

namespace Surfvind_2011
{
	/// <summary>
	/// Types of error messages
	/// </summary>
	public enum LogMessageType
	{
		Error,  //This means fatal error, no application recovery possible
		Info,   //This means general info (Success, Events Logging, and etc.)
		Warning //This means that there is a situat that should be reviewed by admin or coder 
	}

	/// <summary>
	/// Class for logging errors,warnings and info
	/// </summary>
	/// <remarks>
	/// Please notice: To use this logger you need to setup logs directory in web.config and grant
	/// web site access for writing in in.
	/// </remarks>
	public static class Logger
	{

		/// <summary>
		/// Local path for keeping log files
		/// </summary>
		public static string LogPath
		{
			get
			{
				return HttpContext.Current.Server.MapPath("~/Bin");
			}
		}

		/// <summary>
		/// Main Function for logging alows to log message with a type
		/// </summary>
		/// <param name="type">Type of a log message</param>
		/// <param name="message">The message itself</param>
		public static void Log(LogMessageType type, string message)
		{
			try
			{
				using (StreamWriter sw = new StreamWriter(GenerateFileName(type), true))
				{
					sw.WriteLine(DateTime.Now.ToString() + ":" + message);
				}
			}
			catch { } //cannot fall here
		}

		/// <summary>
		/// Helper function, allows to log error with one argument
		/// </summary>
		/// <param name="message"></param>
		public static void LogError(string message)
		{
			Log(LogMessageType.Error, message);
		}

		/// <summary>
		/// Helper function, allows to log warning with one argument
		/// </summary>
		/// <param name="message"></param>
		public static void LogWarning(string message)
		{
			Log(LogMessageType.Warning, message);
		}

		/// <summary>
		/// Helper function, allows to log information with one argument
		/// </summary>
		/// <param name="message"></param>
		public static void LogInfo(string message)
		{
			Log(LogMessageType.Info, message);
		}

		/// <summary>
		/// Function to log only in "compilation debug=true" mode, if this is unset or set to false
		/// no logs are produced
		/// </summary>
		/// <param name="type">Type of log message</param>
		/// <param name="message">The mesasge itself</param>
		public static void LogDebug(LogMessageType type, string message)
		{
			CompilationSection section = ConfigurationManager.GetSection("system.web/compilation") as CompilationSection;
			if (section.Debug == true)
			{
				using (StreamWriter sw = new StreamWriter(Path.Combine(LogPath, "debug.txt"), true))
				{
					sw.WriteLine(DateTime.Now.ToString() + ":" + message);
				}
			}
		}

		/// <summary>
		/// Function to generate file name for log files
		/// </summary>
		/// <param name="type">type of the message to write</param>
		/// <returns>Generated File Name</returns>
		private static string GenerateFileName(LogMessageType type)
		{
			//string fileName = String.Format("{0}_{1}_log.txt", type.ToString(), DateTime.Now.ToString("hh.mm.ss_dddd_MMMM_dd_yyyy"));
			string fileName = "Application_Log.txt";
			return Path.Combine(LogPath, fileName);
		}
	}
}