/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.front.rabbitmq;

import com.webank.webase.front.util.RabbitMQUtils;
import org.junit.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MQUtilsTest {

    @Test
    public void testDistinct() {
        List<String> array = new ArrayList<>();
        array.add("exchange1.queue1");
        array.add("exchange2.queue2");
        Map<String,List<String>> map = RabbitMQUtils.distinctList(array);
        for (String string : map.keySet()){
            System.out.println(string);
            for(String string1 : map.get(string)){
                System.out.println(string1);
            }
        }
    }
}
