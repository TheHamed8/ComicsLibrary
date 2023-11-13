package com.example.comicslibrary

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.comicslibrary.view.CharacterDetailScreen
import com.example.comicslibrary.view.CharactersBottomNav
import com.example.comicslibrary.view.CollectionScreen
import com.example.comicslibrary.view.LibraryScreen
import com.example.comicslibrary.viewmodel.CollectionDbViewModel
import com.example.comicslibrary.viewmodel.LibraryApiViewModel
import com.example.comicslibrary.ui.theme.ComicsLibraryTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Destination(val route: String) {
    object Library : Destination("library")
    object Collection : Destination("collection")
    object CharacterDetail : Destination("character/{characterId}") {
        fun createRoute(characterId: Int?) = "character/$characterId"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComicsLibraryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    CharacterScaffold(navController = navController)
                }
            }
        }
    }


}

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CharacterScaffold(
    navController: NavHostController,
    libraryApiViewModel: LibraryApiViewModel = hiltViewModel(),
    collectionDbViewModel: CollectionDbViewModel = hiltViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = { CharactersBottomNav(navController = navController) }
    ) { paddingValues ->

        NavHost(navController = navController, startDestination = Destination.Library.route) {

            composable(Destination.Library.route) {
                LibraryScreen(
                    navController = navController,
                    paddingValues = paddingValues,
                    vm = libraryApiViewModel
                )
            }

            composable(Destination.Collection.route) {
                CollectionScreen(
                    navController = navController,
                    cvm = collectionDbViewModel
                )
            }

            composable(Destination.CharacterDetail.route) { navBackStackEntry ->
                val id = navBackStackEntry.arguments?.getString("characterId")?.toIntOrNull()
                if (id == null)
                    Toast.makeText(context, "Character id is required", Toast.LENGTH_SHORT).show()
                else {
                    libraryApiViewModel.retrieveSingleCharacter(id)
                    CharacterDetailScreen(
                        lvm = libraryApiViewModel,
                        cvm = collectionDbViewModel,
                        paddingValues = paddingValues,
                        navController = navController
                    )
                }
            }

        }

    }

}
