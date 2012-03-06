package azkaban.webapp.servlet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import azkaban.utils.Utils;
import azkaban.webapp.AzkabanWebServer;
import azkaban.webapp.session.Session;

public class JobManagerServlet extends LoginAbstractAzkabanServlet {
    private static final long serialVersionUID = 1;
    private static final Logger logger = Logger
            .getLogger(JobManagerServlet.class);
    private static final int DEFAULT_UPLOAD_DISK_SPOOL_SIZE = 20 * 1024 * 1024;

    private MultipartParser multipartParser;
    private File tempDir;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.multipartParser = new MultipartParser(
                DEFAULT_UPLOAD_DISK_SPOOL_SIZE);

        tempDir = this.getApplication().getTempDirectory();
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp,
            Session session) throws ServletException, IOException {
        resp.getWriter().write("No get with servlet deploy");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp,
            Session session) throws ServletException, IOException {
        HashMap<String, Object> respObj = new HashMap<String, Object>();
        String action = getParam(req, "action");
        respObj.put("action", action);

        if (action.equals("verify")) {
            handleVerify(req, respObj, session);
        }

        writeJSON(resp, respObj);
    }

    private void handleVerify(HttpServletRequest req,
            Map<String, Object> respObj, Session session)
            throws ServletException {
        String projectName = getParam(req, "project");
        String filename = getParam(req, "filename");
        respObj.put("project", projectName);
        respObj.put("filename", filename);

        if (!filename.toLowerCase().endsWith(".zip")) {
            respObj.put("status", "error");
            respObj.put("message", "File must be a zip file.");
        } else {
            respObj.put("status", "success");
        }
    }

    private void handleDeploy(HttpServletRequest request,
            Map<String, Object> respObj, Session session) throws IOException,
            ServletException {

        if (!ServletFileUpload.isMultipartContent(request)) {
            respObj.put("status", "error");
            respObj.put("message", "No file found.");
        }

        Map<String, Object> params = this.multipartParser
                .parseMultipart(request);
        try {
            final AzkabanWebServer app = super.getApplication();

            FileItem item = (FileItem) params.get("file");
            String deployPath = (String) params.get("path");
            File jobDir = extractFile(item);
        } catch (Exception e) {
            logger.info("Installation Failed.", e);
            respObj.put("status", "error");
            respObj.put("message", "Installation Failed: " + e.getLocalizedMessage());
        }
    }

    private File extractFile(FileItem item) throws IOException,
            ServletException {
        final String contentType = item.getContentType();
        if (contentType.startsWith("application/zip")) {
            return unzipFile(item);
        }

        if (contentType.startsWith("application/x-tar")) {
            return untarFile(item);
        }

        throw new ServletException(String.format("Unsupported file type[%s].",
                contentType));
    }

    private File unzipFile(FileItem item) throws ServletException, IOException {
        File temp = File.createTempFile("job-temp", ".zip");
        temp.deleteOnExit();

        OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));

        IOUtils.copy(item.getInputStream(), out);
        out.close();

        ZipFile zipfile = new ZipFile(temp);
        File unzipped = Utils.createTempDir(tempDir);
        Utils.unzip(zipfile, unzipped);

        temp.delete();
        return unzipped;
    }

    private File untarFile(FileItem item) throws IOException, ServletException {
        File extractionPath = Utils.createTempDir(tempDir);

        if (true) {
            throw new ServletException("Unsupported file type [tar].");
        }

        return extractionPath;
    }
}
