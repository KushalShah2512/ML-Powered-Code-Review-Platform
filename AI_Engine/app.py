from flask import Flask, request, jsonify
from transformers import pipeline
import re

app = Flask(__name__)

print("Loading AI Model (This might take a minute the first time)...")
# Initialize the Hugging Face NLP Pipeline
# We use a zero-shot classifier to detect the "intent" or "risk" of the code
nlp_classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")
print("AI Model Loaded Successfully! 🚀")

@app.route('/', methods=['GET'])
def home():
    return jsonify({
        "message": "Welcome to the CodeReview.AI Engine!",
        "endpoints_available": ["/status", "/analyze"]
    })

@app.route('/status', methods=['GET'])
def status():
    return jsonify({"status": "online", "message": "CodeReview AI Engine is running!"})

@app.route('/analyze', methods=['POST'])
def analyze_code():
    data = request.json
    code_content = data.get('codeContent', '')

    print(f"\n[AI Engine] Analyzing code snippet ({len(code_content)} characters)...")
    
    issues_found = []
    fixes_recommended = [] # NEW: We will store our actionable advice here!
    base_score = 100

    # --- 1. NLP AI ANALYSIS ---
    risk_labels = ["contains hardcoded passwords", "contains sql injection", "insecure cryptography", "safe and clean code"]
    ai_result = nlp_classifier(code_content[:1000], risk_labels)
    
    top_label = ai_result['labels'][0]
    top_score = ai_result['scores'][0]

    if top_label != "safe and clean code" and top_score > 0.4:
        issues_found.append(f"AI Warning: {top_label} (Confidence: {int(top_score * 100)}%)")
        base_score -= 30
        
        # Add AI mapping fixes
        if top_label == "contains sql injection":
            fixes_recommended.append("💡 Use Prepared Statements or Parameterized Queries instead of string concatenation.")
        elif top_label == "contains hardcoded passwords":
            fixes_recommended.append("💡 Move credentials to a secure .env file or a Secrets Manager (like AWS Secrets Manager).")

    # --- 2. FAST STATIC ANALYSIS (REGEX SUPPLEMENT) ---
    if re.search(r'password\s*=\s*["\'][^"\']+["\']', code_content, re.IGNORECASE):
        if not any("hardcoded passwords" in issue for issue in issues_found):
            issues_found.append("Critical: Hardcoded password detected via Static Scan")
            fixes_recommended.append("💡 Never hardcode secrets. Use environment variables (e.g., System.getenv(\"DB_PASS\")).")
            base_score -= 20
            
    if re.search(r'SELECT\s+\*\s+FROM\s+\w+\s+WHERE\s+\w+\s*=\s*["\']\s*\+\s*\w+', code_content, re.IGNORECASE):
        if not any("sql injection" in issue for issue in issues_found):
            issues_found.append("Critical: Possible SQL Injection detected via Static Scan")
            fixes_recommended.append("💡 Never concatenate variables into SQL. Use PreparedStatement in Java.")
            base_score -= 40

    # --- 3. FORMAT THE RESPONSE ---
    final_score = max(0, base_score)
    feedback_message = "Clean and secure code." if final_score == 100 else f"Found {len(issues_found)} potential security risks."

    response_data = {
        "score": final_score,
        "feedback": feedback_message,
        "issues": issues_found,
        "fixes": fixes_recommended # NEW: Sending fixes back to Java!
    }
    
    print(f"[AI Engine] Analysis Complete. Score: {final_score}/100")
    return jsonify(response_data)

if __name__ == '__main__':
    app.run(port=5000, debug=True)