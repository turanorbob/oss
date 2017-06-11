package org.jks.docker.test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InfoCmd;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import lombok.extern.java.Log;
import java.util.List;

/**
 * Created by Administrator on 2017/6/11.
 */
@Log
public class Test {
    public static void main(String args[]){
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.10.118:2375")
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        try {
            InspectImageResponse response = dockerClient.inspectImageCmd("busybox").exec();
            log.info(response.toString());
            List<Container> containerList = dockerClient.listContainersCmd().withShowAll(true).exec();
            log.info(containerList.toString());
        } catch (NotFoundException e) {
            log.info("Pulling image 'busybox'");
            // need to block until image is pulled completely
            dockerClient.pullImageCmd("busybox").withTag("latest").exec(new PullImageResultCallback()).awaitSuccess();
        }
    }
}
