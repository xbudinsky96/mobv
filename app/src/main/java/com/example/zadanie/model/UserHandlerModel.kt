import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.zadanie.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserHandlerModel(application: Application): AndroidViewModel(application) {
    val readUsers: LiveData<MutableList<User>>
    private val repository: UserRepository

    init {
        val userDao = UserDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readUsers = repository.readUsers
    }

    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
        }
    }

    fun getUserByName(name: String): User {
        return repository.getUserByName(name)
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }
}