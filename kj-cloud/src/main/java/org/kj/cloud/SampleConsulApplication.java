package org.kj.cloud;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@RestController
@EnableConfigurationProperties
@EnableFeignClients
@Slf4j
public class SampleConsulApplication /*implements ApplicationListener<SimpleRemoteEvent>*/ {

	@Autowired
	private LoadBalancerClient loadBalancer;

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private Environment env;

	@Autowired
	private SampleClient sampleClient;

	@Value("${spring.application.name:kj-cloud}")
	private String appName;

	@RequestMapping("/me")
	public ServiceInstance me() {
		return discoveryClient.getLocalServiceInstance();
	}

	@RequestMapping("/")
	public ServiceInstance lb() {
		return loadBalancer.choose(appName);
	}

	@RequestMapping("/choose")
	public String choose() {
		return loadBalancer.choose(appName).getUri().toString();
	}

	@RequestMapping("/myenv")
	public String env(@RequestParam("prop") String prop) {
		return env.getProperty(prop, "Not Found");
	}

	@RequestMapping("/prop")
	public String prop() {
		return sampleProperties().getProp();
	}

	@RequestMapping("/instances")
	public List<ServiceInstance> instances() {
		List<ServiceInstance> services = discoveryClient.getInstances(appName);
		/*
		services.forEach( (ServiceInstance s) -> 
			{ System.out.println( ToStringBuilder.reflectionToString(s));	
			
			}
		) ;
		*/
		return services ;
	}

	@RequestMapping("/feign")
	public String feign() {
		return sampleClient.choose();
	}

	@Bean
	public SampleProperties sampleProperties() {
		return new SampleProperties();
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleConsulApplication.class, args);
	}

	@FeignClient("kj-cloud")
	public interface SampleClient {

		@RequestMapping(value = "/choose", method = RequestMethod.GET)
		String choose();
	}
}
