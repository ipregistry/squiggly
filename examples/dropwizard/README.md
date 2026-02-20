# Squiggly Filter Jackson Dropwizard Example

This example shows how to use Squiggly Filter in a Dropwizard environment.

This example uses the Issue object described in the main documentation.

To run the example, cd to the examples/dropwizard directory on the command line type the following:

```bash
./gradlew run
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
