package com.smartcare.ui;

import com.smartcare.util.ConfigLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AI Hospital Copilot Panel — SmartCare Hospital System.
 * Sends prompts to Google Gemini AI API using Java 21 HttpClient.
 * Demonstrates: Multithreading (SwingWorker), HttpClient, JSON parsing, file I/O.
 */
public class AICopilotPanel extends JPanel {

    private final DashboardFrame parentFrame;

    private JTextArea chatArea;
    private JTextField promptField;
    private JButton sendButton;
    private JLabel statusLabel;
    private JComboBox<String> promptTemplateCombo;
    private final List<String> chatHistory = new ArrayList<>();

    private static final Color ACCENT_AI    = new Color(32, 201, 151);
    private static final Color DARK_BG      = new Color(18, 24, 38);
    private static final Color MSG_USER_BG  = new Color(13, 110, 253);
    private static final Color MSG_AI_BG    = new Color(40, 50, 68);
    private static final Color BG_LIGHT     = new Color(245, 247, 250);

    // Pre-built hospital prompts for demo
    private static final String[] PROMPT_TEMPLATES = {
        "-- Select a quick prompt --",
        "What are the common symptoms of Malaria vs Dengue fever?",
        "Explain the standard treatment protocol for Type 2 Diabetes.",
        "What medications interact badly with Warfarin?",
        "Describe post-operative care guidelines for appendectomy patients.",
        "What are the warning signs of a stroke? (F.A.S.T. method)",
        "List the standard vitals to monitor for ICU patients.",
        "Explain the difference between Type 1 and Type 2 Diabetes.",
        "What is the first-line treatment for community-acquired pneumonia?",
        "Summarize hypertension management guidelines (JNC 8).",
        "What lab tests are critical for diagnosing kidney failure?"
    };

    public AICopilotPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 0));
        setBackground(DARK_BG);
        initComponents();
        showWelcomeMessage();
    }

    private void initComponents() {
        // ── TOP HEADER ────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(new Color(24, 32, 52));
        topBar.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel icon = new JLabel("✨");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("SmartCare AI Hospital Copilot");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Powered by Google Gemini  •  Clinical Decision Support Assistant");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(32, 201, 151));

        textPanel.add(titleLabel);
        textPanel.add(subLabel);
        brandPanel.add(icon);
        brandPanel.add(textPanel);

        JButton clearBtn = new JButton("🗑  Clear Chat");
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        clearBtn.setBackground(new Color(60, 70, 90));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        clearBtn.addActionListener(e -> clearChat());

        topBar.add(brandPanel, BorderLayout.WEST);
        topBar.add(clearBtn, BorderLayout.EAST);

        // ── CHAT AREA ─────────────────────────────────────────────────────────
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        chatArea.setBackground(DARK_BG);
        chatArea.setForeground(new Color(220, 230, 250));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(new EmptyBorder(15, 20, 15, 20));
        chatArea.setCaretColor(ACCENT_AI);

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createEmptyBorder());
        chatScroll.getViewport().setBackground(DARK_BG);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // ── QUICK TEMPLATES ───────────────────────────────────────────────────
        JPanel templateBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        templateBar.setBackground(new Color(24, 32, 52));
        templateBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(45, 58, 80)));

        JLabel templateLabel = new JLabel("Quick Templates:");
        templateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        templateLabel.setForeground(new Color(32, 201, 151));

        promptTemplateCombo = new JComboBox<>(PROMPT_TEMPLATES);
        promptTemplateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        promptTemplateCombo.setPreferredSize(new Dimension(380, 28));
        promptTemplateCombo.addActionListener(e -> {
            int idx = promptTemplateCombo.getSelectedIndex();
            if (idx > 0) {
                promptField.setText(PROMPT_TEMPLATES[idx]);
                promptTemplateCombo.setSelectedIndex(0);
            }
        });

        templateBar.add(templateLabel);
        templateBar.add(promptTemplateCombo);

        // ── INPUT BAR ─────────────────────────────────────────────────────────
        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(new Color(24, 32, 52));
        inputBar.setBorder(new EmptyBorder(12, 16, 14, 16));

        promptField = new JTextField();
        promptField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        promptField.setBackground(new Color(35, 45, 65));
        promptField.setForeground(Color.WHITE);
        promptField.setCaretColor(ACCENT_AI);
        promptField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 72, 100), 1),
                new EmptyBorder(10, 14, 10, 14)));
        promptField.putClientProperty("JTextField.placeholderText", "Ask the AI about clinical protocols, symptoms, medications...");

        promptField.addActionListener(e -> sendPrompt());  // Enter key triggers send

        sendButton = new JButton("  Send  ▶");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setBackground(ACCENT_AI);
        sendButton.setForeground(new Color(15, 40, 30));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        sendButton.addActionListener(e -> sendPrompt());

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(new Color(120, 150, 180));

        JPanel bottomBar = new JPanel(new BorderLayout(8, 0));
        bottomBar.setBackground(new Color(24, 32, 52));
        bottomBar.add(statusLabel, BorderLayout.WEST);

        inputBar.add(promptField, BorderLayout.CENTER);
        inputBar.add(sendButton,  BorderLayout.EAST);

        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.add(templateBar, BorderLayout.NORTH);
        bottomSection.add(inputBar,    BorderLayout.CENTER);
        bottomSection.add(bottomBar,   BorderLayout.SOUTH);

        add(topBar,      BorderLayout.NORTH);
        add(chatScroll,  BorderLayout.CENTER);
        add(bottomSection, BorderLayout.SOUTH);
    }

    // ── AI COMMUNICATION ──────────────────────────────────────────────────────

    private void sendPrompt() {
        String userInput = promptField.getText().trim();
        if (userInput.isEmpty()) return;

        appendToChat("👤 You", userInput, false);
        promptField.setText("");
        setInputEnabled(false);
        statusLabel.setText("⏳  AI Copilot is thinking...");

        // Use SwingWorker for background HTTP call — demonstrates multithreading
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return callGeminiAPI(userInput);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    appendToChat("✨ AI Copilot", response, true);
                    statusLabel.setText("✅  Response received at " +
                            LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                } catch (Exception ex) {
                    appendToChat("⚠ System", "Failed to get AI response: " + ex.getMessage()
                            + "\n\n💡 Tip: Configure your Gemini API key in src/main/resources/db.properties (ai.apiKey=YOUR_KEY)"
                            + "\n   Get a free key at: https://aistudio.google.com/app/apikey", true);
                    statusLabel.setText("⚠  Connection failed — check API key in db.properties");
                } finally {
                    setInputEnabled(true);
                }
            }
        }.execute();
    }

    private String callGeminiAPI(String userPrompt) throws Exception {
        String apiKey = ConfigLoader.get("ai.apiKey", "");
        String model  = ConfigLoader.get("ai.model", "gemini-2.0-flash");

        if (apiKey.isEmpty() || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            return getDemoResponse(userPrompt);
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        // Build hospital-specific system context
        String systemContext = "You are a professional AI Clinical Decision Support assistant for SmartCare " +
                "Hospital Management System. Provide accurate, concise, and evidence-based medical information. " +
                "Always remind users that AI responses are for informational purposes and clinical decisions " +
                "must be made by qualified medical professionals. Format responses clearly with bullet points " +
                "or numbered lists when appropriate.";

        String jsonBody = String.format("""
                {
                  "contents": [
                    {
                      "parts": [
                        {"text": "%s\\n\\nUser Query: %s"}
                      ]
                    }
                  ],
                  "generationConfig": {
                    "temperature": 0.4,
                    "topK": 32,
                    "topP": 0.95,
                    "maxOutputTokens": 1024
                  }
                }
                """, systemContext.replace("\"", "\\\""), userPrompt.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API Error " + response.statusCode() + ": " + response.body());
        }

        return parseGeminiResponse(response.body());
    }

    private String parseGeminiResponse(String jsonBody) {
        // Simple JSON text extraction without external library dependency
        // Extracts the "text" field from Gemini API response
        try {
            int textStart = jsonBody.indexOf("\"text\": \"");
            if (textStart == -1) textStart = jsonBody.indexOf("\"text\":\"");
            if (textStart == -1) return "No response text found in API reply.";

            int valueStart = jsonBody.indexOf("\"", textStart + 8) + 1;
            int valueEnd   = valueStart;

            // Walk forward, respecting escaped characters
            while (valueEnd < jsonBody.length()) {
                char c = jsonBody.charAt(valueEnd);
                if (c == '\\') { valueEnd += 2; continue; }
                if (c == '"')  break;
                valueEnd++;
            }

            return jsonBody.substring(valueStart, valueEnd)
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .replace("\\'", "'")
                    .trim();
        } catch (Exception e) {
            return "Error parsing AI response. Raw: " + jsonBody.substring(0, Math.min(200, jsonBody.length()));
        }
    }

    /**
     * Demo responses shown when no API key is configured —
     * demonstrates the UI works even without an active API key.
     */
    private String getDemoResponse(String prompt) {
        String p = prompt.toLowerCase();

        if (p.contains("diabetes")) return """
                📋 Type 2 Diabetes Management Overview:
                
                • First-line: Metformin (if no contraindications) + lifestyle modification
                • Target HbA1c: < 7.0% for most patients (individualized)
                • Blood pressure target: < 130/80 mmHg
                • Lifestyle: Low-carb diet, 150 min/week aerobic exercise
                
                Second-line agents (if HbA1c uncontrolled):
                • GLP-1 agonists (Semaglutide) — if CVD risk
                • SGLT-2 inhibitors (Empagliflozin) — if CKD/HF
                • DPP-4 inhibitors (Sitagliptin) — if minimal hypoglycemia risk
                
                ⚠️ This is informational only. Always follow current clinical guidelines and patient-specific factors.
                """;

        if (p.contains("malaria") || p.contains("dengue")) return """
                🦟 Malaria vs Dengue Fever — Key Differences:
                
                MALARIA (Plasmodium parasites):
                • Cyclical fever: every 48-72 hrs (tertian/quartan pattern)
                • Chills, rigors, sweating
                • Splenomegaly, anemia
                • Diagnosis: RDT / Peripheral blood smear / PCR
                • Treatment: Artemisinin-based Combination Therapy (ACT)
                
                DENGUE (Flavivirus, Aedes mosquito):
                • High fever (39-40°C), sudden onset
                • Severe headache, retro-orbital pain
                • Rash (maculopapular), myalgia
                • Warning signs: abdominal pain, bleeding, rapid breathing
                • Thrombocytopenia (platelets < 100,000/mm³)
                • Treatment: Supportive (IV fluids, paracetamol only — NO NSAIDs)
                
                ⚠️ Clinical judgment and local epidemiology are essential for diagnosis.
                """;

        if (p.contains("stroke") || p.contains("fast")) return """
                🧠 Stroke Warning Signs — F.A.S.T. Method:
                
                F — FACE: Ask to smile. Does one side droop?
                A — ARMS: Ask to raise both arms. Does one drift downward?
                S — SPEECH: Ask to repeat a phrase. Is speech slurred or strange?
                T — TIME: Time to call emergency services IMMEDIATELY!
                
                Additional symptoms:
                • Sudden numbness/weakness (face, arm, leg — especially one side)
                • Sudden confusion or trouble understanding
                • Sudden vision loss (one or both eyes)
                • Sudden severe headache with no known cause
                • Sudden trouble walking or loss of balance
                
                ⏰ "Time is Brain" — Every 60 seconds, 1.9 million neurons are lost.
                Goal: Door-to-Needle for tPA ≤ 60 minutes, Door-to-Groin for thrombectomy ≤ 90 min.
                """;

        return """
                👋 SmartCare AI Copilot — Demo Mode
                
                Your query: "%s"
                
                ℹ️ The AI Copilot is running in DEMO MODE because no Gemini API key is configured.
                
                To enable live AI responses:
                1. Get a free key at: https://aistudio.google.com/app/apikey
                2. Open: src/main/resources/db.properties
                3. Set: ai.apiKey=YOUR_ACTUAL_KEY_HERE
                4. Restart the application
                
                Try these sample prompts to see demo responses:
                • "What are the symptoms of malaria vs dengue?"
                • "Explain Type 2 Diabetes treatment"
                • "What is the FAST method for stroke?"
                """.formatted(prompt);
    }

    // ── UI HELPERS ────────────────────────────────────────────────────────────

    private void appendToChat(String sender, String message, boolean isAI) {
        String separator = "─".repeat(60);
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        String formatted = String.format("\n%s  [%s]\n%s\n%s\n",
                sender, timestamp, separator, message.trim());
        chatArea.append(formatted);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void showWelcomeMessage() {
        String apiKey = ConfigLoader.get("ai.apiKey", "");
        boolean hasKey = !apiKey.isEmpty() && !apiKey.equals("YOUR_GEMINI_API_KEY_HERE");

        String welcome = """
                ╔══════════════════════════════════════════════════════════════╗
                ║      ✨  SmartCare AI Hospital Copilot — Ready              ║
                ╚══════════════════════════════════════════════════════════════╝
                
                Welcome! I am your AI-powered Clinical Decision Support Assistant.
                I can help with:
                
                  🩺  Symptom analysis & differential diagnosis guidance
                  💊  Drug information & interaction checks
                  📋  Clinical protocol summaries
                  🔬  Lab value interpretation
                  📊  Treatment guideline references
                
                """ + (hasKey
                ? "✅  API Key configured — Live AI responses ENABLED (Google Gemini)"
                : "⚠️  Demo Mode: Configure ai.apiKey in db.properties for live AI responses") + """
                
                ─────────────────────────────────────────────────────────────
                ⚕  IMPORTANT DISCLAIMER
                This tool is for educational and decision-support purposes only.
                All clinical decisions must be made by qualified medical professionals.
                ─────────────────────────────────────────────────────────────
                
                Type your question below or select a quick template to get started.
                """;

        chatArea.setText(welcome);
    }

    private void clearChat() {
        chatHistory.clear();
        chatArea.setText("");
        showWelcomeMessage();
        statusLabel.setText(" ");
    }

    private void setInputEnabled(boolean enabled) {
        promptField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        promptTemplateCombo.setEnabled(enabled);
        if (enabled) promptField.requestFocus();
    }
}
