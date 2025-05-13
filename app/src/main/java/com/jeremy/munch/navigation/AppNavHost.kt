package com.jeremy.munch.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeremy.munch.ui.screens.auth.LoginScreen
import com.jeremy.munch.ui.screens.auth.RegisterScreen
import com.jeremy.munch.data.UserDatabase
import com.jeremy.munch.repository.UserRepository
import com.jeremy.munch.ui.screens.about.AboutScreen
import com.jeremy.munch.ui.screens.splash.SplashScreen
import com.jeremy.munch.viewmodel.AuthViewModel
import com.jeremy.munch.ui.screens.cart.CartScreen
import com.jeremy.munch.ui.screens.checkout.CheckoutScreen
import com.jeremy.munch.ui.screens.foodlist.FoodListScreen
import com.jeremy.munch.ui.screens.cart.CartViewModel
import com.jeremy.munch.ui.screens.contact.ContactScreen
import com.jeremy.munch.ui.screens.dashboard.DashboardScreen
import com.jeremy.munch.ui.screens.home.HomeScreen
import com.jeremy.munch.ui.screens.item.ItemScreen
import com.jeremy.munch.ui.screens.orderconfirmation.OrderConfirmationScreen
import com.jeremy.munch.ui.screens.start.StartScreen

@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUT_SPLASH,
) {
    val context = LocalContext.current

    // Initialize Room Database and Repository for Authentication
    val appDatabase = UserDatabase.getDatabase(context)
    val authRepository = UserRepository(appDatabase.userDao())
    val authViewModel: AuthViewModel = AuthViewModel(authRepository)

    // ✅ Create a single instance of CartViewModel using a custom ViewModelProvider.Factory
    val cartViewModel: CartViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CartViewModel(context.applicationContext as android.app.Application) as T
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(ROUT_HOME) {
            HomeScreen(navController) // ✅ Pass cartViewModel
        }
        composable(ROUT_SPLASH) {
            SplashScreen(navController)
        }
        composable(ROUT_CART) {
            CartScreen(navController) // ✅ Pass cartViewModel
        }
        composable(ROUT_CHECKOUT) {
            CheckoutScreen(navController)
        }
        composable(ROUT_ORDERCONFIMATION) {
            OrderConfirmationScreen(navController)
        }
        composable(ROUT_FOODLIST) {
            FoodListScreen(navController)
        }
        composable(ROUT_ITEM) {
            ItemScreen(navController)
        }
        composable(ROUT_DASHBOARD) {
            DashboardScreen(navController)
        }
        composable(ROUT_START) {
            StartScreen(navController)
        }
        composable(ROUT_ABOUT) {
            AboutScreen(navController)
        }
        composable(ROUT_CONTACT) {
            ContactScreen(navController)
        }



        // Authentication screens
        composable(ROUT_REGISTER) {
            RegisterScreen(authViewModel, navController) {
                navController.navigate(ROUT_LOGIN) {
                    popUpTo(ROUT_REGISTER) { inclusive = true }
                }
            }
        }

        composable(ROUT_LOGIN) {
            LoginScreen(authViewModel, navController) {
                navController.navigate(ROUT_HOME) {
                    popUpTo(ROUT_LOGIN) { inclusive = true }
                }
            }
        }
    }
}
