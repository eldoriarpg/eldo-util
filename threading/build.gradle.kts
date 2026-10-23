plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Utilitites for threading and scheduling"

dependencies{
    api(project(":core"))
}
