# 依赖一个 jdk8 的基础镜像
FROM java:8
# 然后将打包好的 jar 包复制到 根目录下，名取名为 app.jar
COPY target/foodie-user-web-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
# CMD 在前面讲过充当一个默认参数，所以这里不写 CMD 也可以从外部传递参数
CMD ["--spring.profiles.active=prod"]