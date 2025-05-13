package com.jeremy.munch.ui.screens.cart

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import com.jeremy.munch.ui.theme.OrangeLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ------------------ ROOM SETUP ------------------

// CartEntity with proper PrimaryKey annotation
@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val quantity: Int
)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart")
    suspend fun getAllCartItems(): List<CartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartEntity)

    @Delete
    suspend fun deleteCartItem(cartItem: CartEntity)
}

@Database(entities = [CartEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cart_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ------------------ VIEWMODEL ------------------

class CartViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val cartDao: CartDao = AppDatabase.getInstance(application).cartDao()
    private val _cartList = mutableStateListOf<CartEntity>()
    val cartList: List<CartEntity> get() = _cartList

    init {
        loadCartItems()
    }

    // Load cart items from the database
    private fun loadCartItems() {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                cartDao.getAllCartItems()
            }
            _cartList.clear()
            _cartList.addAll(items)
        }
    }

    // Add an item to the cart
    fun addItemToCart(cartItem: CartEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cartDao.insertCartItem(cartItem)
            }
            loadCartItems() // Refresh cart after adding item
        }
    }

    // Remove an item from the cart
    fun removeCartItem(cartItem: CartEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cartDao.deleteCartItem(cartItem)
            }
            loadCartItems() // Refresh cart after removing item
        }
    }
}

// ------------------ CART SCREEN UI ------------------

@Composable
fun CartScreen(navController: NavController) {
    val viewModel: CartViewModel = viewModel()
    val cartList by remember { mutableStateOf(viewModel.cartList) } // This replaces derivedStateOf

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangeLight) // Set background color for the screen
    ) {
        if (cartList.isEmpty()) {
            Text(
                text = "Your cart is empty",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(cartList) { cartItem ->
                    CartItemCard(cartItem = cartItem, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CartItemCard(cartItem: CartEntity, viewModel: CartViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(Color.White)
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
                Text(text = "\$${cartItem.price}", fontSize = 16.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "x${cartItem.quantity}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(onClick = { viewModel.removeCartItem(cartItem) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Item")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCartScreen() {
    CartScreen(rememberNavController())
}
