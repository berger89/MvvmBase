package de.trbnb.mvvmbase.test.bindableproperty

import androidx.databinding.Bindable
import androidx.lifecycle.SavedStateHandle
import de.trbnb.mvvmbase.BaseViewModel
import de.trbnb.mvvmbase.MvvmBase
import de.trbnb.mvvmbase.bindableproperty.StateSaveOption
import de.trbnb.mvvmbase.bindableproperty.afterSet
import de.trbnb.mvvmbase.bindableproperty.beforeSet
import de.trbnb.mvvmbase.bindableproperty.bindableBoolean
import de.trbnb.mvvmbase.bindableproperty.distinct
import de.trbnb.mvvmbase.bindableproperty.validate
import de.trbnb.mvvmbase.savedstate.BaseStateSavingViewModel
import de.trbnb.mvvmbase.test.TestPropertyChangedCallback
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BindableBooleanPropertyTests {
    @BeforeEach
    fun setup() {
        MvvmBase.disableViewModelLifecycleThreadConstraints()
    }

    @Test
    fun `is false the default value without explicit assignment`() {
        val viewModel = object : BaseViewModel() {
            val property by bindableBoolean()
        }

        assert(!viewModel.property)
    }

    @Test
    fun `does value assignment work`() {
        val viewModel = object : BaseViewModel() {
            var property by bindableBoolean()
        }

        val newValue = true
        assert(viewModel.property != newValue)

        viewModel.property = newValue
        assert(viewModel.property == newValue)
    }

    @Test
    fun `is afterSet() called`() {
        val oldValue = false
        val newValue = true
        val viewModel = object : BaseViewModel() {
            var property by bindableBoolean(oldValue)
                .afterSet { old, new ->
                    assert(newValue == new)
                    assert(oldValue == old)
                }
        }

        viewModel.property = newValue
    }

    @Test
    fun `is distinct() prohibting afterSet() invocation`() {
        val value = true
        var afterSetWasCalled = false
        val viewModel = object : BaseViewModel() {
            var propery by bindableBoolean(value)
                .distinct()
                .afterSet { _, _ -> afterSetWasCalled = true }
        }

        viewModel.propery = value
        assert(!afterSetWasCalled)
    }

    @Test
    fun `is beforeSet() called`() {
        val oldValue = false
        val newValue = true
        assert(oldValue != newValue)

        val viewModel = object : BaseViewModel() {
            var property by bindableBoolean(oldValue)
                .beforeSet { old, new ->
                    assert(old == oldValue)
                    assert(new == newValue)
                }
        }

        assert(viewModel.property == oldValue)
        viewModel.property = newValue
    }

    @Test
    fun `is validate() called`() {
        val oldValue = false
        val newValue = true
        val maxValue = false
        assert(oldValue != newValue)

        val viewModel = object : BaseViewModel() {
            var property by bindableBoolean(defaultValue = oldValue)
                .validate { old, new ->
                    assert(old == oldValue)
                    assert(new == newValue)
                    return@validate maxValue
                }
        }

        assert(viewModel.property == oldValue)
        viewModel.property = newValue
        assert(viewModel.property == maxValue)
    }

    @Test
    fun `is correct field ID used for notifyPropertyChanged()`() {
        val propertyChangedCallback = TestPropertyChangedCallback()
        val viewModel = ViewModelWithBindable()
        viewModel.addOnPropertyChangedCallback(propertyChangedCallback)

        viewModel.property = true
        assert("property" in propertyChangedCallback.changedPropertyIds)
        propertyChangedCallback.clear()
    }

    class ViewModelWithBindable : BaseViewModel() {
                var property by bindableBoolean()
    }

    @Test
    fun `automatic StateSaveOption is set correctly`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AutomaticSavedStateViewModel(savedStateHandle)

        val newValue = true
        viewModel.supportedAutomatic = newValue
        assert(savedStateHandle.get<Boolean>("supportedAutomatic") == newValue)
    }

    class AutomaticSavedStateViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) : BaseStateSavingViewModel(savedStateHandle) {
                var supportedAutomatic by bindableBoolean()
    }

    @Test
    fun `manual StateSaveOption is working correctly`() {
        val savedStateHandle = SavedStateHandle()
        val key = "Bar"
        val viewModel = ManualSavedStateViewModel(key, savedStateHandle)

        val newValue = true
        viewModel.property = newValue
        assert(savedStateHandle.get<Boolean>(key) == newValue)
    }

    class ManualSavedStateViewModel(
        key: String,
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) : BaseStateSavingViewModel(savedStateHandle) {
                var property by bindableBoolean(stateSaveOption = StateSaveOption.Manual(key))
    }

    @Test
    fun `none StateSaveOption is working correctly`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = NoneSavedStateViewModel(savedStateHandle)

        val newValue = true
        viewModel.property = newValue
        assert(savedStateHandle.keys().isEmpty())
    }

    class NoneSavedStateViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) : BaseStateSavingViewModel(savedStateHandle) {
                var property by bindableBoolean(stateSaveOption = StateSaveOption.None)
    }

    @Test
    fun `distinct does prevent notifyPropertyChanged`() {
        val propertyChangedCallback = TestPropertyChangedCallback()
        val viewModel = ViewModelWithDistinct().apply {
            addOnPropertyChangedCallback(propertyChangedCallback)
        }

        viewModel.property = true
        assert("property" in propertyChangedCallback.changedPropertyIds)
        propertyChangedCallback.clear()

        viewModel.property = true
        assert("property" !in propertyChangedCallback.changedPropertyIds)
    }

    class ViewModelWithDistinct : BaseViewModel() {
                var property by bindableBoolean()
            .distinct()
    }
}
