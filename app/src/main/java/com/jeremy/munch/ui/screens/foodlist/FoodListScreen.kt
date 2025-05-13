package com.jeremy.munch.ui.screens.foodlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.jeremy.munch.R


// Room Database Entity for Food
@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val imageRes: Int
)

// DAO for accessing Food data
@Dao
interface FoodDao {
    @Query("SELECT * FROM foods")
    suspend fun getAllFoods(): List<FoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)
}

// Room Database class
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
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ViewModel for Food
class FoodViewModel(context: android.content.Context) : ViewModel() {
    private val foodDao: FoodDao = AppDatabase.getInstance(context).foodDao()

    private val _foodList = MutableStateFlow<List<FoodEntity>>(emptyList())
    val foodList: StateFlow<List<FoodEntity>> = _foodList

    init {
        viewModelScope.launch {
            _foodList.value = withContext(Dispatchers.IO) {
                foodDao.getAllFoods()
            }
        }
    }

    fun addFoodToDatabase(food: FoodEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                foodDao.insertFood(food)
                _foodList.value = foodDao.getAllFoods() // Refresh list after insertion
            }
        }
    }
}

// ViewModel Factory
class FoodViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Food List Screen UI
@Composable
fun FoodListScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: FoodViewModel = viewModel(factory = FoodViewModelFactory(context))
    val foodList = viewModel.foodList.collectAsState()

    LaunchedEffect(Unit) {
        if (foodList.value.isEmpty()) {
            // Add sample data with image references
            viewModel.addFoodToDatabase(
                FoodEntity(name = "Burger", description = "A juicy beef burger", price = 5.99, imageRes = R.drawable.img_11)
            )
            viewModel.addFoodToDatabase(
                FoodEntity(name = "Pizza", description = "Cheesy pizza with pepperoni", price = 7.99, imageRes = R.drawable.img_4)
            )
        }
    }

    // Color for the splash screen
    val splashColor = Color(0xFFFF9800) // Orange tone from SplashScreen color

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(splashColor)
    ) {
        if (foodList.value.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Text("Loading food items...", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(foodList.value) { food ->
                    FoodItemCard(food = food)
                }
            }
        }
    }
}

// Food Item Card UI with images
@Composable
fun FoodItemCard(food: FoodEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Image for the food item
            Image(
                painter = painterResource(id = food.imageRes),
                contentDescription = food.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
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

// Preview of the Food List Screen with Images
@Preview(showBackground = true)
@Composable
fun PreviewFoodListScreen() {
    FoodListScreen(rememberNavController()) // Mock NavController
}
