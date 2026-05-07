import sys

def analyze_code():
    # 1. READ FROM STANDARD INPUT (Robust method)
    try:
        # This reads everything Java sends until it closes the stream
        code_snippet = sys.stdin.read()
    except Exception:
        code_snippet = ""

    if not code_snippet.strip():
        print("0;Unknown;❌ Error: No code received by Python.")
        return

    feedback = []
    score = 100
    
    # --- ANALYSIS LOGIC ---
    if "password" in code_snippet.lower() and "=" in code_snippet:
        feedback.append("⚠️ CRITICAL: Hardcoded password detected.")
        score -= 30
    
    if "Statement" in code_snippet and "PreparedStatement" not in code_snippet:
        feedback.append("🚫 SECURITY: SQL Injection Risk.")
        score -= 20

    if "System.out.println" in code_snippet:
        feedback.append("📝 STYLE: Use Loggers instead of System.out.println.")
        score -= 10

    if "catch (Exception e) {}" in code_snippet or "catch(Exception e){}" in code_snippet:
        feedback.append("🐛 BUG: Empty catch block detected.")
        score -= 15

    # --- CALCULATE RANK ---
    if score >= 90: rank = "Code Ninja 🥷"
    elif score >= 70: rank = "Developer 👨‍💻"
    elif score >= 50: rank = "Junior 👶"
    else: rank = "Bug Hunter 🐛"

    # --- OUTPUT ---
    feedback_str = " | ".join(feedback) if feedback else "✅ Clean Code - Great Job!"
    print(f"{score};{rank};{feedback_str}")

if __name__ == "__main__":
    analyze_code()