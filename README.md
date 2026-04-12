> [!IMPORTANT]  
> Liberty is still in early development, expect lots of issues!

# Liberty

Liberty is a lightweight, JavaFX-based desktop chat application for interacting with local LLMs via [Ollama](https://ollama.com/). It aims to provides a clean, modern interface with a smooth typing effect and built-in conversation management.

## Features

- **Interactive UI**: A simple and responsive chat interface built with JavaFX.
- **Smooth AI Typing**: Experience AI responses with a natural, smooth typing animation.
- **Ollama Integration**: Automatically manages Ollama server startup and model pulling.
- **Conversation Management**: Save and load your chat histories to/from JSON files.
- **Slash Commands**: Use intuitive commands directly in the chat input:
    - `/help`: Display available commands.
    - `/save <filename>`: Save current conversation history.
    - `/load <filename>`: Load a previous conversation history.
    - `/clear`: Clear the chat window.
    - `/exit`: Close the application.
- **Model Support**: Defaulted to use `qwen2.5-coder:7b-instruct`, optimized for coding and technical tasks.

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
- `src/main/java/com/liberty/liberty/OllamaChatService.java`: Handles communication with the Ollama API and conversation history.
- `src/main/java/com/liberty/liberty/OllamaBootstrap.java`: Manages Ollama server and model initialization.
- `conversation_history/`: Directory where saved conversation JSON files are stored.

## Configuration

The default model is set in `OllamaChatService.java`. You can change it by modifying the `MODEL` constant:
```java
private final String MODEL = "qwen2.5-coder:7b-instruct";
```

## Dependencies

- **JavaFX 21**: Controls and FXML.
- **Jackson Databind**: For JSON processing and conversation history.
- **JUnit 5**: For unit testing.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details (if applicable).
