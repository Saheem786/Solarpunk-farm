tasks.register<Exec>("assembleDebug") {
    commandLine("npx", "vite", "build")
}
