package ribbonconfiguration;

import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ribbonconfiguration.impl.NacosSameVersionRule;

@Configuration
public class RibbonConfig {

    @Bean
    public IRule ribbonRule() {
        return new NacosSameVersionRule();
    }



}
