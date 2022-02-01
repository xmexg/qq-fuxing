import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

public class Main {
	public static String lijiqianwang_location = "570 780";// 运行前要填入“立即前往”按钮的x，y坐标
	public static String hutou_location = "550 1010";// 运行前要填入“虎头”按钮的x，y坐标
	public static String TEMP_location = "550 1010";// 运行前要填入“虎头”按钮的x，y坐标
	public static String info_file = "info";// 日志

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("*********************************************");
		Main line = new Main();
		StringBuffer date = line.commonmain("date", true);
		line.writefile(info_file, "\n*********************************************\n" + date);
		String loldev = "", netdev = "", loldevip = "";
		if ((!line.haveADB()) || (!line.canADBconnect()) || args.length != 1) {
			System.out.println("没有ADB或ADB无法连接设备或传入参数数量不对");
			line.writefile(info_file, "没有ADB或ADB无法连接设备或传入参数数量不对\n*********************************************\n");
			System.exit(1);
		}
		String[] ADBList = line.getADBList();
		if (ADBList.length > 2) {
			System.out.println("连接设备太多");
			line.writefile(info_file, "连接设备太多\n*********************************************\n");
			System.exit(2);
		}
		for (int i = 0; i < ADBList.length; i++) {
			if (ADBList[i].indexOf("192.") != -1)
				netdev = ADBList[i];
			if (ADBList[i].indexOf("192.") == -1)
				loldev = ADBList[i];
		}
		switch (args[0]) {
		case "off":
			line.commonmain("adb disconnect", false);
			System.out.println("已断开所有设备");
			line.writefile(info_file, "已断开所有设备\n*********************************************\n");
			System.exit(0);
		case "on":
			if (loldev == "") {
				System.out.println("没有本地设备");
				line.writefile(info_file, "没有设备\n*********************************************\n");
				System.exit(3);
			}
			loldevip = line.getdevip(loldev) + ":5555";
			line.commonmain("adb -s " + loldev + " tcpip 5555", false);
			if (!line.canping(loldevip)) {
				System.out.println("(on)网络不可达");
				line.writefile(info_file, "(on)网络不可达\n*********************************************\n");
				System.exit(4);
			}
			line.commonmain("adb connect " + loldevip, false);
			System.out.println("现在你可以拔下USB了");
			line.QQ(loldevip);
			break;
		case "auto":
			if (loldev != "") {
				loldevip = line.getdevip(loldev) + ":5555";
				line.commonmain("adb -s " + loldev + " tcpip 5555", false);
				if (!line.canping(loldevip)) {
					System.out.println("本地设备网络不可达");
					line.writefile(info_file, "本地设备的网络不可达\n*********************************************\n");
					System.exit(4);
				}
				line.commonmain("adb connect " + loldevip, false);
				System.out.println("现在你可以拔下USB了");
				line.QQ(loldevip);
			} else if (netdev != "") {
				if (!line.canping(netdev)) {
					System.out.println(netdev + "网络不可达");
					line.writefile(info_file, netdev + "网络不可达\n*********************************************\n");
					System.exit(4);
				}
				line.QQ(netdev);
			}
			break;
		default:
			if (!line.canping(args[0])) {
				System.out.println(netdev + "网络不可达");
				line.writefile(info_file, netdev + "网络不可达\n*********************************************\n");
				System.out.println("没有连接任何设备");
				line.writefile(info_file, "没有连接设备\n*********************************************\n");
				System.exit(4);
			}
			String argsip = args[0];
			if (argsip.indexOf(":") == -1) {
				argsip = argsip + ":5555";
			}
			line.commonmain("adb connect " + argsip, false);
			line.QQ(argsip);
			break;
		}
	}

	public void QQ(String ip) throws InterruptedException, IOException {
		StringBuffer face = commonmain("adb -s " + ip + " shell dumpsys window | grep mCurrentFocus", false);
		System.out.println("选定设备:" + ip);
		writefile(info_file, "选定设备:" + ip + "\n");
		System.out.println("当前界面:" + face);
		writefile(info_file, "当前界面:" + face + "\n");
		if ((face.indexOf("NotificationShade") != -1) || face.isEmpty()) {// 说明息屏了
			System.out.println("息屏了");
			writefile(info_file, "息屏了" + "\n");
			Thread.sleep(1000);
			commonmain("adb -s " + ip + " shell input keyevent 26", false);// 点亮屏幕
			Thread.sleep(1000);
			commonmain("adb -s " + ip + " shell input swipe 600 1800 600 400 210", false);
			Thread.sleep(7000);
		}
		System.out.println("正在做一些刷新QQ的操作");
		writefile(info_file, "正在做一些刷新QQ的操作" + "\n");
		commonmain("adb -s " + ip + " shell input keyevent 3", false);// 主屏键
		Thread.sleep(5000);
		commonmain("adb -s " + ip + " shell am start com.tencent.mobileqq/com.tencent.mobileqq.activity.SplashActivity",false);
		Thread.sleep(10000);

		do {// 如果在QQ界面，就不断按返回键
			commonmain("adb -s " + ip + " shell input keyevent 4", false);// 返回键
			Thread.sleep(2000);
		} while (getActivity(ip).indexOf("com.tencent.mobileqq")!=-1);

		System.out.println("打开QQ");
		writefile(info_file, "打开QQ" + "\n");
		commonmain("adb -s " + ip + " shell am start com.tencent.mobileqq/com.tencent.mobileqq.activity.SplashActivity",false);
		Thread.sleep(10000);
		commonmain("adb -s " + ip + " shell input swipe 600 400 600 1800 210", false);
		for(int i=0;!ocr_getxy(ip, "福", 0);i++) {
			Thread.sleep(5000);
			if(i>5) {
				System.out.println("未发现 立即前往 的坐标,程序已退出");
				writefile(info_file, "未发现 立即前往 的坐标,程序已退出\n*********************************************");
				System.exit(1);
			}
		}
		commonmain("adb -s " + ip + " shell input tap " + lijiqianwang_location, false);
		for(int i=0;(i<3) && (!getActivity(ip).equals("com.tencent.mobileqq/com.tencent.mobileqq.spring2022.main.SpringHbTranslucentBrowserActivity"));i++) {
			Thread.sleep(10000);			
		}
		if (getActivity(ip).equals("com.tencent.mobileqq/com.tencent.mobileqq.spring2022.main.SpringHbTranslucentBrowserActivity")) {
			boolean havestars = true;
			System.out.println("开始收集福星");
			writefile(info_file, "开始收集福星" + "\n");
			Thread.sleep(3000);
			for(int i=0;(i<3)&&(!ocr_getxy(ip, "福", 1));i++){//有时会只显示虎头，不显示星星
				commonmain("adb -s " + ip + " shell input keyevent 4", false);// 返回键
				Thread.sleep(2000);
				commonmain("adb -s " + ip + " shell input tap " + lijiqianwang_location, false);
				Thread.sleep(3000);
			}
			getHutou_byScreenSize(ip);
			short star = 0;
			do {
				commonmain("adb -s " + ip + " shell input tap " + hutou_location, false);
				Thread.sleep(6000);
				switch (getActivity(ip)) {
				case "com.tencent.mobileqq/com.tencent.mobileqq.activity.QPublicTransFragmentActivity":
				case "com.tencent.mobileqq/com.tencent.mobileqq.activity.QQBrowserActivity":
					star++;
					break;// 这是正常情况
				case "com.tencent.mobileqq/com.tencent.mobileqq.spring2022.main.SpringHbTranslucentBrowserActivity":
					System.out.println("界面没有发生任何变化，是星星收集结束了吗？");
					writefile(info_file, "界面没有发生任何变化，是星星收集结束了吗？" + "\n");
					break;
				default:
					System.out.println("打开了未知的页面");
					writefile(info_file, "打开了未知的页面" + "\n");
				}
				commonmain("adb -s " + ip + " shell input keyevent 4", false);// 返回键
				Thread.sleep(2000);
				if (getActivity(ip).equals("com.tencent.mobileqq/com.tencent.mobileqq.activity.SplashActivity")) {
					havestars = false;
				}
			} while (havestars);
			System.out.println("本次星星收集结束,共收集了" + star + "颗星星\n*********************************************");
			writefile(info_file, "本次星星收集结束,共收集了" + star + "颗星星\n*********************************************" + "\n");
			commonmain("adb -s " + ip + " shell input keyevent 3", false);// 主屏键
			System.exit(0);
		} else {
			System.out.println("打开了错误的页面,是不是“立即前往”福星的按钮位置发生了变化？");
			writefile(info_file, "打开了错误的页面,是不是“立即前往”福星的按钮位置发生了变化？" + "\n");
			System.out.println(
					"福星的正确Activity为:com.tencent.mobileqq/com.tencent.mobileqq.spring2022.main.SpringHbTranslucentBrowserActivity");
			writefile(info_file,
					"福星的正确Activity为:com.tencent.mobileqq/com.tencent.mobileqq.spring2022.main.SpringHbTranslucentBrowserActivity" + "\n");
			String activity = getActivity(ip);
			System.out.println("当前的Activity为:" + activity);
			writefile(info_file, "当前的Activity为:" + activity + "\n");
			System.exit(5);
		}
	}

	public boolean haveADB() {
		if ((commonmain("adb", false)).indexOf("options") != -1) {
			return true;
		}
		return false;
	}

	public String[] getADBList() {
		String ADBLists = "";
		StringBuffer ADBListtxt = commonmain("adb devices", true);
		int where = ADBListtxt.indexOf("device");
		ADBListtxt.delete(where, where + 8);
		if (ADBListtxt.indexOf("device") != -1) {
			while (ADBListtxt.indexOf("device") != -1) {
				ADBLists = ADBLists + ADBListtxt.substring(ADBListtxt.indexOf("\n") + 1, ADBListtxt.indexOf("\t")) + ",";
				ADBListtxt.delete(ADBListtxt.indexOf("\n"), ADBListtxt.indexOf("device") + 6);
			}
		} else {
			System.out.println("没有adb设备");
		}
		String[] ADBList = ADBLists.split(",");
		return ADBList;
	}

	public String getdevip(String dev) {
		StringBuffer text = commonmain("adb -s " + dev + " shell ifconfig wlan0", false);
		int wheresta = text.indexOf("inet addr:");
		int whereend = text.indexOf(" ", wheresta + 11);
		return text.substring(wheresta + 10, whereend);
	}

	public String getActivity(String dev) {
		StringBuffer text = commonmain("adb -s " + dev + " shell dumpsys window | grep mCurrentFocus", false);
		int wherestr = text.indexOf("com.");
		if(wherestr==-1) {
			wherestr = text.lastIndexOf(" ");
		}
		int whereend = text.indexOf("}", wherestr);
		String activity;
		try {
			activity = text.substring(wherestr, whereend);
		}catch(Exception e) {
			activity = "ERROR_GET_ACTIVITY";
		}
		return activity;
	}

	public boolean canADBconnect() {
		StringBuffer ADBListtxt = commonmain("adb devices", false);
		int where = ADBListtxt.indexOf("device");
		ADBListtxt.delete(where, where + 8);
		if (ADBListtxt.indexOf("device") != -1) {
			return true;
		}
		return false;
	}

	public boolean canping(String ip) {
		System.out.println(ip);
		int wherecolon = ip.indexOf(":");
		if (wherecolon != -1) {
			ip = ip.substring(0, wherecolon);
		}
		StringBuffer pinginfo = commonmain("ping " + ip + " -c 2", false);
		if (pinginfo.indexOf("0%") != -1) {
			return true;
		}
		return false;
	}
	
	public StringBuffer getHutou_byScreenSize(String ip) throws IOException {
		StringBuffer str = commonmain("adb -s " + ip + " shell wm size", false);
		if(str.charAt(str.length()-1)=='\n') {//删除结尾换行符
			str.deleteCharAt(str.length()-1);
		}
		int wherest = str.lastIndexOf(" ");
		str.delete(0,wherest+1);
		int whereisx = str.lastIndexOf("x");
		str.setCharAt(whereisx, ' ');
		System.out.println("屏幕分辨率为:"+str);
		writefile(info_file, "屏幕分辨率为:"+str);
		int x = Integer.valueOf(str.substring(0, whereisx)).intValue();
		int y = Integer.valueOf(str.substring(whereisx+1)).intValue();
		hutou_location = (int)(x*0.5) + " " + (int)(y*0.4);
		System.out.println("已通过ADB更新了虎头坐标为:"+hutou_location);
		writefile(info_file, "已通过ADB更新了虎头坐标为:"+hutou_location);
		return str;
	}
	
	public boolean ocr_getxy(String ip, String string, int type) throws IOException {// 设备ip，要搜索的字，类型：0为立即前往，1为其他
		String fileName = "./output.box";
		File file = new File(fileName);
		if(!file.exists()) {
			file.delete();
			file.createNewFile();
			System.out.println("删除文件");
		}
		commonmain("adb -s " + ip + " shell screencap -p /sdcard/QQstar.png", false);
		commonmain("adb -s " + ip + " pull /sdcard/QQstar.png .", false);
		commonmain("tesseract -l chi_sim ./QQstar.png output makebox", false);
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "utf-8");
		BufferedReader br = new BufferedReader(isr);
		String line, data[], xy = null;
		while ((line = br.readLine()) != null) {
			if (line.indexOf(string) != -1) {
				data = line.split(" ");
				xy = data[1] + " " + data[3];
				switch (type) {
				case 0 :
					lijiqianwang_location = xy;
					System.out.println("通过ocr更新了立即前往的坐标 :" + lijiqianwang_location);
					writefile(info_file, "通过ocr更新了立即前往的坐标 :" + lijiqianwang_location);
					br.close();
					return true;
				case 1 :
					TEMP_location = xy;
					br.close();
					return true;
				default :
					System.out.println("没有这个选项");
					break;
				}
			}
		}
		br.close();
		return false;
	}

	public void writefile(String fileurl, String str) throws IOException {
		File file = new File(fileurl);
		if (!file.exists()) {
			file.createNewFile();
		}
		try (Writer writer = new FileWriter(file, true)) {
			// 把内容转换成字符数组
			char[] data = str.toCharArray();
			// 向文件写入内容
			writer.write(data);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public StringBuffer commonmain(String cmd, boolean echo) {
		StringBuffer echocmd = new StringBuffer();
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmd);
			echocmd = new StringBuffer(InputStream2String(process.getInputStream()));
			if (echo) {
				System.out
						.println("==============================" + "\n" + echocmd + "==============================");
			}
			return echocmd;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String InputStream2String(InputStream inputStream) {
		String result = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		try {
			String temp = "";
			while ((temp = br.readLine()) != null) {
				result += temp + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
