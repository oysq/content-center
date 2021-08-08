package com.itmuch.usercenter.stream.output;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface MySource {

    String MY_OUTPUT = "my_output";

    @Output(MY_OUTPUT)
    MessageChannel output();

}
