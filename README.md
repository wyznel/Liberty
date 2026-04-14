> [!IMPORTANT]  
> Liberty is still in early development, expect lots of issues!

# Liberty

Liberty is a lightweight, JavaFX-based desktop chat application for interacting with local LLMs via [Ollama](https://ollama.com/). It aims to provides a clean, modern interface with a smooth typing effect and built-in conversation management.

## Features

- **Interactive UI**: A simple and responsive chat interface built with JavaFX.
- **Smooth AI Typing**: Experience AI responses with a natural, smooth typing animation.
- **Ollama Integration**: Automatically manages Ollama server startup and model pulling.
- **Tool Calling (Beta)**: Liberty can now perform actions on your file system and extract data:
    - **File Handling**: Create, read, write, append, and list files.
    - **PDF Text Extraction**: Extract text content from PDF documents.
- **Conversation Management**: Save and load your chat histories to/from JSON files.
- **Slash Commands**: Use intuitive commands directly in the chat input:
    - `/help`: Display available commands.
    - `/save <filename>`: Save current conversation history.
    - `/load <filename>`: Load a previous conversation history.
    - `/clear`: Clear the chat window.
    - `/exit`: Close the application.
- **Model Support**: Defaulted to use `qwen3:4b-instruct`, optimized for chat and tool-calling tasks.

## Prerequisites

- **Java**: JDK 24 or higher is required.
- **Ollama**: Ensure [Ollama](https://ollama.com/) is installed and the `ollama` command is accessible in your system's PATH.
- **Maven**: For building and running the project.

## Getting Started

### 1. Build and Run
Use the Maven Wrapper to build and run the application:
```bash
./mvnw clean javafx:run
```

The application will check if the Ollama server is running and ensure the required model is pulled before starting the chat interface.

## Project Structure

- `src/main/java/com/liberty/liberty/Liberty.java`: Main application entry point and UI logic.
- `src/main/java/com/liberty/liberty/OllamaChatService.java`: Handles communication with the Ollama API, tool calling, and conversation history.
- `src/main/java/com/liberty/liberty/OllamaBootstrap.java`: Manages Ollama server and model initialization.
- `src/main/java/com/liberty/liberty/SmoothTyper.java`: Logic for the smooth typing animation.
- `src/main/java/com/liberty/liberty/Tools/`: Package containing tool implementations for LLM interaction.
    - `FileHandlingTools.java`: Tools for local file system operations.
    - `PDFTools.java`: Tools for PDF document text extraction.
- `conversation_history/`: Directory where saved conversation JSON files are stored.
- `sandbox/`: Default workspace for file-based tools.

## Configuration

Available models are listed in `OllamaChatService.java`. You can change the default model by modifying the `AVAILABLE_MODELS` array and the model selection index:

```java
private final static String[] AVAILABLE_MODELS = {
        "gemma4:e4b", "qwen2.5:7b-instruct",
        "qwen3.5:4b", "qwen3:4b-instruct",
        "gemma4:e2b"
};
private final OllamaChatRequest builder = OllamaChatRequest.builder().withModel(AVAILABLE_MODELS[3]);
```

## Dependencies

- **JavaFX 21**: Controls and FXML.
- **ollama4j**: For communication with the Ollama API and tool-calling support.
- **Apache PDFBox**: For PDF text extraction.
- **Jackson Databind**: For JSON processing and conversation history.
- **JUnit 5**: For unit testing.

## License

This project is licensed under the APACHE-2.0 License - see the [LICENSE](LICENSE) file for details.
