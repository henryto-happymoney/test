tasks {
    val tarBuildReports by creating(Tar::class) {
        from(projectDir) {
            include("**/build/reports/**/*")
            include("**/build/containers-logs/**/*")
        }
        includeEmptyDirs = false
        archiveFileName.set("reports.tar.gz")
        destinationDirectory.set(file("$buildDir/artifacts"))
    }
}