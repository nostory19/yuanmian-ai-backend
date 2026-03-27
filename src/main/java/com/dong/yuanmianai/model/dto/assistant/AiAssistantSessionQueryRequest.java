package com.dong.yuanmianai.model.dto.assistant;

import com.dong.yuanmianai.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AiAssistantSessionQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
}
