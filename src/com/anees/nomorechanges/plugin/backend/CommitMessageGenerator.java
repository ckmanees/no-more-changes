package com.anees.nomorechanges.plugin.backend;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;

import com.anees.nomorechanges.plugin.Activator;

public class CommitMessageGenerator {

	private final ChatClient chatClient;

	private static String lastDiff = null;
	private static String lastCommitMessage = null;

	public CommitMessageGenerator() {
		String apiKey = Activator.getOpenAIApiKey();
		if (apiKey == null || apiKey.trim().isEmpty()) {
			throw new IllegalStateException("OpenAI API key is not configured. Set it in Preferences.");
		}

		OpenAiApi openAiApi = OpenAiApi.builder().apiKey(apiKey).build();
		ChatModel chatModel = OpenAiChatModel.builder().openAiApi(openAiApi).build();
		this.chatClient = ChatClient.create(chatModel);
	}

	public String generateCommitMessage(String diff) {

		if (diff != null && diff.equals(lastDiff) && lastCommitMessage != null) {
			return lastCommitMessage;
		}
		String prompt = """
				You are an expert programmer. Analyze the following git diff and generate a
				conventional commit message. The output should be a single block of text,
				perfect for a commit message box.
				Format it as <type>: <subject> followed by an optional body.
				Second line should be empty to separate header and body.
				It should be very concise yet understandable. no need for elaboration.

				Here is the diff:
				---
				%s
				""".formatted(diff);

		String generatedMessage = chatClient.prompt().user(prompt).call().content();

		if (generatedMessage != null && !generatedMessage.isBlank()) {
			lastDiff = diff;
			lastCommitMessage = generatedMessage;
		}
		
		return generatedMessage;
	}
}