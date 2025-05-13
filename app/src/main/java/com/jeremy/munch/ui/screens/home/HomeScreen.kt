package com.jeremy.munch.ui.screens.home

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import com.jeremy.munch.navigation.ROUT_FOODLIST
import com.jeremy.munch.navigation.ROUT_ITEM
import com.jeremy.munch.ui.theme.OrangeLight

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 1. Room Database Setup
@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val imageRes: Int
)

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods")
    suspend fun getAllFoods(): List<FoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)
}

@Database(entities = [FoodEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 2. ViewModel with Factory
class FoodViewModel(application: Application) : AndroidViewModel(application) {
    private val foodDao: FoodDao = AppDatabase.getInstance(application).foodDao()

    suspend fun getAllFoods(): List<FoodEntity> {
        return withContext(Dispatchers.IO) {
            foodDao.getAllFoods()
        }
    }

    fun addFoodToDatabase(food: FoodEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                foodDao.insertFood(food)
            }
        }
    }
}

class FoodViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            return FoodViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// 3. Home Screen
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: FoodViewModel = viewModel(factory = FoodViewModelFactory(application))
    val foodList = remember { mutableStateOf(listOf<FoodEntity>()) }

    LaunchedEffect(Unit) {
        viewModel.addFoodToDatabase(
            FoodEntity(name = "Burger", description = "A juicy beef burger", price = 5.99, imageRes = 0)
        )
        viewModel.addFoodToDatabase(
            FoodEntity(name = "Pizza", description = "Cheesy pizza with pepperoni", price = 7.99, imageRes = 0)
        )
        foodList.value = viewModel.getAllFoods()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangeLight) // Apply theme color
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(foodList.value) { food ->
                FoodItemCard(food = food, navController = navController) // Pass navController
            }
        }
    }
}

// 4. Food Card
@Composable
fun FoodItemCard(food: FoodEntity, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(Color.White) // Card background color
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = food.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = food.description, fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "\$${food.price}", fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                // Navigate to CartScreen
                navController.navigate(ROUT_ITEM)
            }) {
                Text(text = "Add to Cart")
            }
        }
    }
}

// 5. Cart Screen
@Composable
fun CartScreen(navController: NavController) {
    // Assuming you have a list of items in the cart
    val cartItems = remember { mutableStateOf(listOf<FoodEntity>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(OrangeLight) // Use theme color
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(cartItems.value) { food ->
                CartItemCard(food = food)
            }
        }

        // Proceed to Checkout Button
        Button(
            onClick = {
                // Navigate to CheckoutScreen
                navController.navigate("checkout")
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text("Proceed to Checkout")
        }
    }
}

@Composable
fun CartItemCard(food: FoodEntity) {
    // A simple cart item UI
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = food.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = food.description, fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "\$${food.price}", fontSize = 18.sp, color = Color.Black)
        }
    }
}

// 6. Navigation Setup
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("cart") {
            CartScreen(navController)
        }
        composable("checkout") {
            // CheckoutScreen content can be added here later
            Text(text = "Checkout Screen")
        }
    }
}

// 7. MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}
