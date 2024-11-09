package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ChatResponse {
    public String id;
    public String object;
    public long created;
    public String model;
    public List<Choice> choices;
    public Usage usage;
    @JsonProperty("system_fingerprint")
    public String systemFingerprint;

    public String getMessageResponse(){
        return choices.get(0).message.content.toString();
    }



}

 class Choice {
    public int index;
    public Message message;
    public Object logprobs;
    @JsonProperty("finish_reason")
    public String finishReason;
}

 class Message {
    public String role;
    public String content;
    public Object refusal;
}

@JsonIgnoreProperties(ignoreUnknown = true)
 class Usage {
    @JsonProperty("prompt_tokens")
    public int promptTokens;
    @JsonProperty("completion_tokens")
    public int completionTokens;
    @JsonProperty("total_tokens")
    public int totalTokens;
    @JsonProperty("prompt_tokens_details")
    public TokensDetails promptTokensDetails;
    @JsonProperty("completion_tokens_details")
    public TokensDetails completionTokensDetails;
}


@JsonIgnoreProperties(ignoreUnknown = true)
 class TokensDetails {
    @JsonProperty("cached_tokens")
    public int cachedTokens;

    @JsonProperty("reasoning_tokens")
    public int reasoningTokens;
}

