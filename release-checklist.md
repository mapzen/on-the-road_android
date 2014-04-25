# Release Checklist

Instructions to build a new release version of **On the Road** and deploy to [Maven Central][4].

### 0. Deploy Snapshot (Optional)

Test the build process by deploying a new snapshot build.

```bash
$ mvn clean deploy
```

Verify snapshot build was successfully deployed to [Sonatype OSS Snapshots][1].

### 1. Prepare Release

Run the following commands to prepare the release build.

```bash
$ mvn release:clean
$ mvn release:prepare
```
Verify new commits pushed to [Mapzen GitHub Repo][2].

### 2. Stage Release

Run the following command to perform the release build.

```bash
$ mvn release:perform
```
Login to [Sonatype OSS Staging][3] and find the newly created staging repository. Select the repository and click "Close". Enter a description and click "Confirm".

Example:

> Promote artifact on-the-road-0.2

### 3. Release Artifact to Maven Central

Login to [Sonatype OSS Staging][3] and find the promoted staging repository from step 2. Select the repository and click "Release". Enter a description and click "Confirm".

Example:

> Release artifact on-the-road-0.2

**Note: It may take up to two hours for new artifacts to appear in [Maven Central][4].**

For more information see the official [Sonatype OSS Maven Repository Usage Guide][5].

[1]:https://oss.sonatype.org/#view-repositories;snapshots~browsestorage
[2]:https://github.com/mapzen/on-the-road
[3]:https://oss.sonatype.org/#stagingRepositories
[4]:http://search.maven.org/
[5]:https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
