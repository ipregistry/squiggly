# Squiggly Filter Jackson Spring Boot Example

This example shows how to use Squiggly Filter in a Spring Boot web environment using the `squiggly-spring-boot-starter`.

The starter auto-configures everything — no manual bean definitions needed. This example uses the Issue object described in the main documentation.

To run the example, cd to the examples/spring-boot directory on the command line and type the following:

```bash
../gradlew bootRun
```

In another terminal, you can request the issue json by doing the following:

1) To print the raw json
```bash
curl -s -g 'http://localhost:8080/issues'
```

2) To filter the raw json
```bash
curl -s -g 'http://localhost:8080/issues?fields=id,assignee[firstName]'
```
