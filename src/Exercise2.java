import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class Exercise2
{

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private OptimaizeLangDetector langDetector;

    public static void main(String[] args)
    {
        Exercise2 exercise = new Exercise2();
        exercise.run();
    }

    private void run()
    {
        try
        {
            if (!new File("./outputDocuments").exists())
            {
                Files.createDirectory(Paths.get("./outputDocuments"));
            }

            initLangDetector();

            File directory = new File("./documents");
            File[] files = directory.listFiles();
            for (File file : files)
            {
                processFile(file);
            }
        } catch (IOException | SAXException | TikaException e)
        {
            e.printStackTrace();
        }

    }

    private void initLangDetector() throws IOException
    {
        langDetector = new OptimaizeLangDetector();
    }

    private void processFile(File file) throws IOException, SAXException, TikaException
    {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try {
            parser.parse(Files.newInputStream(file.toPath()), handler, metadata, context);
        } catch (Exception e) {
            return;
        }

        String mimeType = metadata.get(Metadata.CONTENT_TYPE);
        String creator = metadata.get(Metadata.CREATOR);
        String creationDateStr = metadata.get(Metadata.CREATION_DATE);
        String lastModificationStr = metadata.get(Metadata.LAST_MODIFIED);

        Date creationDate = null;
        Date lastModification = null;
        if (creationDateStr != null && !creationDateStr.isEmpty())
            try {
                creationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(creationDateStr);
            } catch (Exception ignored) {}


        if (lastModificationStr != null && !lastModificationStr.isEmpty())
            try {
                lastModification = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(lastModificationStr);
            } catch (Exception ignored) {}

        String content = handler.toString();

        String language = null;
        if (content != null && !content.isEmpty())
            try {
                language = langDetector.detect(content).getLanguage();
            } catch (Exception ignored) {}

        saveResult(file.getName(), language, creator, creationDate, lastModification, mimeType, content);
    }

    private void saveResult(String fileName, String language, String creatorName, Date creationDate,
                            Date lastModification, String mimeType, String content)
    {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        int index = fileName.lastIndexOf(".");
        String outName = fileName.substring(0, index) + ".txt";
        try
        {
            PrintWriter printWriter = new PrintWriter("./outputDocuments/" + outName);
            printWriter.write("Name: " + fileName + "\n");
            printWriter.write("Language: " + (language != null ? language : "") + "\n");
            printWriter.write("Creator: " + (creatorName != null ? creatorName : "") + "\n");
            String creationDateStr = creationDate == null ? "" : dateFormat.format(creationDate);
            printWriter.write("Creation date: " + creationDateStr + "\n");
            String lastModificationStr = lastModification == null ? "" : dateFormat.format(lastModification);
            printWriter.write("Last modification: " + lastModificationStr + "\n");
            printWriter.write("MIME type: " + (mimeType != null ? mimeType : "") + "\n");
            printWriter.write("\n");
            printWriter.write(content + "\n");
            printWriter.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

}
