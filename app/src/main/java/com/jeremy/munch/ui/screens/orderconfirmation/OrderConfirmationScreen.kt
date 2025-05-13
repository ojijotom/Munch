package com.jeremy.munch.ui.screens.orderconfirmation

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.compose.*
import androidx.room.*
import com.jeremy.munch.ui.theme.MunchTheme
import kotlinx.coroutines.launch

// Room Database setup

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val items: String,
    val totalPrice: Double,
    val userName: String,
    val userAddress: String
)

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY id DESC LIMIT 1")
    suspend fun getLatestOrder(): OrderEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
}

@Database(entities = [OrderEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Application): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "order_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ViewModel

class OrderConfirmationViewModel(application: Application) : AndroidViewModel(application) {
    private val orderDao: OrderDao = AppDatabase.getInstance(application).orderDao()

    private val _orderState = mutableStateOf<OrderEntity?>(null)
    val orderState: State<OrderEntity?> = _orderState

    init {
        fetchLatestOrder()
    }

    private fun fetchLatestOrder() {
        viewModelScope.launch {
            _orderState.value = orderDao.getLatestOrder()
        }
    }
}

class OrderConfirmationViewModelFactory(
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderConfirmationViewModel::class.java)) {
            return OrderConfirmationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Order Confirmation Composable

@Composable
fun OrderConfirmationScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: OrderConfirmationViewModel = viewModel(
        factory = OrderConfirmationViewModelFactory(context.applicationContext as Application)
    )

    val order = viewModel.orderState.value

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.Center)
            ) {
                Text(
                    text = "Order Confirmation",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Thank you for your order, ${order.userName}!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your order details:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Items: ${order.items}", fontSize = 16.sp, color = Color.Black)
                        Text(
                            text = "Total Price: \$${"%.2f".format(order.totalPrice)}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Delivery Address: ${order.userAddress}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                Button(
                    onClick = { navController.navigate("homeScreen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Go to Home", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { navController.navigate("checkoutScreen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text(text = "Return to Checkout", fontSize = 18.sp)
                }
            }
        }
    }
}

// MainActivity with proper navigation setup

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MunchTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "orderConfirmation") {
        composable("orderConfirmation") {
            OrderConfirmationScreen(navController = navController)
        }
        composable("homeScreen") {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Home Screen", fontSize = 24.sp)
            }
        }
        composable("checkoutScreen") {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Checkout Screen", fontSize = 24.sp)
            }
        }
    }
}
