package com.jeremy.munch.ui.screens.checkout

import android.content.Context
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import com.jeremy.munch.ui.theme.MunchTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ------------ DATABASE ENTITIES & DAO ------------

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val quantity: Int
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val items: String,
    val totalPrice: Double,
    val userName: String,
    val userAddress: String
)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart")
    suspend fun getAllCartItems(): List<CartEntity>

    @Query("DELETE FROM cart")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders")
    suspend fun getAllOrders(): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
}

@Database(entities = [OrderEntity::class, CartEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_app_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ------------ VIEWMODEL ------------

class CheckoutViewModel(context: Context) : ViewModel() {
    private val db = AppDatabase.getInstance(context)
    private val cartDao = db.cartDao()
    private val orderDao = db.orderDao()

    var cartItems by mutableStateOf(listOf<CartEntity>())
        private set

    var totalPrice by mutableStateOf(0.0)
        private set

    init {
        viewModelScope.launch {
            cartItems = cartDao.getAllCartItems()
            totalPrice = calculateTotalPrice(cartItems)
        }
    }

    fun placeOrder(userName: String, userAddress: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val orderItems = cartItems.joinToString(", ") { it.name }
            val order = OrderEntity(
                items = orderItems,
                totalPrice = totalPrice,
                userName = userName,
                userAddress = userAddress
            )
            withContext(Dispatchers.IO) {
                orderDao.insertOrder(order)
                cartDao.clearCart()
            }
            onSuccess()
        }
    }

    private fun calculateTotalPrice(items: List<CartEntity>): Double {
        return items.sumOf { it.price * it.quantity }
    }
}

class CheckoutViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CheckoutViewModel(context) as T
    }
}

// ------------ UI ------------

@Composable
fun CheckoutScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: CheckoutViewModel = viewModel(factory = CheckoutViewModelFactory(context))

    var userName by remember { mutableStateOf("") }
    var userAddress by remember { mutableStateOf("") }

    val OrangeLight = Color(0xFFFF9800)
    val OrangeDark = Color(0xFFFF5722)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangeDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Checkout",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(viewModel.cartItems) { cartItem ->
                    CheckoutItemCard(cartItem, OrangeLight)
                }
            }

            TextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            TextField(
                value = userAddress,
                onValueChange = { userAddress = it },
                label = { Text("Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Total: \$${String.format("%.2f", viewModel.totalPrice)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    if (userName.isNotBlank() && userAddress.isNotBlank()) {
                        viewModel.placeOrder(userName, userAddress) {
                            navController.navigate("orderConfirmation")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeLight,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Place Order", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CheckoutItemCard(cartItem: CartEntity, accentColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cartItem.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\$${cartItem.price}",
                    fontSize = 16.sp,
                    color = accentColor
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "x${cartItem.quantity}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun PreviewCheckoutScreen() {
    val navController = rememberNavController()
    MunchTheme {
        CheckoutScreen(navController = navController)
    }
}
