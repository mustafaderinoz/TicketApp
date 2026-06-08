package com.mustafaderinoz.ticketapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mustafaderinoz.core.domain.auth.AuthRepository
import com.mustafaderinoz.core.domain.auth.UserRole
import com.mustafaderinoz.ticketapp.screen.EventDetailScreen
import com.mustafaderinoz.ticketapp.screen.HomeScreen
import com.mustafaderinoz.ticketapp.screen.LoginScreen
import com.mustafaderinoz.ticketapp.screen.RegisterScreen
import com.mustafaderinoz.ticketapp.screen.StaffScreen
import com.mustafaderinoz.ticketapp.screen.TicketDetailScreen
import org.koin.compose.koinInject





@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = koinInject()
)

{
    val isLoggedIn by authRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
    val userRole by authRepository.userRole.collectAsStateWithLifecycle(initialValue = null)

    when(isLoggedIn)
    {
        null -> SplashScreen()
        true -> {
            when (userRole) {
                null -> SplashScreen()
                UserRole.STAFF-> StaffNavHost(navController)
                UserRole.ADMIN->AdminNavHost(navController)
                else -> AuthedNavHost(navController)
            }
        }
        false -> UnAuthedNavHost(navController)
    }

}

@Composable
private fun SplashScreen(){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        CircularProgressIndicator()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AuthedNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onNavigateToTicketDetail = { ticketId ->
                    navController.navigate(TicketDetail(ticketId))
                },
                onNavigateToEventDetail = { eventId ->
                    navController.navigate(EventDetail(eventId))
                }
            )
        }
        composable<TicketDetail> {
            TicketDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<EventDetail> {
            EventDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Home) { inclusive = false }
                    }
                }
            )
        }
    }
}


@Composable
private fun UnAuthedNavHost(navController: NavHostController){
    NavHost(navController=navController, startDestination = Login) {
        composable<Login>{
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {navController.navigate(Register)}
            )
        }
        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Login) {
                    popUpTo(Register) { inclusive = true }
                }},
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun StaffNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Staff) {
        composable<Staff> {
            StaffScreen() // Parametre kaldırıldı
        }
    }
}


@Composable
private fun AdminNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Admin) {
        composable<Admin> {
            //TODO: ADMIN
        }
    }
}