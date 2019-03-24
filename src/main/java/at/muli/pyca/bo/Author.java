package at.muli.pyca.bo;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class Author {

    private String name;

    private Map<String, Long> seenComments;
}
