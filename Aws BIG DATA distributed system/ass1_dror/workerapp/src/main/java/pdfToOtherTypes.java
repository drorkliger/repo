import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.PDFText2HTML;
import org.apache.pdfbox.rendering.PDFRenderer;




public class pdfToOtherTypes {
    private String path;

    public pdfToOtherTypes(String path) {
        this.path = path;
    }


    public void readPDF(String type) throws Exception {
        switch (type) {
            case "ToText":
                PDFtoTxt(path);
                break;
            case "ToImage":
                PDFtoPNG(path);
                break;
            case "ToHTML":
                PDFToHTML(path);
                break;

        }
    }

    private void PDFToHTML(String filePath) throws IOException {
        PDFText2HTML stripper = new PDFText2HTML();
        File f = new File(filePath);
        PDDocument pdDocument = PDDocument.load(f);
        stripper.setStartPage(1);
        stripper.setEndPage(1);
        StringWriter writer = new StringWriter();
        stripper.writeText(pdDocument, writer);
        PrintWriter pw = new PrintWriter(filePath.substring(0, filePath.length() - 4) + ".html");
        pw.print(writer.toString());
        pw.close();
        pdDocument.close();
    }

    private void PDFtoTxt(String filePath) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        File file = new File(filePath);
        PDDocument pdDocument = PDDocument.load(file);
        pdfStripper.setStartPage(1);
        pdfStripper.setEndPage(1);
        String text = pdfStripper.getText(pdDocument);
        PrintWriter pw = new PrintWriter(changeType(filePath,"txt"));
        pw.print(text);
        pw.close();
        pdDocument.close();
    }


    private void PDFtoPNG(String filePath) throws Exception {
        String out = changeType(filePath,"png");
        PDDocument pd = PDDocument.load(new File(filePath));
        PDFRenderer pr = new PDFRenderer(pd);
        BufferedImage bi = pr.renderImageWithDPI(0, 300);
        ImageIO.write(bi, "png", new File(out));
        pd.close();
    }

    private String changeType(String filePath, String newType){
        return filePath.substring(0, filePath.length() - 4) + "."+ newType;
    }

    public String convertCommandToType(String command)
    {
        String out=".png";
        if(command.equals("ToHTML"))
        {
            out=".html";
        }else if (command.equals("ToText")){
            out=".txt";
        }
        return out;

    }


}