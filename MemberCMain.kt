package com.group.ticketmachine

import com.group.ticketmachine.auth.LoginService
import com.group.ticketmachine.auth.UserStore
import com.group.ticketmachine.core.InMemoryStationProvider
import com.group.ticketmachine.offers.SpecialOfferRepository
import com.group.ticketmachine.offers.SpecialOfferService
import com.group.ticketmachine.com.group.ticketmachine.ui.AdminMenu
import com.group.ticketmachine.util.ConsoleIO

object MemberCMain {
    @JvmStatic
    fun main(args: Array<String> = emptyArray()) {
        // MEMBER C demo entrypoint
        val login = LoginService(UserStore.users)
        val stations = InMemoryStationProvider()
        val repo = SpecialOfferRepository()
        val offers = SpecialOfferService(repo, stations)
        val adminMenu = AdminMenu(offers)

        println("=== Ticket Machine (Member C) ===")
        while (true) {
            val u = ConsoleIO.prompt("Username: ")
            val p = ConsoleIO.prompt("Password: ")
            if (login.login(u, p)) {
                val name = login.currentUser?.username ?: "unknown"
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
        } // end while
    } // end main
} // end object
