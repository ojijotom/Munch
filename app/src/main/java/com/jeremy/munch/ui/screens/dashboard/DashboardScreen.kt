package com.jeremy.munch.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jeremy.munch.navigation.ROUT_ABOUT
import com.jeremy.munch.navigation.ROUT_CONTACT
import com.jeremy.munch.navigation.ROUT_FOODLIST
import com.jeremy.munch.navigation.ROUT_HOME
import com.jeremy.munch.navigation.ROUT_ITEM
import com.jeremy.munch.ui.theme.newOrange
import com.jeremy.munch.ui.theme.newWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(newOrange)
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp),
            colors = CardDefaults.cardColors(newWhite)
        ) {
            TopAppBar(
                title = {
                    Text("Dashboard Section", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu Icon")
                    }
                }
            )
        }

        // Welcome Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .offset(y = (-50).dp)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(newWhite),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Welcome Here!", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("WE VALUE YOUR MONEY!", fontSize = 14.sp, color = newOrange)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Feature Rows
        FeatureRow(
            navController = navController,
            card1 = FeatureCardData("Home", Icons.Default.Home, ROUT_HOME),
            card2 = FeatureCardData("About", Icons.Default.Info, ROUT_ABOUT)
        )

        Spacer(modifier = Modifier.height(30.dp))

        FeatureRow(
            navController = navController,
            card1 = FeatureCardData("Contact", Icons.Default.ContactMail, ROUT_CONTACT),
            card2 = FeatureCardData("Products", Icons.Default.ShoppingCart, ROUT_ITEM),
        )

        Spacer(modifier = Modifier.height(30.dp))

    }
}

@Composable
fun FeatureRow(navController: NavController, card1: FeatureCardData, card2: FeatureCardData) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FeatureCard(data = card1, navController = navController)
        FeatureCard(data = card2, navController = navController)
    }
}

@Composable
fun FeatureCard(data: FeatureCardData, navController: NavController) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(180.dp)
            .clickable { navController.navigate(data.route) },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(newWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = data.title,
                modifier = Modifier.size(80.dp),
                tint = newOrange
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = data.title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

data class FeatureCardData(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(rememberNavController())
}
