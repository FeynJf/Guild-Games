package com.guild.games

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.guild.games.ui.*

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val goMain: () -> Unit = {
        nav.navigate("main") { popUpTo("login") { inclusive = true } }
    }
    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            SplashScreen { loggedIn ->
                nav.navigate(if (loggedIn) "main" else "login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
        composable("login") {
            LoginScreen(
                onSuccess = goMain,
                onRegister = { nav.navigate("register") },
                onRegisterDev = { nav.navigate("registerdev") }
            )
        }
        composable("register") { RegisterScreen(onSuccess = goMain, onBack = { nav.popBackStack() }) }
        composable("registerdev") { RegisterDeveloperScreen(onSuccess = goMain, onBack = { nav.popBackStack() }) }
        composable("main") {
            MainScreen(
                onCreateGuild = { nav.navigate("createguild") },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    nav.navigate("login") { popUpTo("main") { inclusive = true } }
                }
            )
        }
        composable("createguild") { CreateGuildScreen(onBack = { nav.popBackStack() }) }
    }
}

