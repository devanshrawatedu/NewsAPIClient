# NewsAPI Dashboard 🚀

A real-time news search dashboard built with Java. It features a modern, "Google-style" web interface and a local Java backend proxy to bypass CORS restrictions.

## 📋 Features
- **Modern UI**: Clean, dark-mode search interface.
- **Proxy Backend**: Built-in Java HTTP server to handle API requests securely.
- **Dynamic Search**: Fetch news by keywords or categories without page reloads.
- **Fedora Ready**: Optimized for Linux/Fedora environments.

## 🛠️ Setup & Installation

1. **Clone the repository**:
   ```bash
   git clone <your-repo-link>
   cd NewsAPIClient
   ```

2. **Set up your API Key**:
   - Create a `.env` file in the root directory.
   - Add your [NewsAPI](https://newsapi.org) key:
     ```text
     NEWSAPI_KEY=your_key_here
     ```

3. **Compile the project**:
   ```bash
   javac -cp .:lib/json-20251224.jar NewsAPIClient.java
   ```

4. **Run the application**:
   ```bash
   java -cp .:lib/json-20251224.jar NewsAPIClient
   ```
   *The dashboard will automatically open in your default browser at `http://localhost:8080`.*

## ⚖️ Academic Undertaking
I hereby give permission for this mini-project to be used by junior students for academic and reference purposes in the future.

---
**Course:** Advanced Java Programming 
**Author:** Devansh Rawat
**Instructor:** [Prof. Ajay Kaushik](https://github.com/ajaykaushikphd)

