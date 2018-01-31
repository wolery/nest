# Nest: Wolery Core Libraries for Scala

Nest is a set of core libraries that includes ... and much more! :-)

## Usage

The Maven group ID is `com.wolery`, and the artifact ID is `nest`.

To add a dependency on Nest using Maven, use the following:

```xml
<dependency>
  <groupId>com.wolery</groupId>
  <artifactId>nest</artifactId>
  <version> ... </version>
</dependency>
```

## Links

- [GitHub project](https://github.com/wolery/nest)
- [Issue tracker: Report a defect or feature request](https://github.com/wolery/nest/issues/new)
- [StackOverflow: Ask "how-to" and "why-didn't-it-work" questions](https://stackoverflow.com/questions/ask?tags=wolery+nest+scala)

## Limitations

1. Serialized forms of ALL objects are subject to change unless noted otherwise. Do not persist these and assume they can be read by a
future version of the library.

1. Our classes are not designed to protect against a malicious caller. You should not use them for communication between trusted and
untrusted code.

1. We unit-test the libraries using only Oracle JDK 1.9 on Mac OS X. Some features may not currently work correctly in other environments.
