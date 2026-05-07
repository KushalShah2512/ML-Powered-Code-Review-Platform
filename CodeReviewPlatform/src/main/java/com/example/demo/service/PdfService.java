package com.example.demo.service;

import com.example.demo.model.CodeFile; 
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.awt.Color;
import java.time.LocalDate;

@Service
public class PdfService {

    public void export(HttpServletResponse response, CodeFile file) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // 1. Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
        Paragraph title = new Paragraph("Code Analysis Report", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); 

        // 2. Scan Details
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Scan ID: #" + file.getId(), headerFont));
        document.add(new Paragraph("File Name: " + file.getFileName(), bodyFont));
        document.add(new Paragraph("Date: " + LocalDate.now(), bodyFont));
        document.add(new Paragraph(" "));

        // 3. Analysis Results
        document.add(new Paragraph("Analysis Results:", headerFont));
        
        // --- LOGIC TO EXTRACT SCORE ---
        String feedback = file.getFeedback();
        String extractedScore = "Not Available";
        
        // We look for the pattern "[Score: 60/100]"
        if (feedback != null && feedback.contains("[Score:")) {
            try {
                int start = feedback.indexOf("[Score:") + 7; // Move past "[Score:"
                int end = feedback.indexOf("]", start);
                if (end > start) {
                    extractedScore = feedback.substring(start, end).trim(); // Becomes "60/100"
                }
            } catch (Exception e) {
                extractedScore = "Error parsing score";
            }
        }
        // -----------------------------

        // 4. Show Score in BIG RED TEXT
        Font scoreLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font scoreValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.RED);
        
        Paragraph scorePara = new Paragraph();
        scorePara.add(new Chunk("Score: ", scoreLabelFont));
        scorePara.add(new Chunk(extractedScore, scoreValueFont));
        document.add(scorePara);

        // 5. Show Full Feedback
        Font feedbackFont = FontFactory.getFont(FontFactory.COURIER, 11, Color.DARK_GRAY);
        document.add(new Paragraph("Feedback Detail: " + feedback, feedbackFont));
        
        document.add(new Paragraph("------------------------------------------------"));

        // 6. Source Code
        document.add(new Paragraph("Source Code:", headerFont));
        Font codeFont = FontFactory.getFont(FontFactory.COURIER, 10, Color.BLACK);
        document.add(new Paragraph(file.getContent(), codeFont));

        document.close();
    }
}