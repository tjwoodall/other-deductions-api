API Example Microservice
========================
This API allows software packages to retrieve, create and amend, and delete deductions that have been previously populated.

## Requirements
- Scala 2.12.x
- Java 8
- sbt 1.3.7
- [Service Manager](https://github.com/hmrc/service-manager)
 
## Development Setup
 
Run from the console using: `sbt run` (starts on port 7797 by default)
 
Start the service manager profile: `sm --start MTDFB_ALL`
 
# Run tests
```
sbt test
sbt it:test
```

## To view the RAML

Start api definition services

```
sm --start COMBINED_API_DEFINITION API_DEFINITION API_EXAMPLE_MICROSERVICE API_DOCUMENTATION_FRONTEND -f
sm --start ASSETS_FRONTEND -r 3.11.0 -f
```

Go to http://localhost:9680/api-documentation/docs/api/preview and enter http://localhost:7797/api/conf/1.0/application.raml 

## Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/other-deductions-api/issues)


## API Reference / Documentation 
Available on the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation)

# License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
