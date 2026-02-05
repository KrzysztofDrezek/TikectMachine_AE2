package com.group.ticketmachine

import com.group.ticketmachine.auth.AdminUserRepo
import com.group.ticketmachine.auth.LoginService
import com.group.ticketmachine.core.InMemoryStationProvider
import com.group.ticketmachine.db.Db
import com.group.ticketmachine.db.Schema
import com.group.ticketmachine.db.ticketmachine.ui.AdminMenu
import com.group.ticketmachine.offers.SpecialOfferRepository
import com.group.ticketmachine.offers.SpecialOfferService
import com.group.ticketmachine.util.ConsoleIO
import java.nio.file.Files
import java.nio.file.Paths

object MemberCMain {
    @JvmStatic
    fun main(args: Array<String> = emptyArray()) {
        // MEMBER C demo entrypoint (console)
        val dataDir = Paths.get("data")
        Files.createDirectories(dataDir)

        val db = Db(dataDir.resolve("ticketmachine.db"))
        Schema.create(db.connection)
        Schema.migrate(db.connection)
        Schema.seed(db.connection)

        val login = LoginService(AdminUserRepo(db.connection))
        val stations = InMemoryStationProvider()

        // ✅ SQLite-backed offers (same DB as GUI)
        val repo = SpecialOfferRepository(db.connection)
        val offers = SpecialOfferService(repo, stations)
        val adminMenu = AdminMenu(offers)

        println("=== Ticket Machine (Member C) ===")
        while (true) {
            val u = ConsoleIO.prompt("Username: ")
            val p = ConsoleIO.prompt("Password: ")

            if (login.login(u, p)) {
                val name = login.currentUsername() ?: "unknown"
                println("Logged in as $name")

                if (login.isAdmin()) {
                    adminMenu.run()
                } else {
                    println("You are not an admin. No admin functions available.")
                }

                login.logout()
                println("Logged out.\n")
            } else {
                println("Invalid credentials.\n")
            }
        }
    }
}
