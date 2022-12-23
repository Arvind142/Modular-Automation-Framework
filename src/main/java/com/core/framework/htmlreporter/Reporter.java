package com.core.framework.htmlreporter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.core.framework.constant.FrameworkConst;
import com.core.framework.constant.ReportingConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.File;
import java.util.Properties;

@Slf4j
class Reporter {
	
	private static Reporter reporter=null;

    private final ExtentSparkReporter htmlReporter;

    private final ExtentReports extentReport;

    private final String reportingFolder;

    private final String assetFolder;

    private String deviceDetails;

    private Reporter(String reportingFolder) {
        log.debug("HTML Reporter called");
        log.debug("Output folder created @ "+reportingFolder);
        // creating screenshot folder
        assetFolder = reportingFolder + "/"+ ReportingConst.screenshotFolder;
        File folder = new File(assetFolder);
        log.debug((folder.mkdirs() ? "screenshot folder created" : "screenshot folder creation failed"));
        log.debug("Asset folder created @ " + folder.getAbsolutePath());

        // reporting initialized
        htmlReporter = new ExtentSparkReporter(reportingFolder + "/"+ReportingConst.htmlReportName);
        this.reportingFolder = reportingFolder;
        extentReport = new ExtentReports();
        if (new File(FrameworkConst.extent_config_xml).exists()) {
            try {
                htmlReporter.loadXMLConfig(FrameworkConst.extent_config_xml);
            } catch (Exception e) {
                //do Nothing go with default config
            }
        }
        // attach reporter :)
        extentReport.attachReporter(htmlReporter);
        log.debug("html reporting initialized");
    }

    public String takeScreenShotWebPage(WebDriver driver, String fileName) {
        String folderName = assetFolder + "/";
        File f = (new File(folderName));
        if (!f.exists()) {
            log.debug((f.mkdirs() ? "Screenshot folder created" : "Screenshot folder creation failed"));
        }
        String imgPath = folderName + fileName + ".jpg";
        File s = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(s, new File(imgPath));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return ReportingConst.screenshotFolder + fileName + ".jpg";
    }

    public String getDeviceDetails() {
        return deviceDetails;
    }

    public void stopReporting() {
        extentReport.flush();
        log.debug("report flush");
        log.info("Report url: "+htmlReporter.getFile().getAbsolutePath());
    }

    public void setSystemVars(Properties props) {

        if (props == null)
            return;
        if (props.isEmpty())
            return;
        try {
        	this.deviceDetails = System.getProperty("os.name")+"/"+props.getProperty("browser");
        }
        catch(NullPointerException ex) { this.deviceDetails =null;}
        //adding property file details onto report
        for (Object key : props.keySet()) {
            extentReport.setSystemInfo(key.toString(), props.getProperty(key.toString()));
        }
    }

    public String getReportingFolder() {
        return reportingFolder;
    }

    ExtentReports getExtentReport(){
        return this.extentReport;
    }

    public static synchronized Reporter initializeReporting(String reportingFolderPath) {
        if(reporter==null) {
        	File folder = new File(reportingFolderPath);
            // folder check - if not present create one
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String resultFolder;
            if(ReportingConst.haveMulipleReportFolder){
                if(folder.listFiles()!=null) {
                    resultFolder = reportingFolderPath + folder.listFiles().length + 1;
                }
                else{
                    resultFolder = reportingFolderPath + 1;
                }
            }
            else{
                resultFolder = reportingFolderPath;
            }
            folder = new File(resultFolder);
            // folder check - if not present create one
            if (!folder.exists()) {
                folder.mkdirs();
            }
            reporter = new Reporter(resultFolder);
        }
        return reporter;
    }
}
